package com.goetymech.addon.common.menu;

import com.goetymech.addon.common.item.capability.FleshCasketItemHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class FleshCasketContainer extends AbstractContainerMenu {
    private final ItemStack stack;
    private final int tier;

    public int getTier() {
        return tier;
    }

    public static FleshCasketContainer createContainerClientSide(int id, Inventory inventory, FriendlyByteBuf buffer) {
        int tier = buffer.readInt();
        int slotCount = switch (tier) {
            case 2 -> 3;
            case 3 -> 5;
            default -> 1;
        };
        return new FleshCasketContainer(id, inventory, new FleshCasketItemHandler(ItemStack.EMPTY, slotCount), ItemStack.EMPTY, tier);
    }

    public FleshCasketContainer(int id, Inventory playerInventory, FleshCasketItemHandler handler, ItemStack stack, int tier) {
        super(ModMenuTypes.FLESH_CASKET.get(), id);
        this.stack = stack;
        this.tier = tier;
        int slotCount = handler.getSlots();
        int startX = (176 - slotCount * 18) / 2;
        for (int i = 0; i < slotCount; i++) {
            addSlot(new SlotItemHandler(handler, i, startX + i * 18, 25));
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 60 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 120));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int focusSlots = switch (tier) {
                case 2 -> 3;
                case 3 -> 5;
                default -> 1;
            };
            if (pIndex < focusSlots) {
                if (!this.moveItemStackTo(itemstack1, focusSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, focusSlots, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return !stack.isEmpty();
    }
}
