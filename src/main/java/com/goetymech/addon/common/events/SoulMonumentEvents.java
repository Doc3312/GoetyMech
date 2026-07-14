package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.block.entity.SoulMonumentBlockEntity;
import com.Polarice3.Goety.common.items.magic.TaglockKit;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulMonumentEvents {

    @SubscribeEvent
    public static void onVillagerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        Level level = villager.level();
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos villagerPos = villager.blockPosition();
        int range = SoulMonumentBlockEntity.DETECTION_RANGE;

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos checkPos = villagerPos.offset(dx, dy, dz);
                    if (level.getBlockEntity(checkPos) instanceof SoulMonumentBlockEntity monument) {
                        if (monument.isRecording()) {
                            monument.onVillagerDeath(serverLevel, villagerPos);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMonumentServantHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentData().getBoolean("goetymech.monument_summoned")) return;
        if (entity.level().isClientSide) return;
        if (entity.getHealth() - event.getAmount() > 0) return;
        spawnMonumentDeathParticles((ServerLevel) entity.level(), entity);
        event.setCanceled(true);
        entity.discard();
    }

    @SubscribeEvent
    public static void onMonumentServantDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().getBoolean("goetymech.monument_summoned")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMonumentServantDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentData().getBoolean("goetymech.monument_summoned")) return;
        if (entity.level().isClientSide) return;
        spawnMonumentDeathParticles((ServerLevel) entity.level(), entity);
    }

    public static void spawnMonumentDeathParticles(ServerLevel level, Entity entity) {
        double px = entity.getX();
        double py = entity.getY() + entity.getBbHeight() / 4.0F;
        double pz = entity.getZ();
        ServerParticleUtil.surroundCloudParticles(ParticleTypes.PORTAL, px, py, pz, level, 40, 0.15F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, entity.getY() + entity.getBbHeight() / 2.0, pz, 12, 0.4, 0.4, 0.4, 0.2);
        level.playSound(null, px, entity.getY(), pz, SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 2.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide) return;

        ItemStack held = event.getItemStack();
        if (!(held.getItem() instanceof TaglockKit)) return;

        if (!TaglockKit.hasEntity(held)) return;

        var blockEntity = level.getBlockEntity(event.getPos());
        if (!(blockEntity instanceof SoulMonumentBlockEntity monument)) return;

        if (monument.isRecording()) {
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.already_recording"), true);
            }
            return;
        }

        if (monument.isFull()) {
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.full"), true);
            }
            return;
        }

        if (!held.hasTag() || !held.getTag().contains(TaglockKit.TAG_ENTITY)) return;
        java.util.UUID targetUUID = held.getTag().getUUID(TaglockKit.TAG_ENTITY);

        if (monument.tryStartRecording(player, targetUUID)) {
            TaglockKit.removeEntity(held);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.recording_started"), true);
            }
            event.setCanceled(true);
        }
    }
}
