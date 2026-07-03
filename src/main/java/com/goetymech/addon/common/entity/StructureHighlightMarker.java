package com.goetymech.addon.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class StructureHighlightMarker extends Entity {

    private static final EntityDataAccessor<Integer> DATA_MIN_X = SynchedEntityData.defineId(StructureHighlightMarker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MIN_Y = SynchedEntityData.defineId(StructureHighlightMarker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MIN_Z = SynchedEntityData.defineId(StructureHighlightMarker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MAX_X = SynchedEntityData.defineId(StructureHighlightMarker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MAX_Y = SynchedEntityData.defineId(StructureHighlightMarker.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MAX_Z = SynchedEntityData.defineId(StructureHighlightMarker.class, EntityDataSerializers.INT);

    private int despawnTicks = 1200;

    public StructureHighlightMarker(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public StructureHighlightMarker(EntityType<?> type, Level level, AABB bounds) {
        this(type, level);
        setStructureBounds(bounds);
        setPos(bounds.getCenter().x, bounds.getCenter().y, bounds.getCenter().z);
    }

    public void setStructureBounds(AABB bounds) {
        entityData.set(DATA_MIN_X, (int) Math.floor(bounds.minX));
        entityData.set(DATA_MIN_Y, (int) Math.floor(bounds.minY));
        entityData.set(DATA_MIN_Z, (int) Math.floor(bounds.minZ));
        entityData.set(DATA_MAX_X, (int) Math.ceil(bounds.maxX));
        entityData.set(DATA_MAX_Y, (int) Math.ceil(bounds.maxY));
        entityData.set(DATA_MAX_Z, (int) Math.ceil(bounds.maxZ));
    }

    public AABB getStructureBounds() {
        return new AABB(
                entityData.get(DATA_MIN_X), entityData.get(DATA_MIN_Y), entityData.get(DATA_MIN_Z),
                entityData.get(DATA_MAX_X), entityData.get(DATA_MAX_Y), entityData.get(DATA_MAX_Z)
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (despawnTicks-- <= 0) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_MIN_X, 0);
        entityData.define(DATA_MIN_Y, 0);
        entityData.define(DATA_MIN_Z, 0);
        entityData.define(DATA_MAX_X, 0);
        entityData.define(DATA_MAX_Y, 0);
        entityData.define(DATA_MAX_Z, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BoundsMinX")) {
            entityData.set(DATA_MIN_X, tag.getInt("BoundsMinX"));
            entityData.set(DATA_MIN_Y, tag.getInt("BoundsMinY"));
            entityData.set(DATA_MIN_Z, tag.getInt("BoundsMinZ"));
            entityData.set(DATA_MAX_X, tag.getInt("BoundsMaxX"));
            entityData.set(DATA_MAX_Y, tag.getInt("BoundsMaxY"));
            entityData.set(DATA_MAX_Z, tag.getInt("BoundsMaxZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("BoundsMinX", entityData.get(DATA_MIN_X));
        tag.putInt("BoundsMinY", entityData.get(DATA_MIN_Y));
        tag.putInt("BoundsMinZ", entityData.get(DATA_MIN_Z));
        tag.putInt("BoundsMaxX", entityData.get(DATA_MAX_X));
        tag.putInt("BoundsMaxY", entityData.get(DATA_MAX_Y));
        tag.putInt("BoundsMaxZ", entityData.get(DATA_MAX_Z));
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
