package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.capabilities.SoulDominionProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT)
public class SoulDominionChainRenderer {

    private static final ResourceLocation CHAIN_TEXTURE =
            new ResourceLocation(GoetyMechMod.MOD_ID, "textures/entity/soul_dominion/chain.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(CHAIN_TEXTURE, false);

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITY) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        player.getCapability(SoulDominionProvider.CAPABILITY).ifPresent(dom -> {
            if (!dom.isActive()) return;
            if (dom.getLinkedServants().isEmpty()) return;

            Camera camera = event.getCamera();
            Vec3 camPos = camera.getPosition();

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RENDER_TYPE);

            float partialTick = event.getPartialTick();
            long gameTime = player.level.getGameTime();

            for (UUID uuid : dom.getLinkedServants()) {
                if (player.level.getEntity(uuid) instanceof LivingEntity servant && servant.isAlive()) {
                    drawChain(poseStack, vertexConsumer, camPos, player, servant, partialTick, gameTime);
                }
            }

            bufferSource.endBatch(RENDER_TYPE);
        });
    }

    private static void drawChain(PoseStack poseStack, VertexConsumer vertexConsumer,
                                  Vec3 camPos, LivingEntity startEntity, LivingEntity endEntity,
                                  float partialTick, long gameTime) {
        Vec3 start = new Vec3(
                Mth.lerp(partialTick, startEntity.xo, startEntity.getX()),
                Mth.lerp(partialTick, startEntity.yo, startEntity.getY()) + startEntity.getBbHeight() / 2,
                Mth.lerp(partialTick, startEntity.zo, startEntity.getZ())
        );
        Vec3 end = new Vec3(
                Mth.lerp(partialTick, endEntity.xo, endEntity.getX()),
                Mth.lerp(partialTick, endEntity.yo, endEntity.getY()) + endEntity.getBbHeight() / 2,
                Mth.lerp(partialTick, endEntity.zo, endEntity.getZ())
        );

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        Vec3 offset = end.subtract(start);
        Vec3 sight = camPos.subtract(start).scale(-1);
        Vec3 sideOffset = offset.cross(sight).normalize().scale(0.25);

        float age = gameTime + partialTick;
        float uOffset = -age * 0.06f;

        PoseStack.Pose pose = poseStack.last();
        vertex(vertexConsumer, pose, start.add(sideOffset), uOffset, 0);
        vertex(vertexConsumer, pose, start.add(sideOffset.scale(-1)), uOffset, 1);
        vertex(vertexConsumer, pose, end.add(sideOffset.scale(-1)), (float) (offset.length() * 2) + uOffset, 1);
        vertex(vertexConsumer, pose, end.add(sideOffset), (float) (offset.length() * 2) + uOffset, 0);

        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 vec3, float u, float v) {
        consumer.vertex(pose.pose(), (float) vec3.x(), (float) vec3.y(), (float) vec3.z())
                .color(-1)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }
}
