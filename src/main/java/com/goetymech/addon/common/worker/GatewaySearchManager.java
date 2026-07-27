package com.goetymech.addon.common.worker;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.utils.GatewayUtil;
import com.goetymech.addon.common.events.FreecamSpectatorHandler;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CSetFreecamPacket;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.init.ModAttributes;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GatewaySearchManager {

    private static final Map<UUID, SearchState> ACTIVE_SEARCHES = new ConcurrentHashMap<>();
    private static boolean debug = false;

    public static void setDebug(boolean v) {
        debug = v;
    }

    public static void DEBUG(String msg, Object... args) {
        if (debug) {
            GoetyMechMod.LOGGER.info("[GatewaySearch] " + msg, args);
        }
    }

    public static class SearchState {
        public final ServerPlayer player;
        public final TagKey<Structure> tag;
        public final List<ServerLevel> dimensionsToSearch;
        public int dimIndex;
        @Nullable
        public GatewaySearchWorker currentWorker;
        public boolean done;
        public boolean found;

        public SearchState(ServerPlayer player, TagKey<Structure> tag, List<ServerLevel> dimensionsToSearch) {
            this.player = player;
            this.tag = tag;
            this.dimensionsToSearch = dimensionsToSearch;
            this.dimIndex = 0;
        }
    }

    public static void startSearch(ServerPlayer player, TagKey<Structure> tag) {
        UUID uuid = player.getUUID();

        SearchState existing = ACTIVE_SEARCHES.get(uuid);
        if (existing != null) {
            if (existing.currentWorker != null) {
                existing.currentWorker.cancel();
            }
            ACTIVE_SEARCHES.remove(uuid);
        }

        List<ServerLevel> dims = buildDimensionList(player, tag);
        if (dims.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.gateway.no_structure"), true);
            return;
        }

        SearchState state = new SearchState(player, tag, dims);
        ACTIVE_SEARCHES.put(uuid, state);
        startNextWorker(state);

        player.displayClientMessage(
                Component.translatable("info.goetymech.gateway.searching"), true);
    }

    private static List<ServerLevel> buildDimensionList(ServerPlayer player, TagKey<Structure> tag) {
        List<ServerLevel> result = new ArrayList<>();

        if (player.getServer() == null) return result;

        Registry<Structure> registry = player.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
        var holders = registry.getTag(tag);
        if (holders.isEmpty() || !holders.get().iterator().hasNext()) return result;

        ServerLevel playerDim = (ServerLevel) player.level();
        result.add(playerDim);

        for (ServerLevel level : player.getServer().getAllLevels()) {
            if (level == playerDim) continue;
            if (dimCanHostStructure(level, holders.get())) {
                result.add(level);
            }
        }

        return result;
    }

    private static boolean dimCanHostStructure(ServerLevel level, Iterable<Holder<Structure>> holders) {
        Set<Holder<Biome>> dimBiomes = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
        for (Holder<Structure> holder : holders) {
            for (Holder<Biome> biome : holder.value().biomes()) {
                if (dimBiomes.contains(biome)) return true;
            }
        }
        return false;
    }

    private static void startNextWorker(SearchState state) {
        if (state.dimIndex >= state.dimensionsToSearch.size()) {
            finishSearch(state, null, null);
            return;
        }

        ServerLevel targetLevel = state.dimensionsToSearch.get(state.dimIndex);
        state.dimIndex++;

        GatewaySearchWorker worker = new GatewaySearchWorker(
                state.player,
                targetLevel,
                state.tag,
                state.player.blockPosition(),
                (pos, foundLevel) -> {
                    if (pos != null) {
                        finishSearch(state, pos, foundLevel);
                    } else {
                        startNextWorker(state);
                    }
                },
                ModConfig.GATEWAY_ADV_SEARCH_RADIUS.get()
        );

        state.currentWorker = worker;
        DEBUG("Started search in dim {} for {}", targetLevel.dimension().location(), state.player.getScoreboardName());
    }

    private static void finishSearch(SearchState state, @Nullable BlockPos pos, @Nullable ServerLevel foundLevel) {
        state.done = true;
        state.found = pos != null;
        ACTIVE_SEARCHES.remove(state.player.getUUID());

        if (pos == null || foundLevel == null) {
            state.player.displayClientMessage(
                    Component.translatable("info.goetymech.gateway.no_structure"), true);
            return;
        }

        DEBUG("Search complete, teleporting {} to {} in {}", state.player.getScoreboardName(), pos, foundLevel.dimension().location());
        executeTeleport(state.player, foundLevel, pos);
    }

    private static void executeTeleport(ServerPlayer player, ServerLevel targetLevel, BlockPos structPos) {
        boolean forceTeleport = checkForceFlag(player);
        Vec3 teleportPos;

        if (forceTeleport) {
            BlockPos hightPos = targetLevel.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, structPos);
            if (hightPos.getY() <= targetLevel.getMinBuildHeight()) {
                hightPos = structPos.atY(targetLevel.getSeaLevel());
            }
            teleportPos = Vec3.atCenterOf(hightPos);
            clearForceFlag(player);
        } else {
            BlockPos safePos = GatewayUtil.findSafeNearStructure(targetLevel, structPos, player);
            if (safePos != null) {
                teleportPos = Vec3.atCenterOf(safePos);
            } else {
                setForceFlag(player, structPos);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.gateway.danger_area"), true);
                return;
            }
        }

        int cost = ModConfig.GATEWAY_ADV_COST.get();
        ItemStack focus = WandUtil.findFocus(player);
        if (!focus.isEmpty()) {
            if (!player.isCreative()) {
                SEHelper.decreaseSouls(player, cost);
            }
            SEHelper.addCooldown(player, focus.getItem(),
                    (int) (ModConfig.GATEWAY_ADV_COOLDOWN.get() * ModAttributes.getCooldownDiscount(player)));
        }

        GatewayUtil.teleportPlayer(targetLevel, player, teleportPos);

        FreecamSpectatorHandler.activate(player);
        ModNetwork.sendTo(player, new S2CSetFreecamPacket(true));
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                ModConfig.FREECAM_DURATION.get(),
                0, false, false, false
        ));
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ACTIVE_SEARCHES.isEmpty()) return;

        long deadline = System.currentTimeMillis() + 10;

        List<UUID> toRemove = new ArrayList<>();
        for (SearchState state : ACTIVE_SEARCHES.values()) {
            if (state.done) {
                toRemove.add(state.player.getUUID());
                continue;
            }
            if (state.currentWorker == null || state.currentWorker.isFinished()) {
                continue;
            }
            if (System.currentTimeMillis() >= deadline) break;
            state.currentWorker.tick();
        }

        for (UUID uuid : toRemove) {
            ACTIVE_SEARCHES.remove(uuid);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            SearchState state = ACTIVE_SEARCHES.remove(sp.getUUID());
            if (state != null && state.currentWorker != null) {
                state.currentWorker.cancel();
            }
        }
    }

    private static boolean checkForceFlag(ServerPlayer player) {
        var data = player.getPersistentData();
        var goetyMech = data.getCompound("GoetyMechData");
        return goetyMech.contains("GatewayAdvSafeFailedPos");
    }

    private static void setForceFlag(ServerPlayer player, BlockPos pos) {
        var data = player.getPersistentData();
        var goetyMech = data.getCompound("GoetyMechData");
        var posTag = new net.minecraft.nbt.CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        posTag.putString("dim", player.level().dimension().location().toString());
        goetyMech.put("GatewayAdvSafeFailedPos", posTag);
        data.put("GoetyMechData", goetyMech);
    }

    private static void clearForceFlag(ServerPlayer player) {
        var data = player.getPersistentData();
        var goetyMech = data.getCompound("GoetyMechData");
        goetyMech.remove("GatewayAdvSafeFailedPos");
        data.put("GoetyMechData", goetyMech);
    }

    public static boolean isSearching(ServerPlayer player) {
        return ACTIVE_SEARCHES.containsKey(player.getUUID());
    }

    public static void cancelSearch(ServerPlayer player) {
        SearchState state = ACTIVE_SEARCHES.remove(player.getUUID());
        if (state != null && state.currentWorker != null) {
            state.currentWorker.cancel();
        }
    }
}
