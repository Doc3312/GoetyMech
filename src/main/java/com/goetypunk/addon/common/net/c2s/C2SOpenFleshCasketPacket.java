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

            ItemStack stack = player.getMainHandItem();
            InteractionHand hand = InteractionHand.MAIN_HAND;
            if (!(stack.getItem() instanceof FleshCasketItem)) {
                stack = player.getOffhandItem();
                hand = InteractionHand.OFF_HAND;
                if (!(stack.getItem() instanceof FleshCasketItem)) return;
            }

            int tier = FleshCasketItem.getTier(stack);
            SimpleMenuProvider provider = new SimpleMenuProvider(
                    (id, inventory, p) -> new FleshCasketContainer(
                            id, inventory, FleshCasketItemHandler.get(stack), stack, tier),
                    stack.getHoverName());
            NetworkHooks.openScreen(player, provider, buf -> buf.writeInt(tier));
        });
        ctx.get().setPacketHandled(true);
    }
}
