package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.net.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 灵魂统御同步 S2C 包。
 */
public class S2CSoulDominionSyncPacket {
    private final int entityId;
    private final boolean active;
    private final long expireTick;
    private final List<UUID> linkedServants;
    private final int range;
    private final int duration;

    public S2CSoulDominionSyncPacket(int entityId, boolean active, long expireTick,
                                     List<UUID> linkedServants, int range, int duration) {
        this.entityId = entityId;
        this.active = active;
        this.expireTick = expireTick;
        this.linkedServants = linkedServants;
        this.range = range;
        this.duration = duration;
    }

    public static void encode(S2CSoulDominionSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeBoolean(packet.active);
        buf.writeLong(packet.expireTick);
        buf.writeVarInt(packet.linkedServants.size());
        for (UUID uuid : packet.linkedServants) {
            buf.writeUUID(uuid);
        }
        buf.writeVarInt(packet.range);
        buf.writeVarInt(packet.duration);
    }

    public static S2CSoulDominionSyncPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        boolean active = buf.readBoolean();
        long expireTick = buf.readLong();
        int count = buf.readVarInt();
        List<UUID> servants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            servants.add(buf.readUUID());
        }
        int range = buf.readVarInt();
        int duration = buf.readVarInt();
        return new S2CSoulDominionSyncPacket(entityId, active, expireTick, servants, range, duration);
    }

    public static void handle(S2CSoulDominionSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleSoulDominionSync(packet)));
        ctx.get().setPacketHandled(true);
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean isActive() {
        return active;
    }

    public long getExpireTick() {
        return expireTick;
    }

    public List<UUID> getLinkedServants() {
        return linkedServants;
    }

    public int getRange() {
        return range;
    }

    public int getDuration() {
        return duration;
    }
}
