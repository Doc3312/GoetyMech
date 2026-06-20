package com.goetypunk.addon.common.block;

import com.goetypunk.addon.common.block.entity.StressReaderBlockEntity;
import com.goetypunk.addon.common.item.StressFocus;
import com.goetypunk.addon.config.ModConfig;
import com.goetypunk.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StressReaderBlock extends DirectionalKineticBlock
        implements IBE<StressReaderBlockEntity> {

    public StressReaderBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.5f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<StressReaderBlockEntity> getBlockEntityClass() {
        return StressReaderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StressReaderBlockEntity> getBlockEntityType() {
        return ModBlocks.STRESS_READER_ENTITY.get();
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StressReaderBlockEntity reader) {
            return reader.getComparatorOutput();
        }
        return 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof StressFocus && !player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                StressFocus.bindToReader(stack, level, pos);
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof StressReaderBlockEntity reader) {
                    int capacity = (int) reader.getNetworkCapacity();
                    int damage = Math.max(1, (int) (capacity / ModConfig.STRESS_DAMAGE_DIVISOR.get()));
                    StressFocus.updateDamageCache(stack, damage);
                }
                player.displayClientMessage(
                        Component.translatable("info.goetypunk.stress_focus.bound_success"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
