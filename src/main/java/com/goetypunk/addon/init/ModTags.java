package com.goetypunk.addon.init;

import com.goetypunk.addon.GoetyPunkMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * GoetyPunk 自定义标签
 * 用于标记需要完整训练结构的兵种类型（熔炉不可直接转化）
 */
public class ModTags {

    /** 需要不祥火炬完整训练结构才能转化的兵种标签 */
    public static final TagKey<EntityType<?>> REQUIRES_TRAINING_STRUCTURE =
            TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation(GoetyPunkMod.MOD_ID, "requires_training_structure"));
}