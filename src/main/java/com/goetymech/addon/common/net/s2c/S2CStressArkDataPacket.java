package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.net.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * 应力方舟数据 S2C 包。同步容量/应力。
 */
public class S2CStressArkDataPacket {
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
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleStressArkData(packet)));
        ctx.get().setPacketHandled(true);
    }

    public float getTotalCapacity() {
        return totalCapacity;
    }

    public float getTotalStress() {
        return totalStress;
    }

    public int getNetworkCount() {
        return networkCount;
    }

    public float getEngineConsumption() {
        return engineConsumption;
    }
}
