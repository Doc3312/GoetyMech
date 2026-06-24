package com.goetypunk.addon.common.net.c2s;

import com.goetypunk.addon.common.item.capability.FleshCasketItemHandler;
import com.goetypunk.addon.common.item.curio.FleshCasketItem;
import com.goetypunk.addon.common.menu.FleshCasketContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class C2SOpenFleshCasketPacket {

    public static void encode(C2SOpenFleshCasketPacket packet, FriendlyByteBuf buf) {}

    public static C2SOpenFleshCasketPacket decode(FriendlyByteBuf buf) {
        return new C2SOpenFleshCasketPacket();
    }

    public static void handle(C2SOpenFleshCasketPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack mainStack = player.getMainHandItem();
            ItemStack offStack = player.getOffhandItem();
            ItemStack targetStack;
            if (mainStack.getItem() instanceof FleshCasketItem) {
                targetStack = mainStack;
            } else if (offStack.getItem() instanceof FleshCasketItem) {
                targetStack = offStack;
            } else {
                return;
            }

            int tier = FleshCasketItem.getTier(targetStack);
            SimpleMenuProvider provider = new SimpleMenuProvider(
                    (id, inventory, p) -> new FleshCasketContainer(
                            id, inventory, FleshCasketItemHandler.get(targetStack), targetStack, tier),
                    targetStack.getHoverName());
            NetworkHooks.openScreen(player, provider, buf -> buf.writeInt(tier));
        });
        ctx.get().setPacketHandled(true);
    }
}
