package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.common.capabilities.ISoulDominion;
import com.goetymech.addon.common.utils.SoulDominionHelper;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.init.ModAttributes;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
import java.util.UUID;

public class SoulDominionSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return ModConfig.SOUL_DOMINION_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 20;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.SOUL_DOMINION_COOLDOWN.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NECROMANCY;
    }

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 0, 0, 0, 0);
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.DURATION.get());
        list.add(ModEnchantments.RANGE.get());
        return list;
    }

    @Override
    public void useParticle(Level worldIn, LivingEntity caster, ItemStack stack) {
        if (worldIn instanceof ServerLevel server && caster.tickCount % 3 == 0) {
            ServerParticleUtil.addAuraParticles(server, ParticleTypes.SOUL, caster, 1.5f);
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    caster.getX(), caster.getY() + 0.5, caster.getZ(),
                    2, 0.5, 0.5, 0.5, 0.02);
        }
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        if (!(caster instanceof Player player)) return;

        ISoulDominion dom = SoulDominionHelper.getDominion(player);
        if (dom == null) return;

        if (dom.isActive()) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.soul_dominion.already_active")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        int durationSec = ModConfig.SOUL_DOMINION_BASE_DURATION.get();
        int range = ModConfig.SOUL_DOMINION_BASE_RANGE.get();

        if (WandUtil.enchantedFocus(caster)) {
            durationSec += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) * 10;
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster) * 10;
        }

        if (range > 512) range = 512;

        List<UUID> servants = new ArrayList<>();
        for (LivingEntity entity : worldIn.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range),
                e -> e != player && e.isAlive() && e instanceof IOwned owned
                        && owned.getTrueOwner() == player)) {
            servants.add(entity.getUUID());
        }

        if (servants.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.soul_dominion.no_servants")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        dom.setActive(true);
        dom.setExpireTick(worldIn.getGameTime() + durationSec * 20L);
        dom.setLinkedServants(servants);
        dom.setRange(range);
        dom.setDuration(durationSec);

        if (player instanceof ServerPlayer sp) {
            SoulDominionHelper.sendSyncPacket(sp);
        }

        int cooldown = (int) (ModConfig.SOUL_DOMINION_COOLDOWN.get()
                * ModAttributes.getCooldownDiscount(player));
        ItemStack focus = WandUtil.findFocus(player);
        if (!focus.isEmpty()) {
            SEHelper.addCooldown(player, focus.getItem(), cooldown);
        }

        if (!player.isCreative()) {
            int cost = ModConfig.SOUL_DOMINION_COST.get();
            if (cost > 0) SEHelper.decreaseSouls(player, cost);
        }

        player.displayClientMessage(
                Component.translatable("info.goetymech.soul_dominion.activated",
                                servants.size(), durationSec)
                        .withStyle(ChatFormatting.DARK_PURPLE), true);

        this.playSound(worldIn, caster, this.CastingSound());
    }
}
