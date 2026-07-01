package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.utils.CuriosHelper;
import com.goetymech.addon.common.utils.WallUtil;
import com.goetymech.addon.common.net.c2s.C2SCancelSlamPacket;
import com.goetymech.addon.common.net.c2s.C2SJumpChargePacket;
import com.goetymech.addon.common.net.c2s.C2SWallInputPacket;
import com.goetymech.addon.common.net.c2s.C2SWallJumpOffPacket;
import com.goetymech.addon.common.net.c2s.C2SFlightInputPacket;
import com.goetymech.addon.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PrototypeRenderEvents {

    private static final ResourceLocation PROTOTYPE_TEXTURE =
            new ResourceLocation(GoetyMechMod.MOD_ID, "textures/entity/prototype.png");

    // 鈹€鈹€ Wall-running client state (visual only) 鈹€鈹€
    private static Vec3 clientWallNormal = Vec3.ZERO;
    private static Quaternionf clientWallQuat = new Quaternionf();
    private static Quaternionf clientWallQuatTarget = new Quaternionf();
    private static int clientWallRecoveryTicks = 0;
    private static boolean clientWasWallRunning = false;

    // 鈹€鈹€ Jump charge client state 鈹€鈹€
    private static int clientChargeTicks = 0;
    private static int lastSentCharge = 0;
    private static boolean prevJumpDown = false;

    // 鈹€鈹€ Slam client state (synced via S2CSlamStatePacket) 鈹€鈹€
    private static boolean clientSlamming = false;

    public static void setClientSlamming(boolean slamming) {
        clientSlamming = slamming;
    }

    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?
    //  1. Client tick: wall detection + jump charge C2S
    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player = mc.player;

        if (event.phase == TickEvent.Phase.START) {
            ClientWallRunState.tickSprintRetain();
            ClientWallRunState.sprintDown = mc.options.keySprint.isDown() || player.isSprinting();
            float fwd = mc.options.keyUp.isDown() ? 1.0f : (mc.options.keyDown.isDown() ? -1.0f : 0.0f);
            float strafe = 0.0f;
            if (mc.options.keyLeft.isDown()) strafe += 1.0f;
            if (mc.options.keyRight.isDown()) strafe -= 1.0f;
            ClientWallRunState.forwardInput = fwd;
            ClientWallRunState.strafeInput = strafe;

            boolean sprinting = ClientWallRunState.sprintDown || player.isSprinting() || ClientWallRunState.shouldSprint();
            boolean hasProto = PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player);
            Vec3 wallDir = WallUtil.getWallDirection(player, 0.5);
            if (sprinting && hasProto && !player.isFallFlying() && !player.isInWater() && wallDir != null) {
                ClientWallRunState.wallNormal = wallDir;
                ClientWallRunState.graceTicks = 0;
            } else if (sprinting && hasProto && !player.isFallFlying() && ClientWallRunState.graceTicks > 0) {
                ClientWallRunState.tickGrace();
            } else {
                ClientWallRunState.wallNormal = Vec3.ZERO;
                ClientWallRunState.graceTicks = 0;
            }

            if (ClientWallRunState.isActive() && mc.options.keyShift.isDown()) {
                ClientWallRunState.wallNormal = Vec3.ZERO;
                ClientWallRunState.graceTicks = 0;
            }
            return;
        }

        if (event.phase != TickEvent.Phase.END) return;

        boolean jumpDown = mc.options.keyJump.isDown();
        boolean wasPressed = jumpDown && !prevJumpDown;
        prevJumpDown = jumpDown;

        // 鈹€鈹€ Update client wall state for camera/model roll 鈹€鈹€
        updateClientWallState(player);

        // Cancel slam on jump press (not during wall-run)
        if (wasPressed && clientWallNormal.lengthSqr() < 0.5) {
            ModNetwork.CHANNEL.sendToServer(new C2SCancelSlamPacket());
        }

        // Wall jump-off via C2S
        if (wasPressed && clientWallNormal.lengthSqr() >= 0.5) {
            ModNetwork.CHANNEL.sendToServer(new C2SWallJumpOffPacket());
        }

        // 鈹€鈹€ Wall input C2S (forward + strafe, throttled every 2 ticks) 鈹€鈹€
        if (clientWallNormal.lengthSqr() >= 0.5) {
            if (player.tickCount % 2 == 0) {
                float wallFwd = mc.options.keyUp.isDown() ? 1.0f : (mc.options.keyDown.isDown() ? -1.0f : 0.0f);
                float wallStrafe = 0.0f;
                if (mc.options.keyLeft.isDown()) wallStrafe += 1.0f;
                if (mc.options.keyRight.isDown()) wallStrafe -= 1.0f;
                ModNetwork.CHANNEL.sendToServer(new C2SWallInputPacket(wallFwd, wallStrafe));
            }
        } else {
            if (player.tickCount % 5 == 0) {
                ModNetwork.CHANNEL.sendToServer(new C2SWallInputPacket(0.0f, 0.0f));
            }
        }

        // 鈹€鈹€ Jump charge C2S 鈹€鈹€
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasPrototype = PrototypeItem.isPrototype(chest);
        if (!hasPrototype) {
            hasPrototype = CuriosHelper.hasPrototypeInCurios(player);
        }

        boolean inWater = player.isInWater() || player.level().getFluidState(player.blockPosition()).is(FluidTags.WATER);
        if (jumpDown && hasPrototype && !player.isFallFlying()) {
            if (player.onGround() || inWater) {
                clientChargeTicks = Math.min(clientChargeTicks + 1, 60);
            }
            if (clientChargeTicks > 0) {
                float fwd = player.zza;
                boolean jumping = jumpDown;
                if (lastSentCharge == 0) {
                    Vec3 vel = player.getDeltaMovement();
                    double clientHSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                    ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(1, fwd, jumping, clientHSpeed));
                    lastSentCharge = 1;
                } else if (player.tickCount % 5 == 0) {
                    ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(clientChargeTicks, fwd, jumping, 0.0));
                    lastSentCharge = clientChargeTicks;
                }
            }
        } else if (!jumpDown) {
            if (lastSentCharge > 0) {
                ModNetwork.CHANNEL.sendToServer(new C2SJumpChargePacket(0, player.zza, false, 0.0));
                lastSentCharge = 0;
            }
            clientChargeTicks = 0;
        }

        // 鈹€鈹€ Slam-only particles 鈹€鈹€
        if (hasPrototype && clientSlamming) {
            spawnBodyTrailParticles(player, new Vector3f(0.35f, 0.03f, 0.03f), 0.35f, ParticleTypes.FLAME, -0.5);
            spawnBodyTrailParticles(player, new Vector3f(0.15f, 0.015f, 0.015f), 0.35f, null, -0.5);
        }

        // 鈹€鈹€ Flight forward input C2S 鈹€鈹€
        boolean hasPrototypeForFlight = PrototypeItem.isPrototype(chest);
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

    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?
    //  2. Camera rotation during wall-running (quaternion SLERP)
    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        float rollDeg = quatRollDeg(clientWallQuat);
        GoetyMechMod.LOGGER.debug("CLIENT_CAMERA: rollDeg={}, wallQuat=({},{},{},{})",
                String.format("%.2f", rollDeg),
                String.format("%.3f", clientWallQuat.x),
                String.format("%.3f", clientWallQuat.y),
                String.format("%.3f", clientWallQuat.z),
                String.format("%.3f", clientWallQuat.w));
        if (Math.abs(rollDeg) < 1.0f) return;
        event.setRoll(event.getRoll() + rollDeg);
    }

    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?
    //  3. Model rotation during wall-running
    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?

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

    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?
    //  4. Render Prototype on player body
    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);

        if (chestStack.getItem() instanceof PrototypeMimicItem
                && !PrototypeMimicItem.getSourceStack(chestStack).isEmpty()) return;

        boolean hasInChest = PrototypeItem.isPrototype(chestStack);

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

    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?
    //  5. Item tooltip: assimilation kill count
    // 

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!PrototypeItem.isPrototype(event.getItemStack())) return;
        Player player = event.getEntity();
        if (player == null) return;

        int kills = PrototypeItem.getAssimilationKills(player);
        var tooltip = event.getToolTip();

        tooltip.add(Component.translatable("info.goetymech.prototype.assimilation", kills));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal("褰撳墠宸茶В閿佽兘鍔涳細"));
            tooltip.add(Component.literal("鍚屽寲").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("鑷敱濂旇窇").withStyle(ChatFormatting.RED));

            int tier = PrototypeItem.getAssimilationTier(kills);
            String speedSuffix = tier <= 1 ? "1/3" : tier == 2 ? "2/3" : "3/3";
            tooltip.add(Component.literal("绮剧伒涔嬮€?" + speedSuffix + ")").withStyle(ChatFormatting.RED));

            boolean hasGlide = PrototypeItem.hasFlight(kills);
            tooltip.add(Component.literal("婊戠繑").withStyle(hasGlide ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));

            boolean hasSlam = PrototypeItem.hasSlam(kills);
            tooltip.add(Component.literal("瀛愬脊淇啿").withStyle(hasSlam ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));

            int nextMilestone = PrototypeItem.getNextImmunityMilestone(kills);
            tooltip.add(kills >= 150
                    ? Component.literal("鐨笅鎶ょ敳  涓嬫杩涘寲锛? + nextMilestone + "鍑绘潃").withStyle(ChatFormatting.RED)
                    : Component.literal("鐨笅鎶ょ敳").withStyle(ChatFormatting.DARK_GRAY));

            int multPercent = Math.round((PrototypeItem.getDamageMultiplier(kills) - 1.0f) * 100.0f);
            int bonusDamage = Math.round(PrototypeItem.getBonusMagicDamage(kills));
            int immunityThreshold = PrototypeItem.getDamageImmunityThreshold(kills);
            String immunitySuffix = kills >= 150 ? "  浼ゅ鍏嶇柅锛? + immunityThreshold : "";
            tooltip.add(Component.literal("鏀诲嚮鍔涘姞鎴愶細+" + multPercent + "%  鐥呮瘨闄勫姞浼ゅ锛?" + bonusDamage + immunitySuffix)
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("褰撳墠宸茶В閿佽兘鍔涳細")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("鎸変綇 ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("Shift").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                            .append(Component.literal(" 鏌ョ湅璇︽儏").withStyle(ChatFormatting.GRAY))));
        }
    }

    //  Internal helpers (quaternion-based wall rotation)
    // 鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺?

    private static void updateClientWallState(Player player) {
        Minecraft mc = Minecraft.getInstance();
        boolean sprinting = mc.options.keySprint.isDown() || player.isSprinting();
        boolean hasPrototype = PrototypeItem.isHoldingPrototype(player) || CuriosHelper.hasPrototypeInCurios(player);

        Vec3 wallDir = WallUtil.getWallDirection(player, 0.5);
        GoetyMechMod.LOGGER.debug("CLIENT_WALL: sprint={}, isSprintState={}, hasProto={}, wallDir={}, isFly={}",
                mc.options.keySprint.isDown(), player.isSprinting(), hasPrototype, wallDir != null ? String.format("(%.2f,%.2f,%.2f)", wallDir.x, wallDir.y, wallDir.z) : "null",
                player.isFallFlying());
        boolean onWall = sprinting && hasPrototype && !player.isFallFlying() && !player.isInWater() && wallDir != null;

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
