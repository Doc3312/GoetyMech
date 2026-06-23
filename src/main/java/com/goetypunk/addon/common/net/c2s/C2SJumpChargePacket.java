package com.goetypunk.addon.common.net.c2s;

import com.goetypunk.addon.common.events.PrototypeEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SJumpChargePacket {
    private final int action;
    private final float forward;
    private final boolean jumping;
    private final double clientGroundSpeed;

    public C2SJumpChargePacket(int action, float forward, boolean jumping, double clientGroundSpeed) {
        this.action = action;
        this.forward = forward;
        this.jumping = jumping;
        this.clientGroundSpeed = clientGroundSpeed;
    }

    public static void encode(C2SJumpChargePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.action);
        buf.writeFloat(packet.forward);
        buf.writeBoolean(packet.jumping);
        buf.writeDouble(packet.clientGroundSpeed);
    }

    public static C2SJumpChargePacket decode(FriendlyByteBuf buf) {
        return new C2SJumpChargePacket(
                buf.readVarInt(),
                buf.readFloat(),
                buf.readBoolean(),
                buf.readDouble()
        );
    }

    public static void handle(C2SJumpChargePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            PrototypeEvents.handleJumpChargeFromClient(player, packet.action, packet.forward, packet.jumping, packet.clientGroundSpeed);
        });
        ctx.get().setPacketHandled(true);
    }
}
