package com.goetymech.addon.gametest;

import com.goetymech.addon.GoetyMechMod;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 飞行测试暂时移除。飞行核心逻辑在 PrototypeTravelMixin 中，
 * 是 LivingEntity.travel() HEAD 注入的 velocity-set 模型，
 * 依赖 Minecraft 运行时环境，不适合纯 GameTest 验证。
 * 功能验证需通过 runClient 手动测试。
 */
@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PrototypeFlightTests {

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        // No tests for now — flight model is Mixin-based, runtime-tested
    }
}
