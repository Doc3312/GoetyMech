package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.ModClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSetFreecamPacket {
    private final boolean active;

    public S2CSetFreecamPacket(boolean active) {
        this.active = active;
    }

    public static void encode(S2CSetFreecamPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.active);
    }

    public static S2CSetFreecamPacket decode(FriendlyByteBuf buf) {
        return new S2CSetFreecamPacket(buf.readBoolean());
    }

    public static void handle(S2CSetFreecamPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ModClientEvents.freecamActive = packet.active);
        ctx.get().setPacketHandled(true);
    }
}