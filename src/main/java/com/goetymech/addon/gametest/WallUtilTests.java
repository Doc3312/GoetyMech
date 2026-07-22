package com.goetymech.addon.gametest;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.utils.WallUtil;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
/**
 * 墙跑工具单元测试。
 */
public class WallUtilTests {

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(WallUtilTests.class);
    }

    @GameTest(timeoutTicks = 200)
    public static void testProjectOnPlane(GameTestHelper helper) {
        Vec3 normal = new Vec3(0, 0, 1);
        Vec3 vec = new Vec3(1, 2, 3);
        Vec3 projected = WallUtil.projectOnPlane(vec, normal);
        if (Math.abs(projected.x - 1) > 0.001 || Math.abs(projected.y - 2) > 0.001 || Math.abs(projected.z) > 0.001) {
            helper.fail("projectOnPlane failed: expected (1,2,0) got " + projected);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testProjectOnPlaneCollinear(GameTestHelper helper) {
        Vec3 normal = new Vec3(1, 0, 0);
        Vec3 vec = new Vec3(5, 0, 0);
        Vec3 projected = WallUtil.projectOnPlane(vec, normal);
        if (projected.lengthSqr() > 0.001) {
            helper.fail("projectOnPlane failed: expected (0,0,0) got " + projected);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testProjectOnPlanePerpendicular(GameTestHelper helper) {
        Vec3 normal = new Vec3(1, 0, 0);
        Vec3 vec = new Vec3(0, 1, 0);
        Vec3 projected = WallUtil.projectOnPlane(vec, normal);
        if (Math.abs(projected.x) > 0.001 || Math.abs(projected.y - 1) > 0.001 || Math.abs(projected.z) > 0.001) {
            helper.fail("projectOnPlane failed: expected (0,1,0) got " + projected);
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testHasAdjacentWall(GameTestHelper helper) {
        BlockPos wallPos = new BlockPos(1, 0, 0);
        helper.setBlock(wallPos, Blocks.STONE);
        Player player = helper.makeMockPlayer();
        player.setPos(player.getX() + 0.3, player.getY(), player.getZ());
        if (!WallUtil.hasAdjacentWall(player)) {
            helper.fail("hasAdjacentWall expected true with block at (1,0,0)");
            return;
        }
        helper.succeed();
    }

    @GameTest(timeoutTicks = 200)
    public static void testNoAdjacentWall(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (WallUtil.hasAdjacentWall(player)) {
            helper.fail("hasAdjacentWall expected false in open area");
            return;
        }
        helper.succeed();
    }
}
