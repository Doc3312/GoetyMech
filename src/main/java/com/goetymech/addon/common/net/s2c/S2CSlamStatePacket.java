package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.net.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Slam 状态 S2C 包。同步 slam 开始/结束。
 */
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
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleSlamState(packet)));
        ctx.get().setPacketHandled(true);
    }

    public boolean isSlamming() {
        return slamming;
    }
}
