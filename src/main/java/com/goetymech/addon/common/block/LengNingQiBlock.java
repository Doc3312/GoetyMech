package com.goetymech.addon.common.block;

import com.goetymech.addon.common.block.entity.LengNingQiBlockEntity;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * 冷凝器。无朝向动力方块：前后（Z 轴）面板为轴接口，左右喇叭带旋转风扇。
 */
public class LengNingQiBlock extends KineticBlock implements IBE<LengNingQiBlockEntity> {

    public LengNingQiBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0f)
                .sound(SoundType.METAL));
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Z;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == Direction.Axis.Z;
    }

    @Override
    public Class<LengNingQiBlockEntity> getBlockEntityClass() {
        return LengNingQiBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LengNingQiBlockEntity> getBlockEntityType() {
        return ModBlocks.LENGNINGQI_ENTITY.get();
    }
}
