package com.goetymech.addon.common.net.c2s;

import com.goetymech.addon.common.magic.effect.VoidMarkEffect;
import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SVoidBlinkPacket {

    public static C2SVoidBlinkPacket decode(FriendlyByteBuf buf) {
        return new C2SVoidBlinkPacket();
    }

    public static void encode(C2SVoidBlinkPacket packet, FriendlyByteBuf buf) {
    }

    public static void handle(C2SVoidBlinkPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!player.hasEffect(ModEffects.VOID_MARK.get())) return;

            var data = player.getPersistentData();
            if (!data.hasUUID(VoidMarkEffect.TAG_TARGET)) return;

            int remaining = data.getInt(VoidMarkEffect.TAG_REMAINING);
            if (remaining <= 0) return;

            ServerLevel level = player.serverLevel();
            Entity target = level.getEntity(data.getUUID(VoidMarkEffect.TAG_TARGET));
            if (!(target instanceof LivingEntity livingTarget) || !livingTarget.isAlive()) {
                cleanup(player);
                return;
            }

            Vec3 behind = calculateBehind(livingTarget);
            if (!isSafe(level, behind)) {
                behind = livingTarget.position();
            }

            player.teleportTo(level, behind.x, behind.y, behind.z, player.getYRot(), player.getXRot());
            level.broadcastEntityEvent(player, (byte) 46);

            remaining--;
            data.putInt(VoidMarkEffect.TAG_REMAINING, remaining);

            int fullDuration = data.getInt(VoidMarkEffect.TAG_WINDOW_DURATION);
            if (fullDuration <= 0) {
                fullDuration = ModConfig.VOID_BLINK_WINDOW_SECONDS.get() * 20;
            }
            player.addEffect(new MobEffectInstance(ModEffects.VOID_MARK.get(), fullDuration, 0, false, true, true));
            data.putLong(VoidMarkEffect.TAG_WINDOW_END, level.getGameTime() + fullDuration);

            if (remaining <= 0) {
                cleanup(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static Vec3 calculateBehind(LivingEntity target) {
        Vec3 look = target.getLookAngle();
        return target.position().subtract(look.x, 0, look.z).add(0, 0.5, 0);
    }

    private static boolean isSafe(ServerLevel level, Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos);
        if (!level.getBlockState(blockPos).isAir() && !level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        if (level.getBlockState(blockPos).getFluidState().is(FluidTags.LAVA)) {
            return false;
        }
        return level.noCollision(null, net.minecraft.world.phys.AABB.ofSize(pos, 0.6, 1.8, 0.6));
    }

    private static void cleanup(ServerPlayer player) {
        var data = player.getPersistentData();
        data.remove(VoidMarkEffect.TAG_TARGET);
        data.remove(VoidMarkEffect.TAG_REMAINING);
        data.remove(VoidMarkEffect.TAG_WINDOW_END);
        data.remove(VoidMarkEffect.TAG_WINDOW_DURATION);
        player.removeEffect(ModEffects.VOID_MARK.get());
    }
}
