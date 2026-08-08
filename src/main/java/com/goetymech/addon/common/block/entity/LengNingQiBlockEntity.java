package com.goetymech.addon.common.block.entity;

import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 冷凝器方块实体。转速由 Create 应力网络驱动，驱动 BESR 中的风扇与转轴旋转。
 */
public class LengNingQiBlockEntity extends KineticBlockEntity {

    public LengNingQiBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.LENGNINGQI_ENTITY.get(), pos, state);
    }
}
