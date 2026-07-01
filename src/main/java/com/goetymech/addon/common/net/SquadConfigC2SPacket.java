package com.goetymech.addon.common.net;

import com.goetymech.addon.common.block.entity.SquadBarracksBlockEntity;
import com.goetymech.addon.common.item.SquadFocus;
import com.goetymech.addon.common.menu.SquadBarracksMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SquadConfigC2SPacket {

    private static final Map<UUID, Long> lastPacketTick = new ConcurrentHashMap<>();

    private final int containerId;
    private final int slotIndex;
    private final String entityTypeId;
    private final boolean clear;

    public SquadConfigC2SPacket(int containerId, int slotIndex, String entityTypeId) {
        this.containerId = containerId;
        this.slotIndex = slotIndex;
        this.entityTypeId = entityTypeId != null ? entityTypeId : "";
        this.clear = entityTypeId == null || entityTypeId.isEmpty();
    }

    public static void encode(SquadConfigC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.containerId);
        buf.writeInt(packet.slotIndex);
        buf.writeUtf(packet.entityTypeId);
    }

    public static SquadConfigC2SPacket decode(FriendlyByteBuf buf) {
        int containerId = buf.readInt();
        int slotIndex = buf.readInt();
        String entityTypeId = buf.readUtf();
        return new SquadConfigC2SPacket(containerId, slotIndex, entityTypeId);
    }

    public static void handle(SquadConfigC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            long now = player.serverLevel().getGameTime();
            Long last = lastPacketTick.get(player.getUUID());
            if (last != null && now - last < 5) return;
            lastPacketTick.put(player.getUUID(), now);
            if (player.containerMenu instanceof SquadBarracksMenu menu && menu.containerId == packet.containerId) {
                SquadBarracksBlockEntity barracks = menu.getBarracks();
                if (barracks.getOwnerUUID() == null || !barracks.isOwner(player)) return;
            if (packet.clear) {
                barracks.clearConfigSlot(packet.slotIndex);
            } else {
                ResourceLocation rl = ResourceLocation.tryParse(packet.entityTypeId);
                if (rl != null) {
                    barracks.setConfigSlot(packet.slotIndex, rl);
                }
            }
            if (barracks.getLevel() instanceof ServerLevel serverLevel) {
                SquadFocus.refreshBoundFoci(serverLevel, barracks.getBlockPos(), barracks);
            }
        }
        });
        ctx.get().setPacketHandled(true);
    }
}
