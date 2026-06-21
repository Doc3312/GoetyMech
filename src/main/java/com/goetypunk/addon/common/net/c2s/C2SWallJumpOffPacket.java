package com.goetypunk.addon.common.net.c2s;

import com.goetypunk.addon.common.events.StormWingEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SWallJumpOffPacket {

    public C2SWallJumpOffPacket() {
    }

    public static void encode(C2SWallJumpOffPacket packet, FriendlyByteBuf buf) {
    }

    public static C2SWallJumpOffPacket decode(FriendlyByteBuf buf) {
        return new C2SWallJumpOffPacket();
    }

    public static void handle(C2SWallJumpOffPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            StormWingEvents.handleWallJumpOff(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
