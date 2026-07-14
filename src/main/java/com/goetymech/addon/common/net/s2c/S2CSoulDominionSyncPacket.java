package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.common.capabilities.SoulDominionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

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
        ctx.get().enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            var entity = level.getEntity(packet.entityId);
            if (entity == null) return;
            entity.getCapability(SoulDominionProvider.CAPABILITY).ifPresent(cap -> {
                cap.setActive(packet.active);
                cap.setExpireTick(packet.expireTick);
                cap.setLinkedServants(packet.linkedServants);
                cap.setRange(packet.range);
                cap.setDuration(packet.duration);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
