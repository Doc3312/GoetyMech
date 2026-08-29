package com.goetymech.addon.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class GoetyMechPartialModels {

    public static final PartialModel WITCH_CONDENSER_SHAFT_STUB =
            PartialModel.of(new ResourceLocation("goetymech", "block/witch_condenser/shaft_stub"));

    public static void init() {
        // 触发静态字段初始化，使 stub 进入 PartialModel.ALL，
        // 赶在 flywheel PartialModelEventHandler 于 ModelEvent.RegisterAdditional/BakingCompleted 扫描前注册，
        // 避免 bakedModel 为 null（Visual 渲染出 16³ 紫黑缺失块）。
        PartialModel stub = WITCH_CONDENSER_SHAFT_STUB;
    }
}
