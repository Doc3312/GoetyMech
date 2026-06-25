package com.goetypunk.addon.client;

import com.goetypunk.addon.common.item.PrototypeMimicItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MimicItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation OVERLAY_TEXTURE =
            new ResourceLocation("goetypunk", "textures/item/prototype_mimic_overlay.png");

    private static MimicItemRenderer INSTANCE;

    public static MimicItemRenderer getInstance() {
        if (INSTANCE == null) {
            Minecraft mc = Minecraft.getInstance();
            INSTANCE = new MimicItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        }
        return INSTANCE;
    }

    private MimicItemRenderer(BlockEntityRenderDispatcher dispatcher, net.minecraft.client.model.geom.EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int combinedLight, int combinedOverlay) {
        ItemStack sourceStack = PrototypeMimicItem.getSourceStack(stack);
        if (sourceStack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BakedModel sourceModel = itemRenderer.getModel(sourceStack, null, null, 0);

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        itemRenderer.render(sourceStack, transformType, false, poseStack, buffer, combinedLight, combinedOverlay, sourceModel);

        if (transformType == ItemDisplayContext.GUI) {
            sourceModel.getTransforms().getTransform(transformType).apply(false, poseStack);
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(OVERLAY_TEXTURE));
            var matrix = poseStack.last().pose();
            consumer.vertex(matrix, 0, 0, 7.5F).color(1f, 1f, 1f, 1f).uv(0, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 0, 16, 7.5F).color(1f, 1f, 1f, 1f).uv(0, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 16, 16, 7.5F).color(1f, 1f, 1f, 1f).uv(1, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 16, 0, 7.5F).color(1f, 1f, 1f, 1f).uv(1, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
        }

        poseStack.popPose();
    }
}
