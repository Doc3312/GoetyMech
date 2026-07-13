package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.capabilities.StressShieldProvider;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StressShieldWorldRenderer {

    private static final ResourceLocation SHARD_TEXTURE = new ResourceLocation("textures/entity/wither/wither_armor.png");

    private static final RenderType SHARD_GLOW = RenderType.create(
            "stress_shard_glow",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEyesShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(SHARD_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private static final float HALF_SIZE = 0.3f;

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
        float partialTick = event.getPartialTick();
        float age = player.tickCount + partialTick;
        float radius = 2.2f;

        double px = Mth.lerp(partialTick, player.xo, player.getX());
        double py = Mth.lerp(partialTick, player.yo, player.getY());
        double pz = Mth.lerp(partialTick, player.zo, player.getZ());
        double baseY = py + player.getBbHeight() * 0.5;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(SHARD_GLOW);

        double cx = event.getCamera().getPosition().x;
        double cy = event.getCamera().getPosition().y;
        double cz = event.getCamera().getPosition().z;

        for (int i = 0; i < count; i++) {
            poseStack.pushPose();

            float angle = (i / (float) count) * 360f + age * 0.6f;
            float yBob = (float) Math.sin(age * 0.12f + i * 0.7f) * 0.3f;
            double sx = px + Math.cos(Math.toRadians(angle)) * radius;
            double sz = pz + Math.sin(Math.toRadians(angle)) * radius;

            poseStack.translate(sx - cx, baseY + yBob - cy, sz - cz);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(age * 0.6f + i * 37f));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(age * 0.3f + i * 53f));

            float alpha = Math.min(1.0f, 0.4f + shieldRatio * 0.4f);
            float uOff = (i * 0.13f + age * 0.01f) % 1.0f;
            float vOff = (i * 0.07f + age * 0.005f) % 1.0f;

            renderCrossedQuads(poseStack, vc, alpha, uOff, vOff);

            poseStack.popPose();
        }
    }

    private static void renderCrossedQuads(PoseStack poseStack, VertexConsumer vc,
                                            float alpha, float uOff, float vOff) {
        Matrix4f mat = poseStack.last().pose();
        Matrix3f norm = poseStack.last().normal();
        float s = HALF_SIZE;

        vertex(mat, norm, vc, -s, -s, 0,   uOff, vOff,         alpha);
        vertex(mat, norm, vc, -s,  s, 0,   uOff, vOff + 0.25f, alpha);
        vertex(mat, norm, vc,  s,  s, 0,   uOff + 0.25f, vOff + 0.25f, alpha);
        vertex(mat, norm, vc,  s, -s, 0,   uOff + 0.25f, vOff, alpha);

        vertex(mat, norm, vc, 0, -s, -s,   uOff + 0.5f, vOff,         alpha);
        vertex(mat, norm, vc, 0,  s, -s,   uOff + 0.5f, vOff + 0.25f, alpha);
        vertex(mat, norm, vc, 0,  s,  s,   uOff + 0.75f, vOff + 0.25f, alpha);
        vertex(mat, norm, vc, 0, -s,  s,   uOff + 0.75f, vOff, alpha);
    }

    private static void vertex(Matrix4f mat, Matrix3f norm, VertexConsumer vc,
                                float x, float y, float z, float u, float v, float alpha) {
        vc.vertex(mat, x, y, z)
                .color(76, 153, 255, (int)(alpha * 255))
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(norm, 0f, 1f, 0f)
                .endVertex();
    }
}
