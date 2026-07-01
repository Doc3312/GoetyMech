package com.goetymech.addon.client.render;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.entity.SculkSpikeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SculkBidingRenderer extends EntityRenderer<SculkSpikeEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(GoetyMechMod.MOD_ID, "sculk_biding"), "main");

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            GoetyMechMod.MOD_ID, "textures/entity/sculk_binding.png");

    private final SculkBidingModel<SculkSpikeEntity> model;

    public SculkBidingRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart modelPart = context.bakeLayer(LAYER);
        this.model = new SculkBidingModel<>(modelPart);
    }

    @Override
    public void render(SculkSpikeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float rise = entity.getRise();
        poseStack.translate(0, (rise - 1.0F) * 0.3F, 0);
        poseStack.scale(2.25F, 2.25F, 2.25F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SculkSpikeEntity entity) {
        return TEXTURE;
    }
}
