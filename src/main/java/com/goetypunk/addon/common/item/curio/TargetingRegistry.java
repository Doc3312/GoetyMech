package com.goetypunk.addon.common.item.curio;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class TargetingRegistry {
    private static final Map<Item, TargetingType> TARGET_MAP = new HashMap<>();

    static {
        registerGoetyFoci();
    }

    private static void registerGoetyFoci() {
        for (var entry : SELF_FOCI) {
            TARGET_MAP.put(entry, TargetingType.SELF);
        }
        for (var entry : RETALIATORY_FOCI) {
            TARGET_MAP.put(entry, TargetingType.RETALIATORY);
        }
        for (var entry : SUMMON_FOCI) {
            TARGET_MAP.put(entry, TargetingType.SUMMON);
        }
        for (var entry : UTILITY_FOCI) {
            TARGET_MAP.put(entry, TargetingType.UTILITY);
        }
    }

    public static TargetingType getTargetingType(Item focusItem) {
        return TARGET_MAP.getOrDefault(focusItem, TargetingType.SELF);
    }

    public static void register(Item focusItem, TargetingType type) {
        TARGET_MAP.put(focusItem, type);
    }

    private static final Item[] SELF_FOCI = {
            ModItems.BULWARK_FOCUS.get(),
            ModItems.IRON_HIDE_FOCUS.get(),
            ModItems.SOUL_HEAL_FOCUS.get(),
            ModItems.CHILLING_FOCUS.get(),
            ModItems.FLYING_FOCUS.get(),
            ModItems.CUSHION_FOCUS.get(),
            ModItems.LAUNCH_FOCUS.get(),
            ModItems.LEAPING_FOCUS.get(),
    };

    private static final Item[] RETALIATORY_FOCI = {
            ModItems.MAGIC_BOLT_FOCUS.get(),
            ModItems.SOUL_BOLT_FOCUS.get(),
            ModItems.SONIC_BOOM_FOCUS.get(),
            ModItems.BITING_FOCUS.get(),
            ModItems.FEAST_FOCUS.get(),
            ModItems.TEETH_FOCUS.get(),
            ModItems.SHREDDING_FOCUS.get(),
            ModItems.SWORD_FOCUS.get(),
            ModItems.LIGHTNING_FOCUS.get(),
            ModItems.SHOCKWAVE_FOCUS.get(),
            ModItems.CORRUPTION_FOCUS.get(),
            ModItems.FIRE_BREATH_FOCUS.get(),
            ModItems.FIREBALL_FOCUS.get(),
            ModItems.LAVABALL_FOCUS.get(),
            ModItems.WITHER_SKULL_FOCUS.get(),
            ModItems.ICE_SPIKE_FOCUS.get(),
            ModItems.FIRE_BLAST_FOCUS.get(),
            ModItems.FLAME_STRIKE_FOCUS.get(),
            ModItems.BOMBARDMENT_FOCUS.get(),
            ModItems.METEOR_SHOWER_FOCUS.get(),
            ModItems.MAGMA_BOMB_FOCUS.get(),
            ModItems.FROST_NOVA_FOCUS.get(),
            ModItems.HAIL_FOCUS.get(),
            ModItems.ICE_STORM_FOCUS.get(),
            ModItems.BLIZZARD_FOCUS.get(),
            ModItems.THUNDERBOLT_FOCUS.get(),
            ModItems.MONSOON_FOCUS.get(),
            ModItems.ELECTROCUTE_FOCUS.get(),
            ModItems.SURGING_FOCUS.get(),
            ModItems.DISCHARGE_FOCUS.get(),
            ModItems.BOLTING_FOCUS.get(),
            ModItems.WIND_BLAST_FOCUS.get(),
            ModItems.WHIRLWIND_FOCUS.get(),
            ModItems.CYCLONE_FOCUS.get(),
            ModItems.QUAKING_FOCUS.get(),
            ModItems.EARTH_PUNCH_FOCUS.get(),
            ModItems.PULVERIZE_FOCUS.get(),
            ModItems.ERUPTION_FOCUS.get(),
            ModItems.SCATTER_FOCUS.get(),
            ModItems.STEAMING_FOCUS.get(),
            ModItems.PRISMA_BEAM_FOCUS.get(),
            ModItems.TIDAL_FOCUS.get(),
            ModItems.RUPTURE_FOCUS.get(),
            ModItems.STELLAR_FOCUS.get(),
            ModItems.VOID_FLASH_FOCUS.get(),
            ModItems.FROST_BREATH_FOCUS.get(),
            ModItems.ARROW_RAIN_FOCUS.get(),
            ModItems.ICEOLOGY_FOCUS.get(),
            ModItems.BANISH_FOCUS.get(),
            ModItems.SKULL_FOCUS.get(),
            ModItems.POISON_DART_FOCUS.get(),
            ModItems.WATER_JET_FOCUS.get(),
            ModItems.BOUNCY_BUBBLE_FOCUS.get(),
            ModItems.RAZOR_WIND_FOCUS.get(),
    };

    private static final Item[] SUMMON_FOCI = {
            ModItems.VEXING_FOCUS.get(),
            ModItems.ROTTING_FOCUS.get(),
            ModItems.OSSEOUS_FOCUS.get(),
            ModItems.SPOOKY_FOCUS.get(),
            ModItems.PHANTASM_FOCUS.get(),
            ModItems.VANGUARD_FOCUS.get(),
            ModItems.BLACKGUARD_FOCUS.get(),
            ModItems.GHOST_FIRE_FOCUS.get(),
            ModItems.REAPING_FOCUS.get(),
            ModItems.LEECHING_FOCUS.get(),
            ModItems.KILLING_FOCUS.get(),
            ModItems.SWARM_FOCUS.get(),
            ModItems.MAULING_FOCUS.get(),
            ModItems.HUNTING_FOCUS.get(),
            ModItems.SLIMY_FOCUS.get(),
            ModItems.CARRION_FOCUS.get(),
            ModItems.FROSTBORN_FOCUS.get(),
            ModItems.SPRIGHTLY_FOCUS.get(),
            ModItems.GHASTLY_FOCUS.get(),
            ModItems.BLAZING_FOCUS.get(),
            ModItems.GUARDIAN_FOCUS.get(),
            ModItems.WATCHING_FOCUS.get(),
            ModItems.BLASTING_FOCUS.get(),
            ModItems.SNARING_FOCUS.get(),
            ModItems.HOGGING_FOCUS.get(),
            ModItems.MINISTROUS_FOCUS.get(),
    };

    private static final Item[] UTILITY_FOCI = {
            ModItems.SOUL_LIGHT_FOCUS.get(),
            ModItems.GLOW_LIGHT_FOCUS.get(),
            ModItems.ILLUMINATE_FOCUS.get(),
            ModItems.CRAFTING_FOCUS.get(),
            ModItems.ENDER_CHEST_FOCUS.get(),
            ModItems.TELEKINESIS_FOCUS.get(),
            ModItems.WEAKENING_FOCUS.get(),
            ModItems.SENSING_FOCUS.get(),
            ModItems.ROTATION_FOCUS.get(),
            ModItems.BURROWING_FOCUS.get(),
            ModItems.BARRICADE_FOCUS.get(),
            ModItems.CALL_FOCUS.get(),
            ModItems.TROOP_FOCUS.get(),
            ModItems.RECALL_FOCUS.get(),
            ModItems.END_WALK_FOCUS.get(),
            ModItems.BLINK_FOCUS.get(),
            ModItems.CHARGE_FOCUS.get(),
            ModItems.SHOCKING_FOCUS.get(),
            ModItems.UPDRAFT_FOCUS.get(),
            ModItems.TREMBLING_FOCUS.get(),
            ModItems.GRAPPLE_FOCUS.get(),
            ModItems.ENTANGLING_FOCUS.get(),
            ModItems.WHISPERING_FOCUS.get(),
            ModItems.BLOSSOMING_FOCUS.get(),
            ModItems.OVERGROWTH_FOCUS.get(),
            ModItems.BIOMINE_FOCUS.get(),
            ModItems.WATER_WHIP_FOCUS.get(),
            ModItems.TRIDENT_STORM_FOCUS.get(),
            ModItems.COMMAND_FOCUS.get(),
            ModItems.ORDER_FOCUS.get(),
            ModItems.TUNNEL_FOCUS.get(),
    };
}
