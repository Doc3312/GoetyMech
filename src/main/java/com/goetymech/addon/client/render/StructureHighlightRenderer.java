package com.goetymech.addon.client.render;

import com.goetymech.addon.mixin.RenderStateShardAccessor;
import com.goetymech.addon.common.entity.StructureHighlightMarker;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class StructureHighlightRenderer extends EntityRenderer<StructureHighlightMarker> {

    private static final RenderType HIGHLIGHT_LINES = createHighlightLines();

    private static RenderType createHighlightLines() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShardAccessor.gp$getRendertypeLinesShader())
                .setTransparencyState(RenderStateShardAccessor.gp$getTranslucentTransparency())
                .setDepthTestState(RenderStateShardAccessor.gp$getNoDepthTest())
                .createCompositeState(false);
        return RenderType.create(
                "goetymech_highlight",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES,
                256,
                false, false,
                state
        );
    }

    public StructureHighlightRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StructureHighlightMarker entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        AABB bounds = entity.getStructureBounds();
        if (bounds == null || bounds.getSize() <= 0) return;

        poseStack.pushPose();
        poseStack.translate(-entity.getX(), -entity.getY(), -entity.getZ());

        VertexConsumer consumer = bufferSource.getBuffer(HIGHLIGHT_LINES);
        LevelRenderer.renderLineBox(
                poseStack, consumer,
                bounds.minX, bounds.minY, bounds.minZ,
                bounds.maxX, bounds.maxY, bounds.maxZ,
                0.2F, 0.8F, 1.0F, 0.9F,
                0.1F, 0.5F, 0.8F
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(StructureHighlightMarker entity) {
        return null;
    }
}
