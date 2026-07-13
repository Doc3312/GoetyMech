package com.goetymech.addon.client.render;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.capabilities.StressShieldProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class StressShieldLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(GoetyMechMod.MOD_ID, "stress_shield"), "main");

    private static final ResourceLocation ENERGY_TEXTURE = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final PlayerModel<T> model;

    public StressShieldLayer(RenderLayerParent<T, M> renderer, EntityModelSet models) {
        super(renderer);
        this.model = new PlayerModel<>(models.bakeLayer(LAYER), false);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.5F), false), 64, 64);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T living, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        var cap = living.getCapability(StressShieldProvider.CAPABILITY).orElse(null);
        if (cap == null || !cap.isActive()) return;

        long gameTime = living.level().getGameTime();
        if (cap.getExpireTick() > 0 && gameTime > cap.getExpireTick()) return;

        float age = living.tickCount + partialTicks;
        float shieldRatio = cap.getMaxShield() > 0 ? Math.min(cap.getShield() / cap.getMaxShield(), 1.0f) : 0;

        this.model.prepareMobModel(living, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float alpha = 0.5f + shieldRatio * 0.5f;
        float uOffset = Math.abs(Mth.cos(age * 0.02F) * 3.0F) % 1.0F;
        float vOffset = age * 0.01F % 1.0F;
        VertexConsumer vc = buffer.getBuffer(
                RenderType.energySwirl(ENERGY_TEXTURE, uOffset, vOffset));
        this.model.renderToBuffer(poseStack, vc, packedLight,
                OverlayTexture.NO_OVERLAY, 0.3F, 0.7F, 1.0F, alpha);
    }
}
