package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.MatureEmbryoItem;
import com.goetymech.addon.common.item.RemoteEngineBlockItem;
import com.goetymech.addon.common.item.SculkBindFocus;
import com.goetymech.addon.common.item.SquadFocus;
import com.goetymech.addon.common.item.StableRiftFocus;
import com.goetymech.addon.common.item.StressFocus;
import com.goetymech.addon.common.item.StressShieldFocus;
import com.goetymech.addon.common.item.TimeFocusPouch;
import com.goetymech.addon.common.item.ChaseFocus;
import com.goetymech.addon.common.item.CrazedSummonFocus;
import com.goetymech.addon.common.item.TwistedEmbryoItem;
import com.goetymech.addon.common.item.UnstableRiftFocus;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.goetymech.addon.common.item.curio.AutoCastDummyWand;
import com.goetymech.addon.common.item.curio.FleshCasketItem;
import com.goetymech.addon.common.item.curio.SoulCatcherCurio;
import com.Polarice3.Goety.api.magic.SpellType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品注册
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GoetyMechMod.MOD_ID);

    public static final RegistryObject<Item> TWISTED_EMBRYO =
            ITEMS.register("twisted_embryo", () ->
                    new TwistedEmbryoItem(new Item.Properties().stacksTo(64))
            );

    public static final RegistryObject<Item> MATURE_EMBRYO =
            ITEMS.register("mature_embryo", () ->
                    new MatureEmbryoItem(new Item.Properties().stacksTo(16))
            );

    public static final RegistryObject<Item> FLESH_FURNACE =
            ITEMS.register("flesh_furnace", () ->
                    new BlockItem(ModBlocks.FLESH_FURNACE.get(), new Item.Properties())
            );

    public static final RegistryObject<Item> BREEDING_NEST =
            ITEMS.register("breeding_nest", () ->
                    new BlockItem(ModBlocks.BREEDING_NEST.get(), new Item.Properties())
            );

    /** 幽匿束缚聚晶 — 锁定+召唤复合法术 */
    public static final RegistryObject<Item> SCULK_BIND_FOCUS =
            ITEMS.register("sculk_bind_focus", SculkBindFocus::new);

    // ── 点将台三阶 BlockItems ──

    public static final RegistryObject<Item> TIER1_SQUAD_BARRACKS =
            ITEMS.register("tier1_squad_barracks", () ->
                    new BlockItem(ModBlocks.TIER1_SQUAD_BARRACKS.get(),
                            new Item.Properties().rarity(Rarity.RARE))
            );

    public static final RegistryObject<Item> TIER2_SQUAD_BARRACKS =
            ITEMS.register("tier2_squad_barracks", () ->
                    new BlockItem(ModBlocks.TIER2_SQUAD_BARRACKS.get(),
                            new Item.Properties().rarity(Rarity.RARE))
            );

    public static final RegistryObject<Item> TIER3_SQUAD_BARRACKS =
            ITEMS.register("tier3_squad_barracks", () ->
                    new BlockItem(ModBlocks.TIER3_SQUAD_BARRACKS.get(),
                            new Item.Properties().rarity(Rarity.RARE))
            );

    /** 兵团聚晶 — 远程一键编队召唤 */
    public static final RegistryObject<Item> SQUAD_FOCUS =
            ITEMS.register("squad_focus", SquadFocus::new);

    /** 裂纬聚晶 — 不稳定的维度裂隙（10s自动回传） */
    public static final RegistryObject<Item> UNSTABLE_RIFT_FOCUS =
            ITEMS.register("unstable_rift_focus", UnstableRiftFocus::new);

    /** 界旅聚晶 — 稳定的维度旅行（安全着陆） */
    public static final RegistryObject<Item> STABLE_RIFT_FOCUS =
            ITEMS.register("stable_rift_focus", StableRiftFocus::new);

    /** 应力聚晶 — 绑定应力读取器，施放碾压飞轮法术 */
    public static final RegistryObject<Item> STRESS_FOCUS =
            ITEMS.register("stress_focus", StressFocus::new);

    /** 应力护盾聚晶 — 绑定应力读取器或方舟，施放护盾 */
    public static final RegistryObject<Item> STRESS_SHIELD_FOCUS =
            ITEMS.register("stress_shield_focus", StressShieldFocus::new);

    /** 追猎聚晶 — 锁定目标后多次闪现传送 */
    public static final RegistryObject<Item> CHASE_FOCUS =
            ITEMS.register("chase_focus", ChaseFocus::new);

    /** 疯狂的改造刌民召唤聚晶 — 召唤疯狂的改造刌民仆从 */
    public static final RegistryObject<Item> CRAZED_SUMMON_FOCUS =
            ITEMS.register("crazed_summon_focus", CrazedSummonFocus::new);

    /** 疯狂的改造刌民刷怪蛋 */
    public static final RegistryObject<Item> CRAZED_ILLAGER_SPAWN_EGG =
            ITEMS.register("crazed_illager_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.CRAZED_ILLAGER,
                            0x3D3D3D, 0xCC3333, new Item.Properties()));

    // ── 时空聚晶袋 — 继承 FocusPack（21 格），打开时冻结时间 ──

    public static final RegistryObject<Item> TIME_FOCUS_POUCH =
            ITEMS.register("time_focus_pouch", TimeFocusPouch::new);

    // ── 应力读取器 BlockItem ──

    public static final RegistryObject<Item> STRESS_READER =
            ITEMS.register("stress_reader", () ->
                    new BlockItem(ModBlocks.STRESS_READER.get(),
                            new Item.Properties().rarity(Rarity.UNCOMMON))
            );

    // ── 远程引擎 BlockItem ──

    public static final RegistryObject<Item> REMOTE_ENGINE =
            ITEMS.register("remote_engine", () ->
                    new RemoteEngineBlockItem(ModBlocks.REMOTE_ENGINE.get(),
                            new Item.Properties().rarity(Rarity.UNCOMMON))
            );

    // ── 应力方舟 BlockItem ──

    public static final RegistryObject<Item> STRESS_ARK =
            ITEMS.register("stress_ark", () ->
                    new BlockItem(ModBlocks.STRESS_ARK.get(),
                            new Item.Properties().rarity(Rarity.EPIC))
            );

    public static final RegistryObject<Item> SOUL_CATCHER =
            ITEMS.register("soul_catcher", SoulCatcherCurio::new);

    // ── 原型 — 鞘翅类物品，无限耐久，推进 + Slam 技能 ──

    public static final RegistryObject<Item> PROTOTYPE =
            ITEMS.register("prototype", PrototypeItem::new);

    // ── 拟态 — 原型 + 胸甲合成，继承属性 ──

    public static final RegistryObject<Item> PROTOTYPE_MIMIC =
            ITEMS.register("prototype_mimic", PrototypeMimicItem::new);

    // ── 血肉晶匣（按等级合并，触发类型为 NBT 属性）──

    public static final RegistryObject<Item> FLESH_CASKET_1 =
            ITEMS.register("flesh_casket_1", () -> new FleshCasketItem(1));
    public static final RegistryObject<Item> FLESH_CASKET_2 =
            ITEMS.register("flesh_casket_2", () -> new FleshCasketItem(2));
    public static final RegistryObject<Item> FLESH_CASKET_3 =
            ITEMS.register("flesh_casket_3", () -> new FleshCasketItem(3));

    public static final RegistryObject<Item> AUTO_CAST_DUMMY_WAND =
            ITEMS.register("auto_cast_dummy_wand", AutoCastDummyWand::new);

    public static final RegistryObject<Item> AUTO_CAST_DUMMY_WAND_T1 =
            ITEMS.register("auto_cast_dummy_wand_t1",
                    () -> new AutoCastDummyWand(SpellType.NONE));
}
