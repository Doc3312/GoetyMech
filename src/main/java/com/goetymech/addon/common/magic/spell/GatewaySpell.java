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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 同维度结构传送法术。副手持结构之眼定位。
 */
public class GatewaySpell extends Spell {

    private static final String TAG_SAFE_FAILED_POS = "GatewaySafeFailedPos";

    private boolean tryMapTeleport(ServerPlayer player, ServerLevel worldIn, ItemStack offhand) {
        BlockPos mapTarget = GatewayUtil.readMapTarget(offhand, worldIn);
        if (mapTarget == null) return false;

        player.displayClientMessage(
                Component.translatable("info.goetymech.gateway.map_target_found"), true);
        doTeleport(worldIn, player, mapTarget);
        return true;
    }

    private void doTeleport(ServerLevel level, ServerPlayer player, BlockPos target) {
        AABB bounds = new AABB(target).inflate(48);
        boolean forceTeleport = checkForceFlag(player);
        Vec3 pos;

        if (forceTeleport) {
            BlockPos hightPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, target);
            if (hightPos.getY() <= level.getMinBuildHeight()) {
                hightPos = target.atY(level.getSeaLevel());
            }
            pos = Vec3.atCenterOf(hightPos);
            clearForceFlag(player);
        } else {
            BlockPos safePos = GatewayUtil.findSafeTeleportPos(level, bounds, player);
            if (safePos != null) {
                pos = Vec3.atCenterOf(safePos);
            } else {
                setForceFlag(player, target);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.gateway.danger_area"), true);
                return;
            }
        }

        int cost = ModConfig.GATEWAY_COST.get();
        ItemStack focus = WandUtil.findFocus(player);
        if (focus.isEmpty()) return;
        if (rightStaff(focus)) {
            cost = Math.max(1, cost / 2);
        }
        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, cost);
        }
        SEHelper.addCooldown(player, focus.getItem(), ModConfig.GATEWAY_COOLDOWN.get());

        GatewayUtil.teleportPlayer(level, player, pos);
        this.playSound(level, player, SoundEvents.ENDERMAN_TELEPORT);
    }

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

        if (offhand.is(net.minecraft.world.item.Items.FILLED_MAP)) {
            if (GatewayUtil.isMapWithMarker(offhand, worldIn)) {
                tryMapTeleport(player, worldIn, offhand);
            } else {
                player.displayClientMessage(
                        Component.translatable("info.goetymech.gateway.no_marker"), true);
            }
            return;
        }

        if (offhand.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.gateway.no_eye"), true);
            return;
        }

        ItemStack backup = offhand.copy();
        GatewayUtil.pendingEyeCapture = true;
        GatewayUtil.pendingEyePos = null;

        offhand.use(worldIn, player, InteractionHand.OFF_HAND);

        GatewayUtil.pendingEyeCapture = false;
        player.setItemInHand(InteractionHand.OFF_HAND, backup);

        BlockPos structPos = GatewayUtil.pendingEyePos;
        GatewayUtil.pendingEyePos = null;

        if (structPos == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.gateway.no_structure"), true);
            return;
        }

        doTeleport(worldIn, player, structPos);
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
