package com.goetypunk.addon.common.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;

public class PrototypeItem extends ElytraItem {

    private static final String TAG_SLAMMING = "goetypunk.slamming";
    private static final String TAG_SLAM_TICK = "goetypunk.slam_tick";
    private static final String TAG_SLAM_START_Y = "goetypunk.slam_start_y";
    private static final String TAG_PREV_CROUCH = "goetypunk.prev_crouch";
    private static final String TAG_FLIGHT_TICK = "goetypunk.flight_tick";

    private static final String TAG_WALL_RUNNING = "goetypunk.wall_running";
    private static final String TAG_WALL_NORMAL_X = "goetypunk.wall_nx";
    private static final String TAG_WALL_NORMAL_Y = "goetypunk.wall_ny";
    private static final String TAG_WALL_NORMAL_Z = "goetypunk.wall_nz";
    private static final String TAG_JUMP_CHARGE_TICK = "goetypunk.jump_charge_tick";
    private static final String TAG_SLAM_INVULN_TICK = "goetypunk.slam_invuln_tick";
    private static final String TAG_SPRINT_TICK = "goetypunk.sprint_tick";
    private static final String TAG_FLIGHT_FORWARD = "goetypunk.flight_forward";
    private static final String TAG_CHARGED_JUMPING = "goetypunk.charged_jumping";
    private static final String TAG_CJ_HX = "goetypunk.cj_hx";
    private static final String TAG_CJ_HZ = "goetypunk.cj_hz";

    public PrototypeItem() {
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

    public static boolean isHoldingPrototype(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PrototypeItem;
    }

    public static boolean hasAnyPrototype(LivingEntity entity) {
        if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PrototypeItem)
            return true;
        if (entity instanceof Player player) {
            try {
                var inv = CuriosApi.getCuriosInventory(player);
                return inv.map(i -> i.findFirstCurio(
                        s -> s.getItem() instanceof PrototypeItem).isPresent()).orElse(false);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
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
        entity.getPersistentData().remove(TAG_SLAM_INVULN_TICK);
    }

    public static int getFlightTick(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_FLIGHT_TICK);
    }

    public static void setFlightTick(LivingEntity entity, int tick) {
        entity.getPersistentData().putInt(TAG_FLIGHT_TICK, tick);
    }

    // ── Wall-running ──

    public static boolean isWallRunning(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TAG_WALL_RUNNING);
    }

    public static void setWallRunning(LivingEntity entity, boolean running) {
        entity.getPersistentData().putBoolean(TAG_WALL_RUNNING, running);
    }

    public static Vec3 getWallNormal(LivingEntity entity) {
        var data = entity.getPersistentData();
        return new Vec3(data.getDouble(TAG_WALL_NORMAL_X), data.getDouble(TAG_WALL_NORMAL_Y), data.getDouble(TAG_WALL_NORMAL_Z));
    }

    public static void setWallNormal(LivingEntity entity, Vec3 normal) {
        entity.getPersistentData().putDouble(TAG_WALL_NORMAL_X, normal.x);
        entity.getPersistentData().putDouble(TAG_WALL_NORMAL_Y, normal.y);
        entity.getPersistentData().putDouble(TAG_WALL_NORMAL_Z, normal.z);
    }

    public static void clearWallState(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_WALL_RUNNING);
        entity.getPersistentData().remove(TAG_WALL_NORMAL_X);
        entity.getPersistentData().remove(TAG_WALL_NORMAL_Y);
        entity.getPersistentData().remove(TAG_WALL_NORMAL_Z);
    }

    // ── Jump charging ──

    public static int getJumpChargeTick(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_JUMP_CHARGE_TICK);
    }

    public static void setJumpChargeTick(LivingEntity entity, int tick) {
        entity.getPersistentData().putInt(TAG_JUMP_CHARGE_TICK, tick);
    }

    public static void clearJumpCharge(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_JUMP_CHARGE_TICK);
    }

    // ── Slam invulnerability ──

    public static int getSlamInvulnTick(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_SLAM_INVULN_TICK);
    }

    public static void setSlamInvulnTick(LivingEntity entity, int tick) {
        entity.getPersistentData().putInt(TAG_SLAM_INVULN_TICK, tick);
    }

    // ── Sprint acceleration ──

    public static int getSprintTick(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_SPRINT_TICK);
    }

    public static void setSprintTick(LivingEntity entity, int tick) {
        entity.getPersistentData().putInt(TAG_SPRINT_TICK, tick);
    }

    public static void clearSprint(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_SPRINT_TICK);
    }

    // ── Flight forward input ──

    public static float getFlightForward(LivingEntity entity) {
        return entity.getPersistentData().getFloat(TAG_FLIGHT_FORWARD);
    }

    public static void setFlightForward(LivingEntity entity, float forward) {
        entity.getPersistentData().putFloat(TAG_FLIGHT_FORWARD, forward);
    }

    // ── Charged jump state ──

    public static boolean isChargedJumping(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TAG_CHARGED_JUMPING);
    }

    public static void setChargedJumping(LivingEntity entity, boolean active) {
        entity.getPersistentData().putBoolean(TAG_CHARGED_JUMPING, active);
    }

    public static double getCjHx(LivingEntity entity) {
        return entity.getPersistentData().getDouble(TAG_CJ_HX);
    }

    public static void setCjHx(LivingEntity entity, double hx) {
        entity.getPersistentData().putDouble(TAG_CJ_HX, hx);
    }

    public static double getCjHz(LivingEntity entity) {
        return entity.getPersistentData().getDouble(TAG_CJ_HZ);
    }

    public static void setCjHz(LivingEntity entity, double hz) {
        entity.getPersistentData().putDouble(TAG_CJ_HZ, hz);
    }

    public static void clearChargedJump(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_CHARGED_JUMPING);
        entity.getPersistentData().remove(TAG_CJ_HX);
        entity.getPersistentData().remove(TAG_CJ_HZ);
    }
}
