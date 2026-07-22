package com.goetymech.addon.api.time;

import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 时停公开 API。包装 TimeStopUtils 供其他模组调用。
 */
public class TimeStopAPI {
    public static boolean canMove(Entity entity) {
        return TimeStopUtils.canMove(entity);
    }

    public static synchronized void use(boolean z, LivingEntity source) {
        TimeStopUtils.use(z, source, true);
    }

    public static synchronized void use(boolean z, LivingEntity source, boolean force) {
        TimeStopUtils.use(z, source, force, 180, true);
    }

    public static synchronized void use(boolean z, LivingEntity source, boolean force, int time) {
        TimeStopUtils.use(z, source, force, time, true);
    }
}
