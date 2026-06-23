package com.goetypunk.addon.common.net.c2s;

import com.goetypunk.addon.common.item.PrototypeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SWallInputPacket {
    private final float forward;
    private final float strafe;

    public C2SWallInputPacket(float forward, float strafe) {
        this.forward = forward;
        this.strafe = strafe;
    }

    public static void encode(C2SWallInputPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.forward);
        buf.writeFloat(packet.strafe);
    }

    public static C2SWallInputPacket decode(FriendlyByteBuf buf) {
        return new C2SWallInputPacket(buf.readFloat(), buf.readFloat());
    }

    public static void handle(C2SWallInputPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            PrototypeItem.setWallForward(player, packet.forward);
            PrototypeItem.setWallStrafe(player, packet.strafe);
        });
        ctx.get().setPacketHandled(true);
    }
}
