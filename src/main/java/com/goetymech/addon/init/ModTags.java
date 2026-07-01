package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * goetymech 鑷畾涔夋爣绛?
 * 鐢ㄤ簬鏍囪闇€瑕佸畬鏁磋缁冪粨鏋勭殑鍏电绫诲瀷锛堢啍鐐変笉鍙洿鎺ヨ浆鍖栵級
 */
public class ModTags {

    /** 闇€瑕佷笉绁ョ伀鐐畬鏁磋缁冪粨鏋勬墠鑳借浆鍖栫殑鍏电鏍囩 */
    public static final TagKey<EntityType<?>> REQUIRES_TRAINING_STRUCTURE =
            TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation(GoetyMechMod.MOD_ID, "requires_training_structure"));
}