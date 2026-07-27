package com.goetymech.addon.common.worker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;

public class GatewaySearchWorker {

    @FunctionalInterface
    public interface Callback {
        void onComplete(@Nullable BlockPos pos, @Nullable ServerLevel level);
    }

    private static final int[] RADII = { 3, 5, 10, 15, 25, 40, 60, 90, 130, 190, 270, 380, 500 };

    private final ServerPlayer player;
    private final ServerLevel level;
    private final TagKey<Structure> tag;
    private final BlockPos origin;
    private final Callback callback;
    private final int maxRadius;

    private int radiusIndex;
    private boolean finished;
    private int lastLoggedRadius;

    public GatewaySearchWorker(ServerPlayer player, ServerLevel level, TagKey<Structure> tag, BlockPos origin, Callback callback, int maxRadius) {
        this.player = player;
        this.level = level;
        this.tag = tag;
        this.origin = origin;
        this.callback = callback;
        this.maxRadius = maxRadius;
        this.radiusIndex = 0;
    }

    public GatewaySearchWorker(ServerPlayer player, ServerLevel level, TagKey<Structure> tag, BlockPos origin, Callback callback) {
        this(player, level, tag, origin, callback, 500);
    }

    public boolean tick() {
        if (finished) return true;

        long deadline = System.currentTimeMillis() + 10;

        while (System.currentTimeMillis() < deadline) {
            if (radiusIndex >= RADII.length) {
                GatewaySearchManager.DEBUG("Search exhausted all radius steps for {}", player.getScoreboardName());
                finish(null, level);
                return true;
            }

            int radius = RADII[radiusIndex];
            radiusIndex++;

            if (radius > maxRadius) {
                GatewaySearchManager.DEBUG("Search reached maxRadius={} for {}", maxRadius, player.getScoreboardName());
                finish(null, level);
                return true;
            }

            if (radius / 25 > lastLoggedRadius) {
                lastLoggedRadius = radius / 25;
                GatewaySearchManager.DEBUG("Search radius={} chunks for {}", radius, player.getScoreboardName());
            }

            BlockPos found = level.findNearestMapStructure(tag, origin, radius, false);
            if (found != null) {
                GatewaySearchManager.DEBUG("Found structure at {} (radius={}) in dim {} for {}",
                        found, radius, level.dimension().location(), player.getScoreboardName());
                finish(found, level);
                return true;
            }
        }

        return false;
    }

    private void finish(@Nullable BlockPos pos, ServerLevel foundLevel) {
        finished = true;
        callback.onComplete(pos, foundLevel);
    }

    public boolean isFinished() {
        return finished;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public void cancel() {
        finished = true;
    }
}
