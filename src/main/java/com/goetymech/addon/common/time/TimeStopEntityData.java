package com.goetymech.addon.common.time;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public class TimeStopEntityData {
    private static final String KEY_COUNT = "goetymech_time_stop_count";
    private static final String KEY_CAN_MOVE = "goetymech_time_stop_can_move";

    public static int getTimeStopCount(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        return tag.getInt(KEY_COUNT);
    }

    public static void setTimeStopCount(LivingEntity entity, int count) {
        entity.getPersistentData().putInt(KEY_COUNT, count);
    }

    public static boolean canMove(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        if (!tag.contains(KEY_CAN_MOVE)) return false;
        return tag.getBoolean(KEY_CAN_MOVE);
    }

    public static void setCanMove(LivingEntity entity, boolean canMove) {
        entity.getPersistentData().putBoolean(KEY_CAN_MOVE, canMove);
    }
}
