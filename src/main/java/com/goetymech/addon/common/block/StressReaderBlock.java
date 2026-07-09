package com.goetymech.addon.common.block;

import com.goetymech.addon.common.block.entity.StressReaderBlockEntity;
import com.goetymech.addon.common.item.RemoteEngineBlockItem;
import com.goetymech.addon.common.item.StressFocus;
import com.goetymech.addon.common.item.StressShieldFocus;
import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.AllShapes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

public class StressReaderBlock extends DirectionalKineticBlock
        implements IBE<StressReaderBlockEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger("StressReaderBlock");

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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if (preferred == null || (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())) {
            Direction nearestLookingDirection = context.getNearestLookingDirection();
            return defaultBlockState().setValue(FACING, context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite());
        }
        return defaultBlockState().setValue(FACING, preferred);
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

        // Sneak + right-click on reader with bound RemoteEngine item → clear reader binding
        if (player.isShiftKeyDown() && stack.getItem() instanceof RemoteEngineBlockItem
                && RemoteEngineBlockItem.isBound(stack)
                && RemoteEngineBlockItem.getBoundPos(stack).equals(pos)) {
            //LOGGER.debug("[StressReaderBlock] UNBIND at {} | isClient={}", pos, level.isClientSide);
            if (!level.isClientSide) {
                RemoteEngineBlockItem.clearBinding(stack);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.remote_engine.unbound"), true);
            }
            return InteractionResult.SUCCESS;
        }

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
                        Component.translatable("info.goetymech.stress_focus.bound_success"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (stack.getItem() instanceof StressShieldFocus && !player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                StressShieldFocus.bindToReader(stack, level, pos);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.stress_shield.bound"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && !level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof StressReaderBlockEntity reader) {
                reader.setPlacerUUID(player.getUUID());
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return AllShapes.CASING_12PX.get(state.getValue(FACING));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
