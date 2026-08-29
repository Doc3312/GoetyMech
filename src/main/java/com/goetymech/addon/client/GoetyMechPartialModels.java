package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class GoetyMechPartialModels {

    public static final PartialModel WITCH_CONDENSER_SHAFT_STUB =
            PartialModel.of(new ResourceLocation(GoetyMechMod.MOD_ID, "block/witch_condenser/shaft_stub"));

    public static void init() {
        // 触发静态字段初始化（PartialModel 在静态初始化时即注册，无需 RegisterAdditional）
    }
}
