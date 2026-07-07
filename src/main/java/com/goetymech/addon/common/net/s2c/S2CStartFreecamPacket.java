package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.FreecamHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CStartFreecamPacket {
    private final double cameraX;
    private final double cameraY;
    private final double cameraZ;
    private final double bodyX;
    private final double bodyY;
    private final double bodyZ;
    private final float yaw;
    private final float pitch;

    public S2CStartFreecamPacket(Vec3 cameraPos, Vec3 bodyPos, float yaw, float pitch) {
        this.cameraX = cameraPos.x;
        this.cameraY = cameraPos.y;
        this.cameraZ = cameraPos.z;
        this.bodyX = bodyPos.x;
        this.bodyY = bodyPos.y;
        this.bodyZ = bodyPos.z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(S2CStartFreecamPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.cameraX);
        buf.writeDouble(packet.cameraY);
        buf.writeDouble(packet.cameraZ);
        buf.writeDouble(packet.bodyX);
        buf.writeDouble(packet.bodyY);
        buf.writeDouble(packet.bodyZ);
        buf.writeFloat(packet.yaw);
        buf.writeFloat(packet.pitch);
    }

    public static S2CStartFreecamPacket decode(FriendlyByteBuf buf) {
        return new S2CStartFreecamPacket(
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(S2CStartFreecamPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            FreecamHandler handler = FreecamHandler.getInstance();
            handler.activate(
                    new Vec3(packet.cameraX, packet.cameraY, packet.cameraZ),
                    new Vec3(packet.bodyX, packet.bodyY, packet.bodyZ),
                    packet.yaw, packet.pitch
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
