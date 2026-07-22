package com.goetymech.addon.common.block;

import com.goetymech.addon.common.block.entity.SoulMonumentBlockEntity;
import com.goetymech.addon.common.item.SquadFocus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public abstract class SoulMonumentBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Shapes.box(1.0 / 16.0, 0.0, 3.0 / 16.0, 15.0 / 16.0, 2.0, 13.0 / 16.0);

    public SoulMonumentBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public abstract int getCapacity();

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!hand.equals(InteractionHand.MAIN_HAND)) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SoulMonumentBlockEntity monument)) return InteractionResult.PASS;

        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            if (!level.isClientSide && monument.getUsedSlots() > 0) {
                ListTag uuidList = new ListTag();
                java.util.List<String> names = monument.getSavedEntityNames();
                java.util.List<java.util.UUID> uuids = monument.getSavedEntityUUIDs();
                for (int i = 0; i < uuids.size(); i++) {
                    CompoundTag entry = new CompoundTag();
                    entry.putUUID("UUID", uuids.get(i));
                    if (i < names.size()) {
                        entry.putString("Name", names.get(i));
                    }
                    uuidList.add(entry);
                }
                ItemStack dropStack = new ItemStack(this.asItem());
                dropStack.getOrCreateTag().put("MonumentUUIDs", uuidList);
                popResource(level, pos, dropStack);
                level.removeBlock(pos, false);
            } else {
                level.destroyBlock(pos, true);
            }
            return InteractionResult.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && held.getItem() instanceof SquadFocus) {
            if (!level.isClientSide) {
                SquadFocus squadFocus = (SquadFocus) held.getItem();
                squadFocus.bindToMonument(held, level, pos, monument);
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.3F, 1.2F);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.squad_focus.bound_success_monument"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SoulMonumentBlockEntity monument) {
            if (placer instanceof Player player) {
                monument.setOwner(player.getUUID());
            }
            if (stack.hasTag() && stack.getTag().contains("MonumentUUIDs")) {
                ListTag uuidList = stack.getTag().getList("MonumentUUIDs", Tag.TAG_COMPOUND);
                java.util.List<java.util.UUID> uuids = new java.util.ArrayList<>();
                java.util.List<String> names = new java.util.ArrayList<>();
                for (int i = 0; i < uuidList.size(); i++) {
                    CompoundTag entry = uuidList.getCompound(i);
                    if (entry.hasUUID("UUID")) {
                        uuids.add(entry.getUUID("UUID"));
                    }
                    if (entry.contains("Name")) {
                        names.add(entry.getString("Name"));
                    }
                }
                monument.setSavedEntityUUIDs(uuids);
                if (names.size() == uuids.size()) {
                    monument.setSavedEntityNames(names);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                unbindFoci(serverLevel, pos);
            }
            super.onRemove(state, level, pos, newState, moving);
        }
    }

    private static void unbindFoci(ServerLevel level, BlockPos pos) {
        ResourceLocation dimId = level.dimension().location();
        var server = level.getServer();
        if (server == null) return;
        for (var serverLevel : server.getAllLevels()) {
            for (var player : serverLevel.players()) {
                for (ItemStack stack : SquadFocus.findAllBoundFoci(player, pos, dimId)) {
                    SquadFocus.clearBinding(stack);
                }
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulMonumentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (world, pos, state1, be) -> {
            if (be instanceof SoulMonumentBlockEntity monument) {
                SoulMonumentBlockEntity.tick(world, pos, state1, monument);
            }
        };
    }
}
