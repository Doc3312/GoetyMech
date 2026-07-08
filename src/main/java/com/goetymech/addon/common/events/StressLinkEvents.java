package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.block.entity.StressArkBlockEntity;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetymech.addon.common.utils.GlobalBlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class StressLinkEvents {

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide()) return;
        GoetyMechMod.STRESS_LINK.levelLoaded(level);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        GoetyMechMod.STRESS_LINK.tick();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        GoetyMechMod.STRESS_LINK.onServerStopping();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        GoetyMechMod.STRESS_LINK.playerLogin(player);

        StressArkBlockEntity foundArk = null;
        for (GlobalBlockPos arkPos : GoetyMechMod.STRESS_LINK.getArksForOwner(player.getUUID())) {
            ServerLevel level = server.getLevel(arkPos.dimension());
            if (level != null) {
                BlockEntity be = level.getBlockEntity(arkPos.pos());
                if (be instanceof StressArkBlockEntity ark) {
                    foundArk = ark;
                    break;
                }
            }
        }

        if (foundArk != null) {
            float consumption = GoetyMechMod.STRESS_LINK.getEngineConsumptionForOwner(player.getUUID());
            ModNetwork.sendTo(player, new S2CStressArkDataPacket(
                    foundArk.getCachedTotalCapacity(),
                    foundArk.getCachedTotalStress(),
                    foundArk.getCachedNetworkCount(),
                    consumption));
        } else {
            ModNetwork.sendTo(player, new S2CStressArkDataPacket(-1, -1, 0, 0));
        }
    }
}