package com.goetymech.addon.common.time;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.common.data.TimeStopSavedData;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CTimeStopDimensionPacket;
import com.goetymech.addon.common.net.s2c.S2CTimeStopStatePacket;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;

/**
 * 时停核心引擎（服务端）。
 * 管理全局时停标志、广播 S2C 状态包、维护时停维度持久化。
 * use() 为同步入口，支持倒计时自动解除。
 */
public class TimeStopUtils {
    public static volatile boolean isTimeStop;

    public static boolean andSameDimension(Level level) {
        if (level == null) return false;
        if (level.isClientSide) {
            return Boolean.TRUE.equals(DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
                    () -> () -> ClientContext.isTimeStop_andSameDimension));
        }
        if (!isTimeStop) return false;
        TimeStopSavedData saved = TimeStopSavedData.readOrCreate(((ServerLevel) level).getServer());
        return saved.dimensions.contains(level.dimension().location());
    }

    public static boolean canMove(Entity entity) {
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return true;
            return TimeStopEntityData.canMove(player);
        } else if (entity instanceof LivingEntity living) {
            return TimeStopEntityData.canMove(living);
        }
        return false;
    }

    public static synchronized void use(boolean z, LivingEntity source) {
        use(z, source, true);
    }

    public static synchronized void use(boolean z, LivingEntity source, boolean force) {
        use(z, source, force, 180, true);
    }

    public static synchronized void use(boolean z, LivingEntity source, boolean force, int time, boolean playSoundEffect) {
        if (z && !ModConfig.TIME_STOP_ENABLED.get()) return;
        if (source.level().isClientSide) return;

        if (!z) {
            for (LivingEntity living : source.level().getEntitiesOfClass(LivingEntity.class,
                    new AABB(new BlockPos(0, 0, 0)).inflate(30000000))) {
                if (living != source) {
                    if (TimeStopEntityData.getTimeStopCount(living) > 0 && living.isAlive()) {
                        if (force) TimeStopEntityData.setTimeStopCount(source, 0);
                        return;
                    }
                }
            }
        }

        isTimeStop = z;

        if (!isTimeStop) {
            TimeStopSavedData.readOrCreate(((ServerLevel) source.level()).getServer())
                    .removeTsDimension(source.level().dimension());
        }

        ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                new S2CTimeStopStatePacket(isTimeStop, playSoundEffect, source.getId()));

        if (isTimeStop) {
            ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CTimeStopDimensionPacket(
                    new ResourceLocation(""), source.level().dimension().location()));
        } else {
            ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CTimeStopDimensionPacket(
                    source.level().dimension().location(), new ResourceLocation("")));
        }

        if (z) {
            TimeStopSavedData.readOrCreate(((ServerLevel) source.level()).getServer())
                    .addTsDimension(source.level().dimension());
            if (ModConfig.TIME_STOP_COUNTDOWN_ENABLED.get()) {
                TimeStopEntityData.setTimeStopCount(source,
                        Math.max(TimeStopEntityData.getTimeStopCount(source), time));
            }
        } else {
            if (force) TimeStopEntityData.setTimeStopCount(source, 0);
        }
    }
}
