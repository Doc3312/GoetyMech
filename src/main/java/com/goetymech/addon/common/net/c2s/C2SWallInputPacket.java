package com.goetymech.addon.common.net.c2s;

import com.goetymech.addon.common.events.PrototypeEvents;
import com.goetymech.addon.common.item.PrototypeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SWallInputPacket {
    private final float forward;
    private final float strafe;
    private final boolean wallRunning;
    private final double wallNormalX;
    private final double wallNormalZ;

    public C2SWallInputPacket(float forward, float strafe, boolean wallRunning, double wallNormalX, double wallNormalZ) {
        this.forward = forward;
        this.strafe = strafe;
        this.wallRunning = wallRunning;
        this.wallNormalX = wallNormalX;
        this.wallNormalZ = wallNormalZ;
    }

    public static void encode(C2SWallInputPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.forward);
        buf.writeFloat(packet.strafe);
        buf.writeBoolean(packet.wallRunning);
        buf.writeDouble(packet.wallNormalX);
        buf.writeDouble(packet.wallNormalZ);
    }

    public static C2SWallInputPacket decode(FriendlyByteBuf buf) {
        return new C2SWallInputPacket(buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(C2SWallInputPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            PrototypeItem.setWallForward(player, packet.forward);
            PrototypeItem.setWallStrafe(player, packet.strafe);
            if (packet.wallRunning) {
                if (!PrototypeItem.isWallRunning(player)) {
                    PrototypeItem.setWallRunning(player, true);
                    PrototypeItem.setWallRunTicks(player, 0);
                }
                PrototypeItem.setWallNormal(player, new Vec3(packet.wallNormalX, 0, packet.wallNormalZ));
            } else {
                if (PrototypeItem.isWallRunning(player)) {
                    PrototypeEvents.stopWallRunFromClient(player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
