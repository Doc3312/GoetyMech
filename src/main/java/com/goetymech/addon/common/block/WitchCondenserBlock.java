package com.goetymech.addon.common.block;

import com.goetymech.addon.common.block.entity.WitchCondenserBlockEntity;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 巫术冷凝机。Create 应力蒸馏机，FACING 6 向，轴接口在 FACING 反面。
 */
public class WitchCondenserBlock extends DirectionalKineticBlock implements IBE<WitchCondenserBlockEntity> {

    public WitchCondenserBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0f)
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
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public Class<WitchCondenserBlockEntity> getBlockEntityClass() {
        return WitchCondenserBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WitchCondenserBlockEntity> getBlockEntityType() {
        return ModBlocks.WITCH_CONDENSER_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlocks.WITCH_CONDENSER_ENTITY.get().create(pos, state);
    }
}
