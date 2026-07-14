package com.goetymech.addon.common.block.entity;

import com.goetymech.addon.common.block.SoulMonumentBlock;
import com.goetymech.addon.common.block.Tier1SoulMonumentBlock;
import com.goetymech.addon.common.block.Tier2SoulMonumentBlock;
import com.goetymech.addon.common.block.Tier3SoulMonumentBlock;
import com.goetymech.addon.common.data.SoulMonumentDataManager;
import com.goetymech.addon.common.item.SquadFocus;
import com.goetymech.addon.init.ModBlocks;
import com.Polarice3.Goety.utils.MiscCapHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
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
    private static final String TAG_OWNER_UUID = "OwnerUUID";

    public static final int RECORDING_DURATION = 6000;
    public static final int DETECTION_RANGE = 10;

    private final List<UUID> savedEntityUUIDs = new ArrayList<>();
    private final List<String> savedEntityNames = new ArrayList<>();
    private int maxCapacity;
    @Nullable private UUID ownerUUID;

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

    public int getTier() {
        Block block = getBlockState().getBlock();
        if (block instanceof Tier3SoulMonumentBlock) return 3;
        if (block instanceof Tier2SoulMonumentBlock) return 2;
        return 1;
    }

    public int getUsedSlots() { return savedEntityUUIDs.size(); }

    public int getRemainingSlots() { return maxCapacity - savedEntityUUIDs.size(); }

    public boolean isFull() { return savedEntityUUIDs.size() >= maxCapacity; }

    public void setOwner(UUID uuid) { this.ownerUUID = uuid; }

    public boolean isOwner(Player player) {
        return ownerUUID != null && player.getUUID().equals(ownerUUID);
    }

    public boolean isRecording() { return recording; }

    @Nullable
    public UUID getTaggedEntityUUID() { return taggedEntityUUID; }

    public List<UUID> getSavedEntityUUIDs() { return new ArrayList<>(savedEntityUUIDs); }

    public List<String> getSavedEntityNames() { return new ArrayList<>(savedEntityNames); }

    public boolean tryStartRecording(Player player, UUID targetUUID) {
        if (recording || isFull()) return false;
        recording = true;
        recordingTimer = RECORDING_DURATION;
        taggedEntityUUID = targetUUID;
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.5F, 0.8F);
        }
        markUpdated();
        return true;
    }

    public void cancelRecording() {
        recording = false;
        recordingTimer = 0;
        taggedEntityUUID = null;
        markUpdated();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SoulMonumentBlockEntity be) {
        if (level.isClientSide) {
            if (be.recording) {
                level.addParticle(ParticleTypes.SMOKE,
                        pos.getX() + level.random.nextDouble(),
                        pos.getY() + level.random.nextDouble() * 2,
                        pos.getZ() + level.random.nextDouble(),
                        0, 0.05, 0);
                level.addParticle(ParticleTypes.SOUL,
                        pos.getX() + level.random.nextDouble(),
                        pos.getY() + level.random.nextDouble() * 2,
                        pos.getZ() + level.random.nextDouble(),
                        0, 0.05, 0);

                if (level.getGameTime() % 5 == 0 && be.taggedEntityUUID != null) {
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos).inflate(DETECTION_RANGE))) {
                        if (e.getUUID().equals(be.taggedEntityUUID)) {
                            for (int i = 0; i < 5; i++) {
                                level.addParticle(ParticleTypes.ENCHANT,
                                        e.getRandomX(1.0D), e.getRandomY() + 0.5D, e.getRandomZ(1.0D),
                                        level.random.nextGaussian() * 0.02D,
                                        level.random.nextGaussian() * 0.02D,
                                        level.random.nextGaussian() * 0.02D);
                        }
                    }
                }
            }
        }
            return;
        }

        if (be.recording) {
            be.recordingTimer--;
            if (be.recordingTimer > 0 && be.taggedEntityUUID != null) {
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(pos).inflate(DETECTION_RANGE))) {
                    if (e.getUUID().equals(be.taggedEntityUUID)) {
                        if (level.getGameTime() % 20 == 0) {
                            MiscCapHelper.setShakeTime(e, 20);
                        }
                        break;
                    }
                }
            }
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
        savedEntityNames.add(target.getDisplayName().getString());

        target.getPersistentData().putBoolean("goetymech.recorded", true);
        target.discard();

        SquadFocus.refreshMonumentFoci(level, worldPosition, this);
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

    public void setSavedEntityUUIDs(List<UUID> uuids) {
        savedEntityUUIDs.clear();
        savedEntityUUIDs.addAll(uuids);
        markUpdated();
    }

    public void clearAllData() {
        savedEntityUUIDs.clear();
        savedEntityNames.clear();
        markUpdated();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("MaxCapacity", maxCapacity);
        if (ownerUUID != null) tag.putUUID(TAG_OWNER_UUID, ownerUUID);
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
        ListTag nameList = new ListTag();
        for (String name : savedEntityNames) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Name", name);
            nameList.add(entry);
        }
        tag.put("SavedEntityNames", nameList);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        maxCapacity = tag.getInt("MaxCapacity");
        ownerUUID = tag.hasUUID(TAG_OWNER_UUID) ? tag.getUUID(TAG_OWNER_UUID) : null;
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
        savedEntityNames.clear();
        if (tag.contains("SavedEntityNames")) {
            ListTag nameList = tag.getList("SavedEntityNames", Tag.TAG_COMPOUND);
            for (int i = 0; i < nameList.size(); i++) {
                savedEntityNames.add(nameList.getCompound(i).getString("Name"));
            }
        }
        if (savedEntityNames.size() < savedEntityUUIDs.size() && level instanceof ServerLevel serverLevel) {
            savedEntityNames.clear();
            for (UUID uuid : savedEntityUUIDs) {
                CompoundTag data = SoulMonumentDataManager.loadEntityData(serverLevel, uuid);
                if (data != null && data.contains("id")) {
                    String id = data.getString("id");
                    ResourceLocation rl = ResourceLocation.tryParse(id);
                    if (rl != null) {
                        EntityType<?> type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(rl);
                        savedEntityNames.add(type != null ? type.getDescription().getString() : id);
                    } else {
                        savedEntityNames.add(id);
                    }
                } else {
                    savedEntityNames.add("Unknown");
                }
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
