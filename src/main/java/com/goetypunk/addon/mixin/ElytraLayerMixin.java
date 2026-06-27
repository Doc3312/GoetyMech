package com.goetypunk.addon.mixin;

import com.goetypunk.addon.common.item.PrototypeMimicItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private ElytraLayerMixin(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void gp$cancelElytraIfMimicFused(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                             T livingEntity, float limbSwing, float limbSwingAmount,
                                             float partialTicks, float ageInTicks, float netHeadYaw,
                                             float headPitch, CallbackInfo ci) {
        ItemStack chest = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof PrototypeMimicItem mimic
                && !PrototypeMimicItem.getSourceStack(chest).isEmpty()) {
            ci.cancel();
        }
    }
}
