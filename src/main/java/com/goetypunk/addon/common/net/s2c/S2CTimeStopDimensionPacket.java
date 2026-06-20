package com.goetypunk.addon.common.net.s2c;

import com.goetypunk.addon.client.ClientLevelExpandedContext;
import com.goetypunk.addon.common.LevelEC;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

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
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            ClientLevelExpandedContext ctx2 = (ClientLevelExpandedContext)
                    ((LevelEC) mc.level).gp$levelECData();
            if (!packet.toAddDimensionID.getPath().isEmpty()) {
                ResourceKey<Level> toAdd = ResourceKey.create(Registries.DIMENSION, packet.toAddDimensionID);
                if (mc.level.dimension() == toAdd) {
                    ctx2.currentTimeStopDimension = toAdd;
                }
            }
            if (!packet.toRemoveDimensionID.getPath().isEmpty()) {
                ResourceKey<Level> toRemove = ResourceKey.create(Registries.DIMENSION, packet.toRemoveDimensionID);
                if (mc.level.dimension() == toRemove) {
                    ctx2.currentTimeStopDimension = null;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
