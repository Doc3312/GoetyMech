package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.TimeStopUtilsWrapped;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class S2CTimeStopStatePacket {
    private final boolean isTimeStop;
    private final int user;
    private final boolean playSoundEffect;

    public S2CTimeStopStatePacket(boolean isTimeStop, boolean playSoundEffect, int user) {
        this.isTimeStop = isTimeStop;
        this.playSoundEffect = playSoundEffect;
        this.user = user;
    }

    public static S2CTimeStopStatePacket decode(FriendlyByteBuf buf) {
        return new S2CTimeStopStatePacket(buf.readBoolean(), buf.readBoolean(), buf.readVarInt());
    }

    public static void encode(S2CTimeStopStatePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.isTimeStop);
        buf.writeBoolean(packet.playSoundEffect);
        buf.writeVarInt(packet.user);
    }

    public static void handle(S2CTimeStopStatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            if (packet.isTimeStop) {
                TimeStopUtilsWrapped.enable(packet.user);
            } else {
                TimeStopUtilsWrapped.disable();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
