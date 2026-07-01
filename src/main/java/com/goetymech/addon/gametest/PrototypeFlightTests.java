package com.goetymech.addon.gametest;

import com.goetymech.addon.goetymechMod;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 椋炶娴嬭瘯鏆傛椂绉婚櫎銆傞琛屾牳蹇冮€昏緫鍦?PrototypeTravelMixin 涓紝
 * 鏄?LivingEntity.travel() HEAD 娉ㄥ叆鐨?velocity-set 妯″瀷锛? * 渚濊禆 Minecraft 杩愯鏃剁幆澧冿紝涓嶉€傚悎绾?GameTest 楠岃瘉銆? * 鍔熻兘楠岃瘉闇€閫氳繃 runClient 鎵嬪姩娴嬭瘯銆? */
@Mod.EventBusSubscriber(modid = goetymechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PrototypeFlightTests {

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        // No tests for now 鈥?flight model is Mixin-based, runtime-tested
    }
}
