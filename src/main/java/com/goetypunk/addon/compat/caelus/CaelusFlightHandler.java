package com.goetypunk.addon.compat.caelus;

import com.goetypunk.addon.common.item.StormWingItem;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * 当 StormWing 装备在 Curios 背部槽时，通过 Caelus API 添加飞行 attribute
 * 注意：本类不直接 import CaelusApi，通过 ResourceLocation 字符串查询 attribute，避免类加载失败
 */
public class CaelusFlightHandler {

    private static final UUID FLIGHT_MODIFIER_UUID = UUID.fromString("c1c1b29a-3e6f-4d6b-9f1a-8c5d3e7b2a0f");
    private static final String FLIGHT_MODIFIER_NAME = "storm_wing_curios_flight";

    private static Attribute flightAttribute = null;

    /**
     * 安全获取 Caelus flight attribute — 若 Caelus 未加载则返回 null
     */
    private static Attribute getFlightAttribute() {
        if (flightAttribute != null) return flightAttribute;
        try {
            flightAttribute = ForgeRegistries.ATTRIBUTES.getValue(
                    new ResourceLocation("caelus", "flight"));
        } catch (Exception | NoClassDefFoundError e) {
            // Caelus not available
        }
        return flightAttribute;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!CaelusLoaded.isLoaded()) return;

        Player player = event.player;
        Attribute attr = getFlightAttribute();
        if (attr == null) return;

        AttributeInstance flightAttr = player.getAttribute(attr);
        if (flightAttr == null) return;

        flightAttr.removeModifier(FLIGHT_MODIFIER_UUID);

        if (hasStormWingInCuriosSlot(player)) {
            flightAttr.addTransientModifier(
                    new AttributeModifier(FLIGHT_MODIFIER_UUID, FLIGHT_MODIFIER_NAME, 1.0,
                            AttributeModifier.Operation.ADDITION));
        }
    }

    private static boolean hasStormWingInCuriosSlot(Player player) {
        try {
            var curios = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);
            return curios.map(inv -> inv.findFirstCurio(
                    s -> s.getItem() instanceof StormWingItem).isPresent()).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
