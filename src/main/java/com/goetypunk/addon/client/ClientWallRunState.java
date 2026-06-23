package com.goetypunk.addon.client;

import net.minecraft.world.phys.Vec3;

public class ClientWallRunState {
    public static Vec3 wallNormal = Vec3.ZERO;
    public static float forwardInput = 0.0f;
    public static float strafeInput = 0.0f;
    public static boolean sprintDown = false;

    public static boolean isActive() {
        return wallNormal.lengthSqr() >= 0.5;
    }
}
