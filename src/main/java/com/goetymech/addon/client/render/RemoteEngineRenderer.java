package com.goetymech.addon.client.render;

import com.goetymech.addon.common.block.entity.RemoteEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteEngineRenderer extends KineticBlockEntityRenderer<RemoteEngineBlockEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger("RemoteEngineRenderer");

    public RemoteEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(RemoteEngineBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        LOGGER.debug("[REMOTE_ENGINE] renderSafe called at pos={}", be.getBlockPos());

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(
                net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);

        SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, facing);
        standardKineticRotationTransform(shaft, be, light);
        shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}
