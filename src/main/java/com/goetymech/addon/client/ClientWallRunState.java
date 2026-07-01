package com.goetymech.addon.client;

import net.minecraft.world.phys.Vec3;

public class ClientWallRunState {
    public static Vec3 wallNormal = Vec3.ZERO;
    public static float forwardInput = 0.0f;
    public static float strafeInput = 0.0f;
    public static boolean sprintDown = false;

    public static int graceTicks = 0;
    public static Vec3 lastWallNormal = Vec3.ZERO;

    public static int sprintRetainTicks = 0;

    public static boolean isActive() {
        return wallNormal.lengthSqr() >= 0.5 || graceTicks > 0;
    }

    public static boolean shouldSprint() {
        return isActive() || sprintRetainTicks > 0;
    }

    public static void applyGrace(int ticks) {
        graceTicks = ticks;
        lastWallNormal = wallNormal;
    }

    public static void tickGrace() {
        if (graceTicks > 0) {
            graceTicks--;
            if (wallNormal.lengthSqr() < 0.5 && graceTicks > 0) {
                wallNormal = lastWallNormal;
            }
        }
    }

    public static void tickSprintRetain() {
        if (sprintRetainTicks > 0) {
            sprintRetainTicks--;
        }
    }
}
