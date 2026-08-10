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
import net.minecraft.server.level.ServerPlayer;
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

    /** 时停中心（开袋玩家位置）。use(false) 后清空。 */
    public static volatile BlockPos timeStopCenter;

    /**
     * 是否启用范围限制（三档模式）。
     * 0 = 始终全维度；1 = 自动（≥2 玩家在线限制）；2 = 始终限制。
     */
    public static boolean shouldLimitRange(ServerLevel level) {
        return switch (ModConfig.cachedTimeStopLimitMode) {
            case 0 -> false;
            case 2 -> true;
            default -> level.getServer().getPlayerCount() >= 2;
        };
    }

    /**
     * 实体/位置是否在时停范围内。全维度模式恒 true；限制模式下为同维度 + 中心球体距离判断。
     */
    public static boolean isInRange(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return true;
        if (!shouldLimitRange(serverLevel)) return true;
        if (timeStopCenter == null) return false;
        long rangeSq = (long) ModConfig.cachedTimeStopRange * ModConfig.cachedTimeStopRange;
        return andSameDimension(level) && pos.distSqr(timeStopCenter) <= rangeSq;
    }

    public static boolean isInRange(Entity entity) {
        return isInRange(entity.level(), entity.blockPosition());
    }

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
            ServerLevel serverLevel = (ServerLevel) source.level();
            AABB scanArea = (shouldLimitRange(serverLevel) && timeStopCenter != null)
                    ? new AABB(timeStopCenter).inflate(ModConfig.cachedTimeStopRange)
                    : new AABB(new BlockPos(0, 0, 0)).inflate(30000000);
            for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class, scanArea)) {
                if (living != source) {
                    if (TimeStopEntityData.getTimeStopCount(living) > 0 && living.isAlive()) {
                        if (force) TimeStopEntityData.setTimeStopCount(source, 0);
                        return;
                    }
                }
            }
        }

        isTimeStop = z;
        timeStopCenter = z ? source.blockPosition() : null;

        if (!isTimeStop) {
            TimeStopSavedData.readOrCreate(((ServerLevel) source.level()).getServer())
                    .removeTsDimension(source.level().dimension());
        }

        // 冻结广播：限制模式只发给中心范围内的玩家；解冻广播始终 ALL（兜底，保证曾冻结者必定解除）
        if (isTimeStop && shouldLimitRange((ServerLevel) source.level()) && timeStopCenter != null) {
            ServerLevel serverLevel = (ServerLevel) source.level();
            long rangeSq = (long) ModConfig.cachedTimeStopRange * ModConfig.cachedTimeStopRange;
            for (ServerPlayer target : serverLevel.players()) {
                if (target.blockPosition().distSqr(timeStopCenter) <= rangeSq) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target),
                            new S2CTimeStopStatePacket(true, playSoundEffect, source.getId()));
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target),
                            new S2CTimeStopDimensionPacket(new ResourceLocation(""), source.level().dimension().location()));
                }
            }
        } else {
            ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                    new S2CTimeStopStatePacket(isTimeStop, playSoundEffect, source.getId()));

            if (isTimeStop) {
                ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CTimeStopDimensionPacket(
                        new ResourceLocation(""), source.level().dimension().location()));
            } else {
                ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CTimeStopDimensionPacket(
                        source.level().dimension().location(), new ResourceLocation("")));
            }
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
