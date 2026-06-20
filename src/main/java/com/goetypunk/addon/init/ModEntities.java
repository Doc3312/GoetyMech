package com.goetypunk.addon.init;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.entity.CrushingWheelEntity;
import com.goetypunk.addon.common.entity.SculkSpikeEntity;
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
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GoetyPunkMod.MOD_ID);

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
}