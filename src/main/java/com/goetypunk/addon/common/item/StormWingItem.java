package com.goetypunk.addon.common.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class StormWingItem extends ElytraItem {

    private static final String TAG_SLAMMING = "goetypunk.slamming";
    private static final String TAG_SLAM_TICK = "goetypunk.slam_tick";
    private static final String TAG_SLAM_START_Y = "goetypunk.slam_start_y";
    private static final String TAG_PREV_CROUCH = "goetypunk.prev_crouch";
    private static final String TAG_FLIGHT_TICK = "goetypunk.flight_tick";

    public StormWingItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return !isSlamming(entity);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public static boolean isHoldingStormWing(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof StormWingItem;
    }

    public static boolean isSlamming(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TAG_SLAMMING);
    }

    public static void setSlamming(LivingEntity entity, boolean slamming) {
        entity.getPersistentData().putBoolean(TAG_SLAMMING, slamming);
    }

    public static int getSlamTick(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_SLAM_TICK);
    }

    public static void setSlamTick(LivingEntity entity, int tick) {
        entity.getPersistentData().putInt(TAG_SLAM_TICK, tick);
    }

    public static double getSlamStartY(LivingEntity entity) {
        return entity.getPersistentData().getDouble(TAG_SLAM_START_Y);
    }

    public static void setSlamStartY(LivingEntity entity, double y) {
        entity.getPersistentData().putDouble(TAG_SLAM_START_Y, y);
    }

    public static boolean getPrevCrouch(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TAG_PREV_CROUCH);
    }

    public static void setPrevCrouch(LivingEntity entity, boolean crouch) {
        entity.getPersistentData().putBoolean(TAG_PREV_CROUCH, crouch);
    }

    public static void clearSlamState(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_SLAMMING);
        entity.getPersistentData().remove(TAG_SLAM_TICK);
        entity.getPersistentData().remove(TAG_SLAM_START_Y);
    }

    public static int getFlightTick(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_FLIGHT_TICK);
    }

    public static void setFlightTick(LivingEntity entity, int tick) {
        entity.getPersistentData().putInt(TAG_FLIGHT_TICK, tick);
    }

}
