package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.block.entity.StressArkBlockEntity;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetymech.addon.common.utils.GlobalBlockPos;
import com.goetymech.addon.common.utils.GlobalStressLinkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class StressLinkEvents {

    private static boolean loaded = false;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        if (!loaded) {
            loaded = true;
            MinecraftServer server = event.getServer();
            if (server != null && server.overworld() != null) {
                GoetyMechMod.STRESS_LINK.server = server;
                GoetyMechMod.STRESS_LINK.levelLoaded(server.overworld());
            }
        }
        
        GoetyMechMod.STRESS_LINK.tick();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        boolean hasArk = false;
        for (GlobalBlockPos arkPos : GoetyMechMod.STRESS_LINK.getArksForOwner(player.getUUID())) {
            ServerLevel level = server.getLevel(arkPos.dimension());
            if (level != null) {
                BlockEntity be = level.getBlockEntity(arkPos.pos());
                if (be instanceof StressArkBlockEntity) {
                    hasArk = true;
                    break;
                }
            }
        }

        if (!hasArk) {
            ModNetwork.sendTo(player, new S2CStressArkDataPacket(-1, -1, 0, 0));
        }
    }
}
