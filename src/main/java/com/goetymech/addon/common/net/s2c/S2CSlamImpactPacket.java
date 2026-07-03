package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.SlamGroundTiltHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSlamImpactPacket {

    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final float damage;

    public S2CSlamImpactPacket(double x, double y, double z, float radius, float damage) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.damage = damage;
    }

    public static void encode(S2CSlamImpactPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.radius);
        buf.writeFloat(packet.damage);
    }

    public static S2CSlamImpactPacket decode(FriendlyByteBuf buf) {
        return new S2CSlamImpactPacket(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(S2CSlamImpactPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level level = net.minecraft.client.Minecraft.getInstance().level;
            if (level == null) return;

            Vec3 pos = new Vec3(packet.x, packet.y, packet.z);

            SlamGroundTiltHandler.startEffect(pos);

            // 播放冲击音效
            level.playLocalSound(
                    packet.x, packet.y, packet.z,
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    2.0F, 0.8F + level.random.nextFloat() * 0.4F, false);

            // 雾化粒子：大量 block 破裂粒子，密度随半径动态调整
            int particleCount = Math.max(30, (int)(packet.radius * 8));
            for (int i = 0; i < particleCount; i++) {
                double ox = (level.random.nextDouble() - 0.5) * packet.radius * 2;
                double oz = (level.random.nextDouble() - 0.5) * packet.radius * 2;
                if (Math.sqrt(ox * ox + oz * oz) > packet.radius) continue;

                BlockPos bp = BlockPos.containing(packet.x + ox, packet.y - 0.5, packet.z + oz);
                var state = level.getBlockState(bp);
                if (state.isAir()) continue;

                level.addParticle(
                        new net.minecraft.core.particles.BlockParticleOption(
                                net.minecraft.core.particles.ParticleTypes.BLOCK, state),
                        bp.getX() + 0.5, bp.getY() + 1.0, bp.getZ() + 0.5,
                        (level.random.nextDouble() - 0.5) * 2,
                        level.random.nextDouble() * 1.5,
                        (level.random.nextDouble() - 0.5) * 2);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getRadius() { return radius; }
    public float getDamage() { return damage; }
}
