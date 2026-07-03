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

import java.util.*;

@Mod.EventBusSubscriber(modid = com.goetymech.addon.GoetyMechMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SlamGroundTiltHandler {

    private static final int MAX_LIFETIME = 60;
    private static final int SCAN_DOWN = 5;

    private static final int EFFECT_ID_LIMIT = 100;
    private static int effectCounter = 0;

    public static void startEffect(Vec3 center) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || !level.isClientSide()) return;

        BlockPos centerPos = BlockPos.containing(center);
        int baseY = centerPos.getY();

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

                    double t = Mth.clamp((hDist - 2.0) / 3.0, 0.0, 1.0);
                    double smoothT = t * t * (3.0 - 2.0 * t);
                    float angleDeg = (float) (5.0 + smoothT * 55.0);
                    float lift = (float) (smoothT * 0.35);
                    double by = groundPos.getY() + 0.5 + lift;

                    float angleRad = angleDeg * Mth.DEG_TO_RAD;

                    double toCx = -dx;
                    double toCz = -dz;
                    double len = Math.sqrt(toCx * toCx + toCz * toCz);
                    if (len < 0.01) continue;

                    double axisX = -toCz / len;
                    double axisZ = toCx / len;

                    Quaternionf rotation = new Quaternionf().setAngleAxis(-angleRad, (float) axisX, 0, (float) axisZ);
                    int lifetime = MAX_LIFETIME;
                    int ringIndex = hDist < 2.5 ? 0 : hDist < 3.5 ? 1 : hDist < 4.5 ? 2 : 3;
                    lifetime -= ringIndex * 5;

                    targets.put(groundPos, new BlockData(state, rotation, (float)(by - groundPos.getY()), lifetime));
                }
            }
        }

        for (var entry : targets.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockData data = entry.getValue();

            level.setBlock(pos, ModBlocks.CRATER.get().defaultBlockState(), 3);
            if (level.getBlockEntity(pos) instanceof CraterBlockEntity be) {
                be.setData(data.state, data.rotation, data.liftHeight, data.lifetime);
            }
        }

        effectCounter++;
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

    private record BlockData(BlockState state, Quaternionf rotation, float liftHeight, int lifetime) {}
}
