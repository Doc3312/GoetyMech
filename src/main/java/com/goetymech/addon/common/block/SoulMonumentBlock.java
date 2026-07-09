package com.goetymech.addon.common.block;

import com.goetymech.addon.common.block.entity.SoulMonumentBlockEntity;
import com.goetymech.addon.common.block.entity.SquadBarracksBlockEntity;
import com.goetymech.addon.common.item.SquadFocus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class SoulMonumentBlock extends BaseEntityBlock {

    public SoulMonumentBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .noOcclusion()
        );
    }

    public abstract int getCapacity();

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!hand.equals(InteractionHand.MAIN_HAND)) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SoulMonumentBlockEntity monument)) return InteractionResult.PASS;

        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            level.destroyBlock(pos, true);
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
        if (placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SoulMonumentBlockEntity monument) {
                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    monument.loadFromData(serverLevel);
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
