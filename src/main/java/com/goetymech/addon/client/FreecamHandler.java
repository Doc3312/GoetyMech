package com.goetymech.addon.client;

import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class FreecamHandler {
    private static FreecamHandler instance;

    private boolean active;
    private Vec3 cameraPos;
    private Vec3 prevCameraPos;
    private Vec3 bodyPos;
    private float yaw;
    private float prevYaw;
    private float pitch;
    private float prevPitch;
    private int remainingTicks;
    private Vec3 velocity;

    private FreecamHandler() {}

    public static FreecamHandler getInstance() {
        if (instance == null) {
            instance = new FreecamHandler();
        }
        return instance;
    }

    public void activate(Vec3 cameraPos, Vec3 bodyPos, float yaw, float pitch) {
        this.active = true;
        this.cameraPos = cameraPos;
        this.prevCameraPos = cameraPos;
        this.bodyPos = bodyPos;
        this.yaw = yaw;
        this.prevYaw = yaw;
        this.pitch = pitch;
        this.prevPitch = pitch;
        this.remainingTicks = ModConfig.FREECAM_DURATION.get();
        this.velocity = Vec3.ZERO;
    }

    public void deactivate() {
        this.active = false;
        this.cameraPos = null;
        this.prevCameraPos = null;
        this.bodyPos = null;
        this.velocity = Vec3.ZERO;
    }

    public void tick() {
        if (!active) return;
        if (remainingTicks > 0) {
            remainingTicks--;
        }
        if (remainingTicks <= 0) {
            endFreecam();
            return;
        }
        prevCameraPos = cameraPos;
        velocity = velocity.scale(0.91);
        cameraPos = cameraPos.add(velocity);
    }

    public void endFreecam() {
        if (!active) return;
        deactivate();
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            ModNetwork.CHANNEL.sendToServer(new com.goetymech.addon.common.net.c2s.C2SFreecamEndPacket());
        }
    }

    public void savePrevRotation() {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
    }

    public boolean isActive() { return active; }
    public Vec3 getCameraPos() { return cameraPos; }
    public Vec3 getPrevCameraPos() { return prevCameraPos; }
    public Vec3 getBodyPos() { return bodyPos; }
    public float getYaw() { return yaw; }
    public float getPrevYaw() { return prevYaw; }
    public float getPitch() { return pitch; }
    public float getPrevPitch() { return prevPitch; }
    public Vec3 getVelocity() { return velocity; }
    public int getRemainingTicks() { return remainingTicks; }

    public void setYaw(float yaw) { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public void setVelocity(Vec3 velocity) { this.velocity = velocity; }
    public void addVelocity(Vec3 delta) { this.velocity = this.velocity.add(delta); }
}
