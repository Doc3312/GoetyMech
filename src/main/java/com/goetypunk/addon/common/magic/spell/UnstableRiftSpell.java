package com.goetypunk.addon.common.magic.spell;

import com.goetypunk.addon.common.utils.RiftUtil;
import com.goetypunk.addon.config.ModConfig;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.events.ArcaTeleporter;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;

public class UnstableRiftSpell extends Spell {

    public static final String TAG_ORIGIN_DIM = "goetypunk.origin_dim";
    public static final String TAG_ORIGIN_X = "goetypunk.origin_x";
    public static final String TAG_ORIGIN_Y = "goetypunk.origin_y";
    public static final String TAG_ORIGIN_Z = "goetypunk.origin_z";
    public static final String TAG_RETURN_TIME = "goetypunk.return_time";

    @Override
    public int defaultSoulCost() {
        return ModConfig.UNSTABLE_RIFT_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 10;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.UNSTABLE_RIFT_COOLDOWN.get();
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
        return SoundEvents.ENDERMAN_TELEPORT;
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

        boolean useStaffBonus = rightStaff(staff);

        ResourceKey<Level> currentDim = player.level().dimension();
        if (!RiftUtil.isRiftable(currentDim)) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.rift_walk.invalid_dim"), true);
            return;
        }

        CompoundTag data = player.getPersistentData();
        if (data.contains(TAG_RETURN_TIME)) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.rift_walk.rift_open"), true);
            return;
        }

        ResourceKey<Level> targetDim = RiftUtil.getPartner(currentDim);
        if (targetDim == null) return;

        Vec3 currentPos = player.position();
        Vec3 targetPos = RiftUtil.mapCoordinates(currentPos, currentDim, targetDim);

        ServerLevel targetLevel = player.getServer().getLevel(targetDim);
        if (targetLevel == null) return;

        WorldBorder border = targetLevel.getWorldBorder();
        if (!border.isWithinBounds(BlockPos.containing(targetPos))) {
            player.displayClientMessage(
                    Component.translatable("info.goetypunk.rift_walk.border_blocked"), true);
            return;
        }

        double y = targetPos.y;
        if (y < targetLevel.getMinBuildHeight()) {
            y = targetLevel.getMinBuildHeight() + 1;
        }
        Vec3 safeTarget = new Vec3(targetPos.x, y, targetPos.z);

        long returnDelay = ModConfig.UNSTABLE_RIFT_RETURN_DELAY.get();
        if (useStaffBonus) {
            returnDelay = (long) (returnDelay * 1.5);
        }
        data.putString(TAG_ORIGIN_DIM, currentDim.location().toString());
        data.putDouble(TAG_ORIGIN_X, currentPos.x);
        data.putDouble(TAG_ORIGIN_Y, currentPos.y);
        data.putDouble(TAG_ORIGIN_Z, currentPos.z);
        data.putLong(TAG_RETURN_TIME, worldIn.getGameTime() + returnDelay);

        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, ModConfig.UNSTABLE_RIFT_COST.get());
        }

        ItemStack focus = WandUtil.findFocus(caster);
        if (!focus.isEmpty()) {
            SEHelper.addCooldown(player, focus.getItem(), ModConfig.UNSTABLE_RIFT_COOLDOWN.get());
        }

        player.changeDimension(targetLevel, new ArcaTeleporter(safeTarget));

        this.playSound(worldIn, caster, SoundEvents.ENDERMAN_TELEPORT);
    }
}
