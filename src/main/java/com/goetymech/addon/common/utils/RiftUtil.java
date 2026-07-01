package com.goetymech.addon.common.utils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RiftUtil {

    private record DimensionPair(ResourceKey<Level> a, ResourceKey<Level> b, double aToBScale) {
        ResourceKey<Level> partner(ResourceKey<Level> dim) {
            if (dim != null && dim.equals(a)) return b;
            if (dim != null && dim.equals(b)) return a;
            return null;
        }
        double scaleTo(ResourceKey<Level> from, ResourceKey<Level> to) {
            if (from.equals(a) && to.equals(b)) return aToBScale;
            if (from.equals(b) && to.equals(a)) return 1.0 / aToBScale;
            return 1.0;
        }
    }

    private static final List<DimensionPair> PAIRS = List.of(
            new DimensionPair(Level.OVERWORLD, Level.NETHER, 1.0 / 8.0)
    );

    public static ResourceKey<Level> getPartner(ResourceKey<Level> current) {
        for (DimensionPair pair : PAIRS) {
            ResourceKey<Level> partner = pair.partner(current);
            if (partner != null) return partner;
        }
        return null;
    }

    public static boolean isRiftable(ResourceKey<Level> dim) {
        return getPartner(dim) != null;
    }

    public static Vec3 mapCoordinates(Vec3 pos, ResourceKey<Level> from, ResourceKey<Level> to) {
        for (DimensionPair pair : PAIRS) {
            ResourceKey<Level> partner = pair.partner(from);
            if (partner != null && partner.equals(to)) {
                double scale = pair.scaleTo(from, to);
                return new Vec3(pos.x * scale, pos.y, pos.z * scale);
            }
        }
        return pos;
    }
}
