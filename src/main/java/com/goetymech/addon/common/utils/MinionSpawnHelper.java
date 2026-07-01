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
 * 仆从生成辅助类
 * 将 TwistedEmbryoItem 和 MatureEmbryoItem 中重复的生成逻辑提取出来，避免代码重复
 * 支持所有 Goety 权绑定和 Neollager 魔法状态处理
 */
public class MinionSpawnHelper {

    public static final double SPAWN_HEIGHT_OFFSET = 0.5D;

    /**
     * 检查 BlockPos 是否为合法站立位置
     * 条件：脚下是固体方块 && 自身位置非固体 && 头顶一格非固体（2格净空）
     */
    public static boolean isValidStanding(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid()
                && !level.getBlockState(pos).isSolid()
                && !level.getBlockState(pos.above()).isSolid();
    }

    /**
     * 从 startPos 开始沿 Y 轴双向扫描，找到第一个合法站立位置
     * @return 合法 BlockPos，或 null（整个垂直空间都找不到）
     */
    @Nullable
    public static BlockPos findGroundPos(ServerLevel level, BlockPos startPos) {
        BlockPos.MutableBlockPos m = startPos.mutable();
        // Phase 1: 向上扫描
        while (m.getY() < level.getMaxBuildHeight() - 2) {
            if (isValidStanding(level, m)) return m.immutable();
            m.move(0, 1, 0);
        }
        // Phase 2: 向下扫描
        m.set(startPos.getX(), startPos.getY() - 1, startPos.getZ());
        while (m.getY() > level.getMinBuildHeight()) {
            if (isValidStanding(level, m)) return m.immutable();
            m.move(0, -1, 0);
        }
        return null;
    }

    /**
     * 通过射线追踪获取生成位置
     * 提取自 TwistedEmbryoItem 和 MatureEmbryoItem 中的重复逻辑
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
     * 生成属于玩家的仆从
     * 自动处理: MobUtil.summonTame (积分保持、persistence、跟随模式)、spawnUpgraded,
     * 以及 Neollager finalizeSpawn 前后两次 setMagic(false)
     *
     * @param <T>       Mob 类型
     * @param type      实体类型
     * @param level     服务器世界
     * @param player    玩家
     * @param spawnPos  生成位置
     * @return 生成成功的实体，失败返回 null
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
     * 播放生成音效和粒子
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
     * 灵魂消耗结果枚举
     */
    public enum SoulCostResult {
        /** 灵魂足够，已扣魂，满血 */
        SUFFICIENT,
        /** 灵魂不足但 > 0，已扣魂，半血 */
        INSUFFICIENT,
        /** 灵魂为 0，免费召唤，半血 */
        DEPLETED
    }

    /**
     * 检查灵魂是否足够，处理半血惩罚和灵魂消耗
     * 语义规则：
     *   - currentSouls >= effectiveCost → 满血，扣 soulCost（内部打折）→ SUFFICIENT
     *   - 0 < currentSouls < effectiveCost → 半血，扣 soulCost → INSUFFICIENT
     *   - currentSouls == 0 → 半血，不扣灵魂 → DEPLETED
     *
     * @return SoulCostResult 枚举，调用方据此决定 UI 提示
     */
    public static SoulCostResult handleSoulCost(Player player, int soulCost, Mob mob) {
        float discount = SEHelper.soulDiscount(player);
        int effectiveCost = Math.max(1, (int) (soulCost * discount));
        int currentSouls = SEHelper.getSoulAmountInt(player);

        if (currentSouls < effectiveCost) {
            // 灵魂不足 → 半血仆从
            mob.setHealth(mob.getMaxHealth() * 0.5F);
            if (currentSouls > 0) {
                SEHelper.decreaseSouls(player, soulCost);
                return SoulCostResult.INSUFFICIENT;
            }
            return SoulCostResult.DEPLETED;
        }

        // 灵魂足够 → 满血
        SEHelper.decreaseSouls(player, soulCost);
        return SoulCostResult.SUFFICIENT;
    }
}