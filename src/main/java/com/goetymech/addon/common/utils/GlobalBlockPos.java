package com.goetymech.addon.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record GlobalBlockPos(ResourceKey<Level> dimension, BlockPos pos) {

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", dimension.location().toString());
        tag.putLong("pos", pos.asLong());
        return tag;
    }

    public static GlobalBlockPos load(CompoundTag tag) {
        ResourceKey<Level> dim = ResourceKey.create(
                Registries.DIMENSION,
                new ResourceLocation(tag.getString("dim")));
        BlockPos pos = BlockPos.of(tag.getLong("pos"));
        return new GlobalBlockPos(dim, pos);
    }

    public static GlobalBlockPos of(Level level, BlockPos pos) {
        return new GlobalBlockPos(level.dimension(), pos);
    }
}
