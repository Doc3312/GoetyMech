package com.goetymech.addon.mixin;

import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, A extends HumanoidModel<T>> extends RenderLayer<T, A> {

    private HumanoidArmorLayerMixin(RenderLayerParent<T, A> renderer) {
        super(renderer);
    }

    @Inject(
        method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gp$renderMimicArmor(PoseStack poseStack, MultiBufferSource buffer, T entity, EquipmentSlot slot, int light, A model, CallbackInfo ci) {
        if (slot != EquipmentSlot.CHEST) return;
        ItemStack stack = entity.getItemBySlot(slot);
        if (!(stack.getItem() instanceof PrototypeMimicItem)) return;
        ItemStack source = PrototypeMimicItem.getSourceStack(stack);
        if (source.isEmpty()) return;

        this.getParentModel().copyPropertiesTo(model);

        model.head.visible = false;
        model.hat.visible = false;
        model.body.visible = true;
        model.rightArm.visible = true;
        model.leftArm.visible = true;
        model.rightLeg.visible = false;
        model.leftLeg.visible = false;

        Model armorModel = IClientItemExtensions.of(source).getGenericArmorModel(entity, source, slot, model);

        ResourceLocation tex = resolveTexture(source, entity, slot);
        if (tex == null) {
            tex = new ResourceLocation("minecraft", "textures/models/armor/iron_layer_1.png");
        }

        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(
                buffer, RenderType.armorCutoutNoCull(tex), false, stack.hasFoil());
        armorModel.renderToBuffer(poseStack, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        if (stack.hasFoil()) {
            VertexConsumer glint = ItemRenderer.getArmorFoilBuffer(
                    buffer, RenderType.armorCutoutNoCull(tex), true, true);
            armorModel.renderToBuffer(poseStack, glint, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        ci.cancel();
    }

    private static ResourceLocation resolveTexture(ItemStack source, LivingEntity entity, EquipmentSlot slot) {
        String custom = source.getItem().getArmorTexture(source, entity, slot, null);
        if (custom != null) {
            return new ResourceLocation(custom);
        }
        if (source.getItem() instanceof ArmorItem armorItem) {
            String matName = armorItem.getMaterial().getName();
            String domain = "minecraft";
            String texture = matName;
            int idx = matName.indexOf(':');
            if (idx != -1) {
                domain = matName.substring(0, idx);
                texture = matName.substring(idx + 1);
            }
            return new ResourceLocation(domain, "textures/models/armor/" + texture + "_layer_1.png");
        }
        return null;
    }
}
