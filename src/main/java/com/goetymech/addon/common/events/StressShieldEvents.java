package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.capabilities.IStressShield;
import com.goetymech.addon.common.utils.StressShieldHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StressShieldEvents {

    private static final long NO_HURT_TICKS = 100;
    private static final float RECOVERY_RATE = 0.01f;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        IStressShield shield = StressShieldHelper.getShield(player);
        if (shield == null || !shield.isActive()) return;

        long gameTime = player.level().getGameTime();
        if (gameTime > shield.getExpireTick()) {
            shield.clear();
            return;
        }

        float damage = event.getAmount();
        float currentShield = shield.getShield();

        if (damage <= currentShield) {
            shield.setShield(currentShield - damage);
            shield.setLastHurtTick(gameTime);
            event.setAmount(0);
        } else {
            float overshoot = damage - currentShield;
            event.setAmount(0);
            shield.clear();

            float aoeDamage = Math.max(overshoot, 3.0f);
            AABB aabb = player.getBoundingBox().inflate(3);
            ServerLevel level = (ServerLevel) player.level();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, aabb,
                    e -> e != player && !e.isAlliedTo(player))) {
                target.hurt(level.damageSources().playerAttack(player), aoeDamage);
                target.knockback(1.5, target.getX() - player.getX(),
                        target.getZ() - player.getZ());
            }
        }

        if (player instanceof ServerPlayer sp) {
            StressShieldHelper.sendUpdatePacket(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        IStressShield shield = StressShieldHelper.getShield(player);
        if (shield == null || !shield.isActive()) return;

        long gameTime = player.level().getGameTime();
        if (gameTime > shield.getExpireTick()) {
            shield.clear();
            StressShieldHelper.sendUpdatePacket(player);
            return;
        }

        if (gameTime - shield.getLastHurtTick() < NO_HURT_TICKS) return;

        float current = shield.getShield();
        float max = shield.getMaxShield();
        if (current >= max) return;

        float recovery = max * RECOVERY_RATE;
        shield.setShield(Math.min(current + recovery, max));
        StressShieldHelper.sendUpdatePacket(player);
    }
}
