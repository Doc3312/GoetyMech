package com.goetymech.addon.common.block;

import com.goetymech.addon.common.block.entity.LengNingQiBlockEntity;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * 巫法冷凝。Create 应力蒸馏机，FACING 6 向，轴接口在 FACING 反面。
 */
public class LengNingQiBlock extends DirectionalKineticBlock implements IBE<LengNingQiBlockEntity> {

    public LengNingQiBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<LengNingQiBlockEntity> getBlockEntityClass() {
        return LengNingQiBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LengNingQiBlockEntity> getBlockEntityType() {
        return ModBlocks.LENGNINGQI_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlocks.LENGNINGQI_ENTITY.get().create(pos, state);
    }
}
