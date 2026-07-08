package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.StressArkHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

public class S2CStressArkDataPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger("ArkPacket-DIAG");

    private final float totalCapacity;
    private final float totalStress;
    private final int networkCount;
    private final float engineConsumption;

    public S2CStressArkDataPacket(float totalCapacity, float totalStress, int networkCount, float engineConsumption) {
        this.totalCapacity = totalCapacity;
        this.totalStress = totalStress;
        this.networkCount = networkCount;
        this.engineConsumption = engineConsumption;
    }

    public static void encode(S2CStressArkDataPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.totalCapacity);
        buf.writeFloat(packet.totalStress);
        buf.writeVarInt(packet.networkCount);
        buf.writeFloat(packet.engineConsumption);
    }

    public static S2CStressArkDataPacket decode(FriendlyByteBuf buf) {
        return new S2CStressArkDataPacket(buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readFloat());
    }

    public static void handle(S2CStressArkDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (packet.totalCapacity < 0) {
                LOGGER.info("[ARK-dx] CLIENT << CLEAR");
                StressArkHUD.clearData();
            } else {
                LOGGER.info("[ARK-dx] CLIENT << DATA: cap={} stress={}", packet.totalCapacity, packet.totalStress);
                StressArkHUD.onData(packet.totalCapacity, packet.totalStress, packet.networkCount, packet.engineConsumption);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
