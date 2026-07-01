package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.StressArkHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class S2CStressArkDataPacket {
    private final float totalCapacity;
    private final float totalStress;
    private final int networkCount;

    public S2CStressArkDataPacket(float totalCapacity, float totalStress, int networkCount) {
        this.totalCapacity = totalCapacity;
        this.totalStress = totalStress;
        this.networkCount = networkCount;
    }

    public static void encode(S2CStressArkDataPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.totalCapacity);
        buf.writeFloat(packet.totalStress);
        buf.writeVarInt(packet.networkCount);
    }

    public static S2CStressArkDataPacket decode(FriendlyByteBuf buf) {
        return new S2CStressArkDataPacket(buf.readFloat(), buf.readFloat(), buf.readVarInt());
    }

    public static void handle(S2CStressArkDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (packet.totalCapacity < 0) {
                StressArkHUD.clearData();
            } else {
                StressArkHUD.onData(packet.totalCapacity, packet.totalStress, packet.networkCount);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
