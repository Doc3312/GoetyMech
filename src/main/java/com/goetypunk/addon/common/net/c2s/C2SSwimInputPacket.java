package com.goetypunk.addon.common.net.c2s;

import com.goetypunk.addon.common.item.PrototypeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSwimInputPacket {
    private final float forward;

    public C2SSwimInputPacket(float forward) {
        this.forward = forward;
    }

    public static void encode(C2SSwimInputPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.forward);
    }

    public static C2SSwimInputPacket decode(FriendlyByteBuf buf) {
        return new C2SSwimInputPacket(buf.readFloat());
    }

    public static void handle(C2SSwimInputPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            PrototypeItem.setSwimForward(player, packet.forward);
        });
        ctx.get().setPacketHandled(true);
    }
}
