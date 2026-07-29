package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.common.data.DepositSavedData;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DepositProvider implements MenuProvider {

    private final UUID freqId;
    private final ServerPlayer player;

    public DepositProvider(UUID freqId, ServerPlayer player) {
        this.freqId = freqId;
        this.player = player;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        SimpleContainer container = new SimpleContainer(54);
        DepositSavedData data = DepositSavedData.readOrCreate(this.player.server);
        if (data.hasItems(this.player.getUUID(), freqId)) {
            List<ItemStack> saved = data.getItems(this.player.getUUID(), freqId);
            for (int i = 0; i < Math.min(saved.size(), 54); i++) {
                container.setItem(i, saved.get(i));
            }
            data.clear(this.player.getUUID(), freqId);
        }
        return new ChestMenu(MenuType.GENERIC_9x6, id, inv, container, 6) {
            @Override
            public void removed(Player p) {
                depositItems(p, container, freqId);
                super.removed(p);
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.goetymech.deposit");
    }

    private static void depositItems(Player player, SimpleContainer container, UUID freqId) {
        if (player.level().isClientSide) return;

        Collection<LogisticallyLinkedBehaviour> links = LogisticallyLinkedBehaviour.getAllPresent(freqId, false);

        outer:
        for (LogisticallyLinkedBehaviour link : links) {
            if (!(link.blockEntity instanceof PackagerLinkBlockEntity plbe)) continue;
            PackagerBlockEntity packager = plbe.getPackager();
            if (packager == null) continue;
            IItemHandler inv = packager.targetInventory.getInventory();
            if (inv == null) continue;

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.isEmpty()) continue;
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(inv, stack, false);
                container.setItem(i, remainder);
            }
        }

        boolean hasRemaining = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                hasRemaining = true;
                break;
            }
        }

        if (hasRemaining && player instanceof ServerPlayer sp) {
            DepositSavedData data = DepositSavedData.readOrCreate(sp.server);
            List<ItemStack> remaining = new ArrayList<>();
            for (int i = 0; i < container.getContainerSize(); i++) {
                remaining.add(container.getItem(i));
            }
            data.setItems(sp.getUUID(), freqId, remaining);
            sp.displayClientMessage(
                    Component.translatable("info.goetymech.deposit.saved"), true);
        }
    }
}
