package com.goetymech.addon.common.net.c2s;

import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * 时停开关 C2S 包。
 */
public class C2STimeStopPacket {
    private final boolean activate;

    public C2STimeStopPacket(boolean activate) {
        this.activate = activate;
    }

    public static C2STimeStopPacket decode(FriendlyByteBuf buf) {
        return new C2STimeStopPacket(buf.readBoolean());
    }

    public static void encode(C2STimeStopPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.activate);
    }

    public static void handle(C2STimeStopPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            TimeStopUtils.use(packet.activate, player, true, 180, false);
        });
        ctx.get().setPacketHandled(true);
    }
}
