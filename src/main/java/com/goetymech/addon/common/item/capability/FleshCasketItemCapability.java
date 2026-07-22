package com.goetymech.addon.common.item.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 血肉晶匣 Capability。管理插槽和自动施法数据。
 */
public class FleshCasketItemCapability implements ICapabilitySerializable<Tag> {
    private final ItemStack stack;
    private final LazyOptional<IItemHandler> holder = LazyOptional.of(this::getHandler);
    private final int size;
    private FleshCasketItemHandler handler;

    public FleshCasketItemCapability(ItemStack stack, int size) {
        this.stack = stack;
        this.size = size;
    }

    @Nonnull
    private FleshCasketItemHandler getHandler() {
        if (handler == null) {
            handler = new FleshCasketItemHandler(stack, this.size);
        }
        return handler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, holder);
    }

    @Override
    public Tag serializeNBT() {
        return getHandler().serializeNBT();
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        getHandler().deserializeNBT((CompoundTag) nbt);
    }
}
