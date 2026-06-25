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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class MimicItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation OVERLAY_TEXTURE =
            new ResourceLocation("goetypunk", "textures/item/prototype_mimic_overlay.png");

    private static MimicItemRenderer INSTANCE;

    public static MimicItemRenderer getInstance() {
        if (INSTANCE == null) {
            Minecraft mc = Minecraft.getInstance();
            INSTANCE = new MimicItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getItemRenderer());
        }
        return INSTANCE;
    }

    private MimicItemRenderer() {
        super(null, null);
    }

    private MimicItemRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher dispatcher,
                              net.minecraft.client.renderer.entity.ItemRenderer itemRenderer) {
        super(dispatcher, itemRenderer);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int combinedLight, int combinedOverlay) {
        ItemStack sourceStack = PrototypeMimicItem.getSourceStack(stack);
        if (sourceStack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer().renderStatic(sourceStack, transformType, combinedLight, combinedOverlay, poseStack, buffer);

        if (transformType == ItemDisplayContext.GUI) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 0.001f);

            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(OVERLAY_TEXTURE));
            var matrix = poseStack.last().pose();
            consumer.vertex(matrix, 0, 0, 0).uv(0, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 0, 16, 0).uv(0, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 16, 16, 0).uv(1, 0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();
            consumer.vertex(matrix, 16, 0, 0).uv(1, 1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0, 0, 1).endVertex();

            poseStack.popPose();
        }
    }
}
