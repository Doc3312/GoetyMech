package com.goetymech.addon.common.utils;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GatewayUtil {

    private static final java.util.Map<ResourceLocation, TagKey<Structure>> TAG_CACHE = new java.util.HashMap<>();

    @Nullable
    public static TagKey<Structure> deriveStructureTag(RegistryAccess registryAccess, ItemStack offhand) {
        if (offhand.isEmpty()) return null;
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(offhand.getItem());
        if (itemId == null) return null;

        return TAG_CACHE.computeIfAbsent(itemId, id -> {
            String path = id.getPath();
            String namespace = id.getNamespace();
            String stem = path.replaceAll("_eye$", "").replaceAll("^eye_of_", "");

            // Try convention: <namespace>:explorer_maps/<stem>
            TagKey<Structure> tag = tryTag(registryAccess, namespace, "explorer_maps/" + stem);
            if (tag != null) return tag;

            // Try vanilla convention: <namespace>:eye_of_<stem>_located
            tag = tryTag(registryAccess, namespace, "eye_of_" + stem + "_located");
            if (tag != null) return tag;

            // Try: <namespace>:<stem>
            tag = tryTag(registryAccess, namespace, stem);
            if (tag != null) return tag;

            return null;
        });
    }

    @Nullable
    private static TagKey<Structure> tryTag(RegistryAccess registryAccess, String namespace, String path) {
        ResourceLocation loc = new ResourceLocation(namespace, path);
        Registry<Structure> registry = registryAccess.registryOrThrow(Registries.STRUCTURE);
        TagKey<Structure> tagKey = TagKey.create(Registries.STRUCTURE, loc);
        if (registry.getTag(tagKey).isPresent() && registry.getTag(tagKey).get().size() > 0) {
            return tagKey;
        }
        return null;
    }

    @Nullable
    public static BlockPos findNearestStructure(ServerLevel level, TagKey<Structure> tag, BlockPos origin) {
        return level.findNearestMapStructure(tag, origin, 100, false);
    }

    @Nullable
    public static AABB getStructureBoundingBox(ServerLevel level, BlockPos pos, TagKey<Structure> tag) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos, tag);
        if (start == null || start == StructureStart.INVALID_START) return null;
        net.minecraft.world.level.levelgen.structure.BoundingBox bb = start.getBoundingBox();
        return new AABB(bb.minX(), bb.minY(), bb.minZ(), bb.maxX() + 1, bb.maxY() + 1, bb.maxZ() + 1);
    }

    @Nullable
    public static BlockPos findSafeTeleportPos(ServerLevel level, AABB structureBounds, Player player) {
        int startRadius = 20;
        int maxRadius = 50;
        int verticalRange = 20;

        BlockPos center = BlockPos.containing(structureBounds.getCenter());

        for (int radius = startRadius; radius <= maxRadius; radius += 5) {
            for (int dy = -verticalRange; dy <= verticalRange; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                        BlockPos candidate = center.offset(dx, dy, dz);
                        if (isInsideStructureBounds(structureBounds, candidate)) continue;
                        Vec3 safe = checkPosition(level, candidate);
                        if (safe != null) return BlockPos.containing(safe);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isInsideStructureBounds(AABB bounds, BlockPos pos) {
        return bounds.minX <= pos.getX() && pos.getX() <= bounds.maxX
                && bounds.minY <= pos.getY() && pos.getY() <= bounds.maxY
                && bounds.minZ <= pos.getZ() && pos.getZ() <= bounds.maxZ;
    }

    @Nullable
    public static Vec3 checkPosition(ServerLevel level, BlockPos pos) {
        if (pos.getY() < level.getMinBuildHeight()) return null;
        Optional<Vec3> safe = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, pos);
        if (safe.isEmpty()) return null;
        BlockPos safePos = BlockPos.containing(safe.get());
        if (hasLavaAt(level, safePos)) return null;
        return safe.get();
    }

    private static boolean hasLavaAt(ServerLevel level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getFluidState(pos.offset(dx, 0, dz)).is(net.minecraft.tags.FluidTags.LAVA)) return true;
            }
        }
        return level.getFluidState(pos.below()).is(net.minecraft.tags.FluidTags.LAVA);
    }

    public static boolean teleportPlayer(ServerLevel targetLevel, Player player, Vec3 pos) {
        if (player.level().dimension() != targetLevel.dimension()) {
            com.Polarice3.Goety.common.events.ArcaTeleporter teleporter =
                    new com.Polarice3.Goety.common.events.ArcaTeleporter(pos);
            ((net.minecraft.server.level.ServerPlayer) player).changeDimension(targetLevel, teleporter);
        } else {
            player.teleportTo(targetLevel, pos.x, pos.y, pos.z, Set.of(), player.getYRot(), player.getXRot());
        }
        return true;
    }
}
