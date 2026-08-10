package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.net.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * 时停状态 S2C 包。广播冻结/解冻。
 */
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
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleTimeStopState(packet)));
        ctx.get().setPacketHandled(true);
    }

    public boolean isTimeStop() {
        return isTimeStop;
    }

    public int getUser() {
        return user;
    }
}
