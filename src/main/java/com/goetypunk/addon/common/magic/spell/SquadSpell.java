package com.goetypunk.addon.common.magic.spell;

import com.goetypunk.addon.common.block.entity.SquadBarracksBlockEntity;
import com.goetypunk.addon.common.item.SquadFocus;
import com.goetypunk.addon.common.utils.MinionSpawnHelper;
import com.goetypunk.addon.config.ModConfig;
import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.magic.SpellType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SquadSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return 0;
    }

    @Override
    public int defaultCastDuration() {
        return 40;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.SQUAD_SUMMON_COOLDOWN.get();
    }

    @Override
    public boolean hasCustomCooldown(LivingEntity caster, ItemStack staff, ItemStack focus, int initialCooldown) {
        return true;
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.VOID;
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.EVOKER_PREPARE_SUMMON;
    }

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 0, 0, 0, 0);
    }

    @Override
    public void useParticle(Level worldIn, LivingEntity caster, ItemStack stack) {
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        if (!(caster instanceof ServerPlayer player)) return;
        ItemStack focus = WandUtil.findFocus(caster);
        if (!(focus.getItem() instanceof SquadFocus)) return;

        boolean useStaffBonus = rightStaff(staff);

        if (isShifting(caster)) {
            BlockPos pos = SquadFocus.getBoundPos(focus);
            String dim = SquadFocus.getBoundDimension(focus);
            if (pos != null && dim != null) {
                String key = SquadFocus.makeSummonerKey(pos, dim);
                for (Entity entity : worldIn.getAllEntities()) {
                    if (entity instanceof IOwned owned && owned.getTrueOwner() == caster) {
                        if (key.equals(entity.getPersistentData().getString(SquadFocus.TAG_SUMMONER))) {
                            owned.dismiss();
                        }
                    }
                }
            }
            this.playSound(worldIn, caster, CastingSound());
            return;
        }

        if (!SquadFocus.isBound(focus)) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.not_bound").withStyle(ChatFormatting.RED), true);
            return;
        }

        BlockPos boundPos = SquadFocus.getBoundPos(focus);
        String boundDim = SquadFocus.getBoundDimension(focus);
        if (boundPos == null || boundDim == null) return;

        ResourceLocation dimRl = ResourceLocation.tryParse(boundDim);
        if (dimRl == null) return;

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimRl);
        ServerLevel blockLevel = player.getServer().getLevel(dimKey);
        if (blockLevel == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.dimension_not_found").withStyle(ChatFormatting.RED), true);
            return;
        }

        int tier = SquadFocus.getCachedTier(focus);
        if (tier < 3 && blockLevel != worldIn) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.wrong_dimension").withStyle(ChatFormatting.RED), true);
            return;
        }

        if (!blockLevel.isLoaded(boundPos)) {
            blockLevel.getChunkAt(boundPos);
        }

        if (!(blockLevel.getBlockEntity(boundPos) instanceof SquadBarracksBlockEntity barracks)) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.block_missing").withStyle(ChatFormatting.RED), true);
            SquadFocus.clearBinding(focus);
            return;
        }

        if (!barracks.isOwner(player)) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.not_owner").withStyle(ChatFormatting.RED), true);
            return;
        }

        List<Map.Entry<ResourceLocation, Integer>> squad = barracks.getSquadComposition();
        if (squad.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.empty_squad").withStyle(ChatFormatting.RED), true);
            return;
        }

        int totalConfigUnits = squad.stream().mapToInt(Map.Entry::getValue).sum();
        double discount = switch (tier) {
            case 2 -> ModConfig.SQUAD_TIER2_DISCOUNT.get();
            case 3 -> ModConfig.SQUAD_TIER3_DISCOUNT.get();
            default -> 1.0;
        };

        int baseCost = ModConfig.MATURE_EMBRYO_SOUL_COST.get();
        int unitCost = (int) (baseCost * discount);

        int availableUnits = barracks.countAvailableUnits();
        if (availableUnits == 0) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.no_embryos")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        int canSpawn;
        if (!player.isCreative() && unitCost > 0) {
            int playerSouls = SEHelper.getSoulAmountInt(player);
            canSpawn = Math.min(availableUnits, playerSouls / unitCost);
        } else {
            canSpawn = availableUnits;
        }

        if (canSpawn == 0) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.insufficient_souls", unitCost)
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        List<Map.Entry<ResourceLocation, Integer>> consumedSquad = barracks.consumeSquad(worldIn, player, canSpawn);
        if (consumedSquad.isEmpty()) return;

        int consumedUnits = consumedSquad.stream().mapToInt(Map.Entry::getValue).sum();
        int actualSoulCost = consumedUnits * unitCost;

        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, actualSoulCost);
        }

        int lifespan = ModConfig.SQUAD_LIFESPAN.get();
        if (useStaffBonus) {
            lifespan = (int) (lifespan * 1.5);
        }
        String summonerKey = SquadFocus.makeSummonerKey(boundPos, boundDim);
        spawnSquad(player, worldIn, consumedSquad, lifespan, summonerKey);

        SquadFocus.updateCachedData(focus, barracks);

        if (consumedUnits < totalConfigUnits) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.squad_focus.partial_summon", totalConfigUnits - consumedUnits)
                            .withStyle(ChatFormatting.YELLOW), true);
        }

        int cooldown = (int) (ModConfig.SQUAD_SUMMON_COOLDOWN.get());
        if (!focus.isEmpty()) {
            SEHelper.addCooldown(player, focus.getItem(), cooldown);
        }
        this.playSound(worldIn, caster, SoundEvents.EVOKER_CAST_SPELL);
    }

    private void spawnSquad(ServerPlayer player, ServerLevel playerLevel,
                            List<Map.Entry<ResourceLocation, Integer>> squad, int lifespan, String summonerKey) {
        List<EntityType<? extends Mob>> spawnQueue = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Integer> entry : squad) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entry.getKey());
            if (type != null && type.create(playerLevel) instanceof Mob) {
                for (int i = 0; i < entry.getValue(); i++) {
                    spawnQueue.add((EntityType<? extends Mob>) type);
                }
            }
        }

        int total = spawnQueue.size();
        if (total == 0) return;

        double radius = Math.min(2.5 + total * 0.15, 6.0);

        for (int i = 0; i < total; i++) {
            double angle = (double) i / total * Math.PI * 2;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY();

            Vec3 spawnPos = new Vec3(x, y, z);

            Mob mob = MinionSpawnHelper.spawnTimedMinion(spawnQueue.get(i), playerLevel, player, spawnPos, lifespan);
            if (mob != null) {
                mob.getPersistentData().putString(SquadFocus.TAG_SUMMONER, summonerKey);
            }
        }
    }
}
