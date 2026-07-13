package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.capabilities.StressShieldProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StressShieldWorldRenderer {

    private static final ModelResourceLocation SHARD_MODEL = new ModelResourceLocation(
            new ResourceLocation("minecraft:blue_stained_glass"), "");
    private static final Direction[] DIRECTIONS = Direction.values();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        var cap = player.getCapability(StressShieldProvider.CAPABILITY).orElse(null);
        if (cap == null || !cap.isActive()) return;

        long gameTime = player.level().getGameTime();
        if (cap.getExpireTick() > 0 && gameTime > cap.getExpireTick()) return;

        float shieldRatio = cap.getMaxShield() > 0 ? Math.min(cap.getShield() / cap.getMaxShield(), 1.0f) : 0;
        if (shieldRatio <= 0) return;

        int count = 1 + (int) (shieldRatio * 11);
        float age = player.tickCount + event.getPartialTick();
        float radius = 2.2f;
        double baseY = player.getY() + player.getBbHeight() * 0.5;

        BakedModel model;
        try {
            model = mc.getModelManager().getModel(SHARD_MODEL);
        } catch (Exception e) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        for (int i = 0; i < count; i++) {
            poseStack.pushPose();

            float angle = (i / (float) count) * 360f + age * 0.6f;
            float yBob = (float) Math.sin(age * 0.12f + i * 0.7f) * 0.3f;
            double x = player.getX() + Math.cos(Math.toRadians(angle)) * radius;
            double z = player.getZ() + Math.sin(Math.toRadians(angle)) * radius;

            poseStack.translate(x - event.getCamera().getPosition().x,
                    baseY + yBob - event.getCamera().getPosition().y,
                    z - event.getCamera().getPosition().z);
            poseStack.mulPose(Axis.YP.rotationDegrees(age * 0.6f + i * 30f));
            poseStack.scale(0.3f, 0.3f, 0.3f);

            var vc = bufferSource.getBuffer(RenderType.translucent());
            for (Direction dir : DIRECTIONS) {
                mc.getItemRenderer().renderQuadList(
                        poseStack, vc,
                        model.getQuads(null, dir, player.getRandom(), ModelData.EMPTY, RenderType.translucent()),
                        ItemStack.EMPTY, 0xF000F0, OverlayTexture.NO_OVERLAY);
            }

            poseStack.popPose();
        }
    }
}
