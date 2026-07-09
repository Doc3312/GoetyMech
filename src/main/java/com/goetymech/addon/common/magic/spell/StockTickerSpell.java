package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.common.item.StockTickerFocus;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StockTickerSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return ModConfig.STOCK_TICKER_FOCUS_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 20;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.STOCK_TICKER_FOCUS_COOLDOWN.get();
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
    public SpellType getSpellType() {
        return SpellType.VOID;
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.ENDERMAN_TELEPORT;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster,
                            ItemStack staff, SpellStat spellStat) {
        if (!(caster instanceof ServerPlayer player)) return;

        ItemStack focus = WandUtil.findFocus(caster);
        if (!(focus.getItem() instanceof StockTickerFocus)) return;

        if (!StockTickerFocus.isBound(focus)) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.stock_ticker_focus.not_bound"), true);
            return;
        }

        BlockPos boundPos = StockTickerFocus.getBoundPos(focus);
        ResourceLocation boundDim = StockTickerFocus.getBoundDimension(focus);
        if (boundPos == null || boundDim == null) return;

        if (player.getServer() == null) return;
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, boundDim);
        ServerLevel targetLevel = player.getServer().getLevel(dimKey);
        if (targetLevel == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.stock_ticker_focus.dimension_unloaded"), true);
            return;
        }

        BlockEntity be = targetLevel.getBlockEntity(boundPos);
        if (!(be instanceof StockTickerBlockEntity stbe)) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.stock_ticker_focus.ticker_lost"), true);
            StockTickerFocus.clearBinding(focus);
            return;
        }

        if (!stbe.isKeeperPresent()) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.stock_ticker_focus.no_keeper"), true);
            return;
        }

        if (!stbe.behaviour.mayInteract(player)) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.stock_ticker_focus.locked"), true);
            return;
        }

        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, defaultSoulCost());
        }

        SEHelper.addCooldown(player, focus.getItem(), defaultSpellCooldown());

        StockTickerInteractionHandler.interactWithLogisticsManagerAt(player, targetLevel, boundPos);

        this.playSound(worldIn, caster, this.CastingSound());
    }
}