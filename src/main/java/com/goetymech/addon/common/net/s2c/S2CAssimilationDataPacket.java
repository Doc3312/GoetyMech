package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.common.item.PrototypeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CAssimilationDataPacket {

    private final int kills;

    public S2CAssimilationDataPacket(int kills) {
        this.kills = kills;
    }

    public static void encode(S2CAssimilationDataPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.kills);
    }

    public static S2CAssimilationDataPacket decode(FriendlyByteBuf buf) {
        return new S2CAssimilationDataPacket(buf.readInt());
    }

    public static void handle(S2CAssimilationDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PrototypeItem.setAssimilationKills(player, packet.kills);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public int getKills() { return kills; }
}
