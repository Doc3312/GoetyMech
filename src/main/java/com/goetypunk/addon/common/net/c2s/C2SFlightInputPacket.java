package com.goetypunk.addon.common.net.c2s;

import com.goetypunk.addon.common.item.StormWingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SFlightInputPacket {
    private final float forward;

    public C2SFlightInputPacket(float forward) {
        this.forward = forward;
    }

    public static void encode(C2SFlightInputPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.forward);
    }

    public static C2SFlightInputPacket decode(FriendlyByteBuf buf) {
        return new C2SFlightInputPacket(buf.readFloat());
    }

    public static void handle(C2SFlightInputPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            StormWingItem.setFlightForward(player, packet.forward);
        });
        ctx.get().setPacketHandled(true);
    }
}
