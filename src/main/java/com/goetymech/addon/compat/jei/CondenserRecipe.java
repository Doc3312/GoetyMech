package com.goetymech.addon.compat.jei;

import com.goetymech.addon.common.data.CondenserProductManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 巫法冷凝 JEI 配方条目。单类别双布局：
 *  - BIOME：群系蒸馏表（代表物品槽 + 群系名 + 液体概率）
 *  - RITUAL：仪式产出表（仪式名文本 + 液体输出，数量 = 灵魂消耗 × 5）
 */
public class CondenserRecipe {

    public enum Kind { BIOME, RITUAL }

    private final Kind kind;
    private final List<ResourceLocation> biomes;
    private final List<ResourceLocation> tags;
    private final ItemStack icon;
    private final List<FluidStack> fluids;
    private final List<Float> chances;
    private final String ritualName;

    private CondenserRecipe(Kind kind, List<ResourceLocation> biomes, List<ResourceLocation> tags,
                            ItemStack icon, List<FluidStack> fluids, List<Float> chances, String ritualName) {
        this.kind = kind;
        this.biomes = biomes;
        this.tags = tags;
        this.icon = icon;
        this.fluids = fluids;
        this.chances = chances;
        this.ritualName = ritualName;
    }

    public static CondenserRecipe biome(CondenserProductManager.BiomeEntry entry, ItemStack icon) {
        List<FluidStack> fluids = new ArrayList<>();
        List<Float> chances = new ArrayList<>();
        for (CondenserProductManager.FluidChance fc : entry.getLiquids()) {
            fluids.add(new FluidStack(fc.getFluid(), 50));
            chances.add(fc.getChance());
        }
        return new CondenserRecipe(Kind.BIOME, entry.getBiomes(), entry.getTags(), icon, fluids, chances, null);
    }

    public static CondenserRecipe ritual(ResourceLocation ritualId, net.minecraft.world.level.material.Fluid fluid) {
        return new CondenserRecipe(Kind.RITUAL, List.of(), List.of(), ItemStack.EMPTY,
                List.of(new FluidStack(fluid, 500)), List.of(), ritualId.toString());
    }

    public Kind getKind() {
        return kind;
    }

    public List<ResourceLocation> getBiomes() {
        return biomes;
    }

    public List<ResourceLocation> getTags() {
        return tags;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<FluidStack> getFluids() {
        return fluids;
    }

    public List<Float> getChances() {
        return chances;
    }

    public String getRitualName() {
        return ritualName;
    }
}
