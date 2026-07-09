package com.goetymech.addon.common.events;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class PrototypeStateResetHandler {

    private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private static final String[] RESET_TAGS = {
            "goetymech.slamming",
            "goetymech.slam_tick",
            "goetymech.slam_start_y",
            "goetymech.slam_invuln_tick",
            "goetymech.prev_crouch",
            "goetymech.charged_jumping",
            "goetymech.cj_hx",
            "goetymech.cj_hz",
            "goetymech.cj_start_speed",
            "goetymech.wall_running",
            "goetymech.wall_nx",
            "goetymech.wall_ny",
            "goetymech.wall_nz",
            "goetymech.wall_run_ticks",
            "goetymech.jump_charge_tick",
            "goetymech.sprint_tick",
            "goetymech.ground_speed",
            "goetymech.flight_forward",
            "goetymech.swim_tick",
            "goetymech.flight_tick",
            "goetymech.charge_start_y"
    };

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        resetPrototypeState(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        resetPrototypeState(player);
    }

    private static void resetPrototypeState(Player player) {
        var data = player.getPersistentData();
        for (String tag : RESET_TAGS) {
            data.remove(tag);
        }

        AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(SPRINT_MODIFIER_UUID);
        }
    }
}
