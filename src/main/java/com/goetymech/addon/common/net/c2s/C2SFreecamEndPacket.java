package com.goetymech.addon.common.net.c2s;

import com.goetymech.addon.common.events.FreecamEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SFreecamEndPacket {

    public C2SFreecamEndPacket() {}

    public static void encode(C2SFreecamEndPacket packet, FriendlyByteBuf buf) {}

    public static C2SFreecamEndPacket decode(FriendlyByteBuf buf) {
        return new C2SFreecamEndPacket();
    }

    public static void handle(C2SFreecamEndPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            FreecamEvents.onClientEndFreecam(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
