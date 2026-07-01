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
 * 鐗╁搧娉ㄥ唽
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

    /** 骞藉尶鏉熺細鑱氭櫠 鈥?閿佸畾+鍙敜澶嶅悎娉曟湳 */
    public static final RegistryObject<Item> SCULK_BIND_FOCUS =
            ITEMS.register("sculk_bind_focus", SculkBindFocus::new);

    // 鈹€鈹€ 鐐瑰皢鍙颁笁闃?BlockItems 鈹€鈹€

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

    /** 鍏靛洟鑱氭櫠 鈥?杩滅▼涓€閿紪闃熷彫鍞?*/
    public static final RegistryObject<Item> SQUAD_FOCUS =
            ITEMS.register("squad_focus", SquadFocus::new);

    /** 瑁傜含鑱氭櫠 鈥?涓嶇ǔ瀹氱殑缁村害瑁傞殭锛?0s鑷姩鍥炰紶锛?*/
    public static final RegistryObject<Item> UNSTABLE_RIFT_FOCUS =
            ITEMS.register("unstable_rift_focus", UnstableRiftFocus::new);

    /** 鐣屾梾鑱氭櫠 鈥?绋冲畾鐨勭淮搴︽梾琛岋紙瀹夊叏鐫€闄嗭級 */
    public static final RegistryObject<Item> STABLE_RIFT_FOCUS =
            ITEMS.register("stable_rift_focus", StableRiftFocus::new);

    /** 搴斿姏鑱氭櫠 鈥?缁戝畾搴斿姏璇诲彇鍣紝鏂芥斁纰惧帇椋炶疆娉曟湳 */
    public static final RegistryObject<Item> STRESS_FOCUS =
            ITEMS.register("stress_focus", StressFocus::new);

    /** 搴斿姏鎶ょ浘鑱氭櫠 鈥?缁戝畾搴斿姏璇诲彇鍣ㄦ垨鏂硅垷锛屾柦鏀炬姢鐩?*/
    public static final RegistryObject<Item> STRESS_SHIELD_FOCUS =
            ITEMS.register("stress_shield_focus", StressShieldFocus::new);

    /** 杩界寧鑱氭櫠 鈥?閿佸畾鐩爣鍚庡娆￠棯鐜颁紶閫?*/
    public static final RegistryObject<Item> CHASE_FOCUS =
            ITEMS.register("chase_focus", ChaseFocus::new);

    /** 鐤媯鐨勬敼閫犲垖姘戝彫鍞よ仛鏅?鈥?鍙敜鐤媯鐨勬敼閫犲垖姘戜粏浠?*/
    public static final RegistryObject<Item> CRAZED_SUMMON_FOCUS =
            ITEMS.register("crazed_summon_focus", CrazedSummonFocus::new);

    /** 鐤媯鐨勬敼閫犲垖姘戝埛鎬泲 */
    public static final RegistryObject<Item> CRAZED_ILLAGER_SPAWN_EGG =
            ITEMS.register("crazed_illager_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.CRAZED_ILLAGER,
                            0x3D3D3D, 0xCC3333, new Item.Properties()));

    // 鈹€鈹€ 鏃剁┖鑱氭櫠琚?鈥?缁ф壙 FocusPack锛?1 鏍硷級锛屾墦寮€鏃跺喕缁撴椂闂?鈹€鈹€

    public static final RegistryObject<Item> TIME_FOCUS_POUCH =
            ITEMS.register("time_focus_pouch", TimeFocusPouch::new);

    // 鈹€鈹€ 搴斿姏璇诲彇鍣?BlockItem 鈹€鈹€

    public static final RegistryObject<Item> STRESS_READER =
            ITEMS.register("stress_reader", () ->
                    new BlockItem(ModBlocks.STRESS_READER.get(),
                            new Item.Properties().rarity(Rarity.UNCOMMON))
            );

    // 鈹€鈹€ 杩滅▼寮曟搸 BlockItem 鈹€鈹€

    public static final RegistryObject<Item> REMOTE_ENGINE =
            ITEMS.register("remote_engine", () ->
                    new RemoteEngineBlockItem(ModBlocks.REMOTE_ENGINE.get(),
                            new Item.Properties().rarity(Rarity.UNCOMMON))
            );

    // 鈹€鈹€ 搴斿姏鏂硅垷 BlockItem 鈹€鈹€

    public static final RegistryObject<Item> STRESS_ARK =
            ITEMS.register("stress_ark", () ->
                    new BlockItem(ModBlocks.STRESS_ARK.get(),
                            new Item.Properties().rarity(Rarity.EPIC))
            );

    public static final RegistryObject<Item> SOUL_CATCHER =
            ITEMS.register("soul_catcher", SoulCatcherCurio::new);

    // 鈹€鈹€ 鍘熷瀷 鈥?闉樼繀绫荤墿鍝侊紝鏃犻檺鑰愪箙锛屾帹杩?+ Slam 鎶€鑳?鈹€鈹€

    public static final RegistryObject<Item> PROTOTYPE =
            ITEMS.register("prototype", PrototypeItem::new);

    // 鈹€鈹€ 鎷熸€?鈥?鍘熷瀷 + 鑳哥敳鍚堟垚锛岀户鎵垮睘鎬?鈹€鈹€

    public static final RegistryObject<Item> PROTOTYPE_MIMIC =
            ITEMS.register("prototype_mimic", PrototypeMimicItem::new);

    // 鈹€鈹€ 琛€鑲夋櫠鍖ｏ紙鎸夌瓑绾у悎骞讹紝瑙﹀彂绫诲瀷涓?NBT 灞炴€э級鈹€鈹€

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
