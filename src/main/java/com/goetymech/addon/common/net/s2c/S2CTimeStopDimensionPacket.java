package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.net.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * 时停维度 S2C 包。广播冻结维度。
 */
public class S2CTimeStopDimensionPacket {
    private final ResourceLocation toRemoveDimensionID;
    private final ResourceLocation toAddDimensionID;

    public S2CTimeStopDimensionPacket(ResourceLocation toRemove, ResourceLocation toAdd) {
        this.toRemoveDimensionID = toRemove;
        this.toAddDimensionID = toAdd;
    }

    public static S2CTimeStopDimensionPacket decode(FriendlyByteBuf buf) {
        return new S2CTimeStopDimensionPacket(buf.readResourceLocation(), buf.readResourceLocation());
    }

    public static void encode(S2CTimeStopDimensionPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.toRemoveDimensionID);
        buf.writeResourceLocation(packet.toAddDimensionID);
    }

    public static void handle(S2CTimeStopDimensionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleTimeStopDimension(packet)));
        ctx.get().setPacketHandled(true);
    }

    public ResourceLocation getToRemoveDimensionID() {
        return toRemoveDimensionID;
    }

    public ResourceLocation getToAddDimensionID() {
        return toAddDimensionID;
    }
}
