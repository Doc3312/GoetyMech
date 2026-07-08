package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.common.block.entity.StressReaderBlockEntity;
import com.goetymech.addon.common.capabilities.IStressShield;
import com.goetymech.addon.common.item.StressShieldFocus;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.utils.StressShieldHelper;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.init.ModAttributes;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class StressShieldSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return ModConfig.STRESS_SHIELD_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 20;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.STRESS_SHIELD_COOLDOWN.get();
    }

    @Override
    public boolean hasCustomCooldown(LivingEntity caster, ItemStack staff, ItemStack focus, int initialCooldown) {
        return true;
    }

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 0, 0, 0, 0);
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.VOID;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.DURATION.get());
        return list;
    }

    @Override
    public void useParticle(Level worldIn, LivingEntity caster, ItemStack stack) {
        if (worldIn instanceof ServerLevel server && caster.tickCount % 5 == 0) {
            ServerParticleUtil.addParticlesAroundMiddleSelf(server, ParticleTypes.ENCHANTED_HIT, caster);
        }
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        if (!(caster instanceof Player player)) return;

        IStressShield shield = StressShieldHelper.getShield(player);
        if (shield == null) return;

        float stress = readStress(player, worldIn);
        if (stress <= 0) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.stress_shield.no_stress")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        float shieldValue = stress / ModConfig.STRESS_SHIELD_DIVISOR.get();

        if (rightStaff(staff)) {
            shieldValue *= 1.2f;
        }

        int potencyLevel = WandUtil.getLevels(ModEnchantments.POTENCY.get(), player);
        shieldValue *= (1.0f + potencyLevel * 0.1f);

        int durationLevel = WandUtil.getLevels(ModEnchantments.DURATION.get(), player);
        int durationSec = ModConfig.STRESS_SHIELD_BASE_DURATION.get() + durationLevel * 60;

        shield.setShield(shieldValue);
        shield.setMaxShield(shieldValue);
        shield.setExpireTick(worldIn.getGameTime() + durationSec * 20L);
        shield.setLastHurtTick(worldIn.getGameTime());
        if (player instanceof ServerPlayer sp) {
            StressShieldHelper.sendUpdatePacket(sp);
        }

        int cooldown = (int) (ModConfig.STRESS_SHIELD_COOLDOWN.get()
                * ModAttributes.getCooldownDiscount(player));
        ItemStack focus = WandUtil.findFocus(player);
        if (!focus.isEmpty()) {
            SEHelper.addCooldown(player, focus.getItem(), cooldown);
        }
        if (!focus.isEmpty() && focus.getItem() instanceof StressShieldFocus) {
            StressShieldFocus.updateShieldCache(focus, shieldValue);
        }

        if (!player.isCreative()) {
            int cost = ModConfig.STRESS_SHIELD_COST.get();
            if (cost > 0) SEHelper.decreaseSouls(player, cost);
        }

        this.playSound(worldIn, caster, this.CastingSound());
    }

    private float readStress(Player player, ServerLevel worldIn) {
        // Priority 1: Ark path — aggregated capacity
        if (GoetyMechMod.STRESS_LINK.hasArk(player.getUUID())) {
            return GoetyMechMod.STRESS_LINK.getAggregatedCapacity(player.getUUID());
        }

        // Priority 2: Direct reader binding via StressShieldFocus
        ItemStack focus = WandUtil.findFocus(player);
        if (focus.isEmpty() || !(focus.getItem() instanceof StressShieldFocus)) return 0;
        if (!StressShieldFocus.isBound(focus)) return 0;

        BlockPos boundPos = StressShieldFocus.getBoundPos(focus);
        String boundDim = StressShieldFocus.getBoundDimension(focus);
        if (boundPos == null || boundDim == null) return 0;

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.tryParse(boundDim));
        ServerLevel stressLevel = player.getServer().getLevel(dimKey);
        if (stressLevel == null || !stressLevel.isLoaded(boundPos)) return 0;

        if (!(stressLevel.getBlockEntity(boundPos) instanceof StressReaderBlockEntity reader)) return 0;

        return reader.getNetworkCapacity();
    }
}
