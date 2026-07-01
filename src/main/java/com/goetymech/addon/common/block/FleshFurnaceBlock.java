package com.goetymech.addon.common.block;

import com.goetymech.addon.common.block.entity.FleshFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * 琛€鑲夌啍鐐?- 鎵洸鑳氳儙鐨勭児鍒惰浆鍖栧櫒
 * 鏀惧叆鎵洸鑳氳儙 鈫?鐑瑰埗 鈫?鑷姩寮瑰嚭鎴愮啛鑳氳儙
 * 闇€鍏堥€氳繃鎸囪矾鐭崇粦瀹氫笉绁ョ伀鐐‘瀹氬叺绉嶇被鍨? */
public class FleshFurnaceBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public FleshFurnaceBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(3.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .lightLevel(state -> state.getValue(LIT) ? 10 : 0)
                .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(LIT, Boolean.FALSE));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FleshFurnaceBlockEntity furnace)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        // 鎵嬫寔鎵洸鑳氳儙 鈫?灏濊瘯鏀惧叆鐔旂倝鐑瑰埗
        if (held.is(com.goetymech.addon.init.ModItems.TWISTED_EMBRYO.get())) {
            if (furnace.canAcceptInput() && furnace.getTargetEntityTypeId() != null) {
                if (!level.isClientSide) {
                    ItemStack insert = held.split(1);
                    furnace.setCookingStack(insert);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            // 鐔旂倝蹇欑鎴栨棤鏈夋晥璁粌鐩爣锛岄樆姝㈡壄鏇茶儦鑳庤嚜韬Е鍙戝彫鍞?
            return InteractionResult.FAIL;
        }

        // 绌烘墜 鈫?鍙栧嚭褰撳墠姝ｅ湪杞寲鐨勭墿鍝?
        if (held.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack extracted = furnace.removeItem(0, 1);
                if (!extracted.isEmpty()) {
                    if (!player.getInventory().add(extracted)) {
                        player.drop(extracted, false);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FleshFurnaceBlockEntity furnace) {
                furnace.dropCookingStack();
            }
            super.onRemove(state, level, pos, newState, moving);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        LevelAccessor level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, waterlogged);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, LIT);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FleshFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (world, pos, blockState, be) -> {
            if (be instanceof FleshFurnaceBlockEntity furnace) {
                furnace.tick();
            }
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FleshFurnaceBlockEntity furnace) {
                furnace.setPlacerName(player.getDisplayName().getString());
       
         // 鏀剧疆鏃惰嚜鍔ㄦ壂鎻忛摼鎺ラ檮杩戠殑涓嶇ゥ鐏偓
                if (!level.isClientSide) {
                    boolean found = furnace.scanAndLink();
            
        if (found) {
                        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.3F, 1.2F);
                    }
                }
            }
        }
    }
}