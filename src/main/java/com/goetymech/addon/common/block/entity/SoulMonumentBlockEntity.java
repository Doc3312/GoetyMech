package com.goetymech.addon.common.block.entity;

import com.goetymech.addon.common.block.SoulMonumentBlock;
import com.goetymech.addon.common.block.Tier1SoulMonumentBlock;
import com.goetymech.addon.common.block.Tier2SoulMonumentBlock;
import com.goetymech.addon.common.block.Tier3SoulMonumentBlock;
import com.goetymech.addon.common.data.SoulMonumentDataManager;
import com.goetymech.addon.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SoulMonumentBlockEntity extends BlockEntity {

    private static final String TAG_SAVED_UUIDS = "SavedEntityUUIDs";
    private static final String TAG_RECORDING = "Recording";
    private static final String TAG_RECORDING_TIMER = "RecordingTimer";
    private static final String TAG_TAGGED_ENTITY = "TaggedEntityUUID";

    public static final int RECORDING_DURATION = 6000;
    public static final int DETECTION_RANGE = 6;

    private final List<UUID> savedEntityUUIDs = new ArrayList<>();
    private int maxCapacity;

    private boolean recording = false;
    private int recordingTimer = 0;
    @Nullable private UUID taggedEntityUUID = null;

    public SoulMonumentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOUL_MONUMENT_ENTITY.get(), pos, state);
        Block block = state.getBlock();
        if (block instanceof SoulMonumentBlock smb) {
            this.maxCapacity = smb.getCapacity();
        } else {
            this.maxCapacity = 0;
        }
    }

    public int getCapacity() { return maxCapacity; }

    public int getUsedSlots() { return savedEntityUUIDs.size(); }

    public int getRemainingSlots() { return maxCapacity - savedEntityUUIDs.size(); }

    public boolean isFull() { return savedEntityUUIDs.size() >= maxCapacity; }

    public boolean isRecording() { return recording; }

    @Nullable
    public UUID getTaggedEntityUUID() { return taggedEntityUUID; }

    public List<UUID> getSavedEntityUUIDs() { return new ArrayList<>(savedEntityUUIDs); }

    public boolean tryStartRecording(Player player, UUID targetUUID) {
        if (recording || isFull()) return false;
        recording = true;
        recordingTimer = RECORDING_DURATION;
        taggedEntityUUID = targetUUID;
        markUpdated();
        return true;
    }

    public void cancelRecording() {
        recording = false;
        recordingTimer = 0;
        taggedEntityUUID = null;
        markUpdated();
    }

    public int getTier() {
        Block block = getBlockState().getBlock();
        if (block instanceof Tier3SoulMonumentBlock) return 3;
        if (block instanceof Tier2SoulMonumentBlock) return 2;
        return 1;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SoulMonumentBlockEntity be) {
        if (level.isClientSide) return;

        if (be.recording) {
            be.recordingTimer--;
            if (be.recordingTimer <= 0) {
                be.cancelRecording();
            }
        }
    }

    public void onVillagerDeath(ServerLevel level, BlockPos villagerPos) {
        if (!recording || taggedEntityUUID == null) return;

        LivingEntity target = findTaggedEntity(level);
        if (target == null) return;

        if (!target.isAlive()) return;

        AABB detectionBox = new AABB(worldPosition).inflate(DETECTION_RANGE);
        if (!detectionBox.contains(target.position())) return;

        CompoundTag entityData = new CompoundTag();
        target.save(entityData);
        entityData.putUUID("MonumentRecordedUUID", UUID.randomUUID());

        SoulMonumentDataManager.saveEntityData(level, taggedEntityUUID, entityData);
        savedEntityUUIDs.add(taggedEntityUUID);

        target.getPersistentData().putBoolean("goetymech.recorded", true);
        target.discard();

        cancelRecording();
    }

    @Nullable
    private LivingEntity findTaggedEntity(ServerLevel level) {
        if (taggedEntityUUID == null) return null;
        AABB box = new AABB(worldPosition).inflate(DETECTION_RANGE);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.getUUID().equals(taggedEntityUUID)) {
                return entity;
            }
        }
        return null;
    }

    public void loadFromData(ServerLevel level) {
        savedEntityUUIDs.clear();
        for (UUID uuid : savedEntityUUIDs) {
            CompoundTag data = SoulMonumentDataManager.loadEntityData(level, uuid);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("MaxCapacity", maxCapacity);
        tag.putBoolean(TAG_RECORDING, recording);
        tag.putInt(TAG_RECORDING_TIMER, recordingTimer);
        if (taggedEntityUUID != null) {
            tag.putUUID(TAG_TAGGED_ENTITY, taggedEntityUUID);
        }
        ListTag uuidList = new ListTag();
        for (UUID uuid : savedEntityUUIDs) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("UUID", uuid);
            uuidList.add(entry);
        }
        tag.put(TAG_SAVED_UUIDS, uuidList);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        maxCapacity = tag.getInt("MaxCapacity");
        recording = tag.getBoolean(TAG_RECORDING);
        recordingTimer = tag.getInt(TAG_RECORDING_TIMER);
        taggedEntityUUID = tag.hasUUID(TAG_TAGGED_ENTITY) ? tag.getUUID(TAG_TAGGED_ENTITY) : null;
        savedEntityUUIDs.clear();
        if (tag.contains(TAG_SAVED_UUIDS)) {
            ListTag uuidList = tag.getList(TAG_SAVED_UUIDS, Tag.TAG_COMPOUND);
            for (int i = 0; i < uuidList.size(); i++) {
                CompoundTag entry = uuidList.getCompound(i);
                if (entry.hasUUID("UUID")) {
                    savedEntityUUIDs.add(entry.getUUID("UUID"));
                }
            }
        }
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
