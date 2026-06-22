package com.goetypunk.addon.common.net.c2s;

import com.goetypunk.addon.common.events.PrototypeEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SJumpChargePacket {
    private final int action;

    public C2SJumpChargePacket(int action) {
        this.action = action;
    }

    public static void encode(C2SJumpChargePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.action);
    }

    public static C2SJumpChargePacket decode(FriendlyByteBuf buf) {
        return new C2SJumpChargePacket(buf.readVarInt());
    }

    public static void handle(C2SJumpChargePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            PrototypeEvents.handleJumpChargeFromClient(player, packet.action);
        });
        ctx.get().setPacketHandled(true);
    }
}
