package com.goetymech.addon.compat.caelus;

import com.goetymech.addon.common.utils.CuriosHelper;
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
 * 褰?Prototype 瑁呭鍦?Curios 鑳岄儴妲芥椂锛岄€氳繃 Caelus API 娣诲姞椋炶 attribute
 * 娉ㄦ剰锛氭湰绫讳笉鐩存帴 import CaelusApi锛岄€氳繃 ResourceLocation 瀛楃涓叉煡璇?attribute锛岄伩鍏嶇被鍔犺浇澶辫触
 */
public class CaelusFlightHandler {

    private static final UUID FLIGHT_MODIFIER_UUID = UUID.fromString("c1c1b29a-3e6f-4d6b-9f1a-8c5d3e7b2a0f");
    private static final String FLIGHT_MODIFIER_NAME = "prototype_curios_flight";

    private static Attribute flightAttribute = null;

    /**
     * 瀹夊叏鑾峰彇 Caelus flight attribute 鈥?鑻?Caelus 鏈姞杞藉垯杩斿洖 null
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

        if (CuriosHelper.hasPrototypeInCurios(player)) {
            flightAttr.addTransientModifier(
                    new AttributeModifier(FLIGHT_MODIFIER_UUID, FLIGHT_MODIFIER_NAME, 1.0,
                            AttributeModifier.Operation.ADDITION));
        }
    }

}
