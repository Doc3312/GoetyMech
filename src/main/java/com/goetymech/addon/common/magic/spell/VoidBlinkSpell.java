package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.common.magic.effect.VoidMarkEffect;
import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.init.ModAttributes;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.List;

public class VoidBlinkSpell extends Spell {

    @Override
    @javax.annotation.Nullable
    public LivingEntity getTarget(LivingEntity caster, int range) {
        if (caster instanceof Mob mob) {
            return mob.getTarget();
        }
        Vec3 srcVec = caster.getEyePosition(1.0F);
        Vec3 lookVec = caster.getViewVector(1.0F);
        Vec3 destVec = srcVec.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
        AABB aabb = caster.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(3.0, 3.0, 3.0);
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(caster.level(), caster, srcVec, destVec, aabb,
                entity -> (entity instanceof LivingEntity && !entity.isSpectator() && entity.isPickable())
                        || entity instanceof PartEntity<?>);
        if (hitResult != null) {
            return MobUtil.getLivingTarget(hitResult.getEntity());
        }
        return null;
    }

    @Override
    public int defaultSoulCost() {
        return ModConfig.VOID_BLINK_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 20;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.VOID_BLINK_COOLDOWN.get();
    }

    @Override
    public boolean hasCustomCooldown(LivingEntity caster, ItemStack staff, ItemStack focus, int initialCooldown) {
        return true;
    }

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, ModConfig.VOID_BLINK_RANGE.get(), 0.0D, 0, 0.0F);
    }

    @Override
    public com.Polarice3.Goety.api.magic.SpellType getSpellType() {
        return com.Polarice3.Goety.api.magic.SpellType.VOID;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        return List.of(
                ModEnchantments.POTENCY.get(),
                ModEnchantments.DURATION.get(),
                ModEnchantments.RANGE.get()
        );
    }

    @Override
    public void useParticle(Level worldIn, LivingEntity caster, ItemStack stack) {
        if (worldIn instanceof ServerLevel server && caster.tickCount % 5 == 0) {
            ServerParticleUtil.addParticlesAroundMiddleSelf(server, ParticleTypes.PORTAL, caster);
        }
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster,
                            ItemStack staff, SpellStat spellStat) {
        int range = spellStat.getRange();
        if (WandUtil.enchantedFocus(caster)) {
            range += WandUtil.getRangeLevel(caster) * 4;
        }

        LivingEntity target = this.getTarget(caster, range);
        if (target == null) {
            return;
        }

        int uses = ModConfig.VOID_BLINK_MAX_USES.get();
        if (WandUtil.enchantedFocus(caster)) {
            uses += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
        }

        int windowSec = ModConfig.VOID_BLINK_WINDOW_SECONDS.get();
        if (WandUtil.enchantedFocus(caster)) {
            windowSec += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) * 5;
        }

        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, windowSec * 20 + 100, 0, false, false, true));

        int effectDuration = windowSec * 20;
        caster.addEffect(new MobEffectInstance(ModEffects.VOID_MARK.get(), effectDuration, 0, false, true, true));

        var data = caster.getPersistentData();
        data.putUUID(VoidMarkEffect.TAG_TARGET, target.getUUID());
        data.putInt(VoidMarkEffect.TAG_REMAINING, uses);
        data.putInt(VoidMarkEffect.TAG_WINDOW_DURATION, effectDuration);
        data.putLong(VoidMarkEffect.TAG_WINDOW_END, worldIn.getGameTime() + effectDuration);

        if (caster instanceof Player player) {
            ItemStack focus = WandUtil.findFocus(caster);
            if (!focus.isEmpty()) {
                int cooldown = (int) (this.defaultSpellCooldown()
                        * ModAttributes.getCooldownDiscount(caster));
                SEHelper.addCooldown(player, focus.getItem(), cooldown);
            }
        }

        this.playSound(worldIn, caster, SoundEvents.ENDERMAN_TELEPORT);
    }
}
