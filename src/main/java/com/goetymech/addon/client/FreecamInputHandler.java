package com.goetymech.addon.client;

import com.goetymech.addon.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.Vec3;

public class FreecamInputHandler {
    private static final FreecamHandler handler = FreecamHandler.getInstance();

    private static final double ACCEL_FACTOR = 0.05;

    public static void handleInput(float partialTick) {
        if (!handler.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (ModClientEvents.freecamExitKey.isDown()) {
            handler.endFreecam();
            return;
        }

        double speed = ModConfig.FREECAM_SPEED.get();
        float yaw = handler.getYaw();

        Vec3 forward = Vec3.directionFromRotation(0, yaw);
        Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();

        Vec3 input = Vec3.ZERO;
        if (mc.options.keyUp.isDown()) input = input.add(forward);
        if (mc.options.keyDown.isDown()) input = input.add(forward.reverse());
        if (mc.options.keyLeft.isDown()) input = input.add(right.reverse());
        if (mc.options.keyRight.isDown()) input = input.add(right);

        if (mc.options.keyJump.isDown()) input = input.add(0, 1, 0);
        if (mc.options.keyShift.isDown()) input = input.add(0, -1, 0);

        if (input.lengthSqr() > 0) {
            input = input.normalize().scale(speed * ACCEL_FACTOR);
            handler.addVelocity(input);
        }

        handler.setYaw(mc.player.getYRot());
        handler.setPitch(mc.player.getXRot());

        if (mc.player.input != null) {
            mc.player.input.forwardImpulse = 0;
            mc.player.input.leftImpulse = 0;
            mc.player.input.jumping = false;
            mc.player.input.shiftKeyDown = false;
        }
    }
}
