package com.goetymech.addon.client.render;

import com.goetymech.addon.common.block.entity.CraterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.client.model.data.ModelData;

public class CraterBlockRenderer implements BlockEntityRenderer<CraterBlockEntity> {

    public CraterBlockRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CraterBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getOriginalState().isAir()) return;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        float bounce = be.getBounce(partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(be.getRotation());
        poseStack.translate(0, bounce, 0);
        poseStack.translate(-0.5, 0, -0.5);

        dispatcher.renderSingleBlock(be.getOriginalState(), poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
        poseStack.popPose();
    }
}
