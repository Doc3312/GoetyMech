package com.goetypunk.addon.common.utils;

import net.minecraft.world.entity.LivingEntity;
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
}
