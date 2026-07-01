package com.goetymech.addon.common.menu;

import com.goetymech.addon.common.block.entity.SquadBarracksBlockEntity;
import com.goetymech.addon.init.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SquadBarracksMenu extends AbstractContainerMenu {

    private final SquadBarracksBlockEntity barracks;

    public SquadBarracksMenu(int id, Inventory inv, SquadBarracksBlockEntity barracks) {
        super(ModMenuTypes.SQUAD_BARRACKS.get(), id);
        this.barracks = barracks;
        barracks.tryClaim(inv.player);
        Container container = barracks;

        int configSlots = barracks.getConfigSlotCount();
        int reserveSlots = barracks.getReserveSlotCount();

        // 鈹€鈹€ Config slots (dynamic count, up to 18, indices 0 to configSlots-1) 鈹€鈹€
        for (int i = 0; i < configSlots; i++) {
            int r = i / 6;
            int c = i % 6;
            int x = 8 + c * 18;
            int y = 18 + r * 18;
            addSlot(new Slot(container, i, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }

        // 鈹€鈹€ Reserve slots (3 cols 脳 3 rows = 9 slots, remains fixed) 鈹€鈹€
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int index = configSlots + r * 3 + c;
                int x = 134 + c * 18;
                int y = 18 + r * 18;
                addSlot(new Slot(container, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.is(ModItems.MATURE_EMBRYO.get());
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 16;
                    }
                });
            }
        }

        // 鈹€鈹€ Player inventory (3 rows 脳 9 cols) 鈹€鈹€
        int invY = 86;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlot(new Slot(inv, c + r * 9 + 9, 17 + c * 18, invY + r * 18));
            }
        }

        // 鈹€鈹€ Hotbar 鈹€鈹€
        int hotbarY = invY + 3 * 18 + 4;
        for (int c = 0; c < 9; c++) {
            addSlot(new Slot(inv, c, 17 + c * 18, hotbarY));
        }
    }

    public SquadBarracksMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (SquadBarracksBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();

            int configSlots = barracks.getConfigSlotCount();
            int reserveEnd = configSlots + barracks.getReserveSlotCount();
            int playerStart = reserveEnd;
            int hotbarEnd = playerStart + 36;

            if (index >= configSlots && index < reserveEnd) {
                if (!moveItemStackTo(slotStack, playerStart, hotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(ModItems.MATURE_EMBRYO.get())) {
                if (!moveItemStackTo(slotStack, configSlots, reserveEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return barracks.stillValid(player);
    }

    public SquadBarracksBlockEntity getBarracks() {
        return barracks;
    }
}
