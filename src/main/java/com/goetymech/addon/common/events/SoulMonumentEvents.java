package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.block.entity.SoulMonumentBlockEntity;
import com.goetymech.addon.init.ModParticleTypes;
import com.Polarice3.Goety.api.entities.IOwned;
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
import net.minecraft.world.entity.Mob;
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
/**
 * 魂灵丰碑事件。TaglockKit 交互、村民献祭、仆从掉落拦截。
 */
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

    @SubscribeEvent
    public static void onMonumentOwnerAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity();
        ServerLevel level = (ServerLevel) player.level();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Mob mob
                    && mob instanceof IOwned owned
                    && mob.getPersistentData().getBoolean("goetymech.monument_summoned")
                    && owned.getTrueOwner() == player
                    && mob.getTarget() == null
                    && mob.isAlive()) {
                mob.setTarget(target);
            }
        }
    }

    public static void spawnMonumentDeathParticles(ServerLevel level, Entity entity) {
        float w = entity.getBbWidth();
        float h = entity.getBbHeight();
        ServerParticleUtil.sendAlwaysVisibleParticles(level,
                ModParticleTypes.SCALED_GOD_RAY.get(),
                entity.getX(), entity.getY(), entity.getZ(),
                0, w, h, 0, 1.0F);
        int count = Math.max(30, (int)(w * h * 25));
        level.sendParticles(ParticleTypes.SOUL,
                entity.getX(), entity.getY() + h / 2.0, entity.getZ(),
                count, w / 2.0, h / 2.0, w / 2.0, 0.2);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITHER_DEATH, SoundSource.NEUTRAL, 2.0F, 1.0F);
    }

    private static boolean isValidServantForPlayer(Level level, java.util.UUID entityUUID, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity.getUUID().equals(entityUUID)) {
                if (!entity.isAlive()) return false;
                if (!(entity instanceof IOwned owned)) return false;
                java.util.UUID ownerId = owned.getOwnerId();
                return ownerId != null && ownerId.equals(player.getUUID());
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide) return;
        if (!player.isShiftKeyDown()) return;
        if (!player.getMainHandItem().isEmpty()) return;

        var blockEntity = level.getBlockEntity(event.getPos());
        if (!(blockEntity instanceof SoulMonumentBlockEntity monument)) return;
        if (!monument.isOwner(player)) return;

        event.setCanceled(true);

        int result = monument.handleClearClick(level.getGameTime());
        if (result == -1) return;
        if (result == 0) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.clear_done"), true);
        } else if (result == 1) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.clear_step1"), true);
        } else if (result == 2) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.clear_step2"), true);
        }
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

        if (!isValidServantForPlayer(level, targetUUID, player)) {
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.not_a_servant"), true);
            }
            return;
        }

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
