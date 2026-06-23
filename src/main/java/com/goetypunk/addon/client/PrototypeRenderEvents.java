package com.goetypunk.addon.client;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.PrototypeItem;
import com.goetypunk.addon.common.net.ModNetwork;
import com.goetypunk.addon.common.utils.CuriosHelper;
import com.goetypunk.addon.common.utils.WallUtil;
import com.goetypunk.addon.common.net.c2s.C2SCancelSlamPacket;
import com.goetypunk.addon.common.net.c2s.C2SJumpChargePacket;
import com.goetypunk.addon.common.net.c2s.C2SWallJumpOffPacket;
import com.goetypunk.addon.common.net.c2s.C2SFlightInputPacket;
import com.goetypunk.addon.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PrototypeRenderEvents {

    private static final ResourceLocation PROTOTYPE_TEXTURE =
            new ResourceLocation(GoetyPunkMod.MOD_ID, "textures/entity/prototype.png");

    // ── Wall-running client state (visual only) ──
    private static Vec3 clientWallNormal = Vec3.ZERO;
    private static Quaternionf clientWallQuat = new Quaternionf();
    private static Quaternionf clientWallQuatTarget = new Quaternionf();
    private static int clientWallRecoveryTicks = 0;
    private static boolean clientWasWallRunning = false;

    // ── Jump charge client state ──
    private static int clientChargeTicks = 0;
    private static int lastSentCharge = 0;
    private static boolean prevJumpDown = false;

    // ── Slam client state (synced via S2CSlamStatePacket) ──
    private static boolean clientSlamming = false;

    public static void setClientSlamming(boolean slamming) {
        clientSlamming = slamming;
    }

    // ═══════════════════════════════════════════════════
    //  1. Client tick: wall detection + jump charge C2S
    // ═══════════════════════════════════════════════════

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player = mc.player;

        boolean jumpDown = mc.options.keyJump.isDown();
        boolean wasPressed = jumpDown && !prevJumpDown;
        prevJumpDown = jumpDown;

        // ── Update client wall state for camera/model roll ──
        updateClientWallState(player);

        // Cancel slam on jump press (not during wall-run)
        if (wasPressed && clientWallNormal.lengthSqr() < 0.5) {
            ModNetwork.CHANNEL.sendToServer(new C2SCancelSlamPacket());
        }

        // Wall jump-off via C2S
        if (wasPressed && clientWallNormal.lengthSqr() >= 0.5) {
            ModNetwork.CHANNEL.sendToServer(new C2SWallJumpOffPacket());
        }

        // ── Jump charge C2S ──
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasPrototype = chest.getItem() instanceof PrototypeItem;
        if (!hasPrototype) {
            hasPrototype = CuriosHelper.hasPrototypeInCurios(player);
        }

        if (jumpDown && hasPrototype && !player.isFallFlying()) {
            clientChargeTicks = Math.min(clientChargeTicks + 1, 60);
            float fwd = player.zza;
            boolean jumping = jumpDown;
            if (clientChargeTicks == 1) {
                Vec3 vel = player.getDeltaMovement();
                double clientHSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(1, fwd, jumping, clientHSpeed));
                lastSentCharge = 1;
            } else if (clientChargeTicks != lastSentCharge && clientChargeTicks % 5 == 0) {
                ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(clientChargeTicks, fwd, jumping, 0.0));
                lastSentCharge = clientChargeTicks;
            }
        } else {
            if (lastSentCharge > 0) {
                ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(0, player.zza, false, 0.0));
                lastSentCharge = 0;
            }
            clientChargeTicks = 0;
        }

        // ── Slam-only particles ──
        if (hasPrototype && clientSlamming) {
            spawnBodyTrailParticles(player, new Vector3f(0.35f, 0.03f, 0.03f), 0.35f, ParticleTypes.FLAME, -0.5);
            spawnBodyTrailParticles(player, new Vector3f(0.15f, 0.015f, 0.015f), 0.35f, null, -0.5);
        }

        // ── Flight forward input C2S ──
        boolean hasPrototypeForFlight = chest.getItem() instanceof PrototypeItem;
        if (!hasPrototypeForFlight) {
            hasPrototypeForFlight = CuriosHelper.hasPrototypeInCurios(player);
        }
        if (hasPrototypeForFlight && player.isFallFlying()) {
            int tick = player.tickCount;
            boolean forwardDown = mc.options.keyUp.isDown();
            float forwardVal = forwardDown ? 1.0f : 0.0f;
            PrototypeItem.setFlightForward(player, forwardVal);
            if (tick % 3 == 0) {
                ModNetwork.CHANNEL.sendToServer(new C2SFlightInputPacket(forwardVal));
            }
        } else if (player.isFallFlying()) {
            if (player.tickCount % 3 == 0) {
                ModNetwork.CHANNEL.sendToServer(new C2SFlightInputPacket(0.0f));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    //  2. Camera rotation during wall-running (quaternion SLERP)
    // ═══════════════════════════════════════════════════

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        float rollDeg = quatRollDeg(clientWallQuat);
        if (Math.abs(rollDeg) < 1.0f) return;
        event.setRoll(event.getRoll() + rollDeg);
    }

    // ═══════════════════════════════════════════════════
    //  3. Model rotation during wall-running
    // ═══════════════════════════════════════════════════

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        float rollDeg = quatRollDeg(clientWallQuat);
        if (Math.abs(rollDeg) < 1.0f) return;
        float rollRad = rollDeg * Mth.DEG_TO_RAD;
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        model.body.zRot += rollRad;
        model.head.zRot += rollRad;
        model.leftArm.zRot += rollRad;
        model.rightArm.zRot += rollRad;
        model.leftLeg.zRot += rollRad;
        model.rightLeg.zRot += rollRad;
    }

    // ═══════════════════════════════════════════════════
    //  4. Render Prototype on player body
    // ═══════════════════════════════════════════════════

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasInChest = chestStack.getItem() instanceof PrototypeItem;

        if (!hasInChest) {
            hasInChest = CuriosHelper.hasPrototypeInCurios(player);
        }

        if (!hasInChest) return;

        poseStack.pushPose();
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

        model.body.translateAndRotate(poseStack);
        poseStack.translate(0, 0.25, -0.15);
        poseStack.scale(1.8F, 1.8F, 1.8F);

        VertexConsumer consumer = ItemRenderer.getFoilBuffer(
                buffer, RenderType.entityCutout(PROTOTYPE_TEXTURE), false, chestStack.hasFoil());
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    // ═══════════════════════════════════════════════════
    //  Internal helpers (quaternion-based wall rotation)
    // ═══════════════════════════════════════════════════

    private static void updateClientWallState(Player player) {
        Minecraft mc = Minecraft.getInstance();
        boolean sprinting = mc.options.keySprint.isDown();
        boolean hasPrototype = PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player);

        Vec3 wallDir = WallUtil.getWallDirection(player, 0.5);
        boolean onWall = sprinting && hasPrototype && !player.isFallFlying() && wallDir != null;

        if (onWall) {
            clientWallNormal = wallDir;
            float rollDeg = computeWallRollDeg(player, wallDir);
            float limit = (float) (double) ModConfig.WALL_ROTATION_LIMIT.get();
            rollDeg = Mth.clamp(rollDeg, -limit, limit);
            clientWallQuatTarget = new Quaternionf().rotationZ(rollDeg * Mth.DEG_TO_RAD);
            clientWasWallRunning = true;
            clientWallRecoveryTicks = 0;
        } else {
            clientWallNormal = Vec3.ZERO;
            if (clientWasWallRunning) {
                clientWallRecoveryTicks = ModConfig.WALL_RECOVERY_TICKS.get();
                clientWasWallRunning = false;
            }
            if (clientWallRecoveryTicks > 0) {
                clientWallRecoveryTicks--;
                clientWallQuatTarget = new Quaternionf();
            } else {
                clientWallQuatTarget = new Quaternionf();
                clientWallQuat = new Quaternionf();
                return;
            }
        }

        slerpWallRotation();
    }

    private static float computeWallRollDeg(Player player, Vec3 wallDir) {
        Vec3 lookH = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z);
        double len = lookH.length();
        if (len > 0.001) {
            lookH = lookH.scale(1.0 / len);
        } else {
            lookH = new Vec3(0, 0, 1);
        }
        Vec3 right = new Vec3(lookH.z, 0, -lookH.x);
        double side = Mth.clamp(wallDir.x * right.x + wallDir.z * right.z, -1.0, 1.0);
        return (float) Math.toDegrees(Math.asin(side));
    }

    private static float quatRollDeg(Quaternionf q) {
        float w = q.w;
        float x = q.x;
        float y = q.y;
        float z = q.z;
        double roll = Math.atan2(2.0 * (w * z + x * y), 1.0 - 2.0 * (y * y + z * z));
        return (float) Math.toDegrees(roll);
    }

    private static void slerpWallRotation() {
        float dot = clientWallQuat.w * clientWallQuatTarget.w
                  + clientWallQuat.x * clientWallQuatTarget.x
                  + clientWallQuat.y * clientWallQuatTarget.y
                  + clientWallQuat.z * clientWallQuatTarget.z;
        if (dot < 0) {
            clientWallQuatTarget.set(-clientWallQuatTarget.x, -clientWallQuatTarget.y, -clientWallQuatTarget.z, -clientWallQuatTarget.w);
            dot = -dot;
        }
        if (Math.abs(dot) > 0.9999f) {
            clientWallQuat.set(clientWallQuatTarget);
        } else {
            float t = (float) (double) ModConfig.WALL_ROTATION_SPEED.get();
            clientWallQuat.slerp(clientWallQuatTarget, t);
        }
    }

    private static void spawnBodyTrailParticles(Player player, Vector3f color, float scale, ParticleOptions spark, double behind) {
        var r = player.getRandom();
        Vec3 vel = player.getDeltaMovement();
        float yawRad = player.getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yawRad), 0, -Mth.sin(yawRad));

        double[][] anchors = {
                {-0.35, 0.85},
                {0.35, 0.85},
                {-0.25, 1.75},
                {0.25, 1.75},
                {-0.2, 0.15},
                {0.2, 0.15},
        };

        for (double[] a : anchors) {
            double px = player.getX() + right.x * a[0] + vel.x * behind;
            double pz = player.getZ() + right.z * a[0] + vel.z * behind;
            double py = player.getY() + a[1] + vel.y * behind;

            for (int i = 0; i < 5; i++) {
                double jx = px + (r.nextDouble() - 0.5) * 0.2;
                double jy = py + (r.nextDouble() - 0.5) * 0.2;
                double jz = pz + (r.nextDouble() - 0.5) * 0.2;
                double vx = -vel.x * 0.6 + (r.nextDouble() - 0.5) * 0.1;
                double vy = -0.03 + (r.nextDouble() - 0.5) * 0.05;
                double vz = -vel.z * 0.6 + (r.nextDouble() - 0.5) * 0.1;
                player.level().addParticle(new DustParticleOptions(color, scale), jx, jy, jz, vx, vy, vz);
            }

            if (spark != null) {
                player.level().addParticle(spark, px, py, pz,
                        (r.nextDouble() - 0.5) * 0.15, 0.05, (r.nextDouble() - 0.5) * 0.15);
            }
        }
    }
}
