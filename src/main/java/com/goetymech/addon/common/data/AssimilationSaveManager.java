package com.goetymech.addon.common.data;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CAssimilationDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 原型同化数据管理器。持久化玩家同化击杀数。
 */
public class AssimilationSaveManager {
    private static final Set<UUID> DIRTY = new HashSet<>();

    public static void markDirty(ServerPlayer player) {
        DIRTY.add(player.getUUID());
    }

    public static void onPlayerLogin(ServerPlayer player) {
        int cached = loadCachedKills(player);
        if (cached < 0) return;

        int current = PrototypeItem.getAssimilationKills(player);
        if (cached != current) {
            if (cached > current) {
                PrototypeItem.setAssimilationKills(player, cached);
                ModNetwork.sendTo(player, new S2CAssimilationDataPacket(cached));
                GoetyMechMod.LOGGER.info("Assimilation recovered from cache: {} -> {} for {}",
                        current, cached, player.getName().getString());
            }
            markDirty(player);
        }
    }

    public static void flushDirty(MinecraftServer server) {
        if (DIRTY.isEmpty()) return;

        Path baseDir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        for (UUID uuid : DIRTY) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) continue;

            int kills = PrototypeItem.getAssimilationKills(player);
            Path file = baseDir.resolve(player.getStringUUID() + "_assimilation.dat");
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try {
                Files.writeString(tmp, String.valueOf(kills));
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                GoetyMechMod.LOGGER.error("Failed to write assimilation cache for {}: {}", uuid, e.getMessage());
                try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        }
        DIRTY.clear();
    }

    private static int loadCachedKills(ServerPlayer player) {
        Path file = player.server.getWorldPath(LevelResource.PLAYER_DATA_DIR)
                .resolve(player.getStringUUID() + "_assimilation.dat");
        try {
            if (Files.exists(file)) {
                return Integer.parseInt(Files.readString(file).trim());
            }
        } catch (Exception e) {
            GoetyMechMod.LOGGER.warn("Could not read assimilation cache for {}: {}", player.getStringUUID(), e.getMessage());
        }
        return -1;
    }
}
