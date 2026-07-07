package com.goetymech.addon.client;

import com.goetymech.addon.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.Vec3;

public class FreecamInputHandler {
    private static final FreecamHandler handler = FreecamHandler.getInstance();

    public static void handleInput(float partialTick) {
        if (!handler.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.options.keyShift.isDown()) {
            handler.endFreecam();
            return;
        }

        double speed = ModConfig.FREECAM_SPEED.get();
        float yaw = handler.getYaw();

        Vec3 forward = Vec3.directionFromRotation(0, yaw);
        Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();

        Vec3 move = Vec3.ZERO;
        if (mc.options.keyUp.isDown()) move = move.add(forward);
        if (mc.options.keyDown.isDown()) move = move.add(forward.reverse());
        if (mc.options.keyLeft.isDown()) move = move.add(right.reverse());
        if (mc.options.keyRight.isDown()) move = move.add(right);

        if (mc.options.keyJump.isDown()) move = move.add(0, 1, 0);
        if (mc.options.keySprint.isDown()) move = move.add(0, -1, 0);

        if (move.lengthSqr() > 0) {
            move = move.normalize().scale(speed);
        }

        handler.setVelocity(move);
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
