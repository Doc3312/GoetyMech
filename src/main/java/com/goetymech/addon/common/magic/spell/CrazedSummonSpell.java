package com.goetymech.addon.common.magic.spell;

import com.Polarice3.Goety.api.magic.ISummonSpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.utils.BlockFinder;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.goetymech.addon.common.entity.CrazedIllagerServant;
import com.goetymech.addon.init.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class CrazedSummonSpell extends SummonSpell {

    @Override
    public int defaultSoulCost() {
        return 20;
    }

    @Override
    public int defaultCastDuration() {
        return 20;
    }

    @Override
    public int defaultSpellCooldown() {
        return 40;
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.ILL;
    }

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 5, 0, 0, 0, 0);
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.DURATION.get());
        return list;
    }

    @Override
    public int SummonDownDuration() {
        return 100;
    }

    @Override
    public int summonLimit() {
        return 16;
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster) {
        if (caster instanceof Player player) {
            int count = 0;
            for (Entity entity : worldIn.getAllEntities()) {
                if (entity instanceof CrazedIllagerServant servant
                        && servant.getTrueOwner() == caster && servant.isAlive()) {
                    count++;
                }
            }
            if (count >= this.summonLimit()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.crazed_summon.limit"),
                        true);
                return false;
            }
        }
        return super.conditionsMet(worldIn, caster);
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        this.commonResult(worldIn, caster);

        int potency = spellStat.getPotency();
        int duration = spellStat.getDuration();

        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster);
            duration += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1;
        }

        if (!isShifting(caster)) {
            CrazedIllagerServant servant = new CrazedIllagerServant(
                    ModEntities.CRAZED_ILLAGER_SERVANT.get(), worldIn);

            servant.setPos(caster.getX(), caster.getY(), caster.getZ());
            servant.setTrueOwner(caster);

            var blockPos = BlockFinder.SummonRadius(caster.blockPosition(), servant, worldIn);
            servant.moveTo(blockPos, 0.0F, 0.0F);
            MobUtil.moveDownToGround(servant);
            servant.setLimitedLife(MobUtil.getSummonLifespan(worldIn) * duration);
            servant.setPersistenceRequired();
            servant.finalizeSpawn(worldIn, caster.level().getCurrentDifficultyAt(caster.blockPosition()),
                    net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED, null, null);

            this.buffSummon(caster, servant, potency);
            this.SummonSap(caster, servant);
            this.setTarget(caster, servant);

            if (worldIn.addFreshEntity(servant)) {
                this.uponSummon(worldIn, caster, staff, servant);
            }

            this.SummonDown(caster);
            this.summonAdvancement(caster, servant);
        }

        this.playSound(worldIn, caster, this.CastingSound());
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }
}
