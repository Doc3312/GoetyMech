package com.goetymech.addon.common.events;

import com.Polarice3.Goety.client.particles.AoEParticleOption;
import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.common.entities.util.CameraShake;
import com.Polarice3.Goety.common.entities.util.ModFallingBlock;
import com.Polarice3.Goety.utils.BlockFinder;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.utils.CuriosHelper;
import com.goetymech.addon.common.utils.WallUtil;
import com.goetymech.addon.common.data.AssimilationSaveManager;
import com.goetymech.addon.common.net.s2c.S2CAssimilationDataPacket;
import com.goetymech.addon.common.net.s2c.S2CSlamImpactPacket;
import com.goetymech.addon.common.net.s2c.S2CSlamStatePacket;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PrototypeEvents {

    private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private static final String TAG_SLAMMING = "goetymech.slamming";
    private static final String TAG_SLAM_TICK = "goetymech.slam_tick";
    private static final String TAG_SLAM_START_Y = "goetymech.slam_start_y";
    private static final String TAG_PREV_CROUCH = "goetymech.prev_crouch";

    // 鈹€鈹€ Ring animation sequencer 鈹€鈹€
    private static final int RINGS_DELAY = 1;
    private static final int SLAM_DELAY_TICKS = 40; // 2绉?
    private static final Map<ServerLevel, List<SlamAnimData>> ACTIVE_SLAMS = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, List<DelayedDamageData>> PENDING_DAMAGES = new ConcurrentHashMap<>();
    private static final Set<UUID> ASSIMILATION_BONUS_GUARD = new HashSet<>();

    private static class SlamAnimData {
        final UUID playerId;
        final Vec3 center;
        final int floorY;
        final float radius;
        final float fallDistance;
        final int totalRings;
        final List<BlockAnimData> blocks = new ArrayList<>();
        final List<DelayedBlock> pendingBlocks = new ArrayList<>();
        final RandomSource random;
        int nextRingToSpawn = 0;
        int animTick = 0;
        boolean allRingsSpawned = false;

        SlamAnimData(UUID playerId, Vec3 center, int floorY, float radius, float fallDistance, int totalRings, RandomSource random) {
            this.playerId = playerId;
            this.center = center;
            this.floorY = floorY;
            this.radius = radius;
            this.fallDistance = fallDistance;
            this.totalRings = totalRings;
            this.random = random;
        }
    }

    private static class BlockAnimData {
        final ModFallingBlock entity;
        final int spawnAnimTick;
        final int ringIndex;

        BlockAnimData(ModFallingBlock entity, int spawnAnimTick, int ringIndex) {
            this.entity = entity;
            this.spawnAnimTick = spawnAnimTick;
            this.ringIndex = ringIndex;
        }

        int getAge(int currentAnimTick) {
            return currentAnimTick - spawnAnimTick;
        }
    }

    private static class DelayedBlock {
        final ModFallingBlock entity;
        final int spawnTick;
        final int ringIndex;

        DelayedBlock(ModFallingBlock entity, int spawnTick, int ringIndex) {
            this.entity = entity;
            this.spawnTick = spawnTick;
            this.ringIndex = ringIndex;
        }
    }

    private static class DelayedDamageData {
        final UUID playerId;
        final double px, groundY, pz;
        final float damage, radius;
        final long triggerTick;

        DelayedDamageData(UUID playerId, double px, double groundY, double pz, float damage, float radius, long triggerTick) {
            this.playerId = playerId;
            this.px = px;
            this.groundY = groundY;
            this.pz = pz;
            this.damage = damage;
            this.radius = radius;
            this.triggerTick = triggerTick;
        }
    }

    // 鈹€鈹€ 浼ゅ涔樺尯 + 闄勫姞榄旀硶浼ゅ 鈹€鈹€

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        if (victim instanceof Player player && source.getEntity() != null) {
            if (PrototypeItem.hasAnyPrototype(player)) {
                int kills = PrototypeItem.getAssimilationKills(player);
                int threshold = PrototypeItem.getDamageImmunityThreshold(kills);
                if (threshold > 0 && event.getAmount() <= threshold) {
                    event.setCanceled(true);
                    PrototypeItem.setKnockbackImmune(player, true);
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4F, 1.8F);
                    return;
                }
            }
        }

        if (!(source.getEntity() instanceof Player player)) return;
        if (!PrototypeItem.hasAnyPrototype(player)) return;

        int kills = PrototypeItem.getAssimilationKills(player);

        float mult = PrototypeItem.getDamageMultiplier(kills);
        if (mult > 1.001f) {
            event.setAmount(event.getAmount() * mult);
        }

        if (!ASSIMILATION_BONUS_GUARD.add(player.getUUID())) return;
        try {
            float bonus = PrototypeItem.getBonusMagicDamage(kills);
            if (bonus > 0) {
            victim.hurt(
                    victim.level().damageSources().indirectMagic(player, player),
                        bonus);
            }
        } finally {
            ASSIMILATION_BONUS_GUARD.remove(player.getUUID());
        }
    }

    // 鈹€鈹€ 鍧犺惤鍏嶄激 鈹€鈹€

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player
                && (PrototypeItem.isSlamming(player)
                || PrototypeItem.isHoldingPrototype(player)
                || CuriosHelper.hasPrototypeInCurios(player))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player
                && PrototypeItem.getSlamInvulnTick(player) > 0) {
            event.setCanceled(true);
        }
    }

    // 鈹€鈹€ 鍑婚€€鍏嶇柅锛堥厤鍚堝厤浼ら槇鍊硷細浼ゅ 鈮?闃堝€兼椂鍏嶇柅鍑婚€€锛?鈹€鈹€

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player
                && PrototypeItem.isKnockbackImmune(player)) {
            PrototypeItem.setKnockbackImmune(player, false);
            event.setCanceled(true);
        }
    }

    // 鈹€鈹€ LivingTick: Slam 鐘舵€佹満锛坒ires before updateFallFlying 鈫?鍙悓 tick 鍋滈锛?鈹€鈹€

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        boolean slamming = player.getPersistentData().getBoolean(TAG_SLAMMING);

        if (!slamming && isFlyingWithPrototype(player)) {
            handleSlamTrigger(player);
        }

        if (slamming) {
            handleSlamTick(player);
        }

        PrototypeItem.setPrevCrouch(player, player.isShiftKeyDown());

        int invuln = PrototypeItem.getSlamInvulnTick(player);
        if (invuln > 0) {
            PrototypeItem.setSlamInvulnTick(player, invuln - 1);
        }

        PrototypeItem.setKnockbackImmune(player, false);
    }

    // 鈹€鈹€ PlayerTick (END): 鎺ㄨ繘 + 鎶?drag锛坒ires after travel 鈫?淇グ鎸佷箙鍒颁笅涓€ tick锛?鈹€鈹€

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide) return;

        boolean hasPrototype = PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player);
        boolean isSlamming = player.getPersistentData().getBoolean(TAG_SLAMMING);

        if (event.phase == TickEvent.Phase.START) {
            Vec3 startM = player.getDeltaMovement();
            double startHSpeed = Math.sqrt(startM.x * startM.x + startM.z * startM.z);
            GoetyMechMod.LOGGER.debug("D START: zza={}, onGround={}, isFly={}, hSpeed={}",
                    player.zza, player.onGround(), player.isFallFlying(), startHSpeed);

            // 鈹€鈹€ 鐖妫€娴嬶紙鏀惧湪椋炶鍓嶉潰锛?鈹€鈹€
            if (hasPrototype && !isSlamming) {
                if (PrototypeItem.isWallRunning(player)) {
                    handleWallRunTick(player);
                } else if (player.isSprinting() && !player.isFallFlying() && !player.isInWater()) {
                    tryStartWallRun(player);
                }
            }

            // 鈹€鈹€ Ground takeoff / mid-air flight start 鈹€鈹€
            if (!PrototypeItem.isWallRunning(player)
                    && (PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player))
                    && !player.isFallFlying()) {
                int charge = PrototypeItem.getJumpChargeTick(player);
                boolean isCJ = PrototypeItem.isChargedJumping(player);
                int kills = PrototypeItem.getAssimilationKills(player);
                double startHS = Math.sqrt(startM.x * startM.x + startM.z * startM.z);
                GoetyMechMod.LOGGER.debug("PROTO_DIAG START: isCJ={}, charge={}, onGround={}, zza={}, isSprint={}, isFly={}, hSpeed={}",
                        isCJ, charge, player.onGround(), player.zza, player.isSprinting(),
                        player.isFallFlying(), String.format("%.4f", startHS));
                if (player.onGround() && player.zza > 0.0F && charge == 0 && !player.isSprinting()) {
                    Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(m.x, 1.5, m.z);
                    GoetyMechMod.LOGGER.debug("Prototype ground takeoff: zza={}, motion={}", player.zza, m);
                } else if (PrototypeItem.hasFlight(kills) && !player.onGround() && player.zza > 0.0F && charge == 0 && !isCJ) {
                    player.startFallFlying();
                    Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(m.x, Math.max(m.y, 0.3), m.z);
                    GoetyMechMod.LOGGER.debug("Prototype mid-air deployment: motion={}", m);
                }
            }


            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            // 鈹€鈹€ Post-travel diagnostic 鈹€鈹€
            Vec3 postTravelM = player.getDeltaMovement();
            GoetyMechMod.LOGGER.debug("D POST_TRAVEL: zza={}, motion=({}, {}, {}), onGround={}",
                    player.zza,
                    String.format("%.4f", postTravelM.x), String.format("%.4f", postTravelM.y), String.format("%.4f", postTravelM.z),
                    player.onGround());

            // 鈹€鈹€ 杩借釜瀹為檯鍦伴潰閫熷害锛堜粎褰撴鍦ㄧЩ鍔ㄦ椂鏇存柊锛岃搫鍔涙椂涓嶈鐩栵級 鈹€鈹€
            double moved = Math.sqrt(
                    (player.getX() - player.xo) * (player.getX() - player.xo)
                    + (player.getZ() - player.zo) * (player.getZ() - player.zo));
            if (hasPrototype && player.onGround() && !player.isFallFlying()
                    && player.isSprinting() && moved > 0.001) {
                PrototypeItem.setGroundSpeed(player, moved);
            }

            // 鈹€鈹€ 鍦伴潰鍐插埡鍔犻€?鈹€鈹€
            if (hasPrototype && !isSlamming
                    && !PrototypeItem.isWallRunning(player)
                    && !player.isFallFlying()
                    && player.onGround()
                    && player.isSprinting()) {
                int kills = PrototypeItem.getAssimilationKills(player);
                double tierMult = PrototypeItem.getTierMultiplier(kills);
                int current = PrototypeItem.getSprintTick(player) + 1;
                PrototypeItem.setSprintTick(player, current);
                AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (attr != null) {
                    double attrBefore = attr.getValue();
                    boolean hadMod = attr.getModifier(SPRINT_MODIFIER_UUID) != null;
                    GoetyMechMod.LOGGER.debug("SprintTick: current={}, attr={}, hadMod={}, moved={}",
                            current, String.format("%.4f", attrBefore), hadMod,
                            String.format("%.8f", moved));
                    attr.removeModifier(SPRINT_MODIFIER_UUID);
                    if (current >= 1) {
                        double t = Math.min((current - 1) / 80.0, 1.0);
                        double amount = t * ModConfig.SPRINT_MAX_MULTIPLIER.get() * tierMult;
                        attr.addTransientModifier(new AttributeModifier(
                                SPRINT_MODIFIER_UUID, "prototype_sprint", amount,
                                AttributeModifier.Operation.MULTIPLY_TOTAL));
                        double attrAfter = attr.getValue();
                        GoetyMechMod.LOGGER.debug("SprintMod AFTER: current={}, amount={}, attr {}鈫抺}, hasMod={}",
                                current, String.format("%.4f", amount),
                                String.format("%.4f", attrBefore), String.format("%.4f", attrAfter),
                                attr.getModifier(SPRINT_MODIFIER_UUID) != null);
                    }
                }
            } else if (!player.isSprinting()) {
                PrototypeItem.setSprintTick(player, 0);
                PrototypeItem.setGroundSpeed(player, 0);
                AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (attr != null) {
                    attr.removeModifier(SPRINT_MODIFIER_UUID);
                }
            }


            // 鈹€鈹€ Charged jump 姘村钩閫熷害鎸佺画娉ㄥ叆锛堟姷娑?move() 鐨勬懇鎿﹁“鍑忥級 鈹€鈹€
            if (PrototypeItem.isWallRunning(player)) {
                PrototypeItem.clearChargedJump(player);
            } else if (PrototypeItem.isChargedJumping(player)) {
                Vec3 beforeCJ = player.getDeltaMovement();
                double beforeHSpeed = Math.sqrt(beforeCJ.x * beforeCJ.x + beforeCJ.z * beforeCJ.z);
                double cjHx = PrototypeItem.getCjHx(player);
                double cjHz = PrototypeItem.getCjHz(player);
                double targetHSpeed = Math.sqrt(cjHx * cjHx + cjHz * cjHz);
                if (player.onGround()) {
                    PrototypeItem.clearChargedJump(player);
                    GoetyMechMod.LOGGER.debug("PROTO_DIAG CJ_CLEAR onGround: beforeHSpeed={}, targetHSpeed={}, isFly={}",
                            String.format("%.4f", beforeHSpeed), String.format("%.4f", targetHSpeed),
                            player.isFallFlying());
                } else {
                    Vec3 cur = player.getDeltaMovement();
                    player.setDeltaMovement(cjHx, cur.y, cjHz);
                    Vec3 afterCJ = player.getDeltaMovement();
                    double afterHSpeed = Math.sqrt(afterCJ.x * afterCJ.x + afterCJ.z * afterCJ.z);
                    GoetyMechMod.LOGGER.debug("PROTO_DIAG CJ_INJECT: beforeHSpeed={}, target=({},{}), afterHSpeed={}, vy={}, isFly={}",
                            String.format("%.4f", beforeHSpeed),
                            String.format("%.4f", cjHx), String.format("%.4f", cjHz),
                            String.format("%.4f", afterHSpeed),
                            String.format("%.4f", cur.y),
                            player.isFallFlying());
                }
            }

            Vec3 endM = player.getDeltaMovement();
            double endHSpeed = Math.sqrt(endM.x * endM.x + endM.z * endM.z);
            if (hasPrototype) {
                GoetyMechMod.LOGGER.debug("D END: onGround={}, isFly={}, hSpeed={}, sprintTick={}, zza={}",
                        player.onGround(), player.isFallFlying(), endHSpeed,
                        PrototypeItem.getSprintTick(player),
                        player.zza);
            }

            // 鈹€鈹€ Wall movement (server tracks state only; client handles velocity via setDeltaMovement) 鈹€鈹€
            if (PrototypeItem.isWallRunning(player)) {
                return;
            }

            // 鈹€鈹€ 闈為琛岀姸鎬?鈹€鈹€
            if (!player.isFallFlying()) {
                return;
            }
        }
    }

    // 鈹€鈹€ 妫€娴?鈹€鈹€

    private static boolean isFlyingWithPrototype(Player player) {
        if (!player.isFallFlying()) return false;
        if (PrototypeItem.isHoldingPrototype(player)) return true;
        return CuriosHelper.hasPrototypeInCurios(player);
    }

    // 鈹€鈹€ Wall-running detection 鈹€鈹€

    private static void tryStartWallRun(Player player) {
        if (!player.isSprinting()) return;
        Vec3 wallDir = WallUtil.getWallDirection(player, 0.5);
        if (wallDir == null) return;
        if (WallUtil.getWallFriction(player, wallDir) >= 0.8f) return;
        PrototypeItem.setWallRunning(player, true);
        PrototypeItem.setWallNormal(player, wallDir);
        PrototypeItem.setWallRunTicks(player, 0);
        if (player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.42, 0));
            GoetyMechMod.LOGGER.debug("Prototype wall-run LIFT: onGround lift vy=0.42");
        }
        GoetyMechMod.LOGGER.debug("Prototype wall-run START: normal={}", wallDir);
    }

    private static void handleWallRunTick(Player player) {
        Vec3 wallN = PrototypeItem.getWallNormal(player);
        if (wallN.lengthSqr() < 0.5) {
            stopWallRun(player);
            return;
        }

        if (player.isShiftKeyDown()
                || player.isInWater() || player.isInLava()) {
            stopWallRun(player);
            return;
        }

        // 鈹€鈹€ 瑙掕惤妫€娴嬶細褰撳墠澧欓潰娑堝け 鈫?鎵弿鎵€鏈夋柟鍚戞壘鏈€杩戠殑澧?鈹€鈹€
        Vec3 currentWall = WallUtil.getWallDirection(player, 0.5);
        if (currentWall == null) {
            Vec3 newDir = WallUtil.findBestWallDirection(player, 0.8);
            if (newDir != null) {
                if (WallUtil.getWallFriction(player, newDir) >= 0.8f) {
                    stopWallRun(player);
                    return;
                }
                PrototypeItem.setWallNormal(player, newDir);
                GoetyMechMod.LOGGER.debug("Prototype wall-run CORNER: new normal={}", newDir);
            } else {
                stopWallRun(player);
                return;
            }
        } else {
            // 鈹€鈹€ 澧欓潰鎽╂摝妫€娴嬶紙鍐伴潰涓嶅彲璺戯級 鈹€鈹€
            if (WallUtil.getWallFriction(player, wallN) >= 0.8f) {
                stopWallRun(player);
                return;
            }
        }

        // 鈹€鈹€ 绱姞璺戝 tick 鈹€鈹€
        PrototypeItem.setWallRunTicks(player, PrototypeItem.getWallRunTicks(player) + 1);

        // Jump-off is handled via C2SWallJumpOffPacket from client
    }

    public static void handleWallJumpOff(ServerPlayer player) {
        Vec3 wallN = PrototypeItem.getWallNormal(player);
        if (wallN.lengthSqr() < 0.5) return;
        double tierMult = PrototypeItem.getTierMultiplier(PrototypeItem.getAssimilationKills(player));
        Vec3 launch = wallN.scale(ModConfig.WALL_JUMP_OFF_VELOCITY.get() * tierMult);
        Vec3 m = player.getDeltaMovement();
        player.setDeltaMovement(launch.x, Math.max(launch.y + 0.2, m.y + 0.2), launch.z);
        stopWallRun(player);
    }

    private static void stopWallRun(Player player) {
        PrototypeItem.setWallRecoveryTicks(player, ModConfig.WALL_RECOVERY_TICKS.get());
        PrototypeItem.clearWallState(player);
        PrototypeItem.setWallRunning(player, false);
    }

    // 鈹€鈹€ Jump charging (called from C2S packet handler) 鈹€鈹€

    public static void handleJumpChargeFromClient(ServerPlayer player, int action, float forward, boolean jumping, double clientGroundSpeed) {
        if (player.isFallFlying() || PrototypeItem.isWallRunning(player)) return;
        boolean inWater = player.isInWater() || player.level().getFluidState(player.blockPosition()).is(FluidTags.WATER);
        if (action > 0 && !inWater) {
            double startY = PrototypeItem.getChargeStartY(player);
            if (startY == 0.0) {
                PrototypeItem.setChargeStartY(player, player.getY());
            } else if (player.onGround()) {
                PrototypeItem.setChargeStartY(player, player.getY());
            } else if (startY - player.getY() > 6.0) {
                int charge = PrototypeItem.getJumpChargeTick(player);
                if (charge > 0) {
                    applyChargedJump(player, charge);
                }
                return;
            }
        }
        boolean hasPrototype = PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player);
        if (!hasPrototype) return;

        player.setPlayerInput(forward, 0, jumping, false);

        if (clientGroundSpeed > 0.001) {
            PrototypeItem.setGroundSpeed(player, clientGroundSpeed);
            if (action > 0) {
                PrototypeItem.setCjStartSpeed(player, clientGroundSpeed);
            }
        }

        if (action > 0) {
            int capped = Math.min(action, ModConfig.JUMP_MAX_CHARGE_TICKS.get());
            PrototypeItem.setJumpChargeTick(player, capped);
        } else if (action == 0) {
            int charge = PrototypeItem.getJumpChargeTick(player);
            if (charge > 0) {
                applyChargedJump(player, charge);
            }
        }
    }

    private static void applyChargedJump(Player player, int charge) {
        double tierMult = PrototypeItem.getTierMultiplier(PrototypeItem.getAssimilationKills(player));
        if (player.isInWater()) {
            Vec3 look = player.getLookAngle();
            int threshold = ModConfig.JUMP_CHARGE_THRESHOLD.get();
            int maxTicks = ModConfig.JUMP_MAX_CHARGE_TICKS.get();
            double minSpeed = ModConfig.SWIM_CHARGE_MIN_SPEED.get();
            double maxSpeed = ModConfig.SWIM_CHARGE_MAX_SPEED.get();
            double t = charge >= threshold
                    ? Math.min((double)(charge - threshold) / (double)(maxTicks - threshold), 1.0)
                    : 0.0;
            double speed = minSpeed + t * (maxSpeed * tierMult - minSpeed);
            player.setDeltaMovement(look.scale(speed));
            PrototypeItem.setChargedJumping(player, true);
            PrototypeItem.clearJumpCharge(player);
            player.hurtMarked = true;
            return;
        }
        Vec3 m = player.getDeltaMovement();
        double hSpeed = Math.sqrt(m.x * m.x + m.z * m.z);
        double groundSpeed = PrototypeItem.getGroundSpeed(player);
        double attrSpeed = player.getSpeed();
        if (player.isSprinting()) attrSpeed *= 1.3;
        double cjStartSpeed = PrototypeItem.getCjStartSpeed(player);
        double bestSpeed = Math.max(hSpeed, Math.max(groundSpeed, Math.max(attrSpeed, cjStartSpeed)));
        if (bestSpeed > 0.001 && bestSpeed > hSpeed + 0.001
                && (groundSpeed > 0.001 || hSpeed > 0.001 || cjStartSpeed > 0.001 || player.isSprinting())) {
            bestSpeed *= 2.5;
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            m = new Vec3(-Mth.sin(yaw) * bestSpeed, m.y, Mth.cos(yaw) * bestSpeed);
            GoetyMechMod.LOGGER.debug("Sprint inertia: hSpeed={}, groundSpeed={}, attrSpeed={}, cjStartSpeed={}, bestSpeed={}",
                    String.format("%.4f", hSpeed), String.format("%.4f", groundSpeed),
                    String.format("%.4f", attrSpeed), String.format("%.4f", cjStartSpeed),
                    String.format("%.4f", bestSpeed));
        }
        PrototypeItem.setCjStartSpeed(player, 0);
        PrototypeItem.setCjHx(player, m.x);
        PrototypeItem.setCjHz(player, m.z);
        double setHSpeed = Math.sqrt(m.x * m.x + m.z * m.z);
        int threshold = ModConfig.JUMP_CHARGE_THRESHOLD.get();
        if (charge >= threshold) {
            double minH = ModConfig.JUMP_CHARGE_MIN_HEIGHT.get();
            double maxH = ModConfig.JUMP_CHARGE_MAX_HEIGHT.get();
            int maxTicks = ModConfig.JUMP_MAX_CHARGE_TICKS.get();
            double t = (double)(charge - threshold) / (double)(maxTicks - threshold);
            t = Math.min(t, 1.0);
            double height = minH + t * (maxH * tierMult - minH);
            double jumpPower = velocityForHeight(height);
            player.setDeltaMovement(m.x, Math.max(jumpPower, m.y), m.z);
            Vec3 after = player.getDeltaMovement();
            GoetyMechMod.LOGGER.debug("PROTO_DIAG applyChargedJump: charge={}, hSpeed={}, setHSpeed={}, setVY={}, final=({}, {}, {}), isCJ={}",
                    charge, String.format("%.4f", hSpeed),
                    String.format("%.4f", setHSpeed),
                    String.format("%.4f", jumpPower),
                    String.format("%.4f", after.x), String.format("%.4f", after.y), String.format("%.4f", after.z),
                    PrototypeItem.isChargedJumping(player));
        } else if (player.onGround()) {
            double tapH = ModConfig.JUMP_TAP_HEIGHT.get();
            double jumpPower = velocityForHeight(tapH);
            player.setDeltaMovement(m.x, jumpPower, m.z);
            Vec3 after = player.getDeltaMovement();
            GoetyMechMod.LOGGER.debug("PROTO_DIAG applyTapJump: charge={}, hSpeed={}, setHSpeed={}, final=({}, {}, {}), isCJ={}",
                    charge, String.format("%.4f", hSpeed),
                    String.format("%.4f", setHSpeed),
                    String.format("%.4f", after.x), String.format("%.4f", after.y), String.format("%.4f", after.z),
                    PrototypeItem.isChargedJumping(player));
        }
        PrototypeItem.setChargedJumping(player, true);
        PrototypeItem.clearJumpCharge(player);
        player.hurtMarked = true;
    }

    // Minecraft vertical jump with drag: h = 50*v - 4*ln(4/(v+4))/ln(0.98)
    // Bisection to invert: given target height h, find initial velocity v
    private static double velocityForHeight(double targetH) {
        double lo = 0, hi = 10;
        double ln098 = Math.log(0.98);
        for (int i = 0; i < 50; i++) {
            double v = (lo + hi) / 2;
            double h = 50 * v - 4 * Math.log(4 / (v + 4)) / ln098;
            if (h < targetH) lo = v; else hi = v;
        }
        return (lo + hi) / 2;
    }

    // 鈹€鈹€ Slam 鈹€鈹€

    private static void handleSlamTrigger(Player player) {
        boolean nowCrouch = player.isShiftKeyDown();
        boolean prevCrouch = PrototypeItem.getPrevCrouch(player);

        if (nowCrouch && !prevCrouch && PrototypeItem.hasSlam(PrototypeItem.getAssimilationKills(player))) {
            enterSlamState(player);
        }
    }

    private static void enterSlamState(Player player) {
        PrototypeItem.setSlamming(player, true);
        PrototypeItem.setSlamTick(player, 0);
        PrototypeItem.setSlamStartY(player, player.getY());
        PrototypeItem.setSlamInvulnTick(player, 9999);

        // elytraFlightTick() 杩斿洖 false 鈫?鍚?tick 鏅氫簺 updateFallFlying 娓呴櫎椋炶鏍囧織
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, -0.15, motion.z);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 0.6F);

        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CSlamStatePacket(true));
        }
    }

    private static void handleSlamTick(Player player) {
        int tick = PrototypeItem.getSlamTick(player);
        double tierMult = PrototypeItem.getTierMultiplier(PrototypeItem.getAssimilationKills(player));
        boolean onGround = player.onGround();
        boolean inFluid = player.isInWater() || player.isInLava();

        if (onGround || inFluid) {
            triggerSlamImpact(player, onGround);
            return;
        }

        Vec3 motion = player.getDeltaMovement();

        if (tick < ModConfig.SLAM_HOVER_TICKS.get()) {
            player.setDeltaMovement(motion.x * 0.9, 0, motion.z * 0.9);
        } else {
            int fallTick = tick - ModConfig.SLAM_HOVER_TICKS.get();
            double speed = 0.15 + ModConfig.SLAM_GRAVITY_PER_TICK.get() * fallTick;
            speed = Math.min(speed, ModConfig.SLAM_MAX_SPEED.get() * tierMult);
            player.setDeltaMovement(motion.x, -speed, motion.z);
        }

        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
        }

        PrototypeItem.setSlamTick(player, tick + 1);
    }

    // 鈹€鈹€ 鍐插嚮 鈹€鈹€

    private static void triggerSlamImpact(Player player, boolean onGround) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        double startY = PrototypeItem.getSlamStartY(player);
        double fallDistance = Math.max(0, startY - player.getY());
        double tierMult = PrototypeItem.getTierMultiplier(PrototypeItem.getAssimilationKills(player));

        float damage = (float) Math.max(ModConfig.SLAM_MIN_DAMAGE.get(), Math.min(ModConfig.SLAM_MAX_DAMAGE.get() * tierMult, fallDistance * ModConfig.SLAM_DAMAGE_MULTIPLIER.get()));
        float radius = (float) Math.min(ModConfig.SLAM_MAX_RADIUS.get(), Math.max(8.0, fallDistance * ModConfig.SLAM_RADIUS_MULTIPLIER.get()));

        int rings = 4 + Math.max(0, (int)((fallDistance - 10.0f) / 20.0f));
        if (rings > 9) rings = 9;

        int maxRingsByRadius = Math.max(0, (int)Math.floor(radius) - 5);
        rings = Math.min(rings, maxRingsByRadius);

        GoetyMechMod.LOGGER.debug("SlamImpact: startY={}, playerY={}, fallDist={}, radius={}, damage={}, rings={}, multRadius={}, maxRadius={}",
                startY, player.getY(), fallDistance, radius, damage, rings,
                ModConfig.SLAM_RADIUS_MULTIPLIER.get(), ModConfig.SLAM_MAX_RADIUS.get());

        if (radius > 0) {
            applyAoeDamage(player, serverLevel, damage, radius);
            spawnVisuals(player, serverLevel, radius, (float)fallDistance, rings);

            double groundY = Math.floor(BlockFinder.moveDownToGround(player) - 0.0625);
            PENDING_DAMAGES.computeIfAbsent(serverLevel, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(new DelayedDamageData(player.getUUID(), player.getX(), groundY, player.getZ(),
                            damage, radius, serverLevel.getGameTime() + SLAM_DELAY_TICKS));
        }

        serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F,
                0.8F + level.random.nextFloat() * 0.4F);

        if (player instanceof ServerPlayer sp) {
            ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() ->
                            new PacketDistributor.TargetPoint(
                                    player.getX(), player.getY(), player.getZ(),
                                    64, level.dimension())),
                    new S2CSlamImpactPacket(
                            player.getX(), player.getY(), player.getZ(),
                            radius, damage));
        }

        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CSlamStatePacket(false));
        }

        player.fallDistance = 0;
        PrototypeItem.clearSlamState(player);
        PrototypeItem.setSlamInvulnTick(player, 20);
    }

    private static void applyAoeDamage(Player player, ServerLevel level, float damage, float radius) {
        double groundY = Math.floor(BlockFinder.moveDownToGround(player) - 0.0625);
        double px = player.getX();
        double pz = player.getZ();
        GoetyMechMod.LOGGER.debug("AoE damage: radius={}, center=({}, {}, {}), aabbY=[{}, {}]",
                String.format("%.1f", radius),
                String.format("%.2f", px), String.format("%.2f", groundY), String.format("%.2f", pz),
                String.format("%.0f", groundY - 2), String.format("%.0f", groundY + 3));

        AABB aoe = new AABB(
                px - radius, groundY - 2, pz - radius,
                px + radius, groundY + 3, pz + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aoe,
                e -> e != player && e.isAlive() && !e.isSpectator());

        GoetyMechMod.LOGGER.debug("AoE entities in AABB: {}", targets.size());

        DamageSource source = level.damageSources().playerAttack(player);

        for (LivingEntity target : targets) {
            double dx = target.getX() - px;
            double dz = target.getZ() - pz;
            double hDist = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
            double ty = target.getY();
            double vDist = ty - groundY;
            if (hDist > radius) {
                GoetyMechMod.LOGGER.debug("  SKIP {} @ hDist={} (>{}) vDist={} targetY={}",
                        target.getDisplayName().getString(),
                        String.format("%.2f", hDist), String.format("%.1f", radius),
                        String.format("%.2f", vDist), String.format("%.2f", ty));
                continue;
            }
            GoetyMechMod.LOGGER.debug("  HIT {} @ hDist={} vDist={} dmg={}",
                    target.getDisplayName().getString(),
                    String.format("%.2f", hDist), String.format("%.2f", vDist),
                    String.format("%.1f", damage));

            if (target.hurt(source, damage)) {
                double nx = dx / hDist;
                double nz = dz / hDist;
                target.push(nx * 2.5, 0.5 + level.random.nextDouble() * 0.3, nz * 2.5);
                target.hurtMarked = true;
            }
        }
    }

    private static void applyDelayedDamage(Player player, ServerLevel level, DelayedDamageData dd) {
        float halfDmg = dd.damage * 0.5f;
        DamageSource magic = level.damageSources().indirectMagic(player, player);

        AABB aoe = new AABB(
                dd.px - dd.radius, dd.groundY - 2, dd.pz - dd.radius,
                dd.px + dd.radius, dd.groundY + 3, dd.pz + dd.radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aoe,
                e -> e != player && e.isAlive() && !e.isSpectator());

        for (LivingEntity target : targets) {
            double dx = target.getX() - dd.px;
            double dz = target.getZ() - dd.pz;
            double hDist = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
            if (hDist > dd.radius) continue;

            if (target.hurt(magic, halfDmg)) {
                double nx = dx / hDist;
                double nz = dz / hDist;
                target.push(nx * 3.0, 1.0 + level.random.nextDouble() * 0.5, nz * 3.0);
                target.hurtMarked = true;
            }
        }

        level.playSound(null, dd.px, dd.groundY, dd.pz,
                SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 2.0F, 1.0F);

        spawnUpwardShockwave(level, dd);
    }

    private static void spawnUpwardShockwave(ServerLevel level, DelayedDamageData dd) {
        double cx = dd.px;
        double cy = dd.groundY;
        double cz = dd.pz;
        float r = dd.radius;

        // 鈹€鈹€ 鐧借壊榄旀硶鎵╁紶鐜?鈹€鈹€
        level.sendParticles(
                new CircleExplodeParticleOption(0.8f, 0.8f, 1.0f, r, 1),
                cx, cy + 1.25, cz, 1, 0, 0, 0, 0);
    }

    private static void spawnVisuals(Player player, ServerLevel level, float radius, float fallDistance, int rings) {
        var random = level.random;
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        int floorY = (int) Math.floor(BlockFinder.moveDownToGround(player) - 0.0625);

        // 鈹€鈹€ 鍚姩閫愬湀閿欏紑鍗囪捣鍔ㄧ敾 鈹€鈹€
        startRingAnimation(player, level, new Vec3(px, py, pz), floorY, radius, fallDistance, rings, random);

        // 鈹€鈹€ MOBILE 椋炴暎纰庡潡锛堜腑蹇冨悜澶栭锛岃寖鍥?3 鏍煎唴锛?鈹€鈹€
        int debrisCount = 20;
        for (int i = 0; i < debrisCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = random.nextDouble() * 3.0;
            BlockPos targetPos = findGroundPos(level,
                    px + Math.cos(angle) * dist, floorY, pz + Math.sin(angle) * dist);
            if (targetPos == null) continue;
            BlockState state = level.getBlockState(targetPos);
            if (state.isAir()) continue;

            ModFallingBlock debris = new ModFallingBlock(level, 40, state);
            debris.setPos(targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5);
            debris.setDeltaMovement(
                    Math.cos(angle) * (1.0 + random.nextDouble()),
                    0.4 + random.nextDouble() * 0.5,
                    Math.sin(angle) * (1.0 + random.nextDouble()));
            debris.setMode(ModFallingBlock.FallingBlockMode.MOBILE);
            level.addFreshEntity(debris);
        }

        // 鈹€鈹€ 涓績 DEBRIS 纰庡潡锛?5涓紝姘村钩 0.5~2.0 鍚戝椋炴暎锛?鈹€鈹€
        for (int i = 0; i < 15; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = 0.5 + random.nextDouble() * 1.5;
            BlockPos targetPos = findGroundPos(level,
                    px + Math.cos(angle) * dist, floorY, pz + Math.sin(angle) * dist);
            if (targetPos == null) continue;
            BlockState state = level.getBlockState(targetPos);
            if (state.isAir()) continue;

            ModFallingBlock debris = new ModFallingBlock(level, 40, state);
            debris.setPos(targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5);
            debris.setDeltaMovement(
                    Math.cos(angle) * (0.5 + random.nextDouble() * 1.5),
                    0.2 + random.nextDouble() * 0.3,
                    Math.sin(angle) * (0.5 + random.nextDouble() * 1.5));
            debris.setMode(ModFallingBlock.FallingBlockMode.MOBILE);
            level.addFreshEntity(debris);
        }

        // 鈹€鈹€ 绮掑瓙锛氱孩鑹?CircleExplode 澶栧湀杈圭晫锛堝嵆鏃剁垎鍙戯級 鈹€鈹€
        try {
            level.sendParticles(
                    new CircleExplodeParticleOption(1.0f, 0.0f, 0.0f, radius, 2),
                    px, BlockFinder.moveDownToGround(player) + 0.3, pz, 1, 0, 0, 0, 0);
        } catch (Exception ignored) {
        }

        // 鈹€鈹€ 绮掑瓙锛欰oEParticleOption 鍦伴潰鍗婇€忔槑绾㈣壊鍦嗗舰锛堟寔缁樉绀轰激瀹宠寖鍥达級 鈹€鈹€
        try {
            double aoeGroundY = BlockFinder.moveDownToGround(player) + 0.25;
            GoetyMechMod.LOGGER.debug("AoE particle: radius={}, pos=({}, {}, {})",
                    String.format("%.1f", radius),
                    String.format("%.2f", px), String.format("%.2f", aoeGroundY), String.format("%.2f", pz));
            level.sendParticles(
                    new AoEParticleOption(radius, SLAM_DELAY_TICKS),
                    px, aoeGroundY, pz, 0, 0.8, 0.0, 0.0, 1.0);
        } catch (Exception ignored) {
        }

        // 鈹€鈹€ 闀滃ご闇囧姩 CameraShake 鈹€鈹€
        try {
            CameraShake.cameraShake(level, player.position(),
                    radius * 1.5f, 0.15f, 15, 10);
        } catch (Exception ignored) {
        }
    }

    // 鈹€鈹€ Ring animation system 鈹€鈹€

    private static void startRingAnimation(Player player, ServerLevel level, Vec3 center, int floorY,
                                           float radius, float fallDistance, int rings, RandomSource random) {
        if (rings <= 0) return;
        ACTIVE_SLAMS.computeIfAbsent(level, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new SlamAnimData(player.getUUID(), center, floorY, radius, fallDistance, rings, random));
    }

    private static void spawnSingleRing(ServerLevel level, SlamAnimData slam) {
        int ringIdx = slam.nextRingToSpawn;
        double startDist = 5.0;
        double ringInner = startDist + ringIdx;
        double ringOuter = startDist + ringIdx + 1;

        int minX = (int) Math.floor(slam.center.x - ringOuter);
        int maxX = (int) Math.ceil(slam.center.x + ringOuter);
        int minZ = (int) Math.floor(slam.center.z - ringOuter);
        int maxZ = (int) Math.ceil(slam.center.z + ringOuter);

        double cx = slam.center.x;
        double cz = slam.center.z;

        int spawned = 0;
        for (int bx = minX; bx <= maxX; bx++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
                double dx = bx + 0.5 - cx;
                double dz = bz + 0.5 - cz;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < ringInner || dist >= ringOuter) continue;

                BlockPos targetPos = findGroundPos(level, bx + 0.5, slam.floorY, bz + 0.5);
                if (targetPos == null) continue;
                BlockState state = level.getBlockState(targetPos);
                if (state.isAir()) continue;

                if (ringIdx == slam.totalRings - 1) {
                    ModFallingBlock block = new ModFallingBlock(level, 40, state);
                    block.setPos(targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5);
                    block.setMode(ModFallingBlock.FallingBlockMode.MOBILE);
                    double len = Math.sqrt(dx * dx + dz * dz);
                    block.setDeltaMovement(dx / len * (0.3 + slam.random.nextDouble() * 0.6),
                            0.2 + slam.random.nextDouble() * 0.3,
                            dz / len * (0.3 + slam.random.nextDouble() * 0.6));
                    level.addFreshEntity(block);
                    slam.blocks.add(new BlockAnimData(block, slam.animTick, ringIdx));
                    GoetyMechMod.LOGGER.debug("  ring[{}] MOBILE block @({},{},{})",
                            ringIdx,
                            String.format("%.1f", targetPos.getX() + 0.5),
                            targetPos.getY() + 1,
                            String.format("%.1f", targetPos.getZ() + 0.5));
                } else {
                    float desiredHeight = 1.0f + (float)((dist - startDist) / slam.totalRings) * 2.0f + slam.random.nextFloat() * 0.3f;
                    float popUp = (float)Math.sqrt(desiredHeight * 0.2f);

                    ModFallingBlock block = new ModFallingBlock(level,
                            new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5),
                            state, popUp);

                    int delay = slam.random.nextInt(8);
                    GoetyMechMod.LOGGER.debug("  ring[{}] block @({},{},{}): height={} popUp={} delay={}",
                            ringIdx,
                            String.format("%.1f", targetPos.getX() + 0.5),
                            targetPos.getY() + 1,
                            String.format("%.1f", targetPos.getZ() + 0.5),
                            String.format("%.2f", desiredHeight),
                            String.format("%.3f", popUp),
                            delay);
                    if (delay == 0) {
                        level.addFreshEntity(block);
                        slam.blocks.add(new BlockAnimData(block, slam.animTick, ringIdx));
                    } else {
                        slam.pendingBlocks.add(new DelayedBlock(block, slam.animTick + delay, ringIdx));
                    }
                }
                spawned++;
            }
        }
        GoetyMechMod.LOGGER.debug("Slam ring {}: {} blocks, dist [{}-{}], radius={}, floorY={}",
                ringIdx, spawned, String.format("%.1f", ringInner), String.format("%.1f", ringOuter),
                String.format("%.1f", slam.radius), slam.floorY);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        // 鈹€鈹€ Ring animation 鈹€鈹€
        List<SlamAnimData> slams = ACTIVE_SLAMS.get(serverLevel);
        if (slams != null && !slams.isEmpty()) {
            synchronized (slams) {
                Iterator<SlamAnimData> iter = slams.iterator();
                while (iter.hasNext()) {
                    SlamAnimData slam = iter.next();
                    slam.animTick++;

                    slam.pendingBlocks.removeIf(pb -> {
                        if (slam.animTick >= pb.spawnTick) {
                            serverLevel.addFreshEntity(pb.entity);
                            slam.blocks.add(new BlockAnimData(pb.entity, slam.animTick, pb.ringIndex));
                            return true;
                        }
                        return false;
                    });

                    if (!slam.allRingsSpawned && slam.animTick >= slam.nextRingToSpawn * RINGS_DELAY) {
                        spawnSingleRing(serverLevel, slam);
                        slam.nextRingToSpawn++;
                        if (slam.nextRingToSpawn >= slam.totalRings) {
                            slam.allRingsSpawned = true;
                        }
                    }

                    slam.blocks.removeIf(bd -> !bd.entity.isAlive());

                    if (slam.allRingsSpawned && slam.blocks.isEmpty() && slam.pendingBlocks.isEmpty()) {
                        iter.remove();
                    }
                }
            }
            if (slams.isEmpty()) {
                ACTIVE_SLAMS.remove(serverLevel);
            }
        }

        // 鈹€鈹€ Delayed AoE damage 鈹€鈹€
        List<DelayedDamageData> pending = PENDING_DAMAGES.get(serverLevel);
        if (pending != null) {
            synchronized (pending) {
                pending.removeIf(dd -> {
                    if (serverLevel.getGameTime() >= dd.triggerTick) {
                        Player owner = serverLevel.getPlayerByUUID(dd.playerId);
                        if (owner != null) {
                            applyDelayedDamage(owner, serverLevel, dd);
                        }
                        return true;
                    }
                    return false;
                });
            }
            if (pending.isEmpty()) {
                PENDING_DAMAGES.remove(serverLevel);
            }
        }

        // 鈹€鈹€ Indestructible guard: pull prototype from void 鈹€鈹€
        AABB voidBox = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, -64.0, Double.POSITIVE_INFINITY);
        for (ItemEntity item : serverLevel.getEntitiesOfClass(ItemEntity.class, voidBox)) {
            if (PrototypeItem.isPrototype(item.getItem())) {
                item.setPos(item.getX(), 260.0, item.getZ());
                item.setDeltaMovement(0, 0, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ACTIVE_SLAMS.remove(serverLevel);
            PENDING_DAMAGES.remove(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            AssimilationSaveManager.onPlayerLogin(sp);
            int kills = PrototypeItem.getAssimilationKills(sp);
            ModNetwork.sendTo(sp, new S2CAssimilationDataPacket(kills));
        }
    }

    private static int ASSIM_SAVE_COUNTER = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ASSIM_SAVE_COUNTER++;
            if (ASSIM_SAVE_COUNTER >= 1200) {
                ASSIM_SAVE_COUNTER = 0;
                var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    AssimilationSaveManager.flushDirty(server);
                }
            }
        }
    }

    private static BlockPos findGroundPos(ServerLevel level, double x, int startY, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);
        BlockState state = level.getBlockState(pos);
        if (state.getRenderShape() == RenderShape.MODEL && !state.isAir()) {
            return pos.immutable();
        }
        for (int j = 0; j < 3; j++) {
            pos.set(pos.getX(), startY - 1 - j, pos.getZ());
            state = level.getBlockState(pos);
            if (state.getRenderShape() == RenderShape.MODEL && !state.isAir()) {
                return pos.immutable();
            }
        }
        return null;
    }

    // 鈹€鈹€ Kill Absorption 鈹€鈹€

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (!(event.getSource().getEntity() instanceof Player player)) return;

        boolean hasPrototype = PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player);
        if (!hasPrototype) return;

        LivingEntity killed = event.getEntity();
        float mobMaxHealth = killed.getMaxHealth();

        // 鈹€鈹€ Health absorption 鈹€鈹€
        float ratio = (float)(double)ModConfig.KILL_HEALTH_ABSORB_RATIO.get();
        if (ratio > 0.0F) {
            float absorbAmount = mobMaxHealth * ratio;
            float missingHealth = player.getMaxHealth() - player.getHealth();

            if (absorbAmount <= missingHealth) {
                player.heal(absorbAmount);
            } else {
                player.heal(missingHealth);
                float overflow = absorbAmount - missingHealth;
                float currentAbsorption = player.getAbsorptionAmount();
                float cap = player.getMaxHealth() * (float)(double)ModConfig.KILL_ABSORPTION_CAP_MULTIPLIER.get();
                player.setAbsorptionAmount(Math.min(currentAbsorption + overflow, cap));
            }
        }

        // 鈹€鈹€ Food restoration 鈹€鈹€
        float foodRatio = (float)(double)ModConfig.KILL_FOOD_RESTORE_RATIO.get();
        if (foodRatio > 0.0F) {
            float foodAmount = mobMaxHealth * foodRatio;
            if (foodAmount > 0.0F) {
                int foodToAdd = Math.max(1, (int)foodAmount);
                int currentFood = player.getFoodData().getFoodLevel();
                int newFood = Math.min(currentFood + foodToAdd, 20);
                int overflow = (currentFood + foodToAdd) - newFood;

                player.getFoodData().setFoodLevel(newFood);
                if (overflow > 0) {
                    float newSat = Math.min(player.getFoodData().getSaturationLevel() + overflow, newFood);
                    player.getFoodData().setSaturation(newSat);
                }
            }
        }

        // 鈹€鈹€ Assimilation kill count 鈹€鈹€
        PrototypeItem.incrementAssimilationKills(player);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CAssimilationDataPacket(PrototypeItem.getAssimilationKills(player)));
            AssimilationSaveManager.markDirty(sp);
        }
    }

}
