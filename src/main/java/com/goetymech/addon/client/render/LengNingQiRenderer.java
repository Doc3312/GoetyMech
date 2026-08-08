package com.goetymech.addon.client.render;

import com.goetymech.addon.common.block.entity.LengNingQiBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 冷凝器 BESR：左右喇叭中心渲染 Create 风扇叶片（绕 X 轴旋转），前后面板中心渲染半轴（绕 Z 轴旋转）。
 * 无 Flywheel Visual，因此不加 supportsVisualization guard（参照 RemoteEngineRenderer 既有结论）。
 */
public class LengNingQiRenderer extends KineticBlockEntityRenderer<LengNingQiBlockEntity> {

    private static final float FAN_SCALE = 0.4f;
    private static final float FAN_X_OFFSET = 6.5f / 16f;
    private static final float SHAFT_Z_OFFSET = 6.5f / 16f;
    private static final float SHAFT_Y_OFFSET = 3f / 16f;

    public LengNingQiRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(LengNingQiBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = be.getBlockState();
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float speed = be.getSpeed();
        float angle = speed == 0 ? 0 : ((time * speed * 3f / 10) % 360) / 180f * (float) Math.PI;

        VertexConsumer fanVB = buffer.getBuffer(RenderType.cutout());
        VertexConsumer shaftVB = buffer.getBuffer(RenderType.solid());

        // 左右喇叭风扇：块中心 (8,8,8) → 喇叭中心 (1.5,8,8) / (14.5,8,8)
        renderFan(be, blockState, ms, fanVB, angle, light, -FAN_X_OFFSET, Direction.WEST);
        renderFan(be, blockState, ms, fanVB, angle, light, FAN_X_OFFSET, Direction.EAST);

        // 前后面板转轴：中心在面板中心 (8,5,1.5)/(8,5,14.5)，半段朝块内，端面贴面板表面
        renderShaft(be, blockState, ms, shaftVB, angle, light, -SHAFT_Z_OFFSET, Direction.SOUTH);
        renderShaft(be, blockState, ms, shaftVB, angle, light, SHAFT_Z_OFFSET, Direction.NORTH);
    }

    private void renderFan(LengNingQiBlockEntity be, BlockState state, PoseStack ms, VertexConsumer vb,
                           float angle, int light, float xOffset, Direction facing) {
        ms.pushPose();
        ms.translate(xOffset, 0, 0);
        ms.scale(FAN_SCALE, FAN_SCALE, FAN_SCALE);
        SuperByteBuffer fan = CachedBuffers.partialFacing(AllPartialModels.ENCASED_FAN_INNER, state, facing);
        fan.rotateCentered(angle, Direction.Axis.X);
        fan.light(light);
        fan.renderInto(ms, vb);
        ms.popPose();
    }

    private void renderShaft(LengNingQiBlockEntity be, BlockState state, PoseStack ms, VertexConsumer vb,
                             float angle, int light, float zOffset, Direction facing) {
        ms.pushPose();
        ms.translate(0, -SHAFT_Y_OFFSET, zOffset);
        SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, facing);
        shaft.rotateCentered(angle, Direction.Axis.Z);
        shaft.light(light);
        shaft.renderInto(ms, vb);
        ms.popPose();
    }
}
