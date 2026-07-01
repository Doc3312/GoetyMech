package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.magic.spell.SculkBindSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 幽匿束缚事件处理器
 * 定身已由 SculkSpikeEntity 接管（独立 Entity tick，仿 Goety TangleEntity 机制）
 * Warden 可见性 — 延迟 15 tick 后恢复，避免出土动画首帧闪现
 */
@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkBindEvents {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        cleanExpiredLock(entity);
        handleWardenVisibility(entity);
    }

    private static void cleanExpiredLock(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(SculkBindSpell.TAG_LOCK_END)) return;
        if (entity.level().getGameTime() < data.getLong(SculkBindSpell.TAG_LOCK_END)) return;
        data.remove(SculkBindSpell.TAG_LOCK_X);
        data.remove(SculkBindSpell.TAG_LOCK_Y);
        data.remove(SculkBindSpell.TAG_LOCK_Z);
        data.remove(SculkBindSpell.TAG_LOCK_END);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide) return;

        if (!(event.getSource().getEntity() instanceof Warden warden)) return;

        CompoundTag tag = warden.getPersistentData();
        if (!tag.hasUUID("goetymech.bind_caster")) return;
        if (!dead.getUUID().equals(tag.getUUID("goetymech.bind_target"))) return;

        if (warden.level().getPlayerByUUID(tag.getUUID("goetymech.bind_caster")) instanceof ServerPlayer player) {
            warden.increaseAngerAt(player, 100, false);
        }

        tag.remove("goetymech.bind_caster");
        tag.remove("goetymech.bind_target");
    }

    // ==== Warden 延迟可见性 ====

    /** 修复出土动画首帧闪现：spawn 时设 invisible，15 tick 后脑活动已初始化，恢复可见 */
    private static void handleWardenVisibility(LivingEntity entity) {
        if (!(entity instanceof Warden warden)) return;
        CompoundTag tag = entity.getPersistentData();
        long visibleAt = tag.getLong("goetymech.visible_at");
        if (visibleAt <= 0) return;

        if (entity.level().getGameTime() >= visibleAt) {
            warden.setInvisible(false);
            tag.remove("goetymech.visible_at");
        }
    }
}