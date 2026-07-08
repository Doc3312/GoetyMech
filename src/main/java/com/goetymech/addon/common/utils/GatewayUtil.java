package com.goetymech.addon.common.utils;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
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
            // 1. Try data-driven mapping (from JSON reload listener)
            ResourceLocation mapped = GatewayMappingLoader.getTagForItem(id);
            if (mapped != null) {
                TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, mapped);
                Registry<Structure> registry = registryAccess.registryOrThrow(Registries.STRUCTURE);
                if (registry.getTag(tag).isPresent() && registry.getTag(tag).get().size() > 0) {
                    return tag;
                }
            }

            // 2. Fallback: convention-based derivation
            String path = id.getPath();
            String namespace = id.getNamespace();
            String stem = path.replaceAll("_eye$", "").replaceAll("^eye_of_", "");

            TagKey<Structure> tag = tryTag(registryAccess, namespace, "explorer_maps/" + stem);
            if (tag != null) return tag;

            tag = tryTag(registryAccess, namespace, "eye_of_" + stem + "_located");
            if (tag != null) return tag;

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

    public static boolean isMapWithMarker(ItemStack stack, ServerLevel level) {
        if (!stack.is(Items.FILLED_MAP)) return false;
        MapItemSavedData data = MapItem.getSavedData(stack, level);
        if (data == null) return false;
        for (MapDecoration deco : data.decorations.values()) {
            if (deco.getType() != MapDecoration.Type.PLAYER
                    && deco.getType() != MapDecoration.Type.FRAME
                    && deco.getType() != MapDecoration.Type.PLAYER_OFF_MAP
                    && deco.getType() != MapDecoration.Type.PLAYER_OFF_LIMITS) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static BlockPos readMapTarget(ItemStack stack, ServerLevel level) {
        if (!stack.is(Items.FILLED_MAP)) return null;
        MapItemSavedData data = MapItem.getSavedData(stack, level);
        if (data == null) return null;
        for (MapDecoration deco : data.decorations.values()) {
            if (deco.getType() == MapDecoration.Type.PLAYER
                    || deco.getType() == MapDecoration.Type.FRAME
                    || deco.getType() == MapDecoration.Type.PLAYER_OFF_MAP
                    || deco.getType() == MapDecoration.Type.PLAYER_OFF_LIMITS) {
                continue;
            }
            int scaleMul = 1 << data.scale;
            int worldX = (int)(deco.getX() / 2.0f * scaleMul) + data.centerX;
            int worldZ = (int)(deco.getY() / 2.0f * scaleMul) + data.centerZ;
            int worldY = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(worldX, 0, worldZ)).getY();
            return new BlockPos(worldX, worldY, worldZ);
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
        BlockPos center = BlockPos.containing(structureBounds.getCenter());

        for (int radius = 20; radius <= 50; radius += 5) {
            for (BlockPos candidate : perimeterCandidates(center, radius)) {
                if (isInsideStructureBounds(structureBounds, candidate)) continue;
                BlockPos safe = checkPosition(level, candidate);
                if (safe != null) return safe;
            }
        }

        return null;
    }

    private static java.util.List<BlockPos> perimeterCandidates(BlockPos center, int radius) {
        java.util.List<BlockPos> list = new java.util.ArrayList<>();
        int r = radius;
        list.add(center.offset(r, 0, 0));
        list.add(center.offset(-r, 0, 0));
        list.add(center.offset(0, 0, r));
        list.add(center.offset(0, 0, -r));
        if (r >= 10) {
            list.add(center.offset(r, 0, r));
            list.add(center.offset(r, 0, -r));
            list.add(center.offset(-r, 0, r));
            list.add(center.offset(-r, 0, -r));
        }
        return list;
    }

    private static boolean isInsideStructureBounds(AABB bounds, BlockPos pos) {
        return bounds.minX <= pos.getX() && pos.getX() <= bounds.maxX
                && bounds.minZ <= pos.getZ() && pos.getZ() <= bounds.maxZ;
    }

    @Nullable
    public static BlockPos checkPosition(ServerLevel level, BlockPos xzPos) {
        int groundY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, xzPos).getY();
        if (groundY <= level.getMinBuildHeight()) return null;
        BlockPos candidate = xzPos.atY(groundY);
        Optional<Vec3> safe = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, candidate);
        if (safe.isEmpty()) return null;
        BlockPos safePos = BlockPos.containing(safe.get());
        if (hasLavaAt(level, safePos)) return null;
        return safePos;
    }

    private static boolean hasLavaAt(ServerLevel level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getFluidState(pos.offset(dx, 0, dz)).is(FluidTags.LAVA)) return true;
            }
        }
        return level.getFluidState(pos.below()).is(FluidTags.LAVA);
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
