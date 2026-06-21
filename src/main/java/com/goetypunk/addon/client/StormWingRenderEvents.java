package com.goetypunk.addon.client;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.StormWingItem;
import com.goetypunk.addon.common.net.ModNetwork;
import com.goetypunk.addon.common.utils.CuriosHelper;
import com.goetypunk.addon.common.utils.WallUtil;
import com.goetypunk.addon.common.net.c2s.C2SCancelSlamPacket;
import com.goetypunk.addon.common.net.c2s.C2SJumpChargePacket;
import com.goetypunk.addon.common.net.c2s.C2SWallJumpOffPacket;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class StormWingRenderEvents {

    private static final ResourceLocation STORM_WING_TEXTURE =
            new ResourceLocation(GoetyPunkMod.MOD_ID, "textures/entity/storm_wing.png");

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
        boolean hasStormWing = chest.getItem() instanceof StormWingItem;
        if (!hasStormWing) {
            hasStormWing = CuriosHelper.hasStormWingInCurios(player);
        }

        if (jumpDown && hasStormWing && !player.isFallFlying()) {
            clientChargeTicks = Math.min(clientChargeTicks + 1, 100);
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
    //  4. Render Storm Wing on player body
    // ═══════════════════════════════════════════════════

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasInChest = chestStack.getItem() instanceof StormWingItem;

        if (!hasInChest) {
            hasInChest = CuriosHelper.hasStormWingInCurios(player);
        }

        if (!hasInChest) return;

        poseStack.pushPose();
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

        model.body.translateAndRotate(poseStack);
        poseStack.translate(0, 0.25, -0.15);
        poseStack.scale(1.8F, 1.8F, 1.8F);

        VertexConsumer consumer = ItemRenderer.getFoilBuffer(
                buffer, RenderType.entityCutout(STORM_WING_TEXTURE), false, chestStack.hasFoil());
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    // ═══════════════════════════════════════════════════
    //  Internal helpers
    // ═══════════════════════════════════════════════════

    private static void updateClientWallState(Player player) {
        boolean sprinting = Minecraft.getInstance().options.keySprint.isDown();
        boolean hasStormWing = player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof StormWingItem;
        if (!hasStormWing) {
            hasStormWing = CuriosHelper.hasStormWingInCurios(player);
        }

        if (!sprinting || !hasStormWing || player.isFallFlying()) {
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
