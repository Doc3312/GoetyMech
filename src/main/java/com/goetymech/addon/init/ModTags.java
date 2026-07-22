package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * GoetyMech 自定义标签
 */
public class ModTags {

    /** 需要不祥火炬完整训练结构才能转化的兵种标签 */
    public static final TagKey<EntityType<?>> REQUIRES_TRAINING_STRUCTURE =
            TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation(GoetyMechMod.MOD_ID, "requires_training_structure"));

}