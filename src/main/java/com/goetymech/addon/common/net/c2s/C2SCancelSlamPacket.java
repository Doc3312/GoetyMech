package com.goetymech.addon.common.net.c2s;

import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CSlamStatePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SCancelSlamPacket {
    public static C2SCancelSlamPacket decode(FriendlyByteBuf buf) {
        return new C2SCancelSlamPacket();
    }

    public static void encode(C2SCancelSlamPacket packet, FriendlyByteBuf buf) {
    }

    public static void handle(C2SCancelSlamPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!PrototypeItem.isSlamming(player)) return;
            player.fallDistance = 0;
            player.setDeltaMovement(player.getDeltaMovement().x, Math.max(0.5, player.getDeltaMovement().y), player.getDeltaMovement().z);
            ModNetwork.sendTo(player, new S2CSlamStatePacket(false));
            PrototypeItem.clearSlamState(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
