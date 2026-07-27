package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.common.utils.RiftUtil;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.events.ArcaTeleporter;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.init.ModAttributes;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * 稳定裂隙法术。创建长期传送通道。
 */
public class StableRiftSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return ModConfig.STABLE_RIFT_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 0;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.STABLE_RIFT_COOLDOWN.get();
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
                    Component.translatable("info.goetymech.rift_walk.invalid_dim"), true);
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
                    Component.translatable("info.goetymech.rift_walk.border_blocked"), true);
            return;
        }

        double y = targetPos.y;
        if (y < targetLevel.getMinBuildHeight()) {
            y = targetLevel.getMinBuildHeight() + 1;
        }

        BlockPos targetBlockPos = BlockPos.containing(targetPos.x, y, targetPos.z);
        Vec3 landingPos = findSafeLanding(targetLevel, targetBlockPos, targetDim);

        int cost = ModConfig.STABLE_RIFT_COST.get();
        if (useStaffBonus) {
            cost = Math.max(1, cost / 2);
        }
        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, cost);
        }

        ItemStack focus = WandUtil.findFocus(caster);
        if (!focus.isEmpty()) {
            SEHelper.addCooldown(player, focus.getItem(), (int)(ModConfig.STABLE_RIFT_COOLDOWN.get() * ModAttributes.getCooldownDiscount(player)));
        }

        player.changeDimension(targetLevel, new ArcaTeleporter(landingPos));

        this.playSound(worldIn, caster, SoundEvents.ENDERMAN_TELEPORT);
    }

    private Vec3 findSafeLanding(ServerLevel level, BlockPos pos, ResourceKey<Level> dim) {
        Optional<Vec3> safe = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, pos);
        if (safe.isPresent() && !hasLavaAt(level, BlockPos.containing(safe.get()))) {
            return safe.get();
        }

        Vec3 nearby = findNearbySafe(level, pos);
        if (nearby != null) return nearby;

        if (dim == Level.NETHER) {
            Vec3 ceiling = new Vec3(pos.getX() + 0.5, 128.0, pos.getZ() + 0.5);
            if (!hasLavaAt(level, BlockPos.containing(ceiling))) return ceiling;
        }
        int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY();
        Vec3 surface = new Vec3(pos.getX() + 0.5, surfaceY + 1.0, pos.getZ() + 0.5);
        if (!hasLavaAt(level, BlockPos.containing(surface))) return surface;

        return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    private static boolean hasLavaAt(ServerLevel level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getFluidState(pos.offset(dx, 0, dz)).is(FluidTags.LAVA)) return true;
            }
        }
        return level.getFluidState(pos.below()).is(FluidTags.LAVA);
    }

    private Vec3 findNearbySafe(ServerLevel level, BlockPos origin) {
        for (int radius = 3; radius <= 9; radius += 2) {
            for (int dy = -1; dy <= 10; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                        BlockPos candidate = origin.offset(dx, dy, dz);
                        Optional<Vec3> safe = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, candidate);
                        if (safe.isPresent() && !hasLavaAt(level, BlockPos.containing(safe.get()))) {
                            return safe.get();
                        }
                    }
                }
            }
        }
        return null;
    }
}
