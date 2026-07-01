package com.goetymech.addon.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class WallUtil {

    @Nullable
    public static Vec3 getWallDirection(LivingEntity entity, double range) {
        double width = entity.getBbWidth() * 0.4;
        Vec3 pos = entity.position();

        List<AABB> boxes = Arrays.asList(
                new AABB(pos.x - width, pos.y, pos.z - width,
                        pos.x + width, pos.y + entity.getBbHeight() * 0.3, pos.z + width),
                new AABB(pos.x - width, pos.y + entity.getBbHeight() * 0.85, pos.z - width,
                        pos.x + width, pos.y + entity.getBbHeight(), pos.z + width)
        );

        int wallX = 0, wallZ = 0;
        if (boxes.stream().noneMatch(b -> entity.level().noCollision(b.expandTowards(range, 0, 0)))) wallX++;
        if (boxes.stream().noneMatch(b -> entity.level().noCollision(b.expandTowards(-range, 0, 0)))) wallX--;
        if (boxes.stream().noneMatch(b -> entity.level().noCollision(b.expandTowards(0, 0, range)))) wallZ++;
        if (boxes.stream().noneMatch(b -> entity.level().noCollision(b.expandTowards(0, 0, -range)))) wallZ--;
        if (wallX == 0 && wallZ == 0) return null;
        return new Vec3(wallX, 0, wallZ);
    }

    public static Vec3 projectOnPlane(Vec3 vector, Vec3 normal) {
        return vector.subtract(normal.scale(vector.dot(normal)));
    }

    public static boolean hasAdjacentWall(LivingEntity entity) {
        return getWallDirection(entity, 0.5) != null;
    }

    @Nullable
    public static Vec3 findBestWallDirection(LivingEntity entity, double range) {
        double width = entity.getBbWidth() * 0.4;
        Vec3 pos = entity.position();
        List<AABB> boxes = Arrays.asList(
                new AABB(pos.x - width, pos.y, pos.z - width,
                        pos.x + width, pos.y + entity.getBbHeight() * 0.3, pos.z + width),
                new AABB(pos.x - width, pos.y + entity.getBbHeight() * 0.85, pos.z - width,
                        pos.x + width, pos.y + entity.getBbHeight(), pos.z + width)
        );

        record DirWithDist(Vec3 dir, double dist) {}
        DirWithDist best = null;

        Vec3[][] candidates = {
                {new Vec3(1, 0, 0), new Vec3(range, 0, 0)},
                {new Vec3(-1, 0, 0), new Vec3(-range, 0, 0)},
                {new Vec3(0, 0, 1), new Vec3(0, 0, range)},
                {new Vec3(0, 0, -1), new Vec3(0, 0, -range)}
        };

        for (Vec3[] c : candidates) {
            if (boxes.stream().noneMatch(b -> entity.level().noCollision(b.expandTowards(c[1])))) {
                double dist = measureWallDist(entity, c[0], range);
                if (best == null || dist < best.dist) {
                    best = new DirWithDist(c[0], dist);
                }
            }
        }

        return best != null ? best.dir() : null;
    }

    private static double measureWallDist(LivingEntity entity, Vec3 dir, double maxRange) {
        double step = 0.1;
        Vec3 pos = entity.position();
        for (double d = step; d <= maxRange; d += step) {
            Vec3 check = pos.add(dir.scale(d));
            if (!entity.level().noCollision(new AABB(check, check).inflate(0.2))) {
                return d;
            }
        }
        return maxRange;
    }

    public static float getWallFriction(LivingEntity entity, Vec3 wallNormal) {
        Vec3 pos = entity.position();
        BlockPos targetPos = BlockPos.containing(pos.x + wallNormal.x * 0.5, pos.y, pos.z + wallNormal.z * 0.5);
        BlockState blockState = entity.level().getBlockState(targetPos);
        return blockState.getBlock().getFriction();
    }
}
