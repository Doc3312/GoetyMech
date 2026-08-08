package com.goetymech.addon.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.goetymech.addon.GoetyMechMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 巫法冷凝产出表数据管理器。
 * 加载 data/goetymech/condenser/ 下的 JSON：
 *  - biome_products.json：群系蒸馏表（biomes 精确 > tags 批量，条目内液体独立概率）
 *  - ritual_products.json：仪式专属表（仪式 recipe id → 固定液体）
 */
public class CondenserProductManager extends SimpleJsonResourceReloadListener {

    public static final CondenserProductManager INSTANCE = new CondenserProductManager();
    private static final Logger LOGGER = LoggerFactory.getLogger(GoetyMechMod.MOD_ID);

    private static List<BiomeEntry> biomeEntries = List.of();
    private static Map<ResourceLocation, Fluid> ritualFluids = Map.of();

    private CondenserProductManager() {
        super(new Gson(), "condenser");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                         ProfilerFiller profilerFiller) {
        List<BiomeEntry> newBiomeEntries = new ArrayList<>();
        Map<ResourceLocation, Fluid> newRitualFluids = new HashMap<>();

        map.forEach((id, element) -> {
            if (!element.isJsonObject()) {
                return;
            }
            String path = id.getPath();
            if (path.equals("biome_products")) {
                parseBiomeProducts(element.getAsJsonObject(), newBiomeEntries);
            } else if (path.equals("ritual_products")) {
                parseRitualProducts(element.getAsJsonObject(), newRitualFluids);
            }
        });

        biomeEntries = List.copyOf(newBiomeEntries);
        ritualFluids = Map.copyOf(newRitualFluids);
        LOGGER.info("Condenser: loaded {} biome entries, {} ritual fluids", biomeEntries.size(), ritualFluids.size());
    }

    private static void parseBiomeProducts(JsonObject root, List<BiomeEntry> out) {
        JsonArray entries = root.getAsJsonArray("entries");
        if (entries == null) {
            return;
        }
        for (JsonElement el : entries) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject obj = el.getAsJsonObject();
            List<ResourceLocation> biomes = parseIdList(obj.getAsJsonArray("biomes"));
            List<ResourceLocation> tags = parseIdList(obj.getAsJsonArray("tags"));
            List<FluidChance> liquids = parseLiquids(obj.getAsJsonArray("liquids"));
            if (liquids.isEmpty()) {
                continue;
            }
            out.add(new BiomeEntry(biomes, tags, liquids));
        }
    }

    private static void parseRitualProducts(JsonObject root, Map<ResourceLocation, Fluid> out) {
        JsonArray rituals = root.getAsJsonArray("rituals");
        if (rituals == null) {
            return;
        }
        for (JsonElement el : rituals) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject obj = el.getAsJsonObject();
            ResourceLocation ritualId = ResourceLocation.tryParse(obj.get("ritual").getAsString());
            Fluid fluid = parseFluid(obj.get("fluid").getAsString());
            if (ritualId == null || fluid == null) {
                continue;
            }
            out.put(ritualId, fluid);
        }
    }

    private static List<ResourceLocation> parseIdList(JsonArray array) {
        List<ResourceLocation> list = new ArrayList<>();
        if (array == null) {
            return list;
        }
        for (JsonElement el : array) {
            ResourceLocation rl = ResourceLocation.tryParse(el.getAsString());
            if (rl != null) {
                list.add(rl);
            }
        }
        return list;
    }

    private static List<FluidChance> parseLiquids(JsonArray array) {
        List<FluidChance> list = new ArrayList<>();
        if (array == null) {
            return list;
        }
        for (JsonElement el : array) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject obj = el.getAsJsonObject();
            Fluid fluid = parseFluid(obj.get("fluid").getAsString());
            if (fluid == null) {
                continue;
            }
            float chance = obj.has("chance") ? obj.get("chance").getAsFloat() : 100f;
            list.add(new FluidChance(fluid, chance));
        }
        return list;
    }

    /** 容错解析流体：未注册的流体跳过并告警（未来液体未做时表已可写） */
    private static Fluid parseFluid(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) {
            LOGGER.warn("Condenser: invalid fluid id '{}', skipped", id);
            return null;
        }
        if (!ForgeRegistries.FLUIDS.containsKey(rl)) {
            LOGGER.warn("Condenser: fluid '{}' not registered, skipped", id);
            return null;
        }
        return ForgeRegistries.FLUIDS.getValue(rl);
    }

    /** 群系匹配：biomes 精确条目优先，其次 tags 条目，同级按文件顺序先到先得 */
    public static BiomeEntry matchBiome(Holder<Biome> biome) {
        for (BiomeEntry entry : biomeEntries) {
            if (entry.matchesExact(biome)) {
                return entry;
            }
        }
        for (BiomeEntry entry : biomeEntries) {
            if (entry.matchesTag(biome)) {
                return entry;
            }
        }
        return null;
    }

    public static Fluid getRitualFluid(ResourceLocation recipeId) {
        return ritualFluids.get(recipeId);
    }

    public static List<BiomeEntry> getBiomeEntries() {
        return biomeEntries;
    }

    public static Map<ResourceLocation, Fluid> getRitualFluids() {
        return ritualFluids;
    }

    public static class BiomeEntry {
        private final List<ResourceLocation> biomes;
        private final List<ResourceLocation> tags;
        private final List<FluidChance> liquids;

        public BiomeEntry(List<ResourceLocation> biomes, List<ResourceLocation> tags, List<FluidChance> liquids) {
            this.biomes = biomes;
            this.tags = tags;
            this.liquids = liquids;
        }

        public boolean matchesExact(Holder<Biome> biome) {
            for (ResourceLocation rl : biomes) {
                if (biome.is(ResourceKey.create(Registries.BIOME, rl))) {
                    return true;
                }
            }
            return false;
        }

        public boolean matchesTag(Holder<Biome> biome) {
            for (ResourceLocation rl : tags) {
                if (biome.is(TagKey.create(Registries.BIOME, rl))) {
                    return true;
                }
            }
            return false;
        }

        public List<ResourceLocation> getBiomes() {
            return biomes;
        }

        public List<ResourceLocation> getTags() {
            return tags;
        }

        public List<FluidChance> getLiquids() {
            return liquids;
        }
    }

    public static class FluidChance {
        private final Fluid fluid;
        private final float chance;

        public FluidChance(Fluid fluid, float chance) {
            this.fluid = fluid;
            this.chance = chance;
        }

        public Fluid getFluid() {
            return fluid;
        }

        public float getChance() {
            return chance;
        }
    }
}
