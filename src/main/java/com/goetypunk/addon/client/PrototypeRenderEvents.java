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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
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
    private static float clientWallRoll = 0.0f;
    private static float clientWallRollTarget = 0.0f;

    // ── Jump charge client state ──
    private static int clientChargeTicks = 0;
    private static int lastSentCharge = 0;
    private static boolean prevJumpDown = false;

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
            // 第一拍立即发包，不等节流
            if (clientChargeTicks == 1) {
                ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(1));
                lastSentCharge = 1;
            } else if (clientChargeTicks != lastSentCharge && clientChargeTicks % 5 == 0) {
                ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(clientChargeTicks));
                lastSentCharge = clientChargeTicks;
            }
            // Particles
            if (clientChargeTicks > 0 && clientChargeTicks % 3 == 0) {
                var random = player.getRandom();
                double px = player.getX() + (random.nextDouble() - 0.5) * 0.6;
                double pz = player.getZ() + (random.nextDouble() - 0.5) * 0.6;
                player.level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.CRIT,
                        px, player.getY(), pz,
                        (random.nextDouble() - 0.5) * 0.1, 0.1, (random.nextDouble() - 0.5) * 0.1);
                if (clientChargeTicks > 40) {
                    player.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                            px, player.getY() + 0.1, pz,
                            0, 0.05, 0);
                }
            }
        } else {
            if (lastSentCharge > 0) {
                ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(0));
                lastSentCharge = 0;
            }
            clientChargeTicks = 0;
        }

        // ── Sprint particle trail ──
        if (hasPrototype && player.isSprinting() && player.zza > 0.0F && !player.isFallFlying()) {
            if (player.tickCount % 2 == 0) {
                var r = player.getRandom();
                Vec3 vel = player.getDeltaMovement();
                int count = 8 + r.nextInt(5);
                for (int i = 0; i < count; i++) {
                    double h = r.nextDouble() * 1.8;
                    double behind = -r.nextDouble() * 0.8;
                    double px = player.getX() + vel.x * behind + (r.nextDouble() - 0.5) * 0.3;
                    double pz = player.getZ() + vel.z * behind + (r.nextDouble() - 0.5) * 0.3;
                    double py = player.getY() + h;
                    double vx = -vel.x * 1.5 + (r.nextDouble() - 0.5) * 0.1;
                    double vz = -vel.z * 1.5 + (r.nextDouble() - 0.5) * 0.1;
                    player.level().addParticle(
                            new DustParticleOptions(new Vector3f(0.35f, 0.03f, 0.03f), 0.7f),
                            px, py, pz, vx, -0.06, vz);
                }
            }
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
    //  2. Camera roll during wall-running
    // ═══════════════════════════════════════════════════

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (Math.abs(clientWallRoll) < 1.0f) return;
        event.setRoll(event.getRoll() + clientWallRoll);
    }

    // ═══════════════════════════════════════════════════
    //  3. Model roll during wall-running
    // ═══════════════════════════════════════════════════

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (Math.abs(clientWallRoll) < 1.0f) return;
        float rollRad = clientWallRoll * Mth.DEG_TO_RAD;
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
    //  Internal helpers
    // ═══════════════════════════════════════════════════

    private static void updateClientWallState(Player player) {
        boolean sprinting = Minecraft.getInstance().options.keySprint.isDown();
        boolean hasPrototype = player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PrototypeItem;
        if (!hasPrototype) {
            hasPrototype = CuriosHelper.hasPrototypeInCurios(player);
        }

        if (!sprinting || !hasPrototype || player.isFallFlying()) {
            clientWallRollTarget = 0.0f;
            smoothRoll();
            return;
        }

        Vec3 wallDir = WallUtil.getWallDirection(player, 0.5);
        if (wallDir != null) {
            clientWallNormal = wallDir;
            Vec3 lookH = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z);
            double len = lookH.length();
            if (len > 0.001) {
                lookH = lookH.scale(1.0 / len);
            } else {
                lookH = new Vec3(0, 0, 1);
            }
            Vec3 right = new Vec3(lookH.z, 0, -lookH.x);
            double side = wallDir.x * right.x + wallDir.z * right.z;
            clientWallRollTarget = (float) Math.toDegrees(Math.asin(Mth.clamp(side, -1.0, 1.0)));
        } else {
            clientWallRollTarget = 0.0f;
        }

        smoothRoll();
    }

    private static void smoothRoll() {
        float diff = clientWallRollTarget - clientWallRoll;
        if (Math.abs(diff) < 0.5f) {
            clientWallRoll = clientWallRollTarget;
        } else {
            clientWallRoll += diff * 0.15f;
        }
    }
}
