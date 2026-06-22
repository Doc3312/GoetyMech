package com.goetypunk.addon.gametest;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.events.StormWingEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class StormWingFlightTests {

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(StormWingFlightTests.class);
    }

    private static final double BOOST_FORCE = 0.12;
    private static final double HOVER_LIFT  = 0.04;

    // ═══════════════════════════════════════════════════
    //  1. 加法加速：forward=1，Z 方向前推
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testForwardThrustZ(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                new Vec3(0, 0, 1), 1.0f, BOOST_FORCE, 0);
        if (accel.z <= 0) {
            helper.fail("Expected positive Z accel, got " + accel);
            return;
        }
        if (Math.abs(accel.z - BOOST_FORCE) > 0.001) {
            helper.fail("Expected Z accel = " + BOOST_FORCE + ", got " + accel.z);
            return;
        }
        if (Math.abs(accel.x) > 0.001 || Math.abs(accel.y) > 0.001) {
            helper.fail("Expected only Z component, got " + accel);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testForwardThrustDiagonal(GameTestHelper helper) {
        Vec3 look = new Vec3(0.5, 0, 0.5).normalize();
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                look, 1.0f, BOOST_FORCE, 0);
        double expectedMag = BOOST_FORCE;
        double actualMag = accel.length();
        if (Math.abs(actualMag - expectedMag) > 0.001) {
            helper.fail("Expected accel magnitude = " + expectedMag + ", got " + actualMag);
            return;
        }
        // Should point same direction as look
        Vec3 dir = accel.normalize();
        if (dir.distanceTo(look) > 0.001) {
            helper.fail("Accel direction mismatch. Expected " + look + ", got " + dir);
            return;
        }
        helper.succeed();
    }

    // ═══════════════════════════════════════════════════
    //  2. forward=0 时无推力
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testNoForwardNoThrust(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                new Vec3(0, 0, 1), 0.0f, BOOST_FORCE, 0);
        if (accel.lengthSqr() != 0) {
            helper.fail("Expected zero accel when forward=0, got " + accel);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testForwardBelowThreshold(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                new Vec3(0, 0, 1), 0.005f, BOOST_FORCE, 0);
        if (accel.lengthSqr() != 0) {
            helper.fail("Expected zero accel when forward < 0.01, got " + accel);
            return;
        }
        helper.succeed();
    }

    // ═══════════════════════════════════════════════════
    //  3. 恒定抗重力
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testHoverLiftAlwaysApplied(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                new Vec3(0, 0, 1), 0.0f, BOOST_FORCE, HOVER_LIFT);
        if (accel.y <= 0) {
            helper.fail("Expected positive Y (hover lift), got " + accel.y);
            return;
        }
        if (Math.abs(accel.y - HOVER_LIFT) > 0.001) {
            helper.fail("Expected Y = " + HOVER_LIFT + ", got " + accel.y);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testHoverLiftAddsToThrust(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                new Vec3(0, 0, 1), 1.0f, BOOST_FORCE, HOVER_LIFT);
        if (accel.y != HOVER_LIFT) {
            helper.fail("Expected Y = " + HOVER_LIFT + " (lift component), got " + accel.y);
            return;
        }
        if (Math.abs(accel.z - BOOST_FORCE) > 0.001) {
            helper.fail("Expected Z = " + BOOST_FORCE + " (thrust component), got " + accel.z);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testZeroHoverLiftNoVertical(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                new Vec3(0, 0, 1), 0.0f, BOOST_FORCE, 0);
        if (accel.y != 0) {
            helper.fail("Expected zero Y when hoverLift=0, got " + accel.y);
            return;
        }
        helper.succeed();
    }

    // ═══════════════════════════════════════════════════
    //  4. 视线方向全维度
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testLookUpThrust(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                new Vec3(0, 1, 0), 1.0f, BOOST_FORCE, 0);
        if (accel.y <= 0) {
            helper.fail("Expected upward thrust when looking up, got " + accel);
            return;
        }
        if (Math.abs(accel.y - BOOST_FORCE) > 0.001) {
            helper.fail("Expected Y = " + BOOST_FORCE + ", got " + accel.y);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testLookDownThrust(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                new Vec3(0, -1, 0), 1.0f, BOOST_FORCE, 0);
        if (accel.y >= 0) {
            helper.fail("Expected downward thrust when looking down, got " + accel);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void test45DegreeThrust(GameTestHelper helper) {
        Vec3 look = new Vec3(0, 1, 1).normalize();
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                look, 1.0f, BOOST_FORCE, 0);
        double expectedMag = BOOST_FORCE;
        double actualMag = accel.length();
        if (Math.abs(actualMag - expectedMag) > 0.001) {
            helper.fail("Expected magnitude = " + expectedMag + ", got " + actualMag);
            return;
        }
        // Direction matches look
        if (accel.dot(look) <= 0) {
            helper.fail("Accel should point in look direction");
            return;
        }
        helper.succeed();
    }

    // ═══════════════════════════════════════════════════
    //  5. 组合验证
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testCombinedThrustAndLift(GameTestHelper helper) {
        Vec3 look = new Vec3(0.5, 0.5, 0.5).normalize();
        Vec3 accel = StormWingEvents.computeAdditiveAcceleration(
                look, 1.0f, BOOST_FORCE, HOVER_LIFT);
        // Forward = BOOST_FORCE, all in look direction
        double forwardMag = Math.sqrt(accel.x * accel.x + accel.y * accel.y + accel.z * accel.z);
        double expectedForwardMag = BOOST_FORCE;
        if (Math.abs(forwardMag - expectedForwardMag) > 0.001) {
            helper.fail("Thrust magnitude should be " + expectedForwardMag + ", got " + forwardMag);
            return;
        }
        helper.succeed();
    }
}
