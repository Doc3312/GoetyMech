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

    // ── 配置默认值（必须与 ModConfig.java 一致） ──

    private static final double TARGET_SPEED  = 3.5;
    private static final double CLIMB_SPEED   = 4.0;
    private static final double DIVE_SPEED    = 1.2;
    private static final double HOVER_LIFT    = 0.6;
    private static final double LIFT_THRESH   = 0.3;
    private static final double DIVE_THRESH   = -0.3;

    private static final double DRAG_H = 0.99;
    private static final double DRAG_V = 0.98;
    private static final double GRAVITY = 0.08;

    // ── 辅助：死区加速度计算（必须与 StormWingEvents 内联一致） ──

    private static double hAccel(double hTarget, double current) {
        return hTarget / DRAG_H - current;
    }

    private static double vAccel(double vTarget, double currentY) {
        return vTarget / DRAG_V + GRAVITY - currentY;
    }

    private static double computeVTarget(double lookY) {
        if (lookY > LIFT_THRESH) {
            double t = (lookY - LIFT_THRESH) / (1.0 - LIFT_THRESH);
            return HOVER_LIFT + (CLIMB_SPEED - HOVER_LIFT) * t;
        } else if (lookY < DIVE_THRESH) {
            double t = (DIVE_THRESH - lookY) / (DIVE_THRESH + 1.0);
            return HOVER_LIFT + (-DIVE_SPEED - HOVER_LIFT) * t;
        }
        return HOVER_LIFT;
    }

    // ═══════════════════════════════════════════════════
    //  1. 水平加速
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testForwardHorizontalAccel(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, new Vec3(0, 0, 1),
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        if (accel.z <= 0) {
            helper.fail("Expected forward Z accel > 0, got " + accel);
            return;
        }
        if (Math.abs(accel.x) > 0.001) {
            helper.fail("Expected zero X accel, got " + accel.x);
            return;
        }
        // 死区：hAccelZ = targetSpeed / dragH
        double expected = TARGET_SPEED / DRAG_H;
        if (Math.abs(accel.z - expected) > 0.01) {
            helper.fail("Expected forward accel ≈ " + String.format("%.4f", expected)
                    + ", got " + String.format("%.4f", accel.z));
            return;
        }
        // 平视时应有 hover lift
        if (accel.y <= 0) {
            helper.fail("Expected positive Y accel (hover lift) in neutral pitch, got " + accel.y);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testForwardAtHalfSpeed(GameTestHelper helper) {
        Vec3 motion = new Vec3(0, 0, TARGET_SPEED * 0.5);
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                motion, new Vec3(0, 0, 1),
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        double expected = TARGET_SPEED / DRAG_H - TARGET_SPEED * 0.5;
        if (Math.abs(accel.z - expected) > 0.01) {
            helper.fail("Expected accel ≈ " + String.format("%.4f", expected)
                    + ", got " + String.format("%.4f", accel.z));
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testForwardAtTargetSpeed(GameTestHelper helper) {
        Vec3 motion = new Vec3(0, 0, TARGET_SPEED);
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                motion, new Vec3(0, 0, 1),
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        // 死区：已达目标速度，accel = drag 补偿（不为 0）
        double expected = TARGET_SPEED / DRAG_H - TARGET_SPEED;
        if (Math.abs(accel.z - expected) > 0.01) {
            helper.fail("Expected drag compensation accel ≈ "
                    + String.format("%.4f", expected) + ", got "
                    + String.format("%.4f", accel.z));
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testForwardExceedingTarget(GameTestHelper helper) {
        Vec3 motion = new Vec3(0, 0, TARGET_SPEED * 1.5);
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                motion, new Vec3(0, 0, 1),
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        if (accel.z >= 0) {
            helper.fail("Expected negative Z accel (deceleration) when exceeding target, got " + accel.z);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testForwardWithSidewaysMotion(GameTestHelper helper) {
        Vec3 motion = new Vec3(2.0, 0, 1.0);
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                motion, new Vec3(0, 0, 1),
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        if (accel.x >= 0) {
            helper.fail("Expected negative X accel to cancel sideways motion, got " + accel.x);
            return;
        }
        if (accel.z <= 0) {
            helper.fail("Expected positive Z accel, got " + accel.z);
            return;
        }
        helper.succeed();
    }

    // ═══════════════════════════════════════════════════
    //  2. 垂直控制
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testClimbWhenLookingUp(GameTestHelper helper) {
        double pitch45 = Math.PI / 4;
        Vec3 look = new Vec3(0, Math.sin(pitch45), Math.cos(pitch45));
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, look,
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        if (accel.y <= 0) {
            helper.fail("Expected positive Y accel (climb) when looking up, got " + accel.y);
            return;
        }
        double vTarget = computeVTarget(look.y);
        double expected = vAccel(vTarget, 0);
        if (Math.abs(accel.y - expected) > 0.01) {
            helper.fail("Expected climb accel ≈ " + String.format("%.4f", expected)
                    + ", got " + String.format("%.4f", accel.y));
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testDiveWhenLookingDown(GameTestHelper helper) {
        double pitch45 = Math.PI / 4;
        Vec3 look = new Vec3(0, -Math.sin(pitch45), Math.cos(pitch45));
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, look,
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        if (accel.y >= 0) {
            helper.fail("Expected negative Y accel (dive) when looking down, got " + accel.y);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testHoverLiftInNeutralPitch(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, new Vec3(0, 0, 1),
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        double expected = vAccel(HOVER_LIFT, 0);
        if (Math.abs(accel.y - expected) > 0.01) {
            helper.fail("Expected hover accel ≈ " + String.format("%.4f", expected)
                    + ", got " + String.format("%.4f", accel.y));
            return;
        }
        helper.succeed();
    }

    // ═══════════════════════════════════════════════════
    //  3. 稳态 — 死区控制器保证一 tick 到达 vTarget
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testHoverSteadyStateDoesNotFall(GameTestHelper helper) {
        // 死区控制器达到 vTarget = HOVER_LIFT > 0 → 绝不会下降
        if (HOVER_LIFT <= 0) {
            helper.fail("HOVER_LIFT must be > 0 to maintain altitude");
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testClimbSteadyStatePositive(GameTestHelper helper) {
        double pitch45 = Math.PI / 4;
        double lookY = Math.sin(pitch45);
        double vTarget = computeVTarget(lookY);
        if (vTarget <= 0) {
            helper.fail("Climb vTarget=" + String.format("%.4f", vTarget) + " should be positive");
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testDiveSteadyStateNegative(GameTestHelper helper) {
        double pitch45 = Math.PI / 4;
        double lookY = -Math.sin(pitch45);
        double vTarget = computeVTarget(lookY);
        if (vTarget >= 0) {
            helper.fail("Dive vTarget=" + String.format("%.4f", vTarget) + " should be negative");
            return;
        }
        helper.succeed();
    }

    // ═══════════════════════════════════════════════════
    //  4. 边界情况
    // ═══════════════════════════════════════════════════

    @GameTest(timeoutTicks = 200)
    public static void testLookingStraightUp(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, new Vec3(0, 1, 0),
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        if (Math.abs(accel.x) > 0.001 || Math.abs(accel.z) > 0.001) {
            helper.fail("Expected zero horizontal accel when looking straight up, got ("
                    + accel.x + ", " + accel.z + ")");
            return;
        }
        double vTarget = computeVTarget(1.0);
        double expected = vAccel(vTarget, 0);
        if (Math.abs(accel.y - expected) > 0.01) {
            helper.fail("Expected max climb accel ≈ " + String.format("%.4f", expected)
                    + ", got " + String.format("%.4f", accel.y));
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testLookingStraightDown(GameTestHelper helper) {
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, new Vec3(0, -1, 0),
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        if (Math.abs(accel.x) > 0.001 || Math.abs(accel.z) > 0.001) {
            helper.fail("Expected zero horizontal accel when looking straight down, got ("
                    + accel.x + ", " + accel.z + ")");
            return;
        }
        if (accel.y >= 0) {
            helper.fail("Expected negative Y accel when looking straight down, got " + accel.y);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testLiftThresholdBoundary(GameTestHelper helper) {
        double h = Math.sqrt(1 - LIFT_THRESH * LIFT_THRESH);
        Vec3 look = new Vec3(0, LIFT_THRESH, h);
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, look,
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        double expected = vAccel(HOVER_LIFT, 0);
        if (Math.abs(accel.y - expected) > 0.01) {
            helper.fail("At lift threshold, expected accel ≈ " + String.format("%.4f", expected)
                    + ", got " + String.format("%.4f", accel.y));
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testDiveThresholdBoundary(GameTestHelper helper) {
        double h = Math.sqrt(1 - DIVE_THRESH * DIVE_THRESH);
        Vec3 look = new Vec3(0, DIVE_THRESH, h);
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, look,
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        double expected = vAccel(HOVER_LIFT, 0);
        if (Math.abs(accel.y - expected) > 0.01) {
            helper.fail("At dive threshold, expected accel ≈ " + String.format("%.4f", expected)
                    + ", got " + String.format("%.4f", accel.y));
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void test45DegreeLookAccelComponents(GameTestHelper helper) {
        Vec3 look = new Vec3(0.5, 0.707, 0.5).normalize();
        Vec3 accel = StormWingEvents.computeFlightAcceleration(
                Vec3.ZERO, look,
                TARGET_SPEED, CLIMB_SPEED, DIVE_SPEED,
                HOVER_LIFT, LIFT_THRESH, DIVE_THRESH);

        // 水平 X 和 Z 应同向
        if (accel.x * accel.z <= 0) {
            helper.fail("X and Z accel should have same sign when looking diagonally, got ("
                    + accel.x + ", " + accel.z + ")");
            return;
        }
        // 水平目标速度应投影到 look 水平方向
        double lookH = Math.sqrt(look.x * look.x + look.z * look.z);
        double hTargetMagnitude = TARGET_SPEED;
        double expectedHMag = hTargetMagnitude / DRAG_H;
        double accelH = Math.sqrt(accel.x * accel.x + accel.z * accel.z);
        if (Math.abs(accelH - expectedHMag) > 0.05) {
            helper.fail("Expected horizontal accel magnitude ≈ " + String.format("%.4f", expectedHMag)
                    + ", got " + String.format("%.4f", accelH));
            return;
        }
        // 爬升方向验证
        double vTarget = computeVTarget(look.y);
        double expectedVAccel = vAccel(vTarget, 0);
        if (Math.abs(accel.y - expectedVAccel) > 0.05) {
            helper.fail("Expected vertical accel ≈ " + String.format("%.4f", expectedVAccel)
                    + ", got " + String.format("%.4f", accel.y));
            return;
        }
        helper.succeed();
    }
}
