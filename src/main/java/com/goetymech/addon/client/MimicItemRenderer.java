package com.goetymech.addon.client;

import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MimicItemRenderer extends BlockEntityWithoutLevelRenderer {

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

        ItemTransform sourceTransform = sourceModel.getTransforms().getTransform(transformType);
        boolean leftHand = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        new ItemTransform(sourceTransform.rotation, new Vector3f(), sourceTransform.scale)
                .apply(leftHand, poseStack);

        for (BakedModel pass : sourceModel.getRenderPasses(sourceStack, true)) {
            for (RenderType rt : pass.getRenderTypes(sourceStack, true)) {
                VertexConsumer vc = ItemRenderer.getFoilBuffer(buffer, rt, true, sourceStack.hasFoil());
                itemRenderer.renderModelLists(pass, sourceStack, combinedLight, combinedOverlay, poseStack, vc);
            }
        }
    }
}
