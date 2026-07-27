package com.goetymech.addon.common.net;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.net.c2s.C2SOpenFleshCasketPacket;
import com.goetymech.addon.common.net.c2s.C2SCancelSlamPacket;
import com.goetymech.addon.common.net.c2s.C2SChasePacket;
import com.goetymech.addon.common.net.c2s.C2SJumpChargePacket;
import com.goetymech.addon.common.net.c2s.C2STimeStopPacket;
import com.goetymech.addon.common.net.c2s.C2SFreecamEndPacket;
import com.goetymech.addon.common.net.c2s.C2SWallInputPacket;
import com.goetymech.addon.common.net.c2s.C2SWallJumpOffPacket;
import com.goetymech.addon.common.net.c2s.C2SFlightInputPacket;
import com.goetymech.addon.common.net.s2c.S2CTimeStopDimensionPacket;
import com.goetymech.addon.common.net.s2c.S2CTimeStopStatePacket;
import com.goetymech.addon.common.net.s2c.S2CSlamImpactPacket;
import com.goetymech.addon.common.net.s2c.S2CSlamStatePacket;
import com.goetymech.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetymech.addon.common.net.s2c.S2CAssimilationDataPacket;
import com.goetymech.addon.common.net.s2c.S2CSetFreecamPacket;
import com.goetymech.addon.common.net.s2c.S2CStressShieldUpdatePacket;
import com.goetymech.addon.common.net.s2c.S2CSoulDominionSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 模组网络通道。注册所有 C2S/S2C 包。
 */
public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GoetyMechMod.MOD_ID, "network"),
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
        CHANNEL.registerMessage(idx++, C2SWallInputPacket.class,
                C2SWallInputPacket::encode,
                C2SWallInputPacket::decode,
                C2SWallInputPacket::handle);
        CHANNEL.registerMessage(idx++, C2SFlightInputPacket.class,
                C2SFlightInputPacket::encode,
                C2SFlightInputPacket::decode,
                C2SFlightInputPacket::handle);
        CHANNEL.registerMessage(idx++, S2CStressShieldUpdatePacket.class,
                S2CStressShieldUpdatePacket::encode,
                S2CStressShieldUpdatePacket::decode,
                S2CStressShieldUpdatePacket::handle);
        CHANNEL.registerMessage(idx++, S2CAssimilationDataPacket.class,
                S2CAssimilationDataPacket::encode,
                S2CAssimilationDataPacket::decode,
                S2CAssimilationDataPacket::handle);
        CHANNEL.registerMessage(idx++, C2SOpenFleshCasketPacket.class,
                C2SOpenFleshCasketPacket::encode,
                C2SOpenFleshCasketPacket::decode,
                C2SOpenFleshCasketPacket::handle);
        CHANNEL.registerMessage(idx++, C2SChasePacket.class,
                C2SChasePacket::encode,
                C2SChasePacket::decode,
                C2SChasePacket::handle);
        CHANNEL.registerMessage(idx++, C2SFreecamEndPacket.class,
                C2SFreecamEndPacket::encode,
                C2SFreecamEndPacket::decode,
                C2SFreecamEndPacket::handle);
        CHANNEL.registerMessage(idx++, S2CSetFreecamPacket.class,
                S2CSetFreecamPacket::encode,
                S2CSetFreecamPacket::decode,
                S2CSetFreecamPacket::handle);
        CHANNEL.registerMessage(idx++, S2CSoulDominionSyncPacket.class,
                S2CSoulDominionSyncPacket::encode,
                S2CSoulDominionSyncPacket::decode,
                S2CSoulDominionSyncPacket::handle);
    }

    public static <MSG> void sendTo(ServerPlayer player, MSG msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
