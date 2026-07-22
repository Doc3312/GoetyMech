package com.goetymech.addon.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 信标效果扫描。丰碑旁信标 buff 传播给召唤仆从。
 */
public class BeaconHelper {

    private static final Field PRIMARY_POWER;
    private static final Field SECONDARY_POWER;
    private static final Field LEVELS;

    static {
        Field primary = null;
        Field secondary = null;
        Field levels = null;
        try {
            primary = BeaconBlockEntity.class.getDeclaredField("primaryPower");
            primary.setAccessible(true);
            secondary = BeaconBlockEntity.class.getDeclaredField("secondaryPower");
            secondary.setAccessible(true);
            levels = BeaconBlockEntity.class.getDeclaredField("levels");
            levels.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
        PRIMARY_POWER = primary;
        SECONDARY_POWER = secondary;
        LEVELS = levels;
    }

    public static List<MobEffectInstance> collectBeaconEffects(ServerLevel level, BlockPos center, int range) {
        List<MobEffectInstance> effects = new ArrayList<>();
        Set<MobEffect> added = new HashSet<>();

        if (PRIMARY_POWER == null || SECONDARY_POWER == null || LEVELS == null) return effects;

        int rangeSq = range * range;
        int chunkRadius = (range + 15) / 16;

        for (int cx = (center.getX() >> 4) - chunkRadius; cx <= (center.getX() >> 4) + chunkRadius; cx++) {
            for (int cz = (center.getZ() >> 4) - chunkRadius; cz <= (center.getZ() >> 4) + chunkRadius; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof BeaconBlockEntity beacon) {
                        BlockPos bp = be.getBlockPos();
                        if (bp.distSqr(center) > rangeSq) continue;
                        try {
                            int beaconLevels = LEVELS.getInt(beacon);
                            MobEffect primary = (MobEffect) PRIMARY_POWER.get(beacon);
                            MobEffect secondary = (MobEffect) SECONDARY_POWER.get(beacon);

                            if (primary != null && beaconLevels >= 1 && added.add(primary)) {
                                int amplifier = (beaconLevels >= 4 || (beaconLevels >= 3 && secondary == primary)) ? 1 : 0;
                                effects.add(new MobEffectInstance(primary, -1, amplifier, true, true));
                            }
                            if (secondary != null && secondary != primary && beaconLevels >= 4 && added.add(secondary)) {
                                effects.add(new MobEffectInstance(secondary, -1, 0, true, true));
                            }
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                }
            }
        }

        return effects;
    }
}
