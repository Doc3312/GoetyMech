package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModEntities;
import com.goetymech.addon.common.entity.SculkSpikeEntity;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.init.ModAttributes;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.util.Unit;

import java.util.List;

/**
 * 骞藉尶鏉熺細娉曟湳
 * 瀹氳韩锛歅ersistentData 瀛橀攣瀹氫綅锛屼簨浠跺鐞嗗櫒姣?tick 寮哄埗褰掍綅
 * Warden锛氬彫鍞ゅ師鐗?Warden + GameEvent.SHRIEK 婵€鎬?鈫?鍘熺増鎸姩妫€娴嬮摼鑷劧绱㈡晫
 */
public class SculkBindSpell extends Spell {

    // 瀹氳韩鏍囩 鈥?涓?SculkBindEvents 鍏变韩
    public static final String TAG_LOCK_X = "goetymech.sculk_lock_x";
    public static final String TAG_LOCK_Y = "goetymech.sculk_lock_y";
    public static final String TAG_LOCK_Z = "goetymech.sculk_lock_z";
    public static final String TAG_LOCK_END = "goetymech.sculk_lock_end";

    // defaultSoulCost() 鍦?MagicFocus 鏋勯€犳椂琚皟鐢紝闇€瑕?loadConfig 宸叉彁鍓嶅姞杞介厤缃?    // 鍙傝 GoetyMechMod 鏋勯€犲嚱鏁颁腑 ModConfig.loadConfig() 璋冪敤
    @Override
    public int defaultSoulCost() {
        return ModConfig.SCULK_BIND_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 30;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.SCULK_BIND_SUCCESS_COOLDOWN.get();
    }

    @Override
    public boolean hasCustomCooldown(LivingEntity caster, ItemStack staff, ItemStack focus, int initialCooldown) {
        return true;
    }

    // defaultStats() 浠呭湪杩愯鏃惰璋冪敤锛坓etTarget / WandUtil.getStats锛夛紝鍙畨鍏ㄨ鍙栭厤缃?    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, ModConfig.SCULK_BIND_RANGE.get(), 0.0D, 0, 0.0F);
    }

    @Override
    public com.Polarice3.Goety.api.magic.SpellType getSpellType() {
        return com.Polarice3.Goety.api.magic.SpellType.VOID;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        return List.of(ModEnchantments.RANGE.get(), ModEnchantments.DURATION.get());
    }

    @Override
    public void useParticle(Level worldIn, LivingEntity caster, ItemStack stack) {
        if (worldIn instanceof ServerLevel server && caster.tickCount % 5 == 0) {
            ServerParticleUtil.addParticlesAroundMiddleSelf(server, ParticleTypes.SCULK_SOUL, caster);
        }
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster,
                            ItemStack staff, SpellStat spellStat) {
        // 娉曟潠绫诲瀷澧炲己妫€娴嬶紙Goety 鏍囧噯妯″紡锛屼负鍚庣画鎵╁睍棰勭暀锛?
        boolean useStaffBonus = rightStaff(staff);

        // 1. 灏勭嚎妫€娴嬬洰鏍?
        int range = spellStat.getRange();
        if (WandUtil.enchantedFocus(caster)) {
            range += WandUtil.getRangeLevel(caster);
        }
        LivingEntity target = this.getTarget(caster, range);
        if (target == null) {
            if (caster instanceof ServerPlayer player) {
                player.displayClientMessage(
                        Component.translatable("info.goetymech.sculk_bind_focus.no_target"), true);
            }
            if (caster instanceof Player player) {
                ItemStack focus = WandUtil.findFocus(caster);
                if (!focus.isEmpty()) {
                    int cooldown = (int) (ModConfig.SCULK_BIND_MISS_COOLDOWN.get()
                            * ModAttributes.getCooldownDiscount(caster));
                    SEHelper.addCooldown(player, focus.getItem(), cooldown);
                }
            }
            return;
        }

        // 2. 闄勯瓟鍔犳垚锛坉uration 浣滀负澧為噺锛屼笉褰卞搷鍩虹閿佸畾鏃堕棿锛?
        int baseLockSeconds = ModConfig.SCULK_BIND_LOCK_DURATION.get();
        int lockSeconds = baseLockSeconds;
        if (WandUtil.enchantedFocus(caster)) {
            lockSeconds += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1;
        }
        int lockTicks = lockSeconds * 20;

        // 3. 绌轰腑鐩爣 鈫?璺岃惤
        if (!target.onGround()) {
            BlockPos groundPos = findGroundBelow(worldIn, target.blockPosition());
            if (groundPos != null) {
                int fallDist = Math.max(0, target.blockPosition().getY() - groundPos.getY());
                if (fallDist > 3) {
                    target.hurt(worldIn.damageSources().fall(),
                            Mth.ceil(fallDist - 3.0F));
                }
                target.teleportTo(groundPos.getX() + 0.5, groundPos.getY() + 1.0,
                        groundPos.getZ() + 0.5);
            }
        }

        // 4. 閿佸畾浣嶅瓨鍏?PersistentData 鈫?浜嬩欢澶勭悊鍣ㄦ瘡 tick teleportTo
        BlockPos lockPos = target.blockPosition();
        CompoundTag tag = target.getPersistentData();
        tag.putDouble(TAG_LOCK_X, lockPos.getX() + 0.5);
        tag.putDouble(TAG_LOCK_Y, lockPos.getY());
        tag.putDouble(TAG_LOCK_Z, lockPos.getZ() + 0.5);
        tag.putLong(TAG_LOCK_END, worldIn.getGameTime() + lockTicks);

        // 榛戞殫 5 绉?
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
                100, 0, false, false, true));

        // 5. 灏栧埡 + 瀹氳韩閿氱偣锛氬皷鍒哄疄浣撲綔涓虹嫭绔?Entity锛屾瘡 tick 灏嗙洰鏍囧己鍒跺綊浣?
        BlockPos spikePos = target.blockPosition();
        SculkSpikeEntity spike = new SculkSpikeEntity(ModEntities.SCULK_SPIKE.get(), worldIn);
        spike.setPos(spikePos.getX() + 0.5, spikePos.getY(), spikePos.getZ() + 0.5);
        spike.setBindTarget(target);
        spike.setLifeSpan(lockTicks);
        worldIn.addFreshEntity(spike);

        // 6. 澹版尝绮掑瓙
        worldIn.sendParticles(ParticleTypes.SONIC_BOOM,
                spikePos.getX() + 0.5, spikePos.getY() + 1.5, spikePos.getZ() + 0.5,
                1, 0, 0, 0, 0);

        // 7. 鍙敜鍘熺増 Warden + 娉ㄥ叆鎰ゆ€?+ 绂侀亖鍦帮紙淇濈暀鍑哄湡鍔ㄧ敾锛?
        int wardenCount = ModConfig.SCULK_BIND_WARDEN_COUNT.get();
        if (useStaffBonus) {
            wardenCount += 1;
        }

        // 灏栧暩闊虫晥锛氬湪 Warden 鐢熸垚鍓嶅彧鎾竴娆★紝瑕嗙洊鏁翠釜鍙敜鍖哄煙
        worldIn.playSound(null, spikePos, SoundEvents.SCULK_SHRIEKER_SHRIEK,
                SoundSource.HOSTILE, 3.0F, 1.0F);

        // 杈呭姪绱㈡晫锛氬彂灏勪竴娆?GameEvent.SHRIEK 瑙﹀彂鎸姩妫€娴嬮摼
        worldIn.gameEvent(target, GameEvent.SHRIEK, target.blockPosition());

        for (int i = 0; i < wardenCount; i++) {
            Warden warden = new Warden(net.minecraft.world.entity.EntityType.WARDEN, worldIn);

            // 鐢熸垚浣嶇疆锛氱洰鏍囧懆鍥?4 鏍硷紝闅忔満鏂瑰悜
            double angle = worldIn.random.nextDouble() * Math.PI * 2;
            double dist = 4.0;
            double spawnX = target.getX() + Math.cos(angle) * dist;
            double spawnZ = target.getZ() + Math.sin(angle) * dist;
            double spawnY = findSolidGround(worldIn, Mth.floor(spawnX), Mth.floor(spawnZ),
                    target.blockPosition().getY());

            warden.setPos(spawnX, spawnY, spawnZ);
            warden.setPersistenceRequired();

            // 绂佹閬佸湴锛氳缃?60s 鍐峰嵈锛堝嚭鍦熸湡闂村氨宸茬敓鏁堬紝鍑哄湡瀹屾垚鍚庝笉浼氶€?DIG锛?            warden.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200);
            // 瑙﹀彂鍑哄湡鍔ㄧ敾锛欱rain 鐨?Emerging 娲诲姩闇€瑕?IS_EMERGING 璁板繂锛?00 tick 鍚庤嚜鍔ㄨ繃鏈?            warden.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, 100);
            // 娉ㄥ叆鎰ゆ€掞細100 瓒呰繃 FIGHT 闃堝€?80锛堝師鐗?AngerLevel.ANGRY.getMinimumAnger() + 20锛?            warden.increaseAngerAt(target, 100, false);

            // 闅愯韩鍓嶅厛璁?EMERGING 濮挎€侊紙spawn packet 鎼哄甫锛夛紝鍐嶈 invisible
            // 寤惰繜 15 tick 鍚庢仮澶嶅彲瑙?鈫?姝ゆ椂 Brain 宸插惎鍔?Emerging 娲诲姩锛屽姩鐢荤姸鎬佸凡鍒濆鍖?            warden.setPose(Pose.EMERGING);
            warden.setInvisible(true);
            warden.getPersistentData().putLong("goetymech.visible_at",
                    worldIn.getGameTime() + 15);
            warden.getPersistentData().putUUID("goetymech.bind_caster", caster.getUUID());
            warden.getPersistentData().putUUID("goetymech.bind_target", target.getUUID());

            worldIn.addFreshEntity(warden);

            if (!target.isAlive()) {
                warden.increaseAngerAt(caster, 100, false);
                warden.getPersistentData().remove("goetymech.bind_caster");
                warden.getPersistentData().remove("goetymech.bind_target");
            }

            ServerParticleUtil.addParticlesAroundMiddleSelf(worldIn,
                    ParticleTypes.SCULK_SOUL, warden);
        }

        this.playSound(worldIn, caster, SoundEvents.SCULK_CATALYST_BLOOM);

        if (caster instanceof Player player) {
            ItemStack focus = WandUtil.findFocus(caster);
            if (!focus.isEmpty()) {
                int cooldown = (int) (ModConfig.SCULK_BIND_SUCCESS_COOLDOWN.get()
                        * ModAttributes.getCooldownDiscount(caster));
                SEHelper.addCooldown(player, focus.getItem(), cooldown);
            }
        }
    }

    /**
     * 鍚戜笅鏌ユ壘鍦伴潰锛氭壂鎻忓埌绗竴涓潪绌烘皵鏂瑰潡鍚庤繑鍥炲叾鍧愭爣
     * @return 鍦伴潰鏂瑰潡鍧愭爣锛屽鏋滃埌搴曚粛鏈壘鍒板垯杩斿洖 null
     */
    private BlockPos findGroundBelow(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        while (mutable.getY() > level.getMinBuildHeight()) {
            if (!level.getBlockState(mutable).isAir()) {
                return mutable.immutable();
            }
            mutable.move(0, -1, 0);
        }
        return null;
    }

    /**
     * 鏌ユ壘鎸囧畾 XZ 澶勫悎閫傜殑鐢熸垚 Y 鍧愭爣
     * 浠庡弬鑰?Y 寮€濮嬪弻鍚戞悳绱紝浼樺厛鍚戜笂鎵鹃潪鍥轰綋椤堕儴锛屽惁鍒欏悜涓嬫壘鍥轰綋椤堕儴
     */
    private double findSolidGround(ServerLevel level, int x, int z, int referenceY) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(x, referenceY, z);

        // 鍚戜笂鎼滅储锛氭壘闈炲浐浣擄紙鍗崇┖姘旓級涓嬫柟鐨勫浐浣撻《閮?
        while (m.getY() < level.getMaxBuildHeight() - 1) {
    
        BlockPos below = m.below();
            if (level.getBlockState(below).isSolid()
                    && !level.getBlockState(m).isSolid()) {
                return m.getY();
            }
            m.move(0, 1, 0);
        }

        // 鍚戜笂鏈壘鍒帮紝浠?referenceY 鍚戜笅鎼滅储
        m.set(x, referenceY - 1, z);
        while (m.getY() > level.getMinBuildHeight()) {
            if (level.getBlockState(m).isSolid()) {
                return m.getY() + 1;
            }
            m.move(0, -1, 0);
        }

        // 鍏滃簳锛氳繑鍥?referenceY
        return referenceY;
    }
}