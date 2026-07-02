package com.goetymech.addon.common.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhantomWandManager {
    private static final Map<UUID, ItemStack> PHANTOM_WANDS = new ConcurrentHashMap<>();

    public static void set(Player player, ItemStack wand) {
        PHANTOM_WANDS.put(player.getUUID(), wand);
    }

    public static void clear(Player player) {
        PHANTOM_WANDS.remove(player.getUUID());
    }

    public static ItemStack get(UUID uuid) {
        return PHANTOM_WANDS.getOrDefault(uuid, ItemStack.EMPTY);
    }
}
