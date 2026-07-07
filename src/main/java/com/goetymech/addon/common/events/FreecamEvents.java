package com.goetymech.addon.common.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FreecamEvents {

    private static final String TAG_FREECAM = "GoetyMechFreecam";
    private static final String TAG_BODY_X = "FreecamBodyX";
    private static final String TAG_BODY_Y = "FreecamBodyY";
    private static final String TAG_BODY_Z = "FreecamBodyZ";

    public static void setFreecam(ServerPlayer player, boolean active) {
        player.getPersistentData().putBoolean(TAG_FREECAM, active);
        if (active) {
            player.setInvulnerable(true);
            player.setNoGravity(true);
            CompoundTag tag = player.getPersistentData();
            tag.putDouble(TAG_BODY_X, player.getX());
            tag.putDouble(TAG_BODY_Y, player.getY());
            tag.putDouble(TAG_BODY_Z, player.getZ());
        } else {
            player.setInvulnerable(false);
            player.setNoGravity(false);
        }
    }

    public static boolean isFreecam(ServerPlayer player) {
        return player.getPersistentData().getBoolean(TAG_FREECAM);
    }

    public static void onClientEndFreecam(ServerPlayer player) {
        setFreecam(player, false);
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isFreecam(player)) return;
        player.setInvulnerable(true);
        player.setNoGravity(true);
        CompoundTag tag = player.getPersistentData();
        double bx = tag.getDouble(TAG_BODY_X);
        double by = tag.getDouble(TAG_BODY_Y);
        double bz = tag.getDouble(TAG_BODY_Z);
        player.setPos(bx, by, bz);
        player.setDeltaMovement(Vec3.ZERO);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isFreecam(player)) return;
        setFreecam(player, false);
    }
}
