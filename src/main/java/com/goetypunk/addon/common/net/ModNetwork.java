package com.goetypunk.addon.common.net;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.net.c2s.C2SCancelSlamPacket;
import com.goetypunk.addon.common.net.c2s.C2SJumpChargePacket;
import com.goetypunk.addon.common.net.c2s.C2STimeStopPacket;
import com.goetypunk.addon.common.net.c2s.C2SWallJumpOffPacket;
import com.goetypunk.addon.common.net.c2s.C2SFlightInputPacket;
import com.goetypunk.addon.common.net.s2c.S2CTimeStopDimensionPacket;
import com.goetypunk.addon.common.net.s2c.S2CTimeStopStatePacket;
import com.goetypunk.addon.common.net.s2c.S2CSlamImpactPacket;
import com.goetypunk.addon.common.net.s2c.S2CSlamStatePacket;
import com.goetypunk.addon.common.net.s2c.S2CStressArkDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GoetyPunkMod.MOD_ID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int idx = 0;

    public static void init() {
        CHANNEL.registerMessage(idx++, SquadConfigC2SPacket.class,
                SquadConfigC2SPacket::encode,
                SquadConfigC2SPacket::decode,
                SquadConfigC2SPacket::handle);
        CHANNEL.registerMessage(idx++, C2STimeStopPacket.class,
                C2STimeStopPacket::encode,
                C2STimeStopPacket::decode,
                C2STimeStopPacket::handle);
        CHANNEL.registerMessage(idx++, S2CTimeStopStatePacket.class,
                S2CTimeStopStatePacket::encode,
                S2CTimeStopStatePacket::decode,
                S2CTimeStopStatePacket::handle);
        CHANNEL.registerMessage(idx++, S2CTimeStopDimensionPacket.class,
                S2CTimeStopDimensionPacket::encode,
                S2CTimeStopDimensionPacket::decode,
                S2CTimeStopDimensionPacket::handle);
        CHANNEL.registerMessage(idx++, S2CSlamImpactPacket.class,
                S2CSlamImpactPacket::encode,
                S2CSlamImpactPacket::decode,
                S2CSlamImpactPacket::handle);
        CHANNEL.registerMessage(idx++, S2CSlamStatePacket.class,
                S2CSlamStatePacket::encode,
                S2CSlamStatePacket::decode,
                S2CSlamStatePacket::handle);
        CHANNEL.registerMessage(idx++, C2SCancelSlamPacket.class,
                C2SCancelSlamPacket::encode,
                C2SCancelSlamPacket::decode,
                C2SCancelSlamPacket::handle);
        CHANNEL.registerMessage(idx++, C2SJumpChargePacket.class,
                C2SJumpChargePacket::encode,
                C2SJumpChargePacket::decode,
                C2SJumpChargePacket::handle);
        CHANNEL.registerMessage(idx++, C2SWallJumpOffPacket.class,
                C2SWallJumpOffPacket::encode,
                C2SWallJumpOffPacket::decode,
                C2SWallJumpOffPacket::handle);
        CHANNEL.registerMessage(idx++, S2CStressArkDataPacket.class,
                S2CStressArkDataPacket::encode,
                S2CStressArkDataPacket::decode,
                S2CStressArkDataPacket::handle);
        CHANNEL.registerMessage(idx++, C2SFlightInputPacket.class,
                C2SFlightInputPacket::encode,
                C2SFlightInputPacket::decode,
                C2SFlightInputPacket::handle);
    }

    public static <MSG> void sendTo(ServerPlayer player, MSG msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
