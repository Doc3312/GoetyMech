package com.goetymech.addon.compat.curios;

import com.goetymech.addon.GoetyMechMod;
import net.minecraftforge.fml.common.Mod;

/**
 * Curios 槽位说明：
 * Goety 的 {@code Goety.java:778-785} 已注册所有常用基础槽位
 * （BELT、BODY、BACK、CHARM、HEAD、NECKLACE、HANDS、RING、CURIO），
 * 因此本模组无需通过 InterModComms 重复注册。
 * <p>
 * 新增 Curios 饰品只需：
 * 1. ModItems.java 注册物品（implements ICurioItem）
 * 2. data/curios/tags/items/&lt;slot&gt;.json 声明物品→槽位映射
 */
@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CuriosIntegration {
}
