package com.goetymech.addon.common.utils;

import com.goetymech.addon.common.block.entity.RemoteEngineBlockEntity;
import com.goetymech.addon.common.block.entity.StressReaderBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;

public class StressLinkService extends SavedData {

    private static final String DATA_NAME = "goetymech_stresslink";

    // === Persisted ===
    private final Map<GlobalBlockPos, GlobalBlockPos> engineToReader = new LinkedHashMap<>();
    private transient final Map<GlobalBlockPos, GlobalBlockPos> readerToEngine = new HashMap<>();
    private final Map<GlobalBlockPos, UUID> readerOwners = new HashMap<>();
    private transient final Map<UUID, Set<GlobalBlockPos>> ownerReaders = new HashMap<>();

    // === Runtime-only ===
    private transient final Map<GlobalBlockPos, StressReaderBlockEntity> activeReaders = new HashMap<>();
    private transient final Map<GlobalBlockPos, RemoteEngineBlockEntity> activeEngines = new HashMap<>();
    private transient final Map<GlobalBlockPos, UUID> engineRegistry = new HashMap<>();
    private transient final Map<UUID, Set<GlobalBlockPos>> playerEngines = new HashMap<>();

    // P2: actual consumption reported by engines via tick()
    private final Map<GlobalBlockPos, Float> engineConsumption = new HashMap<>();
    private transient final Map<UUID, Float> playerEngineConsumption = new HashMap<>();

    private transient final Map<GlobalBlockPos, ReaderSnapshot> readerCache = new HashMap<>();

    private transient final Map<GlobalBlockPos, UUID> arkRegistry = new HashMap<>();
    private transient final Map<UUID, Set<GlobalBlockPos>> ownerArks = new HashMap<>();

    public record ReaderSnapshot(float capacity, float stress, float speed) {
        public float getAvailable() { return Math.max(0, capacity - stress); }
    }

    public StressLinkService() { super(); }

    // ── Static accessor ──

    @Nullable
    public static StressLinkService get(@Nullable Level level) {
        if (level == null || level.isClientSide) return null;
        return get(((ServerLevel) level).getServer());
    }

    public static StressLinkService get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        return overworld.getDataStorage().computeIfAbsent(
                StressLinkService::loadStatic, StressLinkService::new, DATA_NAME);
    }

    private static StressLinkService loadStatic(CompoundTag tag) {
        return new StressLinkService().load(tag);
    }

    // ── Reader lifecycle ──

    public void onReaderLoad(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());
        activeReaders.put(key, reader);
        readerCache.put(key, new ReaderSnapshot(
                reader.getNetworkCapacity(),
                reader.getNetworkStress(),
                reader.getSpeed()));
        rebuildOwnerIndex();

        GlobalBlockPos enginePos = readerToEngine.get(key);
        if (enginePos != null) pushToEngine(enginePos);

        UUID owner = readerOwners.get(key);
        if (owner != null) pushToPlayerEngines(owner);
    }

    public void onReaderUnload(GlobalBlockPos readerPos) {
        activeReaders.remove(readerPos);
        // P5: push zero to bound engine
        GlobalBlockPos enginePos = readerToEngine.get(readerPos);
        if (enginePos != null) {
            RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
            if (engine != null) engine.applyRemoteUpdate(0, 0, 0);
        }
        // P5: recalc auto-pool for this owner
        UUID owner = readerOwners.get(readerPos);
        if (owner != null) pushToPlayerEngines(owner);
    }

    public void onReaderDestroyed(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());
        activeReaders.remove(key);
        readerCache.remove(key);
        UUID owner = readerOwners.remove(key);
        rebuildOwnerIndex();

        GlobalBlockPos enginePos = readerToEngine.remove(key);
        if (enginePos != null) {
            engineToReader.remove(enginePos);
            RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
            if (engine != null) engine.applyRemoteUpdate(0, 0, 0);
            setDirty();
        }

        // P5: recalc auto-pool
        if (owner != null) pushToPlayerEngines(owner);
    }

    // ── Engine lifecycle ──

    public void onEngineLoad(RemoteEngineBlockEntity engine) {
        if (engine.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(engine.getLevel(), engine.getBlockPos());
        activeEngines.put(key, engine);

        GlobalBlockPos readerPos = engineToReader.get(key);
        if (readerPos != null) pushToEngine(key);
    }

    public void onEngineUnload(GlobalBlockPos enginePos) {
        activeEngines.remove(enginePos);
        engineConsumption.remove(enginePos);
        UUID owner = engineRegistry.remove(enginePos);
        if (owner != null) {
            Set<GlobalBlockPos> set = playerEngines.get(owner);
            if (set != null) {
                set.remove(enginePos);
                if (set.isEmpty()) playerEngines.remove(owner);
            }
            // P5: recalc auto-pool for remaining engines
            pushToPlayerEngines(owner);
        }
    }

    // ── Binding management ──

    public void bind(GlobalBlockPos enginePos, GlobalBlockPos readerPos) {
        GlobalBlockPos oldReader = engineToReader.get(enginePos);
        if (oldReader != null) readerToEngine.remove(oldReader);

        GlobalBlockPos oldEngine = readerToEngine.get(readerPos);
        if (oldEngine != null && !oldEngine.equals(enginePos)) {
            engineToReader.remove(oldEngine);
            RemoteEngineBlockEntity stale = activeEngines.get(oldEngine);
            if (stale != null) stale.applyRemoteUpdate(0, 0, 0);
        }

        engineToReader.put(enginePos, readerPos);
        readerToEngine.put(readerPos, enginePos);
        setDirty();
        pushToEngine(enginePos);
    }

    public void unbind(GlobalBlockPos enginePos) {
        GlobalBlockPos readerPos = engineToReader.remove(enginePos);
        if (readerPos != null) readerToEngine.remove(readerPos);
        setDirty();

        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (engine != null) engine.applyRemoteUpdate(0, 0, 0);
    }

    @Nullable
    public GlobalBlockPos getReaderForEngine(GlobalBlockPos enginePos) {
        return engineToReader.get(enginePos);
    }

    // ── Event-driven update + cache ──

    public void onReaderNetworkChanged(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos readerKey = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());

        readerCache.put(readerKey, new ReaderSnapshot(
                reader.getNetworkCapacity(),
                reader.getNetworkStress(),
                reader.getSpeed()));

        // Push to bound engine (1:1)
        GlobalBlockPos enginePos = readerToEngine.get(readerKey);
        if (enginePos != null) pushToEngine(enginePos);

        // Push to all player engines (auto-pool)
        UUID owner = readerOwners.get(readerKey);
        if (owner != null) pushToPlayerEngines(owner);
    }

    @Nullable
    public ReaderSnapshot getCachedReaderData(GlobalBlockPos readerPos) {
        return readerCache.get(readerPos);
    }

    private void pushToEngine(GlobalBlockPos enginePos) {
        GlobalBlockPos readerPos = engineToReader.get(enginePos);
        if (readerPos == null) return;

        StressReaderBlockEntity reader = activeReaders.get(readerPos);
        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (reader == null || engine == null) return;

        float speed = reader.getSpeed();
        float capacity = reader.getNetworkCapacity();
        float budget = Math.max(0, capacity - reader.getNetworkStress());
        engine.applyRemoteUpdate(speed, budget, capacity);
    }

    // ── Player engine registry (auto-pool) ──

    public void registerPlayerEngine(GlobalBlockPos enginePos, @Nullable UUID ownerUUID) {
        if (ownerUUID == null) return;
        UUID prev = engineRegistry.put(enginePos, ownerUUID);
        if (!ownerUUID.equals(prev)) {
            if (prev != null) {
                Set<GlobalBlockPos> s = playerEngines.get(prev);
                if (s != null) s.remove(enginePos);
            }
            playerEngines.computeIfAbsent(ownerUUID, k -> new HashSet<>()).add(enginePos);
        }
        pushToPlayerEngines(ownerUUID);
    }

    public void pushToPlayerEngines(UUID ownerUUID) {
        Set<GlobalBlockPos> engines = playerEngines.get(ownerUUID);
        if (engines == null || engines.isEmpty()) return;

        // P0: count only active + unbound engines
        List<GlobalBlockPos> activeUnbound = new ArrayList<>();
        for (GlobalBlockPos ep : engines) {
            if (!activeEngines.containsKey(ep)) continue;
            if (engineToReader.containsKey(ep)) continue;
            activeUnbound.add(ep);
        }
        if (activeUnbound.isEmpty()) return;

        // Sum all readers' available stress (cache → fallback to live BE)
        float totalAvailable = 0;
        float totalCapacity = 0;
        for (GlobalBlockPos rp : getReadersForOwner(ownerUUID)) {
            ReaderSnapshot snap = readerCache.get(rp);
            if (snap != null) {
                totalCapacity += snap.capacity();
                totalAvailable += snap.getAvailable();
            } else {
                StressReaderBlockEntity reader = activeReaders.get(rp);
                if (reader != null) {
                    float cap = reader.getNetworkCapacity();
                    float str = reader.getNetworkStress();
                    totalCapacity += cap;
                    totalAvailable += Math.max(0, cap - str);
                    readerCache.put(rp, new ReaderSnapshot(cap, str, reader.getSpeed()));
                }
            }
        }

        // P0: equal split
        float budgetPerEngine = totalAvailable / activeUnbound.size();

        for (GlobalBlockPos ep : activeUnbound) {
            RemoteEngineBlockEntity engine = activeEngines.get(ep);
            if (engine != null) {
                engine.applyRemoteUpdate(16, budgetPerEngine, totalCapacity);
            }
        }
    }

    // ── Engine consumption tracking (P2) ──

    public void reportEngineConsumption(GlobalBlockPos enginePos, float actualStress) {
        Float prev = engineConsumption.put(enginePos, actualStress);
        UUID owner = engineRegistry.get(enginePos);
        if (owner != null) {
            float total = 0;
            for (var entry : engineConsumption.entrySet()) {
                if (owner.equals(engineRegistry.get(entry.getKey()))) {
                    total += entry.getValue();
                }
            }
            playerEngineConsumption.put(owner, total);
        }
    }

    public float getPlayerEngineConsumption(UUID ownerUUID) {
        return playerEngineConsumption.getOrDefault(ownerUUID, 0f);
    }

    public float getTotalSystemStress(UUID ownerUUID) {
        float readerStress = 0;
        for (GlobalBlockPos readerPos : getReadersForOwner(ownerUUID)) {
            ReaderSnapshot snap = readerCache.get(readerPos);
            if (snap != null) readerStress += snap.stress();
        }
        return readerStress + getPlayerEngineConsumption(ownerUUID);
    }

    // ── Reader ownership ──

    public void registerReaderOwner(GlobalBlockPos readerPos, UUID ownerUUID) {
        readerOwners.put(readerPos, ownerUUID);
        setDirty();
        rebuildOwnerIndex();
    }

    public void removeReaderOwner(GlobalBlockPos readerPos) {
        readerOwners.remove(readerPos);
        setDirty();
        rebuildOwnerIndex();
    }

    public List<GlobalBlockPos> getReadersForOwner(UUID ownerUUID) {
        Set<GlobalBlockPos> set = ownerReaders.get(ownerUUID);
        return set != null ? new ArrayList<>(set) : List.of();
    }

    @Nullable
    public UUID getReaderOwner(GlobalBlockPos readerPos) {
        return readerOwners.get(readerPos);
    }

    private void rebuildOwnerIndex() {
        ownerReaders.clear();
        for (var entry : readerOwners.entrySet())
            ownerReaders.computeIfAbsent(entry.getValue(), k -> new HashSet<>()).add(entry.getKey());
    }

    // ── Ark registry ──

    public void registerArk(GlobalBlockPos arkPos, UUID ownerUUID) {
        arkRegistry.put(arkPos, ownerUUID);
        ownerArks.computeIfAbsent(ownerUUID, k -> new LinkedHashSet<>()).add(arkPos);
    }

    public void unregisterArk(GlobalBlockPos arkPos) {
        UUID owner = arkRegistry.remove(arkPos);
        if (owner != null) {
            Set<GlobalBlockPos> arks = ownerArks.get(owner);
            if (arks != null) {
                arks.remove(arkPos);
                if (arks.isEmpty()) ownerArks.remove(owner);
            }
        }
    }

    public List<GlobalBlockPos> getArksForOwner(UUID ownerUUID) {
        Set<GlobalBlockPos> arks = ownerArks.get(ownerUUID);
        return arks != null ? new ArrayList<>(arks) : List.of();
    }

    public int getArkCountForOwner(UUID ownerUUID) {
        Set<GlobalBlockPos> arks = ownerArks.get(ownerUUID);
        return arks != null ? arks.size() : 0;
    }

    // ── SavedData ──

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag bindingsList = new ListTag();
        for (var entry : engineToReader.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.put("engine", entry.getKey().save());
            e.put("reader", entry.getValue().save());
            bindingsList.add(e);
        }
        tag.put("bindings", bindingsList);

        ListTag ownersList = new ListTag();
        for (var entry : readerOwners.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.put("pos", entry.getKey().save());
            e.putUUID("owner", entry.getValue());
            ownersList.add(e);
        }
        tag.put("readerOwners", ownersList);

        return tag;
    }

    public StressLinkService load(CompoundTag tag) {
        engineToReader.clear();
        readerToEngine.clear();
        ListTag bindingsList = tag.getList("bindings", Tag.TAG_COMPOUND);
        for (int i = 0; i < bindingsList.size(); i++) {
            CompoundTag e = bindingsList.getCompound(i);
            GlobalBlockPos enginePos = GlobalBlockPos.load(e.getCompound("engine"));
            GlobalBlockPos readerPos = GlobalBlockPos.load(e.getCompound("reader"));
            engineToReader.put(enginePos, readerPos);
            readerToEngine.put(readerPos, enginePos);
        }

        readerOwners.clear();
        ListTag ownersList = tag.getList("readerOwners", Tag.TAG_COMPOUND);
        for (int i = 0; i < ownersList.size(); i++) {
            CompoundTag e = ownersList.getCompound(i);
            readerOwners.put(GlobalBlockPos.load(e.getCompound("pos")), e.getUUID("owner"));
        }
        rebuildOwnerIndex();

        return this;
    }
}
