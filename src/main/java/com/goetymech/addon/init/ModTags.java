package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * GoetyMech 自定义标签
 */
public class ModTags {

    /** 需要不祥火炬完整训练结构才能转化的兵种标签 */
    public static final TagKey<EntityType<?>> REQUIRES_TRAINING_STRUCTURE =
            TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation(GoetyMechMod.MOD_ID, "requires_training_structure"));

    /** 结构之眼物品标签 — 副手持此标签物品可触发 namespace 全结构扫描 */
    public static final TagKey<Item> STRUCTURE_EYES =
            ItemTags.create(new ResourceLocation(GoetyMechMod.MOD_ID, "structure_eyes"));
}