package com.goetypunk.addon.common.utils;

import com.goetypunk.addon.common.capabilities.IStressShield;
import com.goetypunk.addon.common.capabilities.StressShieldProvider;
import com.goetypunk.addon.common.net.ModNetwork;
import com.goetypunk.addon.common.net.s2c.S2CStressShieldUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class StressShieldHelper {

    public static IStressShield getShield(LivingEntity entity) {
        return entity.getCapability(StressShieldProvider.CAPABILITY).orElse(null);
    }

    public static void sendUpdatePacket(ServerPlayer player) {
        IStressShield shield = getShield(player);
        if (shield != null) {
            ModNetwork.sendTo(player, new S2CStressShieldUpdatePacket(
                    player.getId(), shield.getShield(), shield.getMaxShield(),
                    shield.getExpireTick(), shield.getLastHurtTick()));
        }
    }
}
