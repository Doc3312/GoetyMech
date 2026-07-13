package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.capabilities.IStressShield;
import com.goetymech.addon.common.utils.StressShieldHelper;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StressShieldEvents {

    private static final long NO_HURT_TICKS = 100;
    private static final float RECOVERY_RATE = 0.01f;

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.isCanceled()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        IStressShield shield = StressShieldHelper.getShield(player);
        if (shield == null || !shield.isActive()) return;

        long gameTime = player.level().getGameTime();
        if (gameTime - shield.getLastHurtTick() < 5) {
            GoetyMechMod.LOGGER.info("DBG STRESS: onAttack SKIP {} tick={} cooldownGuard shield={}",
                    player.getScoreboardName(), gameTime, shield.getShield());
            event.setCanceled(true);
            return;
        }

        if (gameTime > shield.getExpireTick()) {
            GoetyMechMod.LOGGER.info("DBG STRESS: onAttack EXPIRE {} tick={}",
                    player.getScoreboardName(), gameTime);
            shield.clear();
            return;
        }

        float damage = event.getAmount();
        float currentShield = shield.getShield();
        ServerLevel serverLevel = (ServerLevel) player.level();

        if (damage <= currentShield) {
            shield.setShield(currentShield - damage);
            shield.setLastHurtTick(gameTime);

            GoetyMechMod.LOGGER.info("DBG STRESS: onAttack ABSORB {} tick={} dmg={} shield={}->{} src={}",
                    player.getScoreboardName(), gameTime, damage, currentShield, shield.getShield(),
                    event.getSource().getMsgId());

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SHIELD_DEBRIS_IMPACT.get(), SoundSource.PLAYERS,
                    1.0F, player.getRandom().nextFloat() * 0.2F + 0.9F);
            ServerParticleUtil.addParticlesAroundMiddleSelf(serverLevel, ParticleTypes.CRIT, player);
            serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1, player.getZ(),
                    8, 0.5, 0.5, 0.5, 0.1);
        } else {
            float overshoot = damage - currentShield;
            shield.clear();

            GoetyMechMod.LOGGER.info("DBG STRESS: onAttack BREAK {} tick={} dmg={} shield={} overshoot={} src={}",
                    player.getScoreboardName(), gameTime, damage, currentShield, overshoot,
                    event.getSource().getMsgId());

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.WILDFIRE_SHIELD_BREAK.get(), SoundSource.PLAYERS,
                    1.2F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY() + 1, player.getZ(),
                    1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX(), player.getY() + 1, player.getZ(),
                    20, 1.5, 1.5, 1.5, 0.3);

            float aoeDamage = Math.max(overshoot, 3.0f);
            AABB aabb = player.getBoundingBox().inflate(3);
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, aabb,
                    e -> e != player && !e.isAlliedTo(player))) {
                target.hurt(serverLevel.damageSources().playerAttack(player), aoeDamage);
                target.knockback(1.5, target.getX() - player.getX(),
                        target.getZ() - player.getZ());
            }
        }

        if (player instanceof ServerPlayer sp) {
            StressShieldHelper.sendUpdatePacket(sp);
        }

        event.setCanceled(true);
        GoetyMechMod.LOGGER.info("DBG STRESS: onAttack CANCEL {} tick={}",
                player.getScoreboardName(), gameTime);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.isCanceled()) return;
        if (event.getAmount() <= 0) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        IStressShield shield = StressShieldHelper.getShield(player);
        if (shield == null || !shield.isActive()) return;

        long gameTime = player.level().getGameTime();
        GoetyMechMod.LOGGER.info("DBG STRESS: onHurt FALLBACK {} tick={} amount={} shield={} src={}",
                player.getScoreboardName(), gameTime, event.getAmount(), shield.getShield(),
                event.getSource().getMsgId());

        event.setCanceled(true);
        event.setAmount(0);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        IStressShield shield = StressShieldHelper.getShield(player);
        if (shield == null || !shield.isActive()) return;

        long gameTime = player.level().getGameTime();
        if (gameTime > shield.getExpireTick()) {
            GoetyMechMod.LOGGER.info("DBG STRESS: recover EXPIRE {} tick={}",
                    player.getScoreboardName(), gameTime);
            shield.clear();
            StressShieldHelper.sendUpdatePacket(player);
            return;
        }

        if (player.tickCount % 10 == 0) {
            ServerParticleUtil.addAuraParticles((ServerLevel) player.level(),
                    ParticleTypes.END_ROD, player, 1.5f);
        }

        if (gameTime - shield.getLastHurtTick() < NO_HURT_TICKS) return;

        float current = shield.getShield();
        float max = shield.getMaxShield();
        if (current >= max) return;

        float recovery = max * RECOVERY_RATE;
        shield.setShield(Math.min(current + recovery, max));

        GoetyMechMod.LOGGER.info("DBG STRESS: recover TICK {} tick={} lastHurt={} shield={}->{} max={} recovery={}",
                player.getScoreboardName(), gameTime, shield.getLastHurtTick(),
                current, shield.getShield(), max, recovery);

        if (gameTime - shield.getLastHurtTick() == NO_HURT_TICKS) {
            player.playSound(ModSounds.SHIELD_UP.get(), 1.0F, 1.0F);
        }

        StressShieldHelper.sendUpdatePacket(player);
    }
}
