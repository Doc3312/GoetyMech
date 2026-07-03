package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

import java.util.*;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SlamGroundTiltHandler {

    private static final int HOLD_TICKS = 16;
    private static final int DISSOLVE_PER_RING = 3;
    private static final int TOTAL_DURATION = HOLD_TICKS + 4 * DISSOLVE_PER_RING;
    private static final int SCAN_DOWN = 5;

    private static final float[] RING_ANGLES = { 8f, 18f, 28f, 35f };

    private static final Map<Integer, GroundTiltEffect> activeEffects = new LinkedHashMap<>();
    private static int nextId = 0;

    private record TiltBlock(
            double x, double y, double z,
            BlockState state,
            Quaternionf rotation,
            int ringIndex
    ) {}

    private static class GroundTiltEffect {
        final List<TiltBlock> blocks;
        final long startTick;

        GroundTiltEffect(List<TiltBlock> blocks, long startTick) {
            this.blocks = blocks;
            this.startTick = startTick;
        }

        long elapsedTicks(long currentTick) {
            return currentTick - startTick;
        }
    }

    public static void startEffect(Vec3 center) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || !level.isClientSide()) return;

        BlockPos centerPos = BlockPos.containing(center);
        int baseY = centerPos.getY();

        List<TiltBlock> blocks = new ArrayList<>();

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double hDist = Math.sqrt(dx * dx + dz * dz);
                if (hDist < 1.5 || hDist > 5.5) continue;

                int ringIndex = hDist < 2.5 ? 0 : hDist < 3.5 ? 1 : hDist < 4.5 ? 2 : 3;

                BlockPos groundPos = findGroundPos(level, centerPos.getX() + dx, baseY, centerPos.getZ() + dz);
                if (groundPos == null) continue;

                BlockState state = level.getBlockState(groundPos);
                if (state.isAir() || state.is(Blocks.BEDROCK)) continue;

                double bx = groundPos.getX() + 0.5;
                double by = groundPos.getY() + 0.5;
                double bz = groundPos.getZ() + 0.5;

                float angleDeg = RING_ANGLES[ringIndex];
                float angleRad = angleDeg * Mth.DEG_TO_RAD;

                double toCx = -dx;
                double toCz = -dz;
                double len = Math.sqrt(toCx * toCx + toCz * toCz);
                if (len < 0.01) continue;

                double axisX = -toCz / len;
                double axisZ = toCx / len;

                Quaternionf rotation = new Quaternionf().setAngleAxis(angleRad, (float) axisX, 0, (float) axisZ);

                blocks.add(new TiltBlock(bx, by, bz, state, rotation, ringIndex));
            }
        }

        if (blocks.isEmpty()) return;

        long tick = level.getGameTime();
        activeEffects.put(nextId++, new GroundTiltEffect(blocks, tick));

        if (activeEffects.size() > 16) {
            activeEffects.values().stream().findFirst().ifPresent(e -> {
                activeEffects.values().remove(e);
            });
        }
    }

    private static BlockPos findGroundPos(Level level, int x, int startY, int z) {
        for (int y = startY; y > startY - SCAN_DOWN; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.isSolidRender(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (activeEffects.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        long tick = level.getGameTime();
        activeEffects.values().removeIf(e -> e.elapsedTicks(tick) > TOTAL_DURATION + DISSOLVE_PER_RING);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        if (activeEffects.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        Vec3 camPos = event.getCamera().getPosition();

        long tick = level.getGameTime();

        for (GroundTiltEffect effect : activeEffects.values()) {
            long elapsed = effect.elapsedTicks(tick);
            if (elapsed > TOTAL_DURATION) continue;

            for (TiltBlock block : effect.blocks) {
                int dissolveStart = HOLD_TICKS + block.ringIndex * DISSOLVE_PER_RING;
                int dissolveEnd = dissolveStart + DISSOLVE_PER_RING;

                if (elapsed >= dissolveEnd) continue;

                float yOffset = 0;
                if (elapsed >= dissolveStart) {
                    float progress = (float) (elapsed - dissolveStart) / DISSOLVE_PER_RING;
                    yOffset = -0.5f * progress;
                }

                poseStack.pushPose();

                double rx = block.x - camPos.x;
                double ry = block.y - camPos.y + yOffset;
                double rz = block.z - camPos.z;
                poseStack.translate(rx, ry, rz);

                poseStack.translate(0, -0.5, 0);
                poseStack.mulPose(block.rotation);
                poseStack.translate(0, 0.5, 0);

                poseStack.translate(-0.5, -0.5, -0.5);

                int light = LevelRenderer.getLightColor(level, BlockPos.containing(block.x, block.y, block.z));
                dispatcher.renderSingleBlock(block.state, poseStack, buffer, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);

                poseStack.popPose();
            }
        }

        buffer.endBatch();
    }
}
