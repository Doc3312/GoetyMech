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
 * 骞藉尶鏉熺細浜嬩欢澶勭悊鍣? * 瀹氳韩宸茬敱 SculkSpikeEntity 鎺ョ锛堢嫭绔?Entity tick锛屼豢 Goety TangleEntity 鏈哄埗锛? * Warden 鍙鎬?鈥?寤惰繜 15 tick 鍚庢仮澶嶏紝閬垮厤鍑哄湡鍔ㄧ敾棣栧抚闂幇
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

    // ==== Warden 寤惰繜鍙鎬?====

    /** 淇鍑哄湡鍔ㄧ敾棣栧抚闂幇锛歴pawn 鏃惰 invisible锛?5 tick 鍚庤剳娲诲姩宸插垵濮嬪寲锛屾仮澶嶅彲瑙?*/
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