package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
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
 * 实体类型注册
 */
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GoetyMechMod.MOD_ID);

    /** 幽匿尖刺 — 定身锚点实体，尺寸与 Goety TangleEntity 一致 */
    public static final RegistryObject<EntityType<SculkSpikeEntity>> SCULK_SPIKE =
            ENTITIES.register("sculk_spike",
                    () -> EntityType.Builder.<SculkSpikeEntity>of(SculkSpikeEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("sculk_spike"));

    /** 粉碎轮 — 应力聚晶施法召唤的飞行实体 */
    public static final RegistryObject<EntityType<CrushingWheelEntity>> CRUSHING_WHEEL =
            ENTITIES.register("crushing_wheel",
                    () -> EntityType.Builder.<CrushingWheelEntity>of(CrushingWheelEntity::new, MobCategory.MISC)
                            .sized(2.0F, 2.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("crushing_wheel"));

    /** 疯狂的改造刌民 — 敌对版本 */
    public static final RegistryObject<EntityType<CrazedIllagerEntity>> CRAZED_ILLAGER =
            ENTITIES.register("crazed_illager",
                    () -> EntityType.Builder.<CrazedIllagerEntity>of(CrazedIllagerEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(10)
                            .build("crazed_illager"));

    /** 疯狂的改造刌民 — 仆从版本 */
    public static final RegistryObject<EntityType<CrazedIllagerServant>> CRAZED_ILLAGER_SERVANT =
            ENTITIES.register("crazed_illager_servant",
                    () -> EntityType.Builder.<CrazedIllagerServant>of(CrazedIllagerServant::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(10)
                            .build("crazed_illager_servant"));

}