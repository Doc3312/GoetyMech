package com.goetymech.addon.mixin;

import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CollectingNeighborUpdater.class)
/**
 * 红石更新 Mixin（收集模式）。时停中取消范围内的更新。
 */
public abstract class CollectingNeighborUpdaterMixin {
    @Shadow @Final private Level level;

    @Inject(method = "shapeUpdate", at = @At("HEAD"), cancellable = true)
    private void gp$cancelShapeUpdate(Direction direction, BlockPos pos, BlockPos neighborPos,
                                     int updateFlags, int recursionLevel, int recursionCount, CallbackInfo ci) {
        gp$cancelWhenTimeFrozen(pos, ci);
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void gp$cancelNeighborChanged(BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving, CallbackInfo ci) {
        gp$cancelWhenTimeFrozen(pos, ci);
    }

    @Inject(method = "updateNeighborsAtExceptFromFacing", at = @At("HEAD"), cancellable = true)
    private void gp$cancelUpdateNeighbors(BlockPos pos, Block block, Direction direction, CallbackInfo ci) {
        gp$cancelWhenTimeFrozen(pos, ci);
    }

    private void gp$cancelWhenTimeFrozen(BlockPos pos, CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && TimeStopUtils.isInRange(this.level, pos))
            ci.cancel();
    }
}
