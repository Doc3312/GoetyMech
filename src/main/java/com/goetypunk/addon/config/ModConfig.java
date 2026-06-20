package com.goetypunk.addon.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.io.File;

/**
 * GoetyPunk Forge 配置
 * 所有可配置数值集中管理
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /** 扭曲胚胎召唤消耗的灵魂数量 */
    public static final ForgeConfigSpec.IntValue TWISTED_EMBRYO_SOUL_COST;

    /** 成熟胚胎召唤消耗的灵魂数量 */
    public static final ForgeConfigSpec.IntValue MATURE_EMBRYO_SOUL_COST;

    /** 血肉熔炉转化时间（tick，300 = 15秒） */
    public static final ForgeConfigSpec.IntValue FLESH_FURNACE_COOKING_TIME;

    /** 血肉熔炉放置时自动扫描不祥火炬的范围 */
    public static final ForgeConfigSpec.IntValue LINK_RANGE;

    // ====== SculkBind Focus 配置 ======

    /** 幽匿束缚聚晶灵魂消耗 */
    public static final ForgeConfigSpec.IntValue SCULK_BIND_COST;

    /** 幽匿束缚聚晶施法成功冷却时间（tick） */
    public static final ForgeConfigSpec.IntValue SCULK_BIND_SUCCESS_COOLDOWN;

    /** 幽匿束缚聚晶未命中冷却时间（tick） */
    public static final ForgeConfigSpec.IntValue SCULK_BIND_MISS_COOLDOWN;

    /** 锁定持续时间（秒） */
    public static final ForgeConfigSpec.IntValue SCULK_BIND_LOCK_DURATION;

    /** 法术范围（格） */
    public static final ForgeConfigSpec.IntValue SCULK_BIND_RANGE;

    /** 召唤Warden数量 */
    public static final ForgeConfigSpec.IntValue SCULK_BIND_WARDEN_COUNT;

    // ====== Squad Barracks 配置 ======

    /** 兵团聚晶施法冷却时间（tick） */
    public static final ForgeConfigSpec.IntValue SQUAD_SUMMON_COOLDOWN;

    /** 二阶点将台灵魂折扣倍率 */
    public static final ForgeConfigSpec.DoubleValue SQUAD_TIER2_DISCOUNT;

    /** 三阶点将台灵魂折扣倍率 */
    public static final ForgeConfigSpec.DoubleValue SQUAD_TIER3_DISCOUNT;

    /** 召唤仆从寿命（tick） */
    public static final ForgeConfigSpec.IntValue SQUAD_LIFESPAN;

    // ====== Remote Engine 配置 ======

    /** 远程引擎应力→转速换算比 */
    public static final ForgeConfigSpec.IntValue REMOTE_ENGINE_STRESS_TO_RPM_RATIO;

    // ====== Rift Walk Focus 配置 ======

    /** 裂纬聚晶（低级裂隙）灵魂消耗 */
    public static final ForgeConfigSpec.IntValue UNSTABLE_RIFT_COST;

    /** 裂纬聚晶冷却时间（tick） */
    public static final ForgeConfigSpec.IntValue UNSTABLE_RIFT_COOLDOWN;

    /** 裂纬聚晶自动回传延迟（tick） */
    public static final ForgeConfigSpec.IntValue UNSTABLE_RIFT_RETURN_DELAY;

    /** 界旅聚晶（高级裂隙）灵魂消耗 */
    public static final ForgeConfigSpec.IntValue STABLE_RIFT_COST;

    /** 界旅聚晶冷却时间（tick） */
    public static final ForgeConfigSpec.IntValue STABLE_RIFT_COOLDOWN;

    // ====== Crushing Wheel 配置 ======

    /** 轮子基础飞行距离（格） */
    public static final ForgeConfigSpec.IntValue CRUSHING_WHEEL_BASE_DISTANCE;

    /** 每级半径附魔增加的飞行距离（格） */
    public static final ForgeConfigSpec.DoubleValue CRUSHING_WHEEL_RADIUS_DISTANCE_BONUS;

    /** 轮子初始速度（格/秒） */
    public static final ForgeConfigSpec.DoubleValue CRUSHING_WHEEL_SPEED;

    /** 轮子加速度（格/秒²） */
    public static final ForgeConfigSpec.DoubleValue CRUSHING_WHEEL_ACCELERATION;

    /** 轮子上限速度（格/秒） */
    public static final ForgeConfigSpec.DoubleValue CRUSHING_WHEEL_MAX_SPEED;

    // ====== Stress Focus 配置 ======

    /** 应力聚晶的灵魂消耗 */
    public static final ForgeConfigSpec.IntValue STRESS_FOCUS_COST;

    /** 施法冷却时间（tick） */
    public static final ForgeConfigSpec.IntValue STRESS_FOCUS_COOLDOWN;

    /** 应力伤害除数（network capacity / 此值 = 基础伤害） */
    public static final ForgeConfigSpec.IntValue STRESS_DAMAGE_DIVISOR;

    // ====== 热路径缓存 ======
    public static int cachedLinkRange;
    public static int cachedFurnaceCookingTime;

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        refreshCache();
    }

    public static void refreshCache() {
        cachedLinkRange = LINK_RANGE.get();
        cachedFurnaceCookingTime = FLESH_FURNACE_COOKING_TIME.get();
    }

    // ====== Time Freeze 配置 ======

    /** 时停总开关 */
    public static final ForgeConfigSpec.BooleanValue TIME_STOP_ENABLED;

    /** 时停倒计时开关（默认关闭） */
    public static final ForgeConfigSpec.BooleanValue TIME_STOP_COUNTDOWN_ENABLED;

    /** 时停默认时长（tick） */
    public static final ForgeConfigSpec.IntValue TIME_STOP_DEFAULT_DURATION;

    static {
        BUILDER.push("time_stop");

        TIME_STOP_ENABLED = BUILDER
                .comment("Enable time stop feature", "范围: true/false，默认 true")
                .define("time_stop_enabled", true);

        TIME_STOP_COUNTDOWN_ENABLED = BUILDER
                .comment("Enable time stop countdown (auto-end after duration)", "范围: true/false，默认 false")
                .define("time_stop_countdown_enabled", false);

        TIME_STOP_DEFAULT_DURATION = BUILDER
                .comment("Default time stop duration in ticks (20 ticks = 1 second)", "仅在 countdown 启用时生效", "范围: 20-72000，默认 180（9秒）")
                .defineInRange("time_stop_default_duration", 180, 20, 72000);

        BUILDER.pop();

        BUILDER.push("items");

        TWISTED_EMBRYO_SOUL_COST = BUILDER
                .comment("扭曲胚胎召唤新刌民时消耗的灵魂数量", "范围: 1-10000，默认 10")
                .defineInRange("twisted_embryo_soul_cost", 10, 1, 10000);

        MATURE_EMBRYO_SOUL_COST = BUILDER
                .comment("成熟胚胎召唤仆从时消耗的灵魂数量", "范围: 1-10000，默认 100")
                .defineInRange("mature_embryo_soul_cost", 100, 1, 10000);

        BUILDER.pop();

        BUILDER.push("blocks");

        FLESH_FURNACE_COOKING_TIME = BUILDER
                .comment("血肉熔炉转化扭曲胚胎所需时间（秒）", "范围: 1-10000，默认 15")
                .defineInRange("flesh_furnace_cooking_time", 15, 1, 10000);

        LINK_RANGE = BUILDER
                .comment("血肉熔炉放置时自动扫描不祥火炬的范围（格）", "范围: 2-32，默认 8")
                .defineInRange("link_range", 8, 2, 32);

        BUILDER.pop();

        // ====== SculkBind Focus 配置 ======
        BUILDER.push("sculk_bind_focus");

        SCULK_BIND_COST = BUILDER
                .comment("幽匿束缚聚晶的灵魂消耗", "范围: 1-1000，默认 80")
                .defineInRange("soul_cost", 80, 1, 1000);

        SCULK_BIND_SUCCESS_COOLDOWN = BUILDER
                .comment("幽匿束缚聚晶施法成功冷却时间（tick）", "范围: 20-12000，默认 100（5秒，测试用）")
                .defineInRange("success_cooldown", 100, 20, 12000);

        SCULK_BIND_MISS_COOLDOWN = BUILDER
                .comment("幽匿束缚聚晶未命中冷却时间（tick）", "范围: 20-12000，默认 80（4秒）")
                .defineInRange("miss_cooldown", 80, 20, 12000);

        SCULK_BIND_LOCK_DURATION = BUILDER
                .comment("锁定持续时间（秒）", "范围: 1-60，默认 10")
                .defineInRange("lock_duration", 10, 1, 60);

        SCULK_BIND_RANGE = BUILDER
                .comment("法术最大射程（格）", "范围: 5-64，默认 16")
                .defineInRange("range", 16, 5, 64);

        SCULK_BIND_WARDEN_COUNT = BUILDER
                .comment("每次施法召唤Warden的数量", "范围: 1-10，默认 1")
                .defineInRange("warden_count", 1, 1, 10);

        BUILDER.pop();

        // ====== Squad Barracks 配置 ======
        BUILDER.push("squad_barracks");

        SQUAD_SUMMON_COOLDOWN = BUILDER
                .comment("兵团聚晶施法冷却时间（tick）", "范围: 20-12000，默认 200（10秒）")
                .defineInRange("squad_summon_cooldown", 200, 20, 12000);

        SQUAD_TIER2_DISCOUNT = BUILDER
                .comment("二阶点将台灵魂折扣倍率", "范围: 0.0-1.0，默认 0.5")
                .defineInRange("tier2_discount", 0.5, 0.0, 1.0);

        SQUAD_TIER3_DISCOUNT = BUILDER
                .comment("三阶点将台灵魂折扣倍率", "范围: 0.0-1.0，默认 0.1")
                .defineInRange("tier3_discount", 0.1, 0.0, 1.0);

        SQUAD_LIFESPAN = BUILDER
                .comment("召唤仆从寿命（tick）", "范围: 20-72000，默认 6000（5分钟）")
                .defineInRange("squad_lifespan", 6000, 20, 72000);

        // ====== Remote Engine 配置 ======
        BUILDER.push("remote_engine");

        REMOTE_ENGINE_STRESS_TO_RPM_RATIO = BUILDER
                .comment("远程引擎的应力→转速换算比", "RPM = 可用应力 / 此值", "例: 可用应力 1024, 比值 64 → 16 RPM", "范围: 1-1024，默认 64")
                .defineInRange("stress_to_rpm_ratio", 64, 1, 1024);

        BUILDER.pop();

        // ====== Rift Walk Focus 配置 ======
        BUILDER.push("rift_walk");

        UNSTABLE_RIFT_COST = BUILDER
                .comment("裂纬聚晶（低级裂隙）的灵魂消耗", "范围: 1-5000，默认 100")
                .defineInRange("unstable_rift_cost", 100, 1, 5000);

        UNSTABLE_RIFT_COOLDOWN = BUILDER
                .comment("裂纬聚晶冷却时间（tick）", "范围: 20-24000，默认 6000（5分钟）")
                .defineInRange("unstable_rift_cooldown", 6000, 20, 24000);

        UNSTABLE_RIFT_RETURN_DELAY = BUILDER
                .comment("裂纬聚晶自动回传延迟（tick）", "范围: 40-600，默认 200（10秒）")
                .defineInRange("unstable_rift_return_delay", 200, 40, 600);

        STABLE_RIFT_COST = BUILDER
                .comment("界旅聚晶（高级裂隙）的灵魂消耗", "范围: 1-5000，默认 500")
                .defineInRange("stable_rift_cost", 500, 1, 5000);

        STABLE_RIFT_COOLDOWN = BUILDER
                .comment("界旅聚晶冷却时间（tick）", "范围: 20-12000，默认 300（15秒）")
                .defineInRange("stable_rift_cooldown", 300, 20, 12000);

        BUILDER.pop();

        // ====== Crushing Wheel 配置 ======
        BUILDER.push("crushing_wheel");

        CRUSHING_WHEEL_BASE_DISTANCE = BUILDER
                .comment("轮子基础飞行距离（格）", "范围: 5-100，默认 50")
                .defineInRange("base_distance", 50, 5, 100);

        CRUSHING_WHEEL_RADIUS_DISTANCE_BONUS = BUILDER
                .comment("每级半径附魔增加的飞行距离（格）", "范围: 0-50，默认 5")
                .defineInRange("radius_distance_bonus", 5.0, 0.0, 50.0);

        CRUSHING_WHEEL_SPEED = BUILDER
                .comment("轮子初始速度（格/秒）", "范围: 1-100，默认 5")
                .defineInRange("speed", 5.0, 1.0, 100.0);

        CRUSHING_WHEEL_ACCELERATION = BUILDER
                .comment("轮子加速度（格/秒²）", "范围: 0-50，默认 2")
                .defineInRange("acceleration", 2.0, 0.0, 50.0);

        CRUSHING_WHEEL_MAX_SPEED = BUILDER
                .comment("轮子上限速度（格/秒）", "范围: 1-100，默认 99")
                .defineInRange("max_speed", 99.0, 1.0, 100.0);

        BUILDER.pop();

        // ====== Stress Focus 配置 ======
        BUILDER.push("stress_focus");

        STRESS_FOCUS_COST = BUILDER
                .comment("应力聚晶的灵魂消耗", "范围: 0-1000，默认 0")
                .defineInRange("soul_cost", 0, 0, 1000);

        STRESS_FOCUS_COOLDOWN = BUILDER
                .comment("施法冷却时间（tick）", "范围: 5-200，默认 20")
                .defineInRange("cooldown", 20, 5, 200);

        STRESS_DAMAGE_DIVISOR = BUILDER
                .comment("应力伤害除数", "绑定的应力读取器的网络容量 / 此值 = 基础伤害", "范围: 100-10000，默认 2000")
                .defineInRange("damage_divisor", 2000, 100, 10000);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /**
     * 手动加载配置文件，确保在物品注册前配置已就绪
     * 仿照 Goety SpellConfig 的做法，避免开发环境中 "Cannot get config value before config is loaded" 崩溃
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