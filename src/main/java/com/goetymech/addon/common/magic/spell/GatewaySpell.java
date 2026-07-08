package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.common.utils.GatewayUtil;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GatewaySpell extends Spell {

    private static final String TAG_SAFE_FAILED_POS = "GatewaySafeFailedPos";

    @Override
    public int defaultSoulCost() {
        return ModConfig.GATEWAY_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 100;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.GATEWAY_COOLDOWN.get();
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
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        if (!(caster instanceof ServerPlayer player)) return;

        ItemStack offhand = player.getOffhandItem();

        TagKey<Structure> tag = GatewayUtil.deriveStructureTag(
                player.getServer().registryAccess(), offhand);
        if (tag == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.gateway.no_eye"), true);
            return;
        }

        BlockPos structPos = GatewayUtil.findNearestStructure(worldIn, tag, player.blockPosition());
        if (structPos == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.gateway.no_structure"), true);
            return;
        }

        AABB bounds = GatewayUtil.getStructureBoundingBox(worldIn, structPos, tag);
        if (bounds == null) {
            bounds = new AABB(structPos).inflate(48);
        }

        boolean forceTeleport = checkForceFlag(player);
        Vec3 pos;

        if (forceTeleport) {
            BlockPos hightPos = worldIn.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, structPos);
            if (hightPos.getY() <= worldIn.getMinBuildHeight()) {
                hightPos = structPos.atY(worldIn.getSeaLevel());
            }
            pos = Vec3.atCenterOf(hightPos);
            clearForceFlag(player);
        } else {
            BlockPos safePos = GatewayUtil.findSafeTeleportPos(worldIn, bounds, player);
            if (safePos != null) {
                pos = Vec3.atCenterOf(safePos);
            } else {
                setForceFlag(player, structPos);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.gateway.danger_area"), true);
                return;
            }
        }

        int cost = ModConfig.GATEWAY_COST.get();
        if (rightStaff(staff)) {
            cost = Math.max(1, cost / 2);
        }
        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, cost);
        }

        ItemStack focus = WandUtil.findFocus(caster);
        if (!focus.isEmpty()) {
            SEHelper.addCooldown(player, focus.getItem(), ModConfig.GATEWAY_COOLDOWN.get());
        }

        GatewayUtil.teleportPlayer(worldIn, player, pos);
        this.playSound(worldIn, caster, SoundEvents.ENDERMAN_TELEPORT);
    }

    private boolean checkForceFlag(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        CompoundTag goetyMech = data.getCompound("GoetyMechData");
        return goetyMech.contains(TAG_SAFE_FAILED_POS);
    }

    private void setForceFlag(ServerPlayer player, BlockPos pos) {
        CompoundTag data = player.getPersistentData();
        CompoundTag goetyMech = data.getCompound("GoetyMechData");
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        posTag.putString("dim", player.level().dimension().location().toString());
        goetyMech.put(TAG_SAFE_FAILED_POS, posTag);
        data.put("GoetyMechData", goetyMech);
    }

    private void clearForceFlag(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        CompoundTag goetyMech = data.getCompound("GoetyMechData");
        goetyMech.remove(TAG_SAFE_FAILED_POS);
        data.put("GoetyMechData", goetyMech);
    }
}
