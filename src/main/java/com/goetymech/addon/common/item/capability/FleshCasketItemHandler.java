package com.goetymech.addon.common.item.capability;

import com.Polarice3.Goety.api.items.magic.IFocus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class FleshCasketItemHandler extends ItemStackHandler {
    private final ItemStack itemStack;

    public FleshCasketItemHandler(ItemStack itemStack, int size) {
        super(size);
        this.itemStack = itemStack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof IFocus;
    }

    @Override
    protected void onContentsChanged(int slot) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        nbt.putBoolean("goetymech-dirty", !nbt.getBoolean("goetymech-dirty"));
    }

    public static FleshCasketItemHandler get(ItemStack stack) {
        IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElseThrow(() -> new IllegalArgumentException("ItemStack is missing item capability"));
        return (FleshCasketItemHandler) handler;
    }
}
