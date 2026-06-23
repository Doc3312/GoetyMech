package com.goetypunk.addon.common.events;

import com.Polarice3.Goety.client.particles.AoEParticleOption;
import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.common.entities.util.CameraShake;
import com.Polarice3.Goety.common.entities.util.ModFallingBlock;
import com.Polarice3.Goety.utils.BlockFinder;
import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.PrototypeItem;
import com.goetypunk.addon.common.net.ModNetwork;
import com.goetypunk.addon.common.utils.CuriosHelper;
import com.goetypunk.addon.common.utils.WallUtil;
import com.goetypunk.addon.common.net.s2c.S2CSlamImpactPacket;
import com.goetypunk.addon.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PrototypeEvents {

    private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private static final String TAG_SLAMMING = "goetypunk.slamming";
    private static final String TAG_SLAM_TICK = "goetypunk.slam_tick";
    private static final String TAG_SLAM_START_Y = "goetypunk.slam_start_y";
    private static final String TAG_PREV_CROUCH = "goetypunk.prev_crouch";

    // ── Ring animation sequencer ──
    private static final int RINGS_DELAY = 1;
    private static final int SLAM_DELAY_TICKS = 40; // 2秒

    private static final Map<ServerLevel, List<SlamAnimData>> ACTIVE_SLAMS = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, List<DelayedDamageData>> PENDING_DAMAGES = new ConcurrentHashMap<>();

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

    // ── 坠落免伤 ──

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

    // ── LivingTick: Slam 状态机（fires before updateFallFlying → 可同 tick 停飞） ──

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
    }

    // ── PlayerTick (END): 推进 + 抗 drag（fires after travel → 修饰持久到下一 tick） ──

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide) return;

        boolean hasPrototype = PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player);
        boolean isSlamming = player.getPersistentData().getBoolean(TAG_SLAMMING);

        if (event.phase == TickEvent.Phase.START) {
            Vec3 startM = player.getDeltaMovement();
            double startHSpeed = Math.sqrt(startM.x * startM.x + startM.z * startM.z);
            GoetyPunkMod.LOGGER.info("D START: zza={}, onGround={}, isFly={}, hSpeed={}",
                    player.zza, player.onGround(), player.isFallFlying(), startHSpeed);

            // ── 爬墙检测（放在飞行前面） ──
            if (hasPrototype && !isSlamming) {
                if (PrototypeItem.isWallRunning(player)) {
                    handleWallRunTick(player);
                } else if (player.isSprinting() && !player.onGround() && !player.isFallFlying()) {
                    tryStartWallRun(player);
                }
            }

            // ── Ground takeoff / mid-air flight start ──
            if (!PrototypeItem.isWallRunning(player)
                    && (PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player))
                    && !player.isFallFlying()) {
                int charge = PrototypeItem.getJumpChargeTick(player);
                boolean isCJ = PrototypeItem.isChargedJumping(player);
                double startHS = Math.sqrt(startM.x * startM.x + startM.z * startM.z);
                GoetyPunkMod.LOGGER.info("PROTO_DIAG START: isCJ={}, charge={}, onGround={}, zza={}, isSprint={}, isFly={}, hSpeed={}",
                        isCJ, charge, player.onGround(), player.zza, player.isSprinting(),
                        player.isFallFlying(), String.format("%.4f", startHS));
                if (player.onGround() && player.zza > 0.0F && charge == 0 && !player.isSprinting()) {
                    Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(m.x, 1.5, m.z);
                    GoetyPunkMod.LOGGER.info("Prototype ground takeoff: zza={}, motion={}", player.zza, m);
                } else if (!player.onGround() && player.zza > 0.0F && !isCJ) {
                    player.startFallFlying();
                    Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(m.x, Math.max(m.y, 0.3), m.z);
                    GoetyPunkMod.LOGGER.info("Prototype mid-air deployment: motion={}", m);
                }
            }


            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            // ── Post-travel diagnostic ──
            Vec3 postTravelM = player.getDeltaMovement();
            GoetyPunkMod.LOGGER.info("D POST_TRAVEL: zza={}, motion=({}, {}, {}), onGround={}",
                    player.zza,
                    String.format("%.4f", postTravelM.x), String.format("%.4f", postTravelM.y), String.format("%.4f", postTravelM.z),
                    player.onGround());

            // ── 追踪实际地面速度（仅当正在移动时更新，蓄力时不覆盖） ──
            double moved = Math.sqrt(
                    (player.getX() - player.xo) * (player.getX() - player.xo)
                    + (player.getZ() - player.zo) * (player.getZ() - player.zo));
            if (hasPrototype && player.onGround() && !player.isFallFlying()
                    && player.isSprinting() && moved > 0.001) {
                PrototypeItem.setGroundSpeed(player, moved);
            }

            // ── 地面冲刺加速 ──
            if (hasPrototype && !isSlamming
                    && !PrototypeItem.isWallRunning(player)
                    && !player.isFallFlying()
                    && player.onGround()
                    && player.isSprinting()) {
                int current = PrototypeItem.getSprintTick(player) + 1;
                PrototypeItem.setSprintTick(player, current);
                AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (attr != null) {
                    double attrBefore = attr.getValue();
                    boolean hadMod = attr.getModifier(SPRINT_MODIFIER_UUID) != null;
                    GoetyPunkMod.LOGGER.info("SprintTick: current={}, attr={}, hadMod={}, moved={}",
                            current, String.format("%.4f", attrBefore), hadMod,
                            String.format("%.8f", moved));
                    attr.removeModifier(SPRINT_MODIFIER_UUID);
                    if (current >= 1) {
                        double t = Math.min((current - 1) / 80.0, 1.0);
                        double amount = t * ModConfig.SPRINT_MAX_MULTIPLIER.get();
                        attr.addTransientModifier(new AttributeModifier(
                                SPRINT_MODIFIER_UUID, "prototype_sprint", amount,
                                AttributeModifier.Operation.MULTIPLY_TOTAL));
                        double attrAfter = attr.getValue();
                        GoetyPunkMod.LOGGER.info("SprintMod AFTER: current={}, amount={}, attr {}→{}, hasMod={}",
                                current, String.format("%.4f", amount),
                                String.format("%.4f", attrBefore), String.format("%.4f", attrAfter),
                                attr.getModifier(SPRINT_MODIFIER_UUID) != null);
                    }
                }
            } else if (!player.isSprinting()) {
                PrototypeItem.setSprintTick(player, 0);
                AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (attr != null) {
                    attr.removeModifier(SPRINT_MODIFIER_UUID);
                }
            }

            // ── Charged jump 水平速度持续注入（抵消 move() 的摩擦衰减） ──
            if (PrototypeItem.isChargedJumping(player)) {
                Vec3 beforeCJ = player.getDeltaMovement();
                double beforeHSpeed = Math.sqrt(beforeCJ.x * beforeCJ.x + beforeCJ.z * beforeCJ.z);
                double cjHx = PrototypeItem.getCjHx(player);
                double cjHz = PrototypeItem.getCjHz(player);
                double targetHSpeed = Math.sqrt(cjHx * cjHx + cjHz * cjHz);
                if (player.onGround()) {
                    PrototypeItem.clearChargedJump(player);
                    GoetyPunkMod.LOGGER.info("PROTO_DIAG CJ_CLEAR onGround: beforeHSpeed={}, targetHSpeed={}, isFly={}",
                            String.format("%.4f", beforeHSpeed), String.format("%.4f", targetHSpeed),
                            player.isFallFlying());
                } else if (!player.isFallFlying()) {
                    Vec3 cur = player.getDeltaMovement();
                    player.setDeltaMovement(cjHx, cur.y, cjHz);
                    Vec3 afterCJ = player.getDeltaMovement();
                    double afterHSpeed = Math.sqrt(afterCJ.x * afterCJ.x + afterCJ.z * afterCJ.z);
                    GoetyPunkMod.LOGGER.info("PROTO_DIAG CJ_INJECT: beforeHSpeed={}, target=({},{}), afterHSpeed={}, vy={}",
                            String.format("%.4f", beforeHSpeed),
                            String.format("%.4f", cjHx), String.format("%.4f", cjHz),
                            String.format("%.4f", afterHSpeed),
                            String.format("%.4f", cur.y));
                } else {
                    GoetyPunkMod.LOGGER.info("PROTO_DIAG CJ_BLOCKED by isFly: beforeHSpeed={}, target=({},{}), isFly={}",
                            String.format("%.4f", beforeHSpeed),
                            String.format("%.4f", cjHx), String.format("%.4f", cjHz),
                            player.isFallFlying());
                }
            }

            Vec3 endM = player.getDeltaMovement();
            double endHSpeed = Math.sqrt(endM.x * endM.x + endM.z * endM.z);
            if (hasPrototype) {
                GoetyPunkMod.LOGGER.info("D END: onGround={}, isFly={}, hSpeed={}, sprintTick={}, zza={}",
                        player.onGround(), player.isFallFlying(), endHSpeed,
                        PrototypeItem.getSprintTick(player),
                        player.zza);
            }

            // ── Wall movement ──
            if (PrototypeItem.isWallRunning(player)) {
                applyWallMovement(player);
                return;
            }

            // ── 非飞行状态 ──
            if (!player.isFallFlying()) {
                return;
            }
        }
    }

    // ── 检测 ──

    private static boolean isFlyingWithPrototype(Player player) {
        if (!player.isFallFlying()) return false;
        if (PrototypeItem.isHoldingPrototype(player)) return true;
        return CuriosHelper.hasPrototypeInCurios(player);
    }

    // ── Wall-running detection ──

    private static void tryStartWallRun(Player player) {
        if (!player.isSprinting()) return;
        Vec3 wallDir = WallUtil.getWallDirection(player, 0.5);
        if (wallDir == null) return;
        PrototypeItem.setWallRunning(player, true);
        PrototypeItem.setWallNormal(player, wallDir);
        GoetyPunkMod.LOGGER.info("Prototype wall-run START: normal={}", wallDir);
    }

    private static void handleWallRunTick(Player player) {
        Vec3 wallN = PrototypeItem.getWallNormal(player);
        if (wallN.lengthSqr() < 0.5) {
            stopWallRun(player);
            return;
        }

        if (!player.isSprinting() || player.isShiftKeyDown()
                || player.isInWater() || player.isInLava()) {
            stopWallRun(player);
            return;
        }

        if (WallUtil.getWallDirection(player, 0.5) == null) {
            stopWallRun(player);
            return;
        }

        // Jump-off is handled via C2SWallJumpOffPacket from client
    }

    public static void handleWallJumpOff(ServerPlayer player) {
        Vec3 wallN = PrototypeItem.getWallNormal(player);
        if (wallN.lengthSqr() < 0.5) return;
        Vec3 launch = wallN.scale(ModConfig.WALL_JUMP_OFF_VELOCITY.get());
        Vec3 m = player.getDeltaMovement();
        player.setDeltaMovement(launch.x, Math.max(launch.y + 0.2, m.y + 0.2), launch.z);
        stopWallRun(player);
    }

    private static void stopWallRun(Player player) {
        PrototypeItem.clearWallState(player);
        PrototypeItem.setWallRunning(player, false);
    }

    // ── Wall movement (applied in PlayerTick.END) ──

    private static void applyWallMovement(Player player) {
        Vec3 wallN = PrototypeItem.getWallNormal(player);
        if (wallN.lengthSqr() < 0.5) return;

        Vec3 look = player.getLookAngle();
        Vec3 forwardOnWall = look.subtract(wallN.scale(look.dot(wallN)));
        double fwLen = forwardOnWall.length();
        if (fwLen < 0.001) {
            forwardOnWall = new Vec3(1, 0, 0).cross(wallN);
            fwLen = forwardOnWall.length();
        }
        forwardOnWall = forwardOnWall.scale(1.0 / fwLen);

        Vec3 rightOnWall = forwardOnWall.cross(wallN).normalize();

        float forward = player.zza;
        float strafe = player.xxa;
        double speed = ModConfig.WALL_RUN_SPEED.get();

        Vec3 move = forwardOnWall.scale(forward * speed)
                .add(rightOnWall.scale(-strafe * speed));

        move = move.add(0, -ModConfig.WALL_GRAVITY.get(), 0);
        move = move.add(wallN.scale(ModConfig.WALL_PULL_SPEED.get()));

        player.setDeltaMovement(move.x, move.y, move.z);
    }

    // ── Jump charging (called from C2S packet handler) ──

    public static void handleJumpChargeFromClient(ServerPlayer player, int action, float forward, boolean jumping, double clientGroundSpeed) {
        if (player.isFallFlying() || PrototypeItem.isWallRunning(player)) return;
        boolean hasPrototype = PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player);
        if (!hasPrototype) return;

        player.setPlayerInput(forward, 0, jumping, false);

        if (clientGroundSpeed > 0.001) {
            PrototypeItem.setGroundSpeed(player, clientGroundSpeed);
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
        Vec3 m = player.getDeltaMovement();
        PrototypeItem.setCjHx(player, m.x);
        PrototypeItem.setCjHz(player, m.z);
        double hSpeed = Math.sqrt(m.x * m.x + m.z * m.z);
        int threshold = ModConfig.JUMP_CHARGE_THRESHOLD.get();
        if (charge >= threshold) {
            double minH = ModConfig.JUMP_CHARGE_MIN_HEIGHT.get();
            double maxH = ModConfig.JUMP_CHARGE_MAX_HEIGHT.get();
            int maxTicks = ModConfig.JUMP_MAX_CHARGE_TICKS.get();
            double t = (double)(charge - threshold) / (double)(maxTicks - threshold);
            t = Math.min(t, 1.0);
            double height = minH + t * (maxH - minH);
            double jumpPower = velocityForHeight(height);
            player.setDeltaMovement(m.x, Math.max(jumpPower, m.y), m.z);
            Vec3 after = player.getDeltaMovement();
            GoetyPunkMod.LOGGER.info("PROTO_DIAG applyChargedJump: charge={}, hSpeed={}, setVY={}, final=({}, {}, {}), isCJ={}",
                    charge, String.format("%.4f", hSpeed),
                    String.format("%.4f", jumpPower),
                    String.format("%.4f", after.x), String.format("%.4f", after.y), String.format("%.4f", after.z),
                    PrototypeItem.isChargedJumping(player));
        } else if (player.onGround()) {
            double tapH = ModConfig.JUMP_TAP_HEIGHT.get();
            double jumpPower = velocityForHeight(tapH);
            player.setDeltaMovement(m.x, jumpPower, m.z);
            Vec3 after = player.getDeltaMovement();
            GoetyPunkMod.LOGGER.info("PROTO_DIAG applyTapJump: charge={}, hSpeed={}, final=({}, {}, {}), isCJ={}",
                    charge, String.format("%.4f", hSpeed),
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

    // ── Slam ──

    private static void handleSlamTrigger(Player player) {
        boolean nowCrouch = player.isShiftKeyDown();
        boolean prevCrouch = PrototypeItem.getPrevCrouch(player);

        if (nowCrouch && !prevCrouch) {
            enterSlamState(player);
        }
    }

    private static void enterSlamState(Player player) {
        PrototypeItem.setSlamming(player, true);
        PrototypeItem.setSlamTick(player, 0);
        PrototypeItem.setSlamStartY(player, player.getY());
        PrototypeItem.setSlamInvulnTick(player, 9999);

        // elytraFlightTick() 返回 false → 同 tick 晚些 updateFallFlying 清除飞行标志
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, -0.15, motion.z);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private static void handleSlamTick(Player player) {
        int tick = PrototypeItem.getSlamTick(player);
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
            speed = Math.min(speed, ModConfig.SLAM_MAX_SPEED.get());
            player.setDeltaMovement(motion.x, -speed, motion.z);
        }

        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
        }

        PrototypeItem.setSlamTick(player, tick + 1);
    }

    // ── 冲击 ──

    private static void triggerSlamImpact(Player player, boolean onGround) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        double startY = PrototypeItem.getSlamStartY(player);
        double fallDistance = Math.max(0, startY - player.getY());

        float damage = (float) Math.max(ModConfig.SLAM_MIN_DAMAGE.get(), Math.min(ModConfig.SLAM_MAX_DAMAGE.get(), fallDistance * ModConfig.SLAM_DAMAGE_MULTIPLIER.get()));
        float radius = (float) Math.min(ModConfig.SLAM_MAX_RADIUS.get(), Math.max(8.0, fallDistance * ModConfig.SLAM_RADIUS_MULTIPLIER.get()));

        int rings = 4 + Math.max(0, (int)((fallDistance - 10.0f) / 20.0f));
        if (rings > 9) rings = 9;

        int maxRingsByRadius = Math.max(0, (int)Math.floor(radius) - 5);
        rings = Math.min(rings, maxRingsByRadius);

        GoetyPunkMod.LOGGER.info("SlamImpact: startY={}, playerY={}, fallDist={}, radius={}, damage={}, rings={}, multRadius={}, maxRadius={}",
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

        player.fallDistance = 0;
        PrototypeItem.clearSlamState(player);
        PrototypeItem.setSlamInvulnTick(player, 20);
    }

    private static void applyAoeDamage(Player player, ServerLevel level, float damage, float radius) {
        double groundY = Math.floor(BlockFinder.moveDownToGround(player) - 0.0625);
        double px = player.getX();
        double pz = player.getZ();
        GoetyPunkMod.LOGGER.info("AoE damage: radius={}, center=({}, {}, {}), aabbY=[{}, {}]",
                String.format("%.1f", radius),
                String.format("%.2f", px), String.format("%.2f", groundY), String.format("%.2f", pz),
                String.format("%.0f", groundY - 2), String.format("%.0f", groundY + 3));

        AABB aoe = new AABB(
                px - radius, groundY - 2, pz - radius,
                px + radius, groundY + 3, pz + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aoe,
                e -> e != player && e.isAlive() && !e.isSpectator());

        GoetyPunkMod.LOGGER.info("AoE entities in AABB: {}", targets.size());

        DamageSource source = level.damageSources().playerAttack(player);

        for (LivingEntity target : targets) {
            double dx = target.getX() - px;
            double dz = target.getZ() - pz;
            double hDist = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
            double ty = target.getY();
            double vDist = ty - groundY;
            if (hDist > radius) {
                GoetyPunkMod.LOGGER.info("  SKIP {} @ hDist={} (>{}) vDist={} targetY={}",
                        target.getDisplayName().getString(),
                        String.format("%.2f", hDist), String.format("%.1f", radius),
                        String.format("%.2f", vDist), String.format("%.2f", ty));
                continue;
            }
            GoetyPunkMod.LOGGER.info("  HIT {} @ hDist={} vDist={} dmg={}",
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

        // ── 白色魔法扩张环 ──
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

        // ── 启动逐圈错开升起动画 ──
        startRingAnimation(player, level, new Vec3(px, py, pz), floorY, radius, fallDistance, rings, random);

        // ── MOBILE 飞散碎块（中心向外飞，范围 3 格内） ──
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

        // ── 中心 DEBRIS 碎块（15个，水平 0.5~2.0 向外飞散） ──
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

        // ── 粒子：红色 CircleExplode 外圈边界（即时爆发） ──
        try {
            level.sendParticles(
                    new CircleExplodeParticleOption(1.0f, 0.0f, 0.0f, radius, 2),
                    px, BlockFinder.moveDownToGround(player) + 0.3, pz, 1, 0, 0, 0, 0);
        } catch (Exception ignored) {
        }

        // ── 粒子：AoEParticleOption 地面半透明红色圆形（持续显示伤害范围） ──
        try {
            double aoeGroundY = BlockFinder.moveDownToGround(player) + 0.25;
            GoetyPunkMod.LOGGER.info("AoE particle: radius={}, pos=({}, {}, {})",
                    String.format("%.1f", radius),
                    String.format("%.2f", px), String.format("%.2f", aoeGroundY), String.format("%.2f", pz));
            level.sendParticles(
                    new AoEParticleOption(radius, SLAM_DELAY_TICKS),
                    px, aoeGroundY, pz, 0, 0.8, 0.0, 0.0, 1.0);
        } catch (Exception ignored) {
        }

        // ── 镜头震动 CameraShake ──
        try {
            CameraShake.cameraShake(level, player.position(),
                    radius * 1.5f, 0.15f, 15, 10);
        } catch (Exception ignored) {
        }
    }

    // ── Ring animation system ──

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
                    GoetyPunkMod.LOGGER.info("  ring[{}] MOBILE block @({},{},{})",
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
                    GoetyPunkMod.LOGGER.info("  ring[{}] block @({},{},{}): height={} popUp={} delay={}",
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
        GoetyPunkMod.LOGGER.info("Slam ring {}: {} blocks, dist [{}-{}], radius={}, floorY={}",
                ringIdx, spawned, String.format("%.1f", ringInner), String.format("%.1f", ringOuter),
                String.format("%.1f", slam.radius), slam.floorY);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        // ── Ring animation ──
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

        // ── Delayed AoE damage ──
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
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ACTIVE_SLAMS.remove(serverLevel);
            PENDING_DAMAGES.remove(serverLevel);
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
}
