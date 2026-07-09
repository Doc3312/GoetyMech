package com.goetymech.addon.common.data;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class SoulMonumentDataManager {

    private static final LevelResource DATA_DIR = new LevelResource("data/goetymech/monument");

    public static Path getDataDir(ServerLevel level) {
        return level.getServer().getWorldPath(DATA_DIR);
    }

    public static Path getEntityFile(ServerLevel level, UUID entityUuid) {
        return getDataDir(level).resolve(entityUuid.toString() + ".dat");
    }

    public static void saveEntityData(ServerLevel level, UUID entityUuid, CompoundTag tag) {
        try {
            Path dir = getDataDir(level);
            Files.createDirectories(dir);
            Path file = dir.resolve(entityUuid.toString() + ".dat");
            NbtIo.writeCompressed(tag, file.toFile());
        } catch (IOException e) {
            GoetyMechMod.LOGGER.error("Failed to save monument entity data for {}: {}", entityUuid, e.getMessage());
        }
    }

    public static CompoundTag loadEntityData(ServerLevel level, UUID entityUuid) {
        try {
            Path file = getEntityFile(level, entityUuid);
            if (Files.exists(file)) {
                return NbtIo.readCompressed(file.toFile());
            }
        } catch (IOException e) {
            GoetyMechMod.LOGGER.error("Failed to load monument entity data for {}: {}", entityUuid, e.getMessage());
        }
        return null;
    }

    public static void deleteEntityData(ServerLevel level, UUID entityUuid) {
        try {
            Path file = getEntityFile(level, entityUuid);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            GoetyMechMod.LOGGER.error("Failed to delete monument entity data for {}: {}", entityUuid, e.getMessage());
        }
    }
}
