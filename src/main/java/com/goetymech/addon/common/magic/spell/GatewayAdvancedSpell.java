package com.goetymech.addon.common.magic.spell;

import com.goetymech.addon.common.events.FreecamSpectatorHandler;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GatewayAdvancedSpell extends Spell {

    private static final String TAG_SAFE_FAILED_POS = "GatewayAdvSafeFailedPos";

    private boolean tryMapTeleport(ServerPlayer player, ItemStack offhand) {
        if (player.getServer() == null) return false;

        ServerLevel playerLevel = (ServerLevel) player.level();
        BlockPos target = GatewayUtil.readMapTarget(offhand, playerLevel);
        if (target == null) return false;

        player.displayClientMessage(
                Component.translatable("info.goetymech.gateway.map_target_found"), true);
        doTeleport(playerLevel, player, target);
        return true;
    }

    private void doTeleport(ServerLevel level, ServerPlayer player, BlockPos target) {
        AABB bounds = new AABB(target).inflate(48);
        boolean forceTeleport = checkForceFlag(player);
        Vec3 teleportPos;

        if (forceTeleport) {
            BlockPos hightPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, target);
            if (hightPos.getY() <= level.getMinBuildHeight()) {
                hightPos = target.atY(level.getSeaLevel());
            }
            teleportPos = Vec3.atCenterOf(hightPos);
            clearForceFlag(player);
        } else {
            BlockPos safePos = GatewayUtil.findSafeTeleportPos(level, bounds, player);
            if (safePos != null) {
                teleportPos = Vec3.atCenterOf(safePos);
            } else {
                setForceFlag(player, target);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.gateway.danger_area"), true);
                return;
            }
        }

        int cost = ModConfig.GATEWAY_ADV_COST.get();
        ItemStack focus = WandUtil.findFocus(player);
        if (focus.isEmpty()) return;
        if (rightStaff(focus)) {
            cost = Math.max(1, cost / 2);
        }
        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, cost);
        }
        SEHelper.addCooldown(player, focus.getItem(), ModConfig.GATEWAY_ADV_COOLDOWN.get());

        GatewayUtil.teleportPlayer(level, player, teleportPos);
        triggerFreecam(level, player);
        this.playSound(level, player, SoundEvents.ENDERMAN_TELEPORT);
    }

    @Override
    public int defaultSoulCost() {
        return ModConfig.GATEWAY_ADV_COST.get();
    }

    @Override
    public int defaultCastDuration() {
        return 100;
    }

    @Override
    public int defaultSpellCooldown() {
        return ModConfig.GATEWAY_ADV_COOLDOWN.get();
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
        if (player.getServer() == null) return;

        ItemStack offhand = player.getOffhandItem();

        if (offhand.is(Items.FILLED_MAP)) {
            if (GatewayUtil.isMapWithMarker(offhand, (ServerLevel) player.level())) {
                tryMapTeleport(player, offhand);
            } else {
                player.displayClientMessage(
                        Component.translatable("info.goetymech.gateway.no_marker"), true);
            }
            return;
        }

        TagKey<Structure> tag = GatewayUtil.deriveStructureTag(
                player.getServer().registryAccess(), offhand);
        if (tag == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.gateway.no_eye"), true);
            return;
        }

        BlockPos bestPos = null;
        ServerLevel bestLevel = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos playerPos = player.blockPosition();

        for (ServerLevel level : player.getServer().getAllLevels()) {
            BlockPos structPos = GatewayUtil.findNearestStructure(level, tag, playerPos);
            if (structPos == null) continue;
            double dist = structPos.distToCenterSqr(playerPos.getX(), playerPos.getY(), playerPos.getZ());
            if (dist < bestDist) {
                bestDist = dist;
                bestPos = structPos;
                bestLevel = level;
            }
        }

        if (bestPos == null || bestLevel == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.gateway.no_structure"), true);
            return;
        }

        AABB bounds = GatewayUtil.getStructureBoundingBox(bestLevel, bestPos, tag);
        if (bounds == null) {
            bounds = new AABB(bestPos).inflate(48);
        }

        boolean forceTeleport = checkForceFlag(player);
        Vec3 teleportPos;

        if (forceTeleport) {
            BlockPos hightPos = bestLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, bestPos);
            if (hightPos.getY() <= bestLevel.getMinBuildHeight()) {
                hightPos = bestPos.atY(bestLevel.getSeaLevel());
            }
            teleportPos = Vec3.atCenterOf(hightPos);
            clearForceFlag(player);
        } else {
            BlockPos safePos = GatewayUtil.findSafeTeleportPos(bestLevel, bounds, player);
            if (safePos != null) {
                teleportPos = Vec3.atCenterOf(safePos);
            } else {
                setForceFlag(player, bestPos);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.gateway.danger_area"), true);
                return;
            }
        }

        int cost = ModConfig.GATEWAY_ADV_COST.get();
        if (rightStaff(staff)) {
            cost = Math.max(1, cost / 2);
        }
        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, cost);
        }

        ItemStack focus = WandUtil.findFocus(caster);
        if (!focus.isEmpty()) {
            SEHelper.addCooldown(player, focus.getItem(), ModConfig.GATEWAY_ADV_COOLDOWN.get());
        }

        GatewayUtil.teleportPlayer(bestLevel, player, teleportPos);
        triggerFreecam(bestLevel, player);
        this.playSound(worldIn, caster, SoundEvents.ENDERMAN_TELEPORT);
    }

    private void triggerFreecam(ServerLevel level, ServerPlayer player) {
        FreecamSpectatorHandler.activate(player);
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                ModConfig.FREECAM_DURATION.get(),
                0, false, false, false
        ));
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
