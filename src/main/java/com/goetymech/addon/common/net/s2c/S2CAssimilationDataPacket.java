package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.net.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 同化数据 S2C 包。同步击杀数到客户端。
 */
public class S2CAssimilationDataPacket {

    private final int kills;

    public S2CAssimilationDataPacket(int kills) {
        this.kills = kills;
    }

    public static void encode(S2CAssimilationDataPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.kills);
    }

    public static S2CAssimilationDataPacket decode(FriendlyByteBuf buf) {
        return new S2CAssimilationDataPacket(buf.readInt());
    }

    public static void handle(S2CAssimilationDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleAssimilationData(packet)));
        ctx.get().setPacketHandled(true);
    }

    public int getKills() {
        return kills;
    }
}
