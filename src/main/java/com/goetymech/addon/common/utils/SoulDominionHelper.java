package com.goetymech.addon.common.utils;

import com.goetymech.addon.common.capabilities.ISoulDominion;
import com.goetymech.addon.common.capabilities.SoulDominionProvider;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CSoulDominionSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class SoulDominionHelper {

    public static ISoulDominion getDominion(LivingEntity entity) {
        return entity.getCapability(SoulDominionProvider.CAPABILITY).orElse(null);
    }

    public static void sendSyncPacket(ServerPlayer player) {
        ISoulDominion dom = getDominion(player);
        if (dom != null) {
            ModNetwork.sendTo(player, new S2CSoulDominionSyncPacket(
                    player.getId(), dom.isActive(), dom.getExpireTick(),
                    dom.getLinkedServants(), dom.getRange(), dom.getDuration()));
        }
    }
}
