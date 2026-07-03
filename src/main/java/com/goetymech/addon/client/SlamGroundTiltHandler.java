package com.goetymech.addon.client;

import com.goetymech.addon.common.block.CraterBlock;
import com.goetymech.addon.common.block.entity.CraterBlockEntity;
import com.goetymech.addon.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

@Mod.EventBusSubscriber(modid = com.goetymech.addon.GoetyMechMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SlamGroundTiltHandler {

    private static final int MAX_LIFETIME = 60;
    private static final int SCAN_DOWN = 5;
    private static final double RADIUS = 6.0;

    public static void startEffect(Vec3 center) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || !level.isClientSide()) return;

        BlockPos centerPos = BlockPos.containing(center);
        int baseY = centerPos.getY();

        double bounceExponentCoef = Math.min(1.0 / (RADIUS * RADIUS), 0.1);

        Map<BlockPos, BlockData> targets = new LinkedHashMap<>();

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double hDist = Math.sqrt(dx * dx + dz * dz);
                if (hDist < 1.5 || hDist > 5.5) continue;

                BlockPos groundPos = findGroundPos(level, centerPos.getX() + dx, baseY, centerPos.getZ() + dz);
                if (groundPos == null) continue;

                if (!targets.containsKey(groundPos)) {
                    BlockState state = level.getBlockState(groundPos);
                    if (state.isAir() || state.is(Blocks.BEDROCK) || state.getBlock() instanceof CraterBlock) continue;

                    // --- EpicFight exact formulas ---

                    // Rotation axis: IMPACT_DIRECTION(0,-1,0) × centerToBlock(dx,0,dz) = (-dz, 0, dx)
                    double axisLen = Math.sqrt(dx * dx + dz * dz);
                    if (axisLen < 0.01) continue;
                    float axisX = (float)(-dz / axisLen);
                    float axisZ = (float)(dx / axisLen);

                    // Angle: (distance/radius) × 15° + random(-5°, +5°)
                    float angleDeg = (float)((hDist / RADIUS) * 15.0 + level.random.nextFloat() * 10.0 - 5.0);

                    Quaternionf rotation = new Quaternionf().setAngleAxis(angleDeg * Mth.DEG_TO_RAD, axisX, 0, axisZ);

                    // Random jitter: X ±7.5°, Y ±20°, Z ±7.5°
                    rotation.mul(new Quaternionf().rotationX((level.random.nextFloat() * 15.0F - 7.5F) * Mth.DEG_TO_RAD));
                    rotation.mul(new Quaternionf().rotationY((level.random.nextFloat() * 40.0F - 20.0F) * Mth.DEG_TO_RAD));
                    rotation.mul(new Quaternionf().rotationZ((level.random.nextFloat() * 15.0F - 7.5F) * Mth.DEG_TO_RAD));

                    // Translate (lift): (0, max(0, distance/radius - 0.5) × 0.5, 0)
                    float liftY = Math.max(0.0F, (float)(hDist / RADIUS) - 0.5F) * 0.5F;
                    Vector3f translate = new Vector3f(0, liftY, 0);

                    // Bounce: distance² × min(1/radius², 0.1)
                    double bouncing = hDist * hDist * bounceExponentCoef;

                    // Ring-based lifetime (inner → outer, 5 tick difference per ring)
                    int ringIndex = hDist < 2.5 ? 0 : hDist < 3.5 ? 1 : hDist < 4.5 ? 2 : 3;
                    int lifetime = MAX_LIFETIME - ringIndex * 5;

                    targets.put(groundPos, new BlockData(state, rotation, translate, bouncing, lifetime));
                }
            }
        }

        for (var entry : targets.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockData data = entry.getValue();

            level.setBlock(pos, ModBlocks.CRATER.get().defaultBlockState(), 3);
            if (level.getBlockEntity(pos) instanceof CraterBlockEntity be) {
                be.setData(data.state, data.rotation, data.translate, data.bouncing, data.lifetime);
            }
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

    private record BlockData(BlockState state, Quaternionf rotation, Vector3f translate, double bouncing, int lifetime) {}
}
