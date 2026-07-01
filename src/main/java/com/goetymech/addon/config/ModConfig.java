package com.goetymech.addon.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.io.File;

/**
 * GoetyMech Forge 閰嶇疆
 * 鎵€鏈夊彲閰嶇疆鏁板€奸泦涓鐞? */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /** 鎵洸鑳氳儙鍙敜娑堣€楃殑鐏甸瓊鏁伴噺 */
    public static final ForgeConfigSpec.IntValue TWISTED_EMBRYO_SOUL_COST;

    /** 鎴愮啛鑳氳儙鍙敜娑堣€楃殑鐏甸瓊鏁伴噺 */
    public static final ForgeConfigSpec.IntValue MATURE_EMBRYO_SOUL_COST;

    /** 琛€鑲夌啍鐐夎浆鍖栨椂闂达紙tick锛?00 = 15绉掞級 */
    public static final ForgeConfigSpec.IntValue FLESH_FURNACE_COOKING_TIME;

    /** 琛€鑲夌啍鐐夋斁缃椂鑷姩鎵弿涓嶇ゥ鐏偓鐨勮寖鍥?*/
    public static final ForgeConfigSpec.IntValue LINK_RANGE;

    // ====== SculkBind Focus 閰嶇疆 ======

    /** 骞藉尶鏉熺細鑱氭櫠鐏甸瓊娑堣€?*/
    public static final ForgeConfigSpec.IntValue SCULK_BIND_COST;

    /** 骞藉尶鏉熺細鑱氭櫠鏂芥硶鎴愬姛鍐峰嵈鏃堕棿锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue SCULK_BIND_SUCCESS_COOLDOWN;

    /** 骞藉尶鏉熺細鑱氭櫠鏈懡涓喎鍗存椂闂达紙tick锛?*/
    public static final ForgeConfigSpec.IntValue SCULK_BIND_MISS_COOLDOWN;

    /** 閿佸畾鎸佺画鏃堕棿锛堢锛?*/
    public static final ForgeConfigSpec.IntValue SCULK_BIND_LOCK_DURATION;

    /** 娉曟湳鑼冨洿锛堟牸锛?*/
    public static final ForgeConfigSpec.IntValue SCULK_BIND_RANGE;

    /** 鍙敜Warden鏁伴噺 */
    public static final ForgeConfigSpec.IntValue SCULK_BIND_WARDEN_COUNT;

    // ====== Squad Barracks 閰嶇疆 ======

    /** 鍏靛洟鑱氭櫠鏂芥硶鍐峰嵈鏃堕棿锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue SQUAD_SUMMON_COOLDOWN;

    /** 浜岄樁鐐瑰皢鍙扮伒榄傛姌鎵ｅ€嶇巼 */
    public static final ForgeConfigSpec.DoubleValue SQUAD_TIER2_DISCOUNT;

    /** 涓夐樁鐐瑰皢鍙扮伒榄傛姌鎵ｅ€嶇巼 */
    public static final ForgeConfigSpec.DoubleValue SQUAD_TIER3_DISCOUNT;

    /** 鍙敜浠嗕粠瀵垮懡锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue SQUAD_LIFESPAN;

    // ====== Remote Engine 閰嶇疆 ======

    /** 杩滅▼寮曟搸搴斿姏鈫掕浆閫熸崲绠楁瘮 */
    public static final ForgeConfigSpec.IntValue REMOTE_ENGINE_STRESS_TO_RPM_RATIO;

    // ====== Rift Walk Focus 閰嶇疆 ======

    /** 瑁傜含鑱氭櫠锛堜綆绾ц闅欙級鐏甸瓊娑堣€?*/
    public static final ForgeConfigSpec.IntValue UNSTABLE_RIFT_COST;

    /** 瑁傜含鑱氭櫠鍐峰嵈鏃堕棿锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue UNSTABLE_RIFT_COOLDOWN;

    /** 瑁傜含鑱氭櫠鑷姩鍥炰紶寤惰繜锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue UNSTABLE_RIFT_RETURN_DELAY;

    /** 鐣屾梾鑱氭櫠锛堥珮绾ц闅欙級鐏甸瓊娑堣€?*/
    public static final ForgeConfigSpec.IntValue STABLE_RIFT_COST;

    /** 鐣屾梾鑱氭櫠鍐峰嵈鏃堕棿锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue STABLE_RIFT_COOLDOWN;

    // ====== Crushing Wheel 閰嶇疆 ======

    /** 杞瓙鍩虹椋炶璺濈锛堟牸锛?*/
    public static final ForgeConfigSpec.IntValue CRUSHING_WHEEL_BASE_DISTANCE;

    /** 姣忕骇鍗婂緞闄勯瓟澧炲姞鐨勯琛岃窛绂伙紙鏍硷級 */
    public static final ForgeConfigSpec.DoubleValue CRUSHING_WHEEL_RADIUS_DISTANCE_BONUS;

    /** 杞瓙鍒濆閫熷害锛堟牸/绉掞級 */
    public static final ForgeConfigSpec.DoubleValue CRUSHING_WHEEL_SPEED;

    /** 杞瓙鍔犻€熷害锛堟牸/绉捖诧級 */
    public static final ForgeConfigSpec.DoubleValue CRUSHING_WHEEL_ACCELERATION;

    /** 杞瓙涓婇檺閫熷害锛堟牸/绉掞級 */
    public static final ForgeConfigSpec.DoubleValue CRUSHING_WHEEL_MAX_SPEED;

    // ====== Stress Focus 閰嶇疆 ======

    /** 搴斿姏鑱氭櫠鐨勭伒榄傛秷鑰?*/
    public static final ForgeConfigSpec.IntValue STRESS_FOCUS_COST;

    /** 鏂芥硶鍐峰嵈鏃堕棿锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue STRESS_FOCUS_COOLDOWN;

    /** 搴斿姏浼ゅ闄ゆ暟锛坣etwork capacity / 姝ゅ€?= 鍩虹浼ゅ锛?*/
    public static final ForgeConfigSpec.IntValue STRESS_DAMAGE_DIVISOR;

    // ====== Stress Shield Focus 閰嶇疆 ======

    /** 搴斿姏鎶ょ浘鑱氭櫠鐨勭伒榄傛秷鑰?*/
    public static final ForgeConfigSpec.IntValue STRESS_SHIELD_COST;

    /** 鏂芥硶鍐峰嵈鏃堕棿锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue STRESS_SHIELD_COOLDOWN;

    /** 搴斿姏鈫掓姢鐩炬崲绠楅櫎鏁?*/
    public static final ForgeConfigSpec.IntValue STRESS_SHIELD_DIVISOR;

    /** 鍩虹鎸佺画鏃堕棿锛堢锛?*/
    public static final ForgeConfigSpec.IntValue STRESS_SHIELD_BASE_DURATION;

    // ====== 鑲插发閰嶇疆 ======

    /** 鑲插发婊¤兘閲忛槇鍊硷紙hunger 鐐规暟锛?*/
    public static final ForgeConfigSpec.IntValue NEST_MAX_ENERGY;

    // ====== 鐑矾寰勭紦瀛?======
    public static int cachedLinkRange;
    public static int cachedFurnaceCookingTime;
    public static int cachedNestMaxEnergy;

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        refreshCache();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        refreshCache();
    }

    public static void refreshCache() {
        cachedLinkRange = LINK_RANGE.get();
        cachedFurnaceCookingTime = FLESH_FURNACE_COOKING_TIME.get();
        cachedNestMaxEnergy = NEST_MAX_ENERGY.get();
    }

    // ====== Prototype 閰嶇疆 ======

    public static final ForgeConfigSpec.DoubleValue SLAM_GRAVITY_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue SLAM_MAX_SPEED;
    public static final ForgeConfigSpec.DoubleValue SLAM_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue SLAM_RADIUS_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue SLAM_MIN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue SLAM_MAX_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue SLAM_MAX_RADIUS;
    public static final ForgeConfigSpec.IntValue SLAM_HOVER_TICKS;
    public static final ForgeConfigSpec.DoubleValue FLIGHT_TARGET_SPEED;
    public static final ForgeConfigSpec.DoubleValue FLIGHT_ACCEL_RATE;
    public static final ForgeConfigSpec.DoubleValue FLIGHT_MAX_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FLIGHT_DECEL_RATE;
    public static final ForgeConfigSpec.DoubleValue SPRINT_MAX_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue SWIM_MAX_MULTIPLIER;

    // ====== Prototype 鈥?Kill Absorption 閰嶇疆 ======

    /** 鍑绘潃鐢熺墿鐢熷懡鍚告敹姣斾緥 */
    public static final ForgeConfigSpec.DoubleValue KILL_HEALTH_ABSORB_RATIO;

    /** 鍚告敹涓婇檺鍊嶇巼锛堢帺瀹?maxHealth 脳 姝ゅ€?= absorption 涓婇檺锛?*/
    public static final ForgeConfigSpec.DoubleValue KILL_ABSORPTION_CAP_MULTIPLIER;

    /** 鍑绘潃椋熺墿鎭㈠姣斾緥 */
    public static final ForgeConfigSpec.DoubleValue KILL_FOOD_RESTORE_RATIO;

    // ====== Prototype 鈥?Wall-running 閰嶇疆 ======

    public static final ForgeConfigSpec.DoubleValue WALL_RUN_SPEED;
    public static final ForgeConfigSpec.DoubleValue WALL_GRAVITY;
    public static final ForgeConfigSpec.DoubleValue WALL_JUMP_OFF_VELOCITY;
    public static final ForgeConfigSpec.DoubleValue WALL_PULL_SPEED;
    public static final ForgeConfigSpec.DoubleValue WALL_ROTATION_SPEED;
    public static final ForgeConfigSpec.IntValue WALL_RECOVERY_TICKS;
    public static final ForgeConfigSpec.DoubleValue WALL_ROTATION_LIMIT;

    // ====== Prototype 鈥?Jump Charge 閰嶇疆 ======

    public static final ForgeConfigSpec.DoubleValue JUMP_TAP_HEIGHT;
    public static final ForgeConfigSpec.IntValue JUMP_CHARGE_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue JUMP_CHARGE_MIN_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue JUMP_CHARGE_MAX_HEIGHT;
    public static final ForgeConfigSpec.IntValue JUMP_MAX_CHARGE_TICKS;
    public static final ForgeConfigSpec.DoubleValue SWIM_CHARGE_MIN_SPEED;
    public static final ForgeConfigSpec.DoubleValue SWIM_CHARGE_MAX_SPEED;

    // ====== Soul Catcher 閰嶇疆 ======

    /** 鐏甸瓊鎹曟墜鍑绘潃闃堝€?*/
    public static final ForgeConfigSpec.IntValue SOUL_CATCHER_KILLS_THRESHOLD;

    /** 鐏甸瓊鎹曟墜闊虫晥鍐峰嵈 (tick) */
    public static final ForgeConfigSpec.IntValue SOUL_CATCHER_SOUND_COOLDOWN;

    // ====== Void Blink Focus 閰嶇疆 ======

    /** 铏氱┖闂幇鑱氭櫠鐏甸瓊娑堣€?*/
    public static final ForgeConfigSpec.IntValue VOID_BLINK_COST;

    /** 铏氱┖闂幇鑱氭櫠鍐峰嵈鏃堕棿锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue VOID_BLINK_COOLDOWN;

    /** 鍩虹閿佸畾璺濈锛堟牸锛?*/
    public static final ForgeConfigSpec.IntValue VOID_BLINK_RANGE;

    /** 鍩虹浼犻€佹鏁?*/
    public static final ForgeConfigSpec.IntValue VOID_BLINK_MAX_USES;

    /** 鍩虹鏁堟灉绐楀彛锛堢锛?*/
    public static final ForgeConfigSpec.IntValue VOID_BLINK_WINDOW_SECONDS;

    // ====== Time Freeze 閰嶇疆 ======

    /** 鏃跺仠鎬诲紑鍏?*/
    public static final ForgeConfigSpec.BooleanValue TIME_STOP_ENABLED;

    /** 鏃跺仠鍊掕鏃跺紑鍏筹紙榛樿鍏抽棴锛?*/
    public static final ForgeConfigSpec.BooleanValue TIME_STOP_COUNTDOWN_ENABLED;

    /** 鏃跺仠榛樿鏃堕暱锛坱ick锛?*/
    public static final ForgeConfigSpec.IntValue TIME_STOP_DEFAULT_DURATION;

    // ====== Flesh Casket 閰嶇疆 ======

    /** Type C 鑷姩鏂芥硶瑙﹀彂闃堝€肩櫨鍒嗘瘮 */
    public static final ForgeConfigSpec.IntValue FLESH_CASKET_THRESHOLD;

    static {
        // ====== Flesh Casket 閰嶇疆 ======
        BUILDER.push("flesh_casket");

        FLESH_CASKET_THRESHOLD = BUILDER
                .comment("config_desc")
                .defineInRange("threshold", 20, 1, 100);

        BUILDER.pop();

        // ====== Soul Catcher 閰嶇疆 ======
        BUILDER.push("soul_catcher");

        SOUL_CATCHER_KILLS_THRESHOLD = BUILDER
                .comment("config_desc")
                .defineInRange("kills_threshold", 5, 1, 1000);

        SOUL_CATCHER_SOUND_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("sound_cooldown_ticks", 40, 0, 1200);

        BUILDER.pop();

        BUILDER.push("time_stop");

        TIME_STOP_ENABLED = BUILDER
                .comment("config_desc")
                .define("time_stop_enabled", true);

        TIME_STOP_COUNTDOWN_ENABLED = BUILDER
                .comment("config_desc")
                .define("time_stop_countdown_enabled", false);

        TIME_STOP_DEFAULT_DURATION = BUILDER
                .comment("config_desc")
                .defineInRange("time_stop_default_duration", 180, 20, 72000);

        BUILDER.pop();

        BUILDER.push("items");

        TWISTED_EMBRYO_SOUL_COST = BUILDER
                .comment("config_desc")
                .defineInRange("twisted_embryo_soul_cost", 10, 1, 10000);

        MATURE_EMBRYO_SOUL_COST = BUILDER
                .comment("config_desc")
                .defineInRange("mature_embryo_soul_cost", 100, 1, 10000);

        BUILDER.pop();

        BUILDER.push("blocks");

        FLESH_FURNACE_COOKING_TIME = BUILDER
                .comment("config_desc")
                .defineInRange("flesh_furnace_cooking_time", 15, 1, 10000);

        LINK_RANGE = BUILDER
                .comment("config_desc")
                .defineInRange("link_range", 8, 2, 32);

        BUILDER.pop();

        BUILDER.push("breeding_nest");

        NEST_MAX_ENERGY = BUILDER
                .comment("config_desc")
                .defineInRange("max_energy", 25, 1, 1000);

        BUILDER.pop();

        // ====== SculkBind Focus 閰嶇疆 ======
        BUILDER.push("sculk_bind_focus");

        SCULK_BIND_COST = BUILDER
                .comment("config_desc")
                .defineInRange("soul_cost", 80, 1, 1000);

        SCULK_BIND_SUCCESS_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("success_cooldown", 100, 20, 12000);

        SCULK_BIND_MISS_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("miss_cooldown", 80, 20, 12000);

        SCULK_BIND_LOCK_DURATION = BUILDER
                .comment("config_desc")
                .defineInRange("lock_duration", 10, 1, 60);

        SCULK_BIND_RANGE = BUILDER
                .comment("config_desc")
                .defineInRange("range", 16, 5, 64);

        SCULK_BIND_WARDEN_COUNT = BUILDER
                .comment("config_desc")
                .defineInRange("warden_count", 1, 1, 10);

        BUILDER.pop();

        // ====== Squad Barracks 閰嶇疆 ======
        BUILDER.push("squad_barracks");

        SQUAD_SUMMON_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("squad_summon_cooldown", 200, 20, 12000);

        SQUAD_TIER2_DISCOUNT = BUILDER
                .comment("config_desc")
                .defineInRange("tier2_discount", 0.5, 0.0, 1.0);

        SQUAD_TIER3_DISCOUNT = BUILDER
                .comment("config_desc")
                .defineInRange("tier3_discount", 0.1, 0.0, 1.0);

        SQUAD_LIFESPAN = BUILDER
                .comment("config_desc")
                .defineInRange("squad_lifespan", 6000, 20, 72000);

        // ====== Remote Engine 閰嶇疆 ======
        BUILDER.push("remote_engine");

        REMOTE_ENGINE_STRESS_TO_RPM_RATIO = BUILDER
                .comment("config_desc")
                .defineInRange("stress_to_rpm_ratio", 64, 1, 1024);

        BUILDER.pop();

        // ====== Rift Walk Focus 閰嶇疆 ======
        BUILDER.push("rift_walk");

        UNSTABLE_RIFT_COST = BUILDER
                .comment("config_desc")
                .defineInRange("unstable_rift_cost", 100, 1, 5000);

        UNSTABLE_RIFT_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("unstable_rift_cooldown", 6000, 20, 24000);

        UNSTABLE_RIFT_RETURN_DELAY = BUILDER
                .comment("config_desc")
                .defineInRange("unstable_rift_return_delay", 200, 40, 600);

        STABLE_RIFT_COST = BUILDER
                .comment("config_desc")
                .defineInRange("stable_rift_cost", 500, 1, 5000);

        STABLE_RIFT_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("stable_rift_cooldown", 300, 20, 12000);

        BUILDER.pop();

        // ====== Crushing Wheel 閰嶇疆 ======
        BUILDER.push("crushing_wheel");

        CRUSHING_WHEEL_BASE_DISTANCE = BUILDER
                .comment("config_desc")
                .defineInRange("base_distance", 50, 5, 100);

        CRUSHING_WHEEL_RADIUS_DISTANCE_BONUS = BUILDER
                .comment("config_desc")
                .defineInRange("radius_distance_bonus", 5.0, 0.0, 50.0);

        CRUSHING_WHEEL_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("speed", 5.0, 1.0, 100.0);

        CRUSHING_WHEEL_ACCELERATION = BUILDER
                .comment("config_desc")
                .defineInRange("acceleration", 2.0, 0.0, 50.0);

        CRUSHING_WHEEL_MAX_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("max_speed", 99.0, 1.0, 100.0);

        BUILDER.pop();

        // ====== Stress Focus 閰嶇疆 ======
        BUILDER.push("stress_focus");

        STRESS_FOCUS_COST = BUILDER
                .comment("config_desc")
                .defineInRange("soul_cost", 0, 0, 1000);

        STRESS_FOCUS_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("cooldown", 20, 5, 200);

        STRESS_DAMAGE_DIVISOR = BUILDER
                .comment("config_desc")
                .defineInRange("damage_divisor", 2000, 100, 10000);

        BUILDER.pop();

        // ====== Stress Shield Focus 閰嶇疆 ======
        BUILDER.push("stress_shield_focus");

        STRESS_SHIELD_COST = BUILDER
                .comment("config_desc")
                .defineInRange("soul_cost", 100, 0, 1000);

        STRESS_SHIELD_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("cooldown", 12000, 100, 24000);

        STRESS_SHIELD_DIVISOR = BUILDER
                .comment("config_desc")
                .defineInRange("divisor", 5000, 500, 50000);

        STRESS_SHIELD_BASE_DURATION = BUILDER
                .comment("config_desc")
                .defineInRange("base_duration", 60, 10, 600);

        BUILDER.pop();

        // ====== Prototype 閰嶇疆 ======
        BUILDER.push("prototype");

        SLAM_GRAVITY_PER_TICK = BUILDER
                .comment("config_desc")
                .defineInRange("slam_gravity_per_tick", 0.25, 0.01, 2.0);

        SLAM_MAX_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("slam_max_speed", 6.0, 0.5, 20.0);

        SLAM_DAMAGE_MULTIPLIER = BUILDER
                .comment("config_desc")
                .defineInRange("slam_damage_multiplier", 1.0, 0.0, 10.0);

        SLAM_RADIUS_MULTIPLIER = BUILDER
                .comment("config_desc")
                .defineInRange("slam_radius_multiplier", 1.0, 0.0, 20.0);

        SLAM_MIN_DAMAGE = BUILDER
                .comment("config_desc")
                .defineInRange("slam_min_damage", 20.0, 1.0, 9999.0);

        SLAM_MAX_DAMAGE = BUILDER
                .comment("config_desc")
                .defineInRange("slam_max_damage", 1200.0, 1.0, 9999.0);

        SLAM_MAX_RADIUS = BUILDER
                .comment("config_desc")
                .defineInRange("slam_max_radius", 100.0, 1.0, 200.0);

        SLAM_HOVER_TICKS = BUILDER
                .comment("config_desc")
                .defineInRange("slam_hover_ticks", 5, 0, 100);

        FLIGHT_TARGET_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("flight_target_speed", 0.5, 0.1, 2.0);

        FLIGHT_ACCEL_RATE = BUILDER
                .comment("config_desc")
                .defineInRange("flight_accel_rate", 0.05, 0.01, 1.0);

        FLIGHT_MAX_MULTIPLIER = BUILDER
                .comment("config_desc")
                .defineInRange("flight_max_multiplier", 10.0, 1.0, 50.0);

        FLIGHT_DECEL_RATE = BUILDER
                .comment("config_desc")
                .defineInRange("flight_decel_rate", 0.97, 0.5, 0.999);

        SPRINT_MAX_MULTIPLIER = BUILDER
                .comment("config_desc")
                .defineInRange("sprint_max_multiplier", 8.0, 0.0, 20.0);

        SWIM_MAX_MULTIPLIER = BUILDER
                .comment("config_desc")
                .defineInRange("swim_max_multiplier", 8.0, 0.0, 20.0);

        KILL_HEALTH_ABSORB_RATIO = BUILDER
                .comment("config_desc")
                .defineInRange("kill_health_absorb_ratio", 0.8, 0.0, 2.0);

        KILL_ABSORPTION_CAP_MULTIPLIER = BUILDER
                .comment("config_desc")
                .defineInRange("kill_absorption_cap_multiplier", 2.0, 1.0, 10.0);

        KILL_FOOD_RESTORE_RATIO = BUILDER
                .comment("config_desc")
                .defineInRange("kill_food_restore_ratio", 0.333, 0.0, 2.0);

        BUILDER.pop();

        // ====== Wall-running 閰嶇疆 ======
        BUILDER.push("wall_run");

        WALL_RUN_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("wall_run_speed", 0.6, 0.01, 3.0);

        WALL_GRAVITY = BUILDER
                .comment("config_desc")
                .defineInRange("wall_gravity", 0.03, 0.0, 1.0);

        WALL_JUMP_OFF_VELOCITY = BUILDER
                .comment("config_desc")
                .defineInRange("wall_jump_off_velocity", 0.6, 0.1, 2.0);

        WALL_PULL_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("wall_pull_speed", 0.3, 0.05, 1.0);

        WALL_ROTATION_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("wall_rotation_speed", 0.15, 0.05, 0.5);

        WALL_RECOVERY_TICKS = BUILDER
                .comment("config_desc")
                .defineInRange("wall_recovery_ticks", 10, 0, 40);

        WALL_ROTATION_LIMIT = BUILDER
                .comment("config_desc")
                .defineInRange("wall_rotation_limit", 85.0, 45.0, 90.0);

        BUILDER.pop();

        // ====== Jump Charge 閰嶇疆 ======
        BUILDER.push("jump_charge");

        JUMP_TAP_HEIGHT = BUILDER
                .comment("config_desc")
                .defineInRange("jump_tap_height", 3.0, 1.0, 10.0);

        JUMP_CHARGE_THRESHOLD = BUILDER
                .comment("config_desc")
                .defineInRange("jump_charge_threshold", 6, 5, 59);

        JUMP_CHARGE_MIN_HEIGHT = BUILDER
                .comment("config_desc")
                .defineInRange("jump_charge_min_height", 8.0, 3.0, 80.0);

        JUMP_CHARGE_MAX_HEIGHT = BUILDER
                .comment("config_desc")
                .defineInRange("jump_charge_max_height", 80.0, 8.0, 200.0);

        JUMP_MAX_CHARGE_TICKS = BUILDER
                .comment("config_desc")
                .defineInRange("jump_max_charge_ticks", 60, 20, 200);

        SWIM_CHARGE_MIN_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("swim_charge_min_speed", 1.5, 0.5, 5.0);

        SWIM_CHARGE_MAX_SPEED = BUILDER
                .comment("config_desc")
                .defineInRange("swim_charge_max_speed", 4.0, 1.0, 10.0);

        BUILDER.pop();

        // ====== Chase Focus 閰嶇疆 ======
        BUILDER.push("chase_focus");

        VOID_BLINK_COST = BUILDER
                .comment("config_desc")
                .defineInRange("soul_cost", 140, 1, 1000);

        VOID_BLINK_COOLDOWN = BUILDER
                .comment("config_desc")
                .defineInRange("cooldown", 3600, 100, 72000);

        VOID_BLINK_RANGE = BUILDER
                .comment("config_desc")
                .defineInRange("range", 20, 5, 64);

        VOID_BLINK_MAX_USES = BUILDER
                .comment("config_desc")
                .defineInRange("max_uses", 5, 1, 20);

        VOID_BLINK_WINDOW_SECONDS = BUILDER
                .comment("config_desc")
                .defineInRange("window_seconds", 10, 1, 60);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /**
     * 鎵嬪姩鍔犺浇閰嶇疆鏂囦欢锛岀‘淇濆湪鐗╁搧娉ㄥ唽鍓嶉厤缃凡灏辩华
     * 浠跨収 Goety SpellConfig 鐨勫仛娉曪紝閬垮厤寮€鍙戠幆澧冧腑 "Cannot get config value before config is loaded" 宕╂簝
     */
    public static void loadConfig(ForgeConfigSpec config, String path) {
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path))
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        file.load();
        config.setConfig(file);
    }
}