package com.goetypunk.addon.common.events;

import com.Polarice3.Goety.client.particles.AoEParticleOption;
import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.common.entities.util.CameraShake;
import com.Polarice3.Goety.common.entities.util.ModFallingBlock;
import com.Polarice3.Goety.utils.BlockFinder;
import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.StormWingItem;
import com.goetypunk.addon.common.net.ModNetwork;
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
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StormWingEvents {

    private static final String TAG_SLAMMING = "goetypunk.slamming";
    private static final String TAG_SLAM_TICK = "goetypunk.slam_tick";
    private static final String TAG_SLAM_START_Y = "goetypunk.slam_start_y";
    private static final String TAG_PREV_CROUCH = "goetypunk.prev_crouch";

    // ── Ring animation sequencer ──
    private static final int RINGS_DELAY = 1;

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
        if (event.getEntity() instanceof Player player && StormWingItem.isSlamming(player)) {
            event.setCanceled(true);
        }
    }

    // ── LivingTick: Slam 状态机（fires before updateFallFlying → 可同 tick 停飞） ──

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        boolean slamming = player.getPersistentData().getBoolean(TAG_SLAMMING);

        if (!slamming && isFlyingWithStormWing(player)) {
            handleSlamTrigger(player);
        }

        if (slamming) {
            handleSlamTick(player);
        }

        // 用 isShiftKeyDown 而非 isCrouching —— 后者在 isFallFlying 时恒为 false
        StormWingItem.setPrevCrouch(player, player.isShiftKeyDown());
    }

    // ── PlayerTick (END): 推进 + 抗 drag（fires after travel → 修饰持久到下一 tick） ──

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide) return;

        if (event.phase == TickEvent.Phase.START) {
            if (StormWingItem.isHoldingStormWing(player) && !player.isFallFlying()) {
                if (player.onGround() && player.zza > 0.0F) {
                    Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(m.x, 2.0, m.z);
                } else if (!player.onGround() && player.zza > 0.0F) {
                    player.startFallFlying();
                    GoetyPunkMod.LOGGER.info("StormWing force-flight mid-air!");
                }
            }
            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            if (!player.isFallFlying()) {
                if (StormWingItem.isHoldingStormWing(player) && player.onGround() && player.zza > 0.0F) {
                    Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(m.x, 2.0, m.z);
                    GoetyPunkMod.LOGGER.info("StormWing ground takeoff!");
                }
                if (StormWingItem.getFlightTick(player) > 0) {
                    StormWingItem.setFlightTick(player, 0);
                }
                return;
            }
            if (!StormWingItem.isHoldingStormWing(player)) return;

            Vec3 motion = player.getDeltaMovement();

            double drag = 0.98;
            double anti = 1.0 / drag;
            motion = motion.multiply(anti, 1.0, anti);

            if (player.zza > 0) {
                Vec3 look = player.getLookAngle();
                double boost = ModConfig.BOOST_ACCELERATION.get();
                motion = motion.add(look.x * boost, 0, look.z * boost);
            }

            int currentFlightTicks = player.getFallFlyingTicks();
            int prevFlightTicks = StormWingItem.getFlightTick(player);
            GoetyPunkMod.LOGGER.info("StormWing flight: prev={}, cur={}, flying={}, held={}, motionY={}",
                    prevFlightTicks, currentFlightTicks, player.isFallFlying(),
                    StormWingItem.isHoldingStormWing(player), motion.y);
            if (prevFlightTicks == 0 && currentFlightTicks > 0) {
                Vec3 look = player.getLookAngle();
                motion = motion.add(look.x * 1.25, look.y * 1.25, look.z * 1.25);
                GoetyPunkMod.LOGGER.info("StormWing BURST applied! look={}, motion={}", look, motion);
            }
            StormWingItem.setFlightTick(player, currentFlightTicks);

            player.setDeltaMovement(motion);
        }
    }

    // ── 检测 ──

    private static boolean isFlyingWithStormWing(Player player) {
        if (!player.isFallFlying()) return false;
        if (StormWingItem.isHoldingStormWing(player)) return true;
        return hasStormWingInCurios(player);
    }

    private static boolean hasStormWingInCurios(Player player) {
        try {
            var inv = CuriosApi.getCuriosInventory(player);
            return inv.map(i -> i.findFirstCurio(
                    s -> s.getItem() instanceof StormWingItem).isPresent()).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Slam ──

    private static void handleSlamTrigger(Player player) {
        boolean nowCrouch = player.isShiftKeyDown();
        boolean prevCrouch = StormWingItem.getPrevCrouch(player);

        if (nowCrouch && !prevCrouch) {
            enterSlamState(player);
        }
    }

    private static void enterSlamState(Player player) {
        StormWingItem.setSlamming(player, true);
        StormWingItem.setSlamTick(player, 0);
        StormWingItem.setSlamStartY(player, player.getY());

        // elytraFlightTick() 返回 false → 同 tick 晚些 updateFallFlying 清除飞行标志
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, -0.15, motion.z);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private static void handleSlamTick(Player player) {
        int tick = StormWingItem.getSlamTick(player);
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

        StormWingItem.setSlamTick(player, tick + 1);
    }

    // ── 冲击 ──

    private static void triggerSlamImpact(Player player, boolean onGround) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        double startY = StormWingItem.getSlamStartY(player);
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
                            damage, radius, serverLevel.getGameTime() + 60));
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
        StormWingItem.clearSlamState(player);
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
        var random = level.random;
        double cx = dd.px;
        double cy = dd.groundY;
        double cz = dd.pz;
        float r = dd.radius;

        // ── 白色魔法扩张环 ──
        level.sendParticles(
                new CircleExplodeParticleOption(0.8f, 0.8f, 1.0f, r, 1),
                cx, cy + 0.3, cz, 1, 0, 0, 0, 0);
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
                    px, py + 0.3, pz, 1, 0, 0, 0, 0);
        } catch (Exception ignored) {
        }

        // ── 粒子：AoEParticleOption 地面半透明红色圆形（持续显示伤害范围） ──
        try {
            double aoeGroundY = BlockFinder.moveDownToGround(player) + 0.25;
            GoetyPunkMod.LOGGER.info("AoE particle: radius={}, pos=({}, {}, {})",
                    String.format("%.1f", radius),
                    String.format("%.2f", px), String.format("%.2f", aoeGroundY), String.format("%.2f", pz));
            level.sendParticles(
                    new AoEParticleOption(radius, 60),
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
