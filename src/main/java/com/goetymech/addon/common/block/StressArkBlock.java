package com.goetymech.addon.common.block;

import com.goetymech.addon.common.block.entity.StressArkBlockEntity;
import com.goetymech.addon.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class StressArkBlock extends BaseEntityBlock {

    public StressArkBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(75.0F, 2400.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .forceSolidOn()
                .noOcclusion()
        );
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof StressArkBlockEntity ark) {
            ark.setOwner(player);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof StressArkBlockEntity ark) {
            if (!hand.equals(InteractionHand.MAIN_HAND)) return InteractionResult.PASS;
            if (!ark.isOwner(player)) {
                return InteractionResult.FAIL;
            }
            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                level.destroyBlock(pos, true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof StressArkBlockEntity ark) {
                ark.onArkRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof StressArkBlockEntity ark && !ark.isOwner(player)) {
            if (!player.isCreative()) {
                return 0.0F;
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof StressArkBlockEntity ark) {
            float cap = ark.getCachedTotalCapacity();
            if (cap <= 0) return 0;
            float ratio = ark.getCachedTotalStress() / cap;
            return Math.min(15, Math.max(0, (int) (ratio * 15)));
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StressArkBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (world, pos, state1, be) -> {
            if (be instanceof StressArkBlockEntity ark) {
                StressArkBlockEntity.tick(world, pos, state1, ark);
            }
        };
    }
}
