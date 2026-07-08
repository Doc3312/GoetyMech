package com.goetymech.addon.common.events;

import com.goetymech.addon.config.ModConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FreecamSpectatorHandler {

    private static final String TAG_ACTIVE = "GoetyMechSpectator";
    private static final String TAG_GAMETYPE = "SpectatorSavedGameType";
    private static final String TAG_POS_X = "SpectatorSavedX";
    private static final String TAG_POS_Y = "SpectatorSavedY";
    private static final String TAG_POS_Z = "SpectatorSavedZ";
    private static final String TAG_DIM = "SpectatorSavedDim";
    private static final String TAG_REMAINING = "SpectatorRemaining";

    public static boolean isActive(ServerPlayer player) {
        return player.getPersistentData().getBoolean(TAG_ACTIVE);
    }

    public static void activate(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        tag.putBoolean(TAG_ACTIVE, true);
        tag.putInt(TAG_GAMETYPE, player.gameMode.getGameModeForPlayer().getId());
        tag.putDouble(TAG_POS_X, player.getX());
        tag.putDouble(TAG_POS_Y, player.getY());
        tag.putDouble(TAG_POS_Z, player.getZ());
        tag.putString(TAG_DIM, player.level().dimension().location().toString());
        tag.putInt(TAG_REMAINING, ModConfig.FREECAM_DURATION.get());

        player.setGameMode(GameType.SPECTATOR);
        player.getAbilities().setFlyingSpeed((float) (ModConfig.FREECAM_SPEED.get() * 0.05));
        player.onUpdateAbilities();
    }

    public static void deactivate(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean(TAG_ACTIVE)) return;

        int gameTypeId = tag.getInt(TAG_GAMETYPE);
        GameType savedType = GameType.byId(gameTypeId);

        double x = tag.getDouble(TAG_POS_X);
        double y = tag.getDouble(TAG_POS_Y);
        double z = tag.getDouble(TAG_POS_Z);
        String dimStr = tag.getString(TAG_DIM);

        tag.remove(TAG_ACTIVE);
        tag.remove(TAG_GAMETYPE);
        tag.remove(TAG_POS_X);
        tag.remove(TAG_POS_Y);
        tag.remove(TAG_POS_Z);
        tag.remove(TAG_DIM);
        tag.remove(TAG_REMAINING);

        player.noPhysics = false;
        player.setGameMode(savedType);
        player.getAbilities().flying = false;
        player.getAbilities().mayfly = false;
        player.getAbilities().setFlyingSpeed(0.05f);
        player.onUpdateAbilities();
        player.setDeltaMovement(Vec3.ZERO);
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), Vec3.ZERO));
        player.fallDistance = 0;

        ServerLevel savedLevel = player.getServer().getLevel(
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimStr)));
        if (savedLevel != null) {
            player.teleportTo(savedLevel, x, y, z, player.getYRot(), player.getXRot());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean(TAG_ACTIVE)) return;

        int remaining = tag.getInt(TAG_REMAINING) - 1;
        if (remaining <= 0) {
            tag.putInt(TAG_REMAINING, 0);
            player.getServer().execute(() -> deactivate(player));
            return;
        }
        tag.putInt(TAG_REMAINING, remaining);
    }
}
