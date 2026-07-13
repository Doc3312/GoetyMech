package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.block.entity.StressReaderBlockEntity;
import com.goetymech.addon.common.entity.CrushingWheelEntity;
import com.goetymech.addon.common.item.CrushingWheelFocus;
import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModEntities;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrushingWheelSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return ModConfig.CRUSHING_WHEEL_FOCUS_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 20;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.CRUSHING_WHEEL_FOCUS_COOLDOWN.get();
    }

    @Override
    public boolean hasCustomCooldown(LivingEntity caster, ItemStack staff,
                                     ItemStack focus, int initialCooldown) {
        return true;
    }

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 0, 0, 0, 0);
    }

    @Override
    public com.Polarice3.Goety.api.magic.SpellType getSpellType() {
        return com.Polarice3.Goety.api.magic.SpellType.VOID;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        return list;
    }

    @Override
    public void useParticle(Level worldIn, LivingEntity caster, ItemStack stack) {
        if (worldIn instanceof ServerLevel server && caster.tickCount % 5 == 0) {
            ServerParticleUtil.addParticlesAroundMiddleSelf(server,
                    ParticleTypes.ENCHANTED_HIT, caster);
        }
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster,
                            ItemStack staff, SpellStat spellStat) {
        if (!(caster instanceof Player player)) return;

        ItemStack focus = WandUtil.findFocus(caster);
        if (!(focus.getItem() instanceof CrushingWheelFocus)) return;

        boolean useStaffBonus = rightStaff(staff);

        UUID playerUUID = player.getUUID();
        int capacity;
        if (GoetyMechMod.STRESS_LINK.hasArk(playerUUID)) {
            capacity = (int) GoetyMechMod.STRESS_LINK.getAggregatedCapacity(playerUUID);
        } else if (CrushingWheelFocus.isBound(focus)) {
            BlockPos boundPos = CrushingWheelFocus.getBoundPos(focus);
            String boundDim = CrushingWheelFocus.getBoundDimension(focus);
            if (boundPos == null || boundDim == null) return;

            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.tryParse(boundDim));
            ServerLevel stressLevel = player.getServer().getLevel(dimKey);
            if (stressLevel == null) return;

            if (!stressLevel.isLoaded(boundPos) ||
                    !(stressLevel.getBlockEntity(boundPos) instanceof StressReaderBlockEntity reader)) {
                player.displayClientMessage(
                        Component.translatable("info.goetymech.crushing_wheel_focus.reader_lost")
                                .withStyle(ChatFormatting.RED), true);
                CrushingWheelFocus.clearBinding(focus);
                return;
            }
            capacity = (int) reader.getNetworkCapacity();
        } else {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.crushing_wheel_focus.not_bound")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        int divisor = ModConfig.STRESS_DAMAGE_DIVISOR.get();
        int damage = Math.max(1, (int) (capacity / divisor));
        if (useStaffBonus) {
            damage = (int) (damage * 1.5);
        }
        //GoetyMechMod.LOGGER.debug("CrushingWheelFocus spell → capacity={} divisor={} damage={}",
        //        capacity, divisor, damage);
        CrushingWheelFocus.updateDamageCache(focus, damage);

        double baseDistance = ModConfig.CRUSHING_WHEEL_BASE_DISTANCE.get();
        double maxDistance = baseDistance;

        Vec3 lookVec = caster.getLookAngle().normalize();
        Vec3 perpVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();

        int potencyLevel = WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
        int wheelCount = Math.min(9, 1 + potencyLevel);
        double spacing = 1.5;
        double startOffset = -(wheelCount - 1) * spacing / 2.0;

        for (int i = 0; i < wheelCount; i++) {
            Vec3 offset = perpVec.scale(startOffset + i * spacing);
            Vec3 spawnPos = caster.getEyePosition().add(lookVec.scale(2.0)).add(offset);

            CrushingWheelEntity wheel = new CrushingWheelEntity(
                    ModEntities.CRUSHING_WHEEL.get(), worldIn);
            wheel.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            wheel.setOwnerUUID(caster.getUUID());
            wheel.setDamage(damage);
            wheel.setMaxDistance(maxDistance);
            wheel.setDirection(lookVec);
            wheel.setYRot(Mth.wrapDegrees(90 - caster.getYRot()));
            worldIn.addFreshEntity(wheel);
        }

        int cooldown = (int) (ModConfig.CRUSHING_WHEEL_FOCUS_COOLDOWN.get());
        if (!focus.isEmpty()) {
            SEHelper.addCooldown(player, focus.getItem(), cooldown);
        }
        if (!player.isCreative()) {
            int cost = ModConfig.CRUSHING_WHEEL_FOCUS_COST.get();
            if (cost > 0) SEHelper.decreaseSouls(player, cost);
        }

        this.playSound(worldIn, caster, this.CastingSound());
    }
}
