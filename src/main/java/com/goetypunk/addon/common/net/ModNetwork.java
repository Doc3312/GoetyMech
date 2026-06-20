package com.goetypunk.addon.common.net;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.net.c2s.C2STimeStopPacket;
import com.goetypunk.addon.common.net.s2c.S2CTimeStopDimensionPacket;
import com.goetypunk.addon.common.net.s2c.S2CTimeStopStatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
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
    }
}
