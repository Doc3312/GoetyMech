package com.goetymech.addon.init;

import com.goetymech.addon.goetymechMod;
import com.goetymech.addon.common.entity.CrazedIllagerEntity;
import com.goetymech.addon.common.entity.CrazedIllagerServant;
import com.goetymech.addon.common.entity.CrushingWheelEntity;
import com.goetymech.addon.common.entity.SculkSpikeEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 瀹炰綋绫诲瀷娉ㄥ唽
 */
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, goetymechMod.MOD_ID);

    /** 骞藉尶灏栧埡 鈥?瀹氳韩閿氱偣瀹炰綋锛屽昂瀵镐笌 Goety TangleEntity 涓€鑷?*/
    public static final RegistryObject<EntityType<SculkSpikeEntity>> SCULK_SPIKE =
            ENTITIES.register("sculk_spike",
                    () -> EntityType.Builder.<SculkSpikeEntity>of(SculkSpikeEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("sculk_spike"));

    /** 绮夌杞?鈥?搴斿姏鑱氭櫠鏂芥硶鍙敜鐨勯琛屽疄浣?*/
    public static final RegistryObject<EntityType<CrushingWheelEntity>> CRUSHING_WHEEL =
            ENTITIES.register("crushing_wheel",
                    () -> EntityType.Builder.<CrushingWheelEntity>of(CrushingWheelEntity::new, MobCategory.MISC)
                            .sized(2.0F, 2.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("crushing_wheel"));

    /** 鐤媯鐨勬敼閫犲垖姘?鈥?鏁屽鐗堟湰 */
    public static final RegistryObject<EntityType<CrazedIllagerEntity>> CRAZED_ILLAGER =
            ENTITIES.register("crazed_illager",
                    () -> EntityType.Builder.<CrazedIllagerEntity>of(CrazedIllagerEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(10)
                            .build("crazed_illager"));

    /** 鐤媯鐨勬敼閫犲垖姘?鈥?浠嗕粠鐗堟湰 */
    public static final RegistryObject<EntityType<CrazedIllagerServant>> CRAZED_ILLAGER_SERVANT =
            ENTITIES.register("crazed_illager_servant",
                    () -> EntityType.Builder.<CrazedIllagerServant>of(CrazedIllagerServant::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(10)
                            .build("crazed_illager_servant"));
}