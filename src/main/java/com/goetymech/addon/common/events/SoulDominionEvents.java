package com.goetymech.addon.common.events;

import com.Polarice3.Goety.api.entities.IOwned;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.capabilities.ISoulDominion;
import com.goetymech.addon.common.utils.SoulDominionHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulDominionEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (event.getAmount() <= 0) return;

        ISoulDominion dom = SoulDominionHelper.getDominion(player);
        if (dom == null || !dom.isActive()) return;

        long gameTime = player.level().getGameTime();
        if (gameTime > dom.getExpireTick()) {
            dom.clear();
            if (player instanceof ServerPlayer sp) {
                SoulDominionHelper.sendSyncPacket(sp);
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) player.level();
        List<LivingEntity> aliveServants = new ArrayList<>();
        for (UUID uuid : dom.getLinkedServants()) {
            if (serverLevel.getEntity(uuid) instanceof LivingEntity living && living.isAlive()) {
                aliveServants.add(living);
            }
        }

        if (aliveServants.isEmpty()) {
            dom.clear();
            if (player instanceof ServerPlayer sp) {
                SoulDominionHelper.sendSyncPacket(sp);
            }
            return;
        }

        float damagePerServant = event.getAmount() / aliveServants.size();
        for (LivingEntity servant : aliveServants) {
            servant.hurt(event.getSource(), damagePerServant);
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        ISoulDominion dom = SoulDominionHelper.getDominion(player);
        if (dom == null || !dom.isActive()) return;

        long gameTime = player.level().getGameTime();
        if (gameTime > dom.getExpireTick()) {
            dom.clear();
            SoulDominionHelper.sendSyncPacket(player);
            return;
        }

        if (player.tickCount % 20 == 0) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            int range = dom.getRange();

            List<LivingEntity> currentServants = serverLevel.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(range),
                    e -> e != player && e.isAlive() && e instanceof IOwned owned
                            && owned.getTrueOwner() == player);

            if (currentServants.isEmpty()) {
                dom.clear();
                SoulDominionHelper.sendSyncPacket(player);

                return;
            }

            List<UUID> newServants = currentServants.stream()
                    .map(LivingEntity::getUUID)
                    .collect(Collectors.toList());

            if (!newServants.equals(dom.getLinkedServants())) {
                dom.setLinkedServants(newServants);
                SoulDominionHelper.sendSyncPacket(player);
            }
        }
    }
}
