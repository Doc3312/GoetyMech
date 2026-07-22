package com.goetymech.addon.common.magic.spell;

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
import net.minecraftforge.network.NetworkHooks;

import java.util.Collection;
import java.util.UUID;

/**
 * 远程仓储中转站。管理物品存入逻辑。
 */
public class DepositProvider implements MenuProvider {

    private final UUID freqId;
    private final SimpleContainer container;

    public DepositProvider(UUID freqId, SimpleContainer container) {
        this.freqId = freqId;
        this.container = container;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
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
        if (links.isEmpty()) return;

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
            SimpleContainer remaining = new SimpleContainer(54);
            for (int i = 0; i < container.getContainerSize(); i++) {
                remaining.setItem(i, container.getItem(i));
            }
            NetworkHooks.openScreen(sp, new DepositProvider(freqId, remaining), buf -> {});
        }
    }
}
