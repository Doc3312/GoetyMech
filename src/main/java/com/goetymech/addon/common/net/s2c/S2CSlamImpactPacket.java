package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.net.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Slam 冲击 S2C 包。同步冲击位置和半径。
 */
public class S2CSlamImpactPacket {

    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final float damage;
    private final float groundDeformRadius;

    public S2CSlamImpactPacket(double x, double y, double z, float radius, float damage, float groundDeformRadius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.damage = damage;
        this.groundDeformRadius = groundDeformRadius;
    }

    public static void encode(S2CSlamImpactPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.radius);
        buf.writeFloat(packet.damage);
        buf.writeFloat(packet.groundDeformRadius);
    }

    public static S2CSlamImpactPacket decode(FriendlyByteBuf buf) {
        return new S2CSlamImpactPacket(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(S2CSlamImpactPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleSlamImpact(packet)));
        ctx.get().setPacketHandled(true);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getRadius() { return radius; }
    public float getDamage() { return damage; }
    public float getGroundDeformRadius() { return groundDeformRadius; }
}
