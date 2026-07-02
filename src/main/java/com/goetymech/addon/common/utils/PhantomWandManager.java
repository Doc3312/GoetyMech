package com.goetymech.addon.common.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhantomWandManager {
    private static final Map<UUID, ItemStack> PHANTOM_WANDS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> PENDING_CLEAR = new ConcurrentHashMap<>();

    public static void set(Player player, ItemStack wand) {
        UUID uuid = player.getUUID();
        PHANTOM_WANDS.put(uuid, wand);
        PENDING_CLEAR.remove(uuid);
    }

    public static void clear(Player player) {
        UUID uuid = player.getUUID();
        PHANTOM_WANDS.remove(uuid);
        PENDING_CLEAR.remove(uuid);
    }

    public static ItemStack get(UUID uuid) {
        return PHANTOM_WANDS.getOrDefault(uuid, ItemStack.EMPTY);
    }

    public static void scheduleClear(UUID uuid, int delayTicks) {
        PENDING_CLEAR.put(uuid, delayTicks);
    }

    public static void tick() {
        for (UUID uuid : new java.util.ArrayList<>(PENDING_CLEAR.keySet())) {
            int remaining = PENDING_CLEAR.get(uuid) - 1;
            if (remaining <= 0) {
                PENDING_CLEAR.remove(uuid);
                PHANTOM_WANDS.remove(uuid);
            } else {
                PENDING_CLEAR.put(uuid, remaining);
            }
        }
    }
}
