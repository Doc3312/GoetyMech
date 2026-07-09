package com.goetymech.addon.common.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;

public class PrototypeItem extends ElytraItem {

    private static final String TAG_SLAMMING = "goetymech.slamming";
    private static final String TAG_SLAM_TICK = "goetymech.slam_tick";
    private static final String TAG_SLAM_START_Y = "goetymech.slam_start_y";
    private static final String TAG_PREV_CROUCH = "goetymech.prev_crouch";
    private static final String TAG_FLIGHT_TICK = "goetymech.flight_tick";

    private static final String TAG_WALL_RUNNING = "goetymech.wall_running";
    private static final String TAG_WALL_NORMAL_X = "goetymech.wall_nx";
    private static final String TAG_WALL_NORMAL_Y = "goetymech.wall_ny";
    private static final String TAG_WALL_NORMAL_Z = "goetymech.wall_nz";
    private static final String TAG_WALL_RUN_TICKS = "goetymech.wall_run_ticks";
    private static final String TAG_WALL_RECOVERY_TICKS = "goetymech.wall_recovery_ticks";
    private static final String TAG_WALL_FORWARD = "goetymech.wall_forward";
    private static final String TAG_WALL_STRAFE = "goetymech.wall_strafe";
    private static final String TAG_JUMP_CHARGE_TICK = "goetymech.jump_charge_tick";
    private static final String TAG_SLAM_INVULN_TICK = "goetymech.slam_invuln_tick";
    private static final String TAG_SPRINT_TICK = "goetymech.sprint_tick";
    private static final String TAG_FLIGHT_FORWARD = "goetymech.flight_forward";
    private static final String TAG_SWIM_TICK = "goetymech.swim_tick";
    private static final String TAG_CHARGED_JUMPING = "goetymech.charged_jumping";
    private static final String TAG_CJ_HX = "goetymech.cj_hx";
    private static final String TAG_CJ_HZ = "goetymech.cj_hz";
    private static final String TAG_GROUND_SPEED = "goetymech.ground_speed";
    private static final String TAG_CJ_START_SPEED = "goetymech.cj_start_speed";
    private static final String TAG_CHARGE_START_Y = "goetymech.charge_start_y";

    private static final String TAG_ASSIMILATION_KILLS = "goetymech.assimilation_kills";
    private static final String TAG_KNOCKBACK_IMMUNE = "goetymech.knockback_immune";

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

    public static boolean isPrototypeItem(Item item) {
        return item instanceof PrototypeItem || item instanceof PrototypeMimicItem;
    }

    public static boolean isPrototype(ItemStack stack) {
        return isPrototypeItem(stack.getItem());
    }

    public static boolean isHoldingPrototype(LivingEntity entity) {
        return isPrototype(entity.getItemBySlot(EquipmentSlot.CHEST));
    }

    public static boolean hasAnyPrototype(LivingEntity entity) {
        if (isPrototype(entity.getItemBySlot(EquipmentSlot.CHEST)))
            return true;
        if (entity instanceof Player player) {
            try {
                var inv = CuriosApi.getCuriosInventory(player);
                return inv.map(i -> i.findFirstCurio(
                        s -> isPrototype(s)).isPresent()).orElse(false);
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

    public static int getWallRunTicks(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_WALL_RUN_TICKS);
    }

    public static void setWallRunTicks(LivingEntity entity, int ticks) {
        entity.getPersistentData().putInt(TAG_WALL_RUN_TICKS, ticks);
    }

    public static int getWallRecoveryTicks(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_WALL_RECOVERY_TICKS);
    }

    public static void setWallRecoveryTicks(LivingEntity entity, int ticks) {
        entity.getPersistentData().putInt(TAG_WALL_RECOVERY_TICKS, ticks);
    }

    public static void clearWallRecovery(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_WALL_RECOVERY_TICKS);
    }

    // ── Wall input (synced from client) ──

    public static float getWallForward(LivingEntity entity) {
        return entity.getPersistentData().getFloat(TAG_WALL_FORWARD);
    }

    public static void setWallForward(LivingEntity entity, float forward) {
        entity.getPersistentData().putFloat(TAG_WALL_FORWARD, forward);
    }

    public static float getWallStrafe(LivingEntity entity) {
        return entity.getPersistentData().getFloat(TAG_WALL_STRAFE);
    }

    public static void setWallStrafe(LivingEntity entity, float strafe) {
        entity.getPersistentData().putFloat(TAG_WALL_STRAFE, strafe);
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

    // ── Swim acceleration ──

    public static int getSwimTick(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_SWIM_TICK);
    }

    public static void setSwimTick(LivingEntity entity, int tick) {
        entity.getPersistentData().putInt(TAG_SWIM_TICK, tick);
    }

    public static void clearSwim(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_SWIM_TICK);
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
        entity.getPersistentData().remove(TAG_CJ_START_SPEED);
    }

    public static double getCjStartSpeed(LivingEntity entity) {
        return entity.getPersistentData().getDouble(TAG_CJ_START_SPEED);
    }

    public static void setCjStartSpeed(LivingEntity entity, double speed) {
        entity.getPersistentData().putDouble(TAG_CJ_START_SPEED, speed);
    }

    // ── Ground speed tracking ──

    public static double getGroundSpeed(LivingEntity entity) {
        return entity.getPersistentData().getDouble(TAG_GROUND_SPEED);
    }

    public static void setGroundSpeed(LivingEntity entity, double speed) {
        entity.getPersistentData().putDouble(TAG_GROUND_SPEED, speed);
    }

    // ── Charge start Y tracking ──

    public static double getChargeStartY(LivingEntity entity) {
        return entity.getPersistentData().getDouble(TAG_CHARGE_START_Y);
    }

    public static void setChargeStartY(LivingEntity entity, double y) {
        entity.getPersistentData().putDouble(TAG_CHARGE_START_Y, y);
    }

    public static void clearChargeStartY(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_CHARGE_START_Y);
    }

    // ── Knockback immunity ──

    public static boolean isKnockbackImmune(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TAG_KNOCKBACK_IMMUNE);
    }

    public static void setKnockbackImmune(LivingEntity entity, boolean immune) {
        if (immune) {
            entity.getPersistentData().putBoolean(TAG_KNOCKBACK_IMMUNE, true);
        } else {
            entity.getPersistentData().remove(TAG_KNOCKBACK_IMMUNE);
        }
    }

    // ── Assimilation kill count ──

    public static int getAssimilationKills(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG_ASSIMILATION_KILLS);
    }

    public static void setAssimilationKills(LivingEntity entity, int kills) {
        entity.getPersistentData().putInt(TAG_ASSIMILATION_KILLS, Math.max(0, kills));
    }

    public static void incrementAssimilationKills(LivingEntity entity) {
        setAssimilationKills(entity, getAssimilationKills(entity) + 1);
    }

    // ── Assimilation tier utilities ──

    public static int getAssimilationTier(int kills) {
        if (kills < 10) return 0;
        if (kills < 50) return 1;
        if (kills < 100) return 2;
        return 3;
    }

    public static int getAssimilationTier(LivingEntity entity) {
        return getAssimilationTier(getAssimilationKills(entity));
    }

    public static double getTierMultiplier(int kills) {
        int tier = getAssimilationTier(kills);
        if (tier <= 1) return 1.0 / 3.0;
        if (tier == 2) return 2.0 / 3.0;
        return 1.0;
    }

    public static boolean hasFlight(int kills) {
        return true;
    }

    public static boolean hasSlam(int kills) {
        return kills >= 50;
    }

    // ── Damage multiplier ──

    public static float getDamageMultiplier(int kills) {
        if (kills <= 0) return 1.0f;
        if (kills <= 100) return 1.0f + (float) kills / 100.0f;
        if (kills <= 1000) return 2.0f + (float) (kills - 100) / 900.0f;
        if (kills <= 10000) return 3.0f + (float) (kills - 1000) / 9000.0f;
        if (kills <= 100000) return 4.0f + (float) (kills - 10000) / 10000.0f;
        return 13.0f + (float) (kills - 100000) / 100000.0f;
    }

    public static float getBonusMagicDamage(int kills) {
        return Math.max(1.0f, kills / 1000.0f);
    }

    public static int getDamageImmunityThreshold(int kills) {
        if (kills < 150) return 0;
        if (kills < 1000) return kills / 100;
        int t = 10 + (kills - 1000) / 1000;
        if (kills >= 10000) t = 19 + (kills - 10000) / 2000;
        return t;
    }

    public static int getNextImmunityMilestone(int kills) {
        if (kills < 150) return 150;
        int current = getDamageImmunityThreshold(kills);
        if (kills < 1000) {
            int next = (current + 1) * 100;
            if (next < 1000) return next;
            return 1000 + (current - 9) * 1000;
        }
        if (kills < 10000) {
            int next = 1000 + (current - 9) * 1000;
            if (next <= 10000) return next;
            return 10000 + (current - 18) * 2000;
        }
        return 10000 + (current - 18) * 2000;
    }
}
