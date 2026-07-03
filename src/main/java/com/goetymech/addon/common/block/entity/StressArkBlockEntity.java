package com.goetymech.addon.common.block.entity;

import com.Polarice3.Goety.api.entities.IChunkLoader;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.KineticNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class StressArkBlockEntity extends BlockEntity implements IChunkLoader {

    public static record ArkLocation(ResourceKey<Level> dimension, BlockPos pos) {}

    private static final Map<UUID, ArkLocation> PLAYER_ACTIVE_ARK = new HashMap<>();

    @Nullable
    public static BlockPos getActiveArkPos(UUID playerUUID) {
        ArkLocation loc = PLAYER_ACTIVE_ARK.get(playerUUID);
        return loc != null ? loc.pos() : null;
    }

    @Nullable
    public static ArkLocation getActiveArkLocation(UUID playerUUID) {
        return PLAYER_ACTIVE_ARK.get(playerUUID);
    }

    @Nullable private UUID ownerUUID;
    private float cachedTotalCapacity;
    private float cachedTotalStress;
    private int cachedNetworkCount;
    private int tickCounter;
    private boolean forceRefresh = true;
    private boolean hasEverSynced;
    public long ticketTime;

    private final Map<BlockPos, EngineEntry> boundEngines = new HashMap<>();

    private static class EngineEntry {
        float consumption;
        long lastSeenTick;
        EngineEntry(float consumption, long lastSeenTick) {
            this.consumption = consumption;
            this.lastSeenTick = lastSeenTick;
        }
    }

    public StressArkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STRESS_ARK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StressArkBlockEntity self) {
        if (!(level instanceof ServerLevel)) return;
        self.chunkLoadBlock();
        if (self.ownerUUID == null) return;
        ArkLocation active = PLAYER_ACTIVE_ARK.get(self.ownerUUID);
        if (active == null) {
            PLAYER_ACTIVE_ARK.put(self.ownerUUID, new ArkLocation(level.dimension(), pos));
        } else if (!active.pos().equals(pos) || !active.dimension().equals(level.dimension())) {
            return;
        }
        self.recomputeIfNeeded();
    }

    public void registerEngine(BlockPos enginePos) {
        boundEngines.putIfAbsent(enginePos, new EngineEntry(0, level != null ? level.getGameTime() : 0));
        setChanged();
    }

    public void unregisterEngine(BlockPos enginePos) {
        boundEngines.remove(enginePos);
        setChanged();
    }

    public void reportConsumption(BlockPos enginePos, float consumption) {
        long now = level != null ? level.getGameTime() : 0;
        EngineEntry entry = boundEngines.get(enginePos);
        if (entry == null) {
            boundEngines.put(enginePos, new EngineEntry(consumption, now));
        } else {
            entry.consumption = consumption;
            entry.lastSeenTick = now;
        }
    }

    public float getRepresentativeRPM() {
        float bestRpm = 0;
        float bestAvailable = -1;
        synchronized (StressReaderBlockEntity.ACTIVE_READERS) {
            for (StressReaderBlockEntity reader : StressReaderBlockEntity.ACTIVE_READERS) {
                if (reader.hasNetwork() && reader.getLevel() != null && reader.getLevel().isLoaded(reader.getBlockPos())) {
                    float available = Math.max(0, reader.getNetworkCapacity() - reader.getNetworkStress());
                    if (available > bestAvailable) {
                        bestAvailable = available;
                        bestRpm = reader.getSpeed();
                    }
                }
            }
        }
        return bestRpm;
    }

    public float getTotalAvailableStress() {
        float total = 0;
        synchronized (StressReaderBlockEntity.ACTIVE_READERS) {
            for (StressReaderBlockEntity reader : StressReaderBlockEntity.ACTIVE_READERS) {
                if (reader.hasNetwork() && reader.getLevel() != null && reader.getLevel().isLoaded(reader.getBlockPos())) {
                    total += Math.max(0, reader.getNetworkCapacity() - reader.getNetworkStress());
                }
            }
        }
        return total;
    }

    public int getEngineCount() {
        return boundEngines.size();
    }

    public float getTotalConsumedByEngines() {
        float total = 0;
        for (EngineEntry entry : boundEngines.values()) {
            total += entry.consumption;
        }
        return total;
    }

    public float getRawAvailableStress() {
        float total = 0;
        synchronized (StressReaderBlockEntity.ACTIVE_READERS) {
            for (StressReaderBlockEntity reader : StressReaderBlockEntity.ACTIVE_READERS) {
                if (reader.hasNetwork() && reader.getLevel() != null && reader.getLevel().isLoaded(reader.getBlockPos())) {
                    float selfConsumption = reader.getConsumedFor(this.worldPosition);
                    total += Math.max(0, reader.getNetworkCapacity() - reader.getNetworkStress() + selfConsumption);
                }
            }
        }
        return total;
    }

    private void recomputeIfNeeded() {
        boolean dirty = StressReaderBlockEntity.NETWORK_UPDATED || forceRefresh;
        if (++tickCounter >= 20) {
            tickCounter = 0;
            dirty = true;
        }
        if (!dirty) return;

        StressReaderBlockEntity.NETWORK_UPDATED = false;
        forceRefresh = false;

        Map<KineticNetwork, StressReaderBlockEntity> networkReaders = new LinkedHashMap<>();
        Set<KineticNetwork> seen = new HashSet<>();
        float totalCap = 0, totalStress = 0;
        int netCount = 0;

        synchronized (StressReaderBlockEntity.ACTIVE_READERS) {
            for (StressReaderBlockEntity reader : StressReaderBlockEntity.ACTIVE_READERS) {
                if (reader.hasNetwork() && reader.getLevel() != null && reader.getLevel().isLoaded(reader.getBlockPos())) {
                    KineticNetwork net = reader.getOrCreateNetwork();
                    networkReaders.putIfAbsent(net, reader);
                    if (seen.add(net)) {
                        totalCap += reader.getNetworkCapacity();
                        totalStress += reader.getNetworkStress();
                        netCount++;
                    }
                }
            }
        }

        boolean changed = Math.abs(totalCap - cachedTotalCapacity) > 0.01f
                || Math.abs(totalStress - cachedTotalStress) > 0.01f
                || netCount != cachedNetworkCount;

        if (changed || !hasEverSynced) {
            cachedTotalCapacity = totalCap;
            cachedTotalStress = totalStress;
            cachedNetworkCount = netCount;
            hasEverSynced = true;
            syncToClient();
        }

        distributeConsumption(networkReaders.values());
        purgeStaleEngines();
    }

    private void distributeConsumption(Collection<StressReaderBlockEntity> readers) {
        if (boundEngines.isEmpty() || readers.isEmpty()) return;

        float totalEngineConsumption = 0;
        for (EngineEntry entry : boundEngines.values()) {
            totalEngineConsumption += entry.consumption;
        }

        if (totalEngineConsumption <= 0) {
            for (StressReaderBlockEntity reader : readers) {
                reader.removeConsumer(this.worldPosition);
            }
            return;
        }

        int n = readers.size();
        float[] available = new float[n];
        float totalAvailable = 0;
        int idx = 0;
        for (StressReaderBlockEntity reader : readers) {
            available[idx] = Math.max(0, reader.getNetworkCapacity() - reader.getNetworkStress() + reader.getConsumedFor(this.worldPosition));
            totalAvailable += available[idx];
            idx++;
        }
        if (totalAvailable <= 0) return;

        idx = 0;
        for (StressReaderBlockEntity reader : readers) {
            float allocated = totalEngineConsumption * (available[idx] / totalAvailable);
            reader.setConsumedRemotely(this.worldPosition, Math.min(allocated, available[idx]));
            idx++;
        }
    }

    private void purgeStaleEngines() {
        if (level == null) return;
        long now = level.getGameTime();
        boundEngines.entrySet().removeIf(e -> now - e.getValue().lastSeenTick > 100);
    }

    private void syncToClient() {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        Player player = level.getPlayerByUUID(ownerUUID);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CStressArkDataPacket(cachedTotalCapacity, cachedTotalStress, cachedNetworkCount, getTotalConsumedByEngines()));
        }
    }

    private void sendClearPacket() {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        Player player = level.getPlayerByUUID(ownerUUID);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CStressArkDataPacket(-1, -1, 0, 0));
        }
    }

    public void setOwner(LivingEntity entity) {
        this.ownerUUID = entity.getUUID();
        if (level != null) {
            PLAYER_ACTIVE_ARK.put(this.ownerUUID, new ArkLocation(level.dimension(), worldPosition));
        }
        setChanged();
        forceRefresh = true;
    }

    public boolean isOwner(Player player) {
        return ownerUUID != null && player.getUUID().equals(ownerUUID);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public float getCachedTotalCapacity() { return cachedTotalCapacity; }
    public float getCachedTotalStress() { return cachedTotalStress; }
    public int getCachedNetworkCount() { return cachedNetworkCount; }

    public void onArkRemoved() {
        sendClearPacket();
        PLAYER_ACTIVE_ARK.remove(ownerUUID);
        // Remove Ark's entries from all readers
        synchronized (StressReaderBlockEntity.ACTIVE_READERS) {
            for (StressReaderBlockEntity reader : StressReaderBlockEntity.ACTIVE_READERS) {
                if (reader.getLevel() != null && reader.getLevel().isLoaded(reader.getBlockPos())) {
                    reader.removeConsumer(this.worldPosition);
                }
            }
        }
        ownerUUID = null;
    }

    @Override
    public boolean shouldChunkLoad() {
        return true;
    }

    @Override
    public long getTicketTime() {
        return this.ticketTime;
    }

    @Override
    public void setTicketTime(long ticketTime) {
        this.ticketTime = ticketTime;
    }

    @Override
    public int selfLoadRadius() {
        return 5;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        ListTag enginesList = new ListTag();
        for (var entry : boundEngines.entrySet()) {
            CompoundTag eTag = new CompoundTag();
            eTag.putLong("Pos", entry.getKey().asLong());
            eTag.putFloat("Consumption", entry.getValue().consumption);
            eTag.putLong("LastSeen", entry.getValue().lastSeenTick);
            enginesList.add(eTag);
        }
        tag.put("BoundEngines", enginesList);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
            if (level != null && !level.isClientSide) {
                PLAYER_ACTIVE_ARK.putIfAbsent(ownerUUID, new ArkLocation(level.dimension(), worldPosition));
            }
        }
        boundEngines.clear();
        if (tag.contains("BoundEngines")) {
            long now = level != null ? level.getGameTime() : 0;
            ListTag list = tag.getList("BoundEngines", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag eTag = list.getCompound(i);
                BlockPos pos = BlockPos.of(eTag.getLong("Pos"));
                float consumption = eTag.getFloat("Consumption");
                boundEngines.put(pos, new EngineEntry(consumption, now));
            }
        }
        forceRefresh = true;
    }
}
