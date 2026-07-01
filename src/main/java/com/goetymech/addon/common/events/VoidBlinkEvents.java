package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.magic.effect.VoidMarkEffect;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.c2s.C2SVoidBlinkPacket;
import com.goetymech.addon.init.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VoidBlinkEvents {

    @SubscribeEvent
    public static void onLivingTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (!player.hasEffect(ModEffects.VOID_MARK.get())) return;

        MobEffectInstance effect = player.getEffect(ModEffects.VOID_MARK.get());
        if (effect == null) return;

        var data = player.getPersistentData();
        long windowEnd = data.getLong(VoidMarkEffect.TAG_WINDOW_END);
        if (player.level().getGameTime() >= windowEnd) {
            if (player.level() instanceof ServerLevel serverLevel) {
                Entity target = serverLevel.getEntity(data.getUUID(VoidMarkEffect.TAG_TARGET));
                if (target instanceof LivingEntity lt) {
                    lt.removeEffect(MobEffects.GLOWING);
                }
            }
            data.remove(VoidMarkEffect.TAG_TARGET);
            data.remove(VoidMarkEffect.TAG_REMAINING);
            data.remove(VoidMarkEffect.TAG_WINDOW_END);
            player.removeEffect(ModEffects.VOID_MARK.get());
        }
    }

    @SubscribeEvent
    public static void onTargetDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        for (ServerPlayer player : event.getEntity().getServer().getPlayerList().getPlayers()) {
            if (!player.hasEffect(ModEffects.VOID_MARK.get())) continue;
            var data = player.getPersistentData();
            if (data.hasUUID(VoidMarkEffect.TAG_TARGET)
                    && data.getUUID(VoidMarkEffect.TAG_TARGET).equals(event.getEntity().getUUID())) {
                data.remove(VoidMarkEffect.TAG_TARGET);
                data.remove(VoidMarkEffect.TAG_REMAINING);
                data.remove(VoidMarkEffect.TAG_WINDOW_END);
                player.removeEffect(ModEffects.VOID_MARK.get());
            }
        }
    }

    @Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {

        private static boolean wasShiftDown = false;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (Minecraft.getInstance().player == null) return;
            if (!Minecraft.getInstance().player.hasEffect(ModEffects.VOID_MARK.get())) {
                wasShiftDown = Minecraft.getInstance().options.keyShift.isDown();
                return;
            }

            boolean isNowDown = Minecraft.getInstance().options.keyShift.isDown();
            if (isNowDown && !wasShiftDown) {
                ModNetwork.CHANNEL.sendToServer(new C2SVoidBlinkPacket());
            }
            wasShiftDown = isNowDown;
        }
    }
}
