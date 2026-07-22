package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.common.capabilities.StressShieldProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 护盾更新 S2C 包。同步护盾值。
 */
public class S2CStressShieldUpdatePacket {
    private final int entityId;
    private final float shield;
    private final float maxShield;
    private final long expireTick;
    private final long lastHurtTick;

    public S2CStressShieldUpdatePacket(int entityId, float shield, float maxShield,
                                       long expireTick, long lastHurtTick) {
        this.entityId = entityId;
        this.shield = shield;
        this.maxShield = maxShield;
        this.expireTick = expireTick;
        this.lastHurtTick = lastHurtTick;
    }

    public static void encode(S2CStressShieldUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeFloat(packet.shield);
        buf.writeFloat(packet.maxShield);
        buf.writeLong(packet.expireTick);
        buf.writeLong(packet.lastHurtTick);
    }

    public static S2CStressShieldUpdatePacket decode(FriendlyByteBuf buf) {
        return new S2CStressShieldUpdatePacket(
                buf.readVarInt(), buf.readFloat(), buf.readFloat(),
                buf.readLong(), buf.readLong());
    }

    public static void handle(S2CStressShieldUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            var entity = level.getEntity(packet.entityId);
            if (entity == null) return;
            entity.getCapability(StressShieldProvider.CAPABILITY).ifPresent(cap -> {
                cap.setShield(packet.shield);
                cap.setMaxShield(packet.maxShield);
                cap.setExpireTick(packet.expireTick);
                cap.setLastHurtTick(packet.lastHurtTick);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
