package com.goetymech.addon.client.render;

import com.goetymech.addon.common.block.entity.CraterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 撞击坑 BESR 渲染器。渲染方块变形和弹跳动画。
 */
public class CraterBlockRenderer implements BlockEntityRenderer<CraterBlockEntity> {

    public CraterBlockRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CraterBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getOriginalState().isAir()) return;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        // EpicFight turnback: lerp translate/rotation → zero in last 5 ticks
        float turnBackTime = 5.0F;
        float lerpAmount = Mth.clamp(partialTick * (1.0F / turnBackTime)
                + (turnBackTime - (be.getMaxLifetime() - be.getLifetime())) * (1.0F / turnBackTime), 0.0F, 1.0F);
        boolean settled = be.getMaxLifetime() <= be.getLifetime() + turnBackTime;

        Vector3f translate;
        Quaternionf rotation;
        if (settled) {
            translate = new Vector3f(
                    Mth.lerp(lerpAmount, be.getTranslate().x(), 0.0F),
                    Mth.lerp(lerpAmount, be.getTranslate().y(), 0.0F),
                    Mth.lerp(lerpAmount, be.getTranslate().z(), 0.0F));
            rotation = new Quaternionf();
            be.getRotation().slerp(new Quaternionf(), lerpAmount, rotation);
        } else {
            translate = be.getTranslate();
            rotation = be.getRotation();
        }

        // EpicFight parabola bounce
        double bounceMax = be.getBouncing();
        double TIME = Math.max(bounceMax * 8.0, 8.0);
        double EXTENDER = 1.0 / Math.pow(TIME * 0.5, 2);
        double MOVE_GRAPH = Math.sqrt(bounceMax / EXTENDER);
        double bouncingAnimation = Math.max(-EXTENDER * Math.pow(be.getLifetime() + partialTick - MOVE_GRAPH, 2) + bounceMax, 0);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(rotation);
        poseStack.translate(translate.x(), translate.y() + bouncingAnimation, translate.z());
        poseStack.translate(-0.5, -0.5, -0.5);

        dispatcher.renderSingleBlock(be.getOriginalState(), poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
        poseStack.popPose();
    }
}
