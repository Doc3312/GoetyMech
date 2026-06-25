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

    private static final RenderType OVERLAY_RENDER_TYPE =
            RenderType.entityCutoutNoCull(OVERLAY_TEXTURE);

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
        sourceModel.getTransforms().getTransform(transformType).apply(false, poseStack);

        for (BakedModel pass : sourceModel.getRenderPasses(sourceStack, true)) {
            for (RenderType rt : pass.getRenderTypes(sourceStack, true)) {
                VertexConsumer vc = ItemRenderer.getFoilBuffer(buffer, rt, true, sourceStack.hasFoil());
                itemRenderer.renderModelLists(pass, sourceStack, combinedLight, combinedOverlay, poseStack, vc);
            }
        }

        if (transformType == ItemDisplayContext.GUI) {
            // 先刷新胸甲缓冲（保证 GPU 层面先画胸甲）
            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                for (BakedModel pass : sourceModel.getRenderPasses(sourceStack, true)) {
                    for (RenderType rt : pass.getRenderTypes(sourceStack, true)) {
                        bs.endBatch(rt);
                    }
                }
            }

            // 再画覆盖层（始终在胸甲之上）
            VertexConsumer consumer = buffer.getBuffer(OVERLAY_RENDER_TYPE);
            var matrix = poseStack.last().pose();
            consumer.vertex(matrix, 0, 1, 0.01F).color(1f, 1f, 1f, 1f).uv(0, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 0, 0, 0.01F).color(1f, 1f, 1f, 1f).uv(0, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 1, 0, 0.01F).color(1f, 1f, 1f, 1f).uv(1, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 1, 1, 0.01F).color(1f, 1f, 1f, 1f).uv(1, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();

            // 刷新覆盖层缓冲
            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch(OVERLAY_RENDER_TYPE);
            }
        }

        poseStack.popPose();
    }
}
