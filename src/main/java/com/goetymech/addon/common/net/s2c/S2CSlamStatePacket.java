package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.PrototypeRenderEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSlamStatePacket {

    private final boolean slamming;

    public S2CSlamStatePacket(boolean slamming) {
        this.slamming = slamming;
    }

    public static void encode(S2CSlamStatePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.slamming);
    }

    public static S2CSlamStatePacket decode(FriendlyByteBuf buf) {
        return new S2CSlamStatePacket(buf.readBoolean());
    }

    public static void handle(S2CSlamStatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PrototypeRenderEvents.setClientSlamming(packet.slamming);
        });
        ctx.get().setPacketHandled(true);
    }
}
