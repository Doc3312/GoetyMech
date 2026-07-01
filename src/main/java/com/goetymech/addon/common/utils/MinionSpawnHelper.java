package com.goetymech.addon.common.utils;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.common.entities.ally.illager.Neollager;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 浠嗕粠鐢熸垚杈呭姪绫?
 * 灏?TwistedEmbryoItem 鍜?MatureEmbryoItem 涓噸澶嶇殑鐢熸垚閫昏緫鎻愬彇鍑烘潵锛岄伩鍏嶄唬鐮侀噸澶?
 * 鏀寔鎵€鏈?Goety 鏉冪粦瀹氬拰 Neollager 榄旀硶鐘舵€佸鐞?
 */
public class MinionSpawnHelper {

    public static final double SPAWN_HEIGHT_OFFSET = 0.5D;

    /**
     * 妫€鏌?BlockPos 鏄惁涓哄悎娉曠珯绔嬩綅缃?
     * 鏉′欢锛氳剼涓嬫槸鍥轰綋鏂瑰潡 && 鑷韩浣嶇疆闈炲浐浣?&& 澶撮《涓€鏍奸潪鍥轰綋锛?鏍煎噣绌猴級
     */
    public static boolean isValidStanding(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid()
                && !level.getBlockState(pos).isSolid()
                && !level.getBlockState(pos.above()).isSolid();
    }

    /**
     * 浠?startPos 寮€濮嬫部 Y 杞村弻鍚戞壂鎻忥紝鎵惧埌绗竴涓悎娉曠珯绔嬩綅缃?
     * @return 鍚堟硶 BlockPos锛屾垨 null锛堟暣涓瀭鐩寸┖闂撮兘鎵句笉鍒帮級
     */
    @Nullable
    public static BlockPos findGroundPos(ServerLevel level, BlockPos startPos) {
        BlockPos.MutableBlockPos m = startPos.mutable();
        // Phase 1: 鍚戜笂鎵弿
        while (m.getY() < level.getMaxBuildHeight() - 2) {
            if (isValidStanding(level, m)) return m.immutable();
            m.move(0, 1, 0);
        }
        // Phase 2: 鍚戜笅鎵弿
        m.set(startPos.getX(), startPos.getY() - 1, startPos.getZ());
        while (m.getY() > level.getMinBuildHeight()) {
            if (isValidStanding(level, m)) return m.immutable();
            m.move(0, -1, 0);
        }
        return null;
    }

    /**
     * 閫氳繃灏勭嚎杩借釜鑾峰彇鐢熸垚浣嶇疆
     * 鎻愬彇鑷?TwistedEmbryoItem 鍜?MatureEmbryoItem 涓殑閲嶅閫昏緫
     */
    public static Vec3 findSpawnPosition(Player player, Level level, double reachDistance) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);
        BlockHitResult blockHit = level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getLocation().add(0, SPAWN_HEIGHT_OFFSET, 0);
        }
        return eyePos.add(lookVec.x * 2.0D, lookVec.y * 2.0D, lookVec.z * 2.0D);
    }

    /**
     * 鐢熸垚灞炰簬鐜╁鐨勪粏浠?
     * 鑷姩澶勭悊: MobUtil.summonTame (绉垎淇濇寔銆乸ersistence銆佽窡闅忔ā寮?銆乻pawnUpgraded,
     * 浠ュ強 Neollager finalizeSpawn 鍓嶅悗涓ゆ setMagic(false)
     *
     * @param <T>       Mob 绫诲瀷
     * @param type      瀹炰綋绫诲瀷
     * @param level     鏈嶅姟鍣ㄤ笘鐣?
     * @param player    鐜╁
     * @param spawnPos  鐢熸垚浣嶇疆
     * @return 鐢熸垚鎴愬姛鐨勫疄浣擄紝澶辫触杩斿洖 null
     */
    @Nullable
    public static Mob spawnMinion(EntityType<? extends Mob> type, ServerLevel level, Player player, Vec3 spawnPos) {
        Mob mob = type.create(level);
        if (mob == null) return null;

        mob.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0F);

        MobUtil.summonTame(mob, player);

        if (mob instanceof Neollager neollager) {
            neollager.setMagic(false);
        }

        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(spawnPos)),
                MobSpawnType.MOB_SUMMONED, null, null);

        if (mob instanceof Neollager neollager2) {
            neollager2.setMagic(false);
        }

        if (level.addFreshEntity(mob)) {
            playSpawnEffects(level, spawnPos);
            return mob;
        }
        return null;
    }

    @Nullable
    public static Mob spawnTimedMinion(EntityType<? extends Mob> type, ServerLevel level, Player player, Vec3 spawnPos, int lifespanTicks) {
        Mob mob = spawnMinion(type, level, player, spawnPos);
        if (mob instanceof IOwned owned) {
            owned.setHasLifespan(true);
            owned.setLifespan(lifespanTicks);
        }
        return mob;
    }

    /**
     * 鎾斁鐢熸垚闊虫晥鍜岀矑瀛?
     */
    public static void playSpawnEffects(ServerLevel level, Vec3 spawnPos) {
        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.PLAYERS,
                1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        level.sendParticles(ParticleTypes.SCULK_SOUL,
                spawnPos.x, spawnPos.y + 0.5D, spawnPos.z,
                15, 0.5D, 0.5D, 0.5D, 0.02D);
    }

    /**
     * 鐏甸瓊娑堣€楃粨鏋滄灇涓?
     */
    public enum SoulCostResult {
        /** 鐏甸瓊瓒冲锛屽凡鎵ｉ瓊锛屾弧琛€ */
        SUFFICIENT,
        /** 鐏甸瓊涓嶈冻浣?> 0锛屽凡鎵ｉ瓊锛屽崐琛€ */
        INSUFFICIENT,
        /** 鐏甸瓊涓?0锛屽厤璐瑰彫鍞わ紝鍗婅 */
        DEPLETED
    }

    /**
     * 妫€鏌ョ伒榄傛槸鍚﹁冻澶燂紝澶勭悊鍗婅鎯╃綒鍜岀伒榄傛秷鑰?
     * 璇箟瑙勫垯锛?
     *   - currentSouls >= effectiveCost 鈫?婊¤锛屾墸 soulCost锛堝唴閮ㄦ墦鎶橈級鈫?SUFFICIENT
     *   - 0 < currentSouls < effectiveCost 鈫?鍗婅锛屾墸 soulCost 鈫?INSUFFICIENT
     *   - currentSouls == 0 鈫?鍗婅锛屼笉鎵ｇ伒榄?鈫?DEPLETED
     *
     * @return SoulCostResult 鏋氫妇锛岃皟鐢ㄦ柟鎹鍐冲畾 UI 鎻愮ず
     */
    public static SoulCostResult handleSoulCost(Player player, int soulCost, Mob mob) {
        float discount = SEHelper.soulDiscount(player);
        int effectiveCost = Math.max(1, (int) (soulCost * discount));
        int currentSouls = SEHelper.getSoulAmountInt(player);

        if (currentSouls < effectiveCost) {
            // 鐏甸瓊涓嶈冻 鈫?鍗婅浠嗕粠
            mob.setHealth(mob.getMaxHealth() * 0.5F);
            if (currentSouls > 0) {
                SEHelper.decreaseSouls(player, soulCost);
                return SoulCostResult.INSUFFICIENT;
            }
            return SoulCostResult.DEPLETED;
        }

        // 鐏甸瓊瓒冲 鈫?婊¤
        SEHelper.decreaseSouls(player, soulCost);
        return SoulCostResult.SUFFICIENT;
    }
}