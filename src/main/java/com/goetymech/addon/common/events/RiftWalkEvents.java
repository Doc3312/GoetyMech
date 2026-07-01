package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.Polarice3.Goety.common.events.ArcaTeleporter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RiftWalkEvents {

    private static final String TAG_ORIGIN_DIM = "goetymech.origin_dim";
    private static final String TAG_ORIGIN_X = "goetymech.origin_x";
    private static final String TAG_ORIGIN_Y = "goetymech.origin_y";
    private static final String TAG_ORIGIN_Z = "goetymech.origin_z";
    private static final String TAG_RETURN_TIME = "goetymech.return_time";

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;

        CompoundTag data = player.getPersistentData();
        if (!data.contains(TAG_RETURN_TIME)) return;

        long returnTime = data.getLong(TAG_RETURN_TIME);
        if (player.level().getGameTime() < returnTime) return;

        String dimStr = data.getString(TAG_ORIGIN_DIM);
        double ox = data.getDouble(TAG_ORIGIN_X);
        double oy = data.getDouble(TAG_ORIGIN_Y);
        double oz = data.getDouble(TAG_ORIGIN_Z);

        data.remove(TAG_ORIGIN_DIM);
        data.remove(TAG_ORIGIN_X);
        data.remove(TAG_ORIGIN_Y);
        data.remove(TAG_ORIGIN_Z);
        data.remove(TAG_RETURN_TIME);

        ResourceKey<Level> originDim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimStr));
        Vec3 originPos = new Vec3(ox, oy, oz);

        ServerLevel currentLevel = (ServerLevel) player.level();
        ServerLevel targetLevel = player.getServer().getLevel(originDim);
        if (targetLevel == null) return;

        if (currentLevel == targetLevel) {
            player.teleportTo(ox, oy, oz);
        } else {
            player.changeDimension(targetLevel, new ArcaTeleporter(originPos));
        }

        player.displayClientMessage(
                Component.translatable("info.goetymech.rift_walk.return"), true);
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }
}
