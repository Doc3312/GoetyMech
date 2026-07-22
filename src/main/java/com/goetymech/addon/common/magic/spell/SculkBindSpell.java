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
 * 幽匿束缚法术
 * 定身：PersistentData 存锁定位，事件处理器每 tick 强制归位
 * Warden：召唤原版 Warden + GameEvent.SHRIEK 激怒 → 原版振动检测链自然索敌
 */
public class SculkBindSpell extends Spell {

    /** PersistentData 定身坐标标签，与 SculkBindEvents 共享用于每 tick 强制归位 */
    public static final String TAG_LOCK_X = "goetymech.sculk_lock_x";
    public static final String TAG_LOCK_Y = "goetymech.sculk_lock_y";
    public static final String TAG_LOCK_Z = "goetymech.sculk_lock_z";
    /** 锁定过期时间戳，由 cleanExpiredLock 检查清理 */
    public static final String TAG_LOCK_END = "goetymech.sculk_lock_end";

    /** 配置项在 GoetyMechMod 构造函数中预加载，构造时即可安全读取 */
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

    /** 仅在运行时被 getTarget/WandUtil.getStats 调用，可安全读取配置 */
    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, ModConfig.SCULK_BIND_RANGE.get(), 0.0D, 0, 0.0F);
    }

    @Override
    public com.Polarice3.Goety.api.magic.SpellType getSpellType() {
        return com.Polarice3.Goety.api.magic.SpellType.VOID;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        return List.of(ModEnchantments.POTENCY.get(), ModEnchantments.RANGE.get(), ModEnchantments.DURATION.get());
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
        // 法杖类型增强检测（Goety 标准模式，为后续扩展预留）
        boolean useStaffBonus = rightStaff(staff);

        // 1. 射线检测目标
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

        // 2. 附魔加成（duration 作为增量，不影响基础锁定时间）
        int baseLockSeconds = ModConfig.SCULK_BIND_LOCK_DURATION.get();
        int lockSeconds = baseLockSeconds;
        if (WandUtil.enchantedFocus(caster)) {
            lockSeconds += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1;
        }
        int lockTicks = lockSeconds * 20;

        // 3. 空中目标 → 跌落
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

        // 4. 锁定位存入 PersistentData → 事件处理器每 tick teleportTo
        BlockPos lockPos = target.blockPosition();
        CompoundTag tag = target.getPersistentData();
        tag.putDouble(TAG_LOCK_X, lockPos.getX() + 0.5);
        tag.putDouble(TAG_LOCK_Y, lockPos.getY());
        tag.putDouble(TAG_LOCK_Z, lockPos.getZ() + 0.5);
        tag.putLong(TAG_LOCK_END, worldIn.getGameTime() + lockTicks);

        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
                100, 0, false, false, true));

        // 5. 尖刺 + 定身锚点：尖刺实体作为独立 Entity，每 tick 将目标强制归位
        BlockPos spikePos = target.blockPosition();
        SculkSpikeEntity spike = new SculkSpikeEntity(ModEntities.SCULK_SPIKE.get(), worldIn);
        spike.setPos(spikePos.getX() + 0.5, spikePos.getY(), spikePos.getZ() + 0.5);
        spike.setBindTarget(target);
        spike.setLifeSpan(lockTicks);
        worldIn.addFreshEntity(spike);

        worldIn.sendParticles(ParticleTypes.SONIC_BOOM,
                spikePos.getX() + 0.5, spikePos.getY() + 1.5, spikePos.getZ() + 0.5,
                1, 0, 0, 0, 0);

        // 7. 召唤原版 Warden + 注入愤怒 + 禁遁地（保留出土动画）
        int wardenCount = ModConfig.SCULK_BIND_WARDEN_COUNT.get();
        if (WandUtil.enchantedFocus(caster)) {
            wardenCount += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
        }
        if (useStaffBonus) {
            wardenCount += 1;
        }

        // 尖啸音效：在 Warden 生成前只播一次，覆盖整个召唤区域
        worldIn.playSound(null, spikePos, SoundEvents.SCULK_SHRIEKER_SHRIEK,
                SoundSource.HOSTILE, 3.0F, 1.0F);

        // 辅助索敌：发射一次 GameEvent.SHRIEK 触发振动检测链
        worldIn.gameEvent(target, GameEvent.SHRIEK, target.blockPosition());

        for (int i = 0; i < wardenCount; i++) {
            Warden warden = new Warden(net.minecraft.world.entity.EntityType.WARDEN, worldIn);

            // 生成位置：目标周围 4 格，随机方向
            double angle = worldIn.random.nextDouble() * Math.PI * 2;
            double dist = 4.0;
            double spawnX = target.getX() + Math.cos(angle) * dist;
            double spawnZ = target.getZ() + Math.sin(angle) * dist;
            double spawnY = findSolidGround(worldIn, Mth.floor(spawnX), Mth.floor(spawnZ),
                    target.blockPosition().getY());

            warden.setPos(spawnX, spawnY, spawnZ);
            warden.setPersistenceRequired();

            // 禁止遁地：设置 60s 冷却（出土期间就已生效，出土完成后不会选 DIG）
            warden.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200);
            // 触发出土动画：Brain 的 Emerging 活动需要 IS_EMERGING 记忆，100 tick 后自动过期
            warden.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, 100);
            // 注入愤怒：100 超过 FIGHT 阈值 80（原版 AngerLevel.ANGRY.getMinimumAnger() + 20）
            warden.increaseAngerAt(target, 100, false);

            // 隐身前先设 EMERGING 姿态（spawn packet 携带），再设 invisible
            // 延迟 15 tick 后恢复可见 → 此时 Brain 已启动 Emerging 活动，动画状态已初始化
            warden.setPose(Pose.EMERGING);
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
     * 向下查找地面：扫描到第一个非空气方块后返回其坐标
     * @param level 服务器世界
     * @param pos 起始扫描坐标
     * @return 地面方块坐标，如果到底仍未找到则返回 null
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
     * 查找指定 XZ 处合适的生成 Y 坐标
     * @param level 服务器世界
     * @param x X 坐标
     * @param z Z 坐标
     * @param referenceY 参考 Y 坐标，从此处开始搜索
     * @return 合适的生成 Y 坐标，兜底返回 referenceY
     */
    private double findSolidGround(ServerLevel level, int x, int z, int referenceY) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(x, referenceY, z);

        // 向上搜索：找非固体（即空气）下方的固体顶部
        while (m.getY() < level.getMaxBuildHeight() - 1) {
            BlockPos below = m.below();
            if (level.getBlockState(below).isSolid()
                    && !level.getBlockState(m).isSolid()) {
                return m.getY();
            }
            m.move(0, 1, 0);
        }

        // 向上未找到，从 referenceY 向下搜索
        m.set(x, referenceY - 1, z);
        while (m.getY() > level.getMinBuildHeight()) {
            if (level.getBlockState(m).isSolid()) {
                return m.getY() + 1;
            }
            m.move(0, -1, 0);
        }

        // 兜底：返回 referenceY
        return referenceY;
    }
}