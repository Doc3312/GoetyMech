package com.goetymech.addon.common.utils;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.block.entity.RemoteEngineBlockEntity;
import com.goetymech.addon.common.block.entity.StressReaderBlockEntity;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.foundation.utility.SavedDataUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.*;

public class GlobalStressLinkManager {

    // === Persistent data ===
    public Map<GlobalBlockPos, GlobalBlockPos> engineToReader = new LinkedHashMap<>();
    public Map<GlobalBlockPos, Set<GlobalBlockPos>> readerToEngine = new HashMap<>();
    public Map<GlobalBlockPos, UUID> readerOwners = new HashMap<>();
    public Map<UUID, Set<GlobalBlockPos>> ownerReaders = new HashMap<>();

    public Map<GlobalBlockPos, StressReaderBlockEntity> activeReaders = new HashMap<>();
    public Map<GlobalBlockPos, RemoteEngineBlockEntity> activeEngines = new HashMap<>();
    public Map<GlobalBlockPos, ReaderSnapshot> readerCache = new HashMap<>();
    public Map<GlobalBlockPos, UUID> arkRegistry = new HashMap<>();
    public Map<UUID, Set<GlobalBlockPos>> ownerArks = new HashMap<>();
    public Map<GlobalBlockPos, Float> engineLocalStress = new HashMap<>();
    public Map<GlobalBlockPos, Float> engineLocalCapacity = new HashMap<>();
    public Map<GlobalBlockPos, Float> engineBudget = new HashMap<>();
    public Map<GlobalBlockPos, Object> engineNetworkKey = new HashMap<>();

    public boolean suppressEngineUpdates = false;
    public Set<GlobalBlockPos> dirtyReaders = new HashSet<>();
    public Set<GlobalBlockPos> overstrainedEngines = new HashSet<>();
    private StressLinkSavedData savedData;

    public record ReaderSnapshot(float capacity, float stress, float speed) {
        public float getAvailable() { return Math.max(0, capacity - stress); }
    }

    public record AggregatedReaderData(float totalCapacity, float totalStress, int uniqueNetworkCount) {}    

    private boolean loading = false;
    private boolean shuttingDown = false;

    public void levelLoaded(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null || server.overworld() != level) return;
        this.server = server;
        loading = true;
        shuttingDown = false;
        cleanUp();
        savedData = null;
        loadStressLinkData(server);
        loading = false;
    }

    public void playerLogin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            loadStressLinkData(server);
        }
    }

    private void loadStressLinkData(MinecraftServer server) {
        if (savedData != null) return;
        savedData = StressLinkSavedData.load(server);
        engineToReader = savedData.getEngineToReader();
        readerToEngine = savedData.getReaderToEngine();
        readerOwners = savedData.getReaderOwners();
        rebuildOwnerIndex();

        for (GlobalBlockPos enginePos : engineToReader.keySet()) {
            forceLoadEngineChunk(null, enginePos);
        }
    }

    public void markDirty() {
        if (shuttingDown) return;
        ensureSavedData();
        if (savedData != null) {
            savedData.setDirty();
        }
    }

    private void ensureSavedData() {
        if (savedData == null && server != null) {
            savedData = StressLinkSavedData.load(server);
            engineToReader = savedData.getEngineToReader();
            readerToEngine = savedData.getReaderToEngine();
            readerOwners = savedData.getReaderOwners();
            rebuildOwnerIndex();
        }
    }

    public void cleanUp() {
        engineToReader.clear();
        readerToEngine.clear();
        readerOwners.clear();
        ownerReaders.clear();
        activeReaders.clear();
        activeEngines.clear();
        readerCache.clear();
        arkRegistry.clear();
        ownerArks.clear();
        engineLocalStress.clear();
        engineLocalCapacity.clear();
        engineBudget.clear();
        engineNetworkKey.clear();
        overstrainedEngines.clear();
        dirtyReaders.clear();
    }

    // ── Reader lifecycle ──

    public void onReaderLoad(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());
        activeReaders.put(key, reader);
        rebuildOwnerIndex();

        Set<GlobalBlockPos> engines = readerToEngine.get(key);
        if (engines != null && !engines.isEmpty()) {
            dirtyReaders.add(key);
        }
    }

    public void onReaderUnload(GlobalBlockPos readerPos) {
        activeReaders.remove(readerPos);
        Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
        if (engines != null) {
            for (GlobalBlockPos ep : engines) {
                RemoteEngineBlockEntity engine = activeEngines.get(ep);
                if (engine != null) engine.applyRemoteUpdate(0, 0, 0, 0);
            }
        }
    }

    public void onReaderDestroyed(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());
        activeReaders.remove(key);
        readerCache.remove(key);
        readerOwners.remove(key);
        rebuildOwnerIndex();

        Set<GlobalBlockPos> engines = readerToEngine.remove(key);
        if (engines != null) {
            for (GlobalBlockPos ep : engines) {
                engineToReader.remove(ep);
                overstrainedEngines.remove(ep);
                unforceLoadEngineChunk(activeEngines.get(ep), ep);
                RemoteEngineBlockEntity engine = activeEngines.get(ep);
                if (engine != null) engine.applyRemoteUpdate(0, 0, 0, 0);
            }
            markDirty();
        }
    }

    // ── Engine lifecycle ──

    public void onEngineLoad(RemoteEngineBlockEntity engine) {
        if (engine.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(engine.getLevel(), engine.getBlockPos());
        activeEngines.put(key, engine);

        GlobalBlockPos readerPos = engineToReader.get(key);
        if (readerPos != null) {
            forceLoadEngineChunk(engine, key);
            dirtyReaders.add(readerPos);
        }
    }

    public void onEngineUnload(GlobalBlockPos enginePos) {
        activeEngines.remove(enginePos);
        overstrainedEngines.remove(enginePos);
        engineLocalStress.remove(enginePos);
        engineLocalCapacity.remove(enginePos);
        engineBudget.remove(enginePos);
        engineNetworkKey.remove(enginePos);
        unforceLoadEngineChunk(null, enginePos);
    }

    // ── Binding management ──

    public void bind(GlobalBlockPos enginePos, GlobalBlockPos readerPos) {
        GlobalBlockPos oldReader = engineToReader.get(enginePos);
        if (oldReader != null) {
            Set<GlobalBlockPos> engines = readerToEngine.get(oldReader);
            if (engines != null) engines.remove(enginePos);
            unforceLoadEngineChunk(null, enginePos);
        }

        engineToReader.put(enginePos, readerPos);
        readerToEngine.computeIfAbsent(readerPos, k -> new LinkedHashSet<>()).add(enginePos);
        markDirty();
        forceLoadEngineChunk(activeEngines.get(enginePos), enginePos);
        dirtyReaders.add(readerPos);
    }

    public void onServerStopping() {
        shuttingDown = true;
    }

    public void unbind(GlobalBlockPos enginePos) {
        if (loading) return;
        GlobalBlockPos readerPos = engineToReader.remove(enginePos);
        if (readerPos != null) {
            Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
            if (engines != null) {
                engines.remove(enginePos);
                if (engines.isEmpty()) readerToEngine.remove(readerPos);
            }
            dirtyReaders.add(readerPos);
        }
        unforceLoadEngineChunk(null, enginePos);
        overstrainedEngines.remove(enginePos);
        engineLocalStress.remove(enginePos);
        engineLocalCapacity.remove(enginePos);
        engineBudget.remove(enginePos);
        engineNetworkKey.remove(enginePos);
        if (!shuttingDown) {
            markDirty();
        }

        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (engine != null) engine.applyRemoteUpdate(0, 0, 0, 0);
    }

    @Nullable
    public GlobalBlockPos getReaderForEngine(GlobalBlockPos enginePos) {
        return engineToReader.get(enginePos);
    }


    public void onReaderNetworkChanged(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos readerKey = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());

        readerCache.put(readerKey, new ReaderSnapshot(
                reader.getNetworkCapacity(),
                reader.getNetworkStress(),
                reader.getSpeed()));

        dirtyReaders.add(readerKey);
    }

    @Nullable
    public ReaderSnapshot getCachedReaderData(GlobalBlockPos readerPos) {
        return readerCache.get(readerPos);
    }


    public void tick() {
        if (dirtyReaders.isEmpty()) return;
        Set<GlobalBlockPos> snapshot = new HashSet<>(dirtyReaders);
        dirtyReaders.clear();

        Set<KineticNetwork> processedNetworks = new HashSet<>();

        for (GlobalBlockPos readerPos : snapshot) {
            StressReaderBlockEntity reader = activeReaders.get(readerPos);
            if (reader == null) continue;

            if (reader.hasNetwork()) {
                KineticNetwork network = reader.getOrCreateNetwork();
                if (processedNetworks.add(network)) {
                    distributeForNetwork(network);
                }
            } else {
                distributeBudget(readerPos);
            }
        }
    }

    private void distributeBudget(GlobalBlockPos readerPos) {
        StressReaderBlockEntity reader = activeReaders.get(readerPos);
        if (reader == null) return;

        Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
        if (engines == null || engines.isEmpty()) return;

        float speed = reader.getSpeed();
        float capacity = reader.getNetworkCapacity();
        float readerStress = reader.getNetworkStress();
        float remoteConsumption = getEngineConsumptionForReader(readerPos);

        float localStress = Math.max(0, readerStress - remoteConsumption);
        float totalBudget = Math.max(0, capacity - localStress);

        Map<Object, List<GlobalBlockPos>> networkToEngines = new LinkedHashMap<>();
        for (GlobalBlockPos ep : engines) {
            if (overstrainedEngines.contains(ep)) continue;
            Object networkKey = engineNetworkKey.getOrDefault(ep, ep);
            networkToEngines.computeIfAbsent(networkKey, k -> new ArrayList<>()).add(ep);
        }

        float allocated = 0;

        suppressEngineUpdates = true;
        try {
            for (var entry : networkToEngines.entrySet()) {
                Object networkKey = entry.getKey();
                List<GlobalBlockPos> engineList = entry.getValue();

                float networkConsumption = 0;
                if (!engineList.isEmpty()) {
                    networkConsumption = engineLocalStress.getOrDefault(engineList.get(0), 0f);
                }

                float availableBudget = Math.max(0, totalBudget - allocated);

                if (availableBudget >= networkConsumption) {
                    float perEngineBudget = availableBudget / engineList.size();
                    for (GlobalBlockPos enginePos : engineList) {
                        overstrainedEngines.remove(enginePos);
                        engineBudget.put(enginePos, availableBudget);
                        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
                        if (engine != null) {
                            pushToEngineInternal(engine, speed, perEngineBudget, capacity, readerStress);
                        }
                    }
                    allocated += networkConsumption;
                } else {
                    for (GlobalBlockPos enginePos : engineList) {
                        overstrainedEngines.add(enginePos);
                        engineBudget.put(enginePos, 0f);
                        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
                        if (engine != null) {
                            pushToEngineInternal(engine, 0, 0, capacity, readerStress);
                        }
                    }
                }
            }
        } finally {
            suppressEngineUpdates = false;
        }

        float finalConsumption = getEngineConsumptionForReader(readerPos);
        reader.setRemoteEngineConsumption(finalConsumption);
    }

    private void distributeForNetwork(KineticNetwork network) {
        Set<GlobalBlockPos> networkReaders = new LinkedHashSet<>();
        for (var entry : activeReaders.entrySet()) {
            StressReaderBlockEntity r = entry.getValue();
            if (r.hasNetwork() && r.getOrCreateNetwork() == network) {
                networkReaders.add(entry.getKey());
            }
        }

        if (networkReaders.isEmpty()) return;

        if (networkReaders.size() == 1) {
            distributeBudget(networkReaders.iterator().next());
            return;
        }

        GlobalBlockPos firstRp = networkReaders.iterator().next();
        StressReaderBlockEntity firstReader = activeReaders.get(firstRp);
        if (firstReader == null) return;

        float speed = firstReader.getSpeed();
        float capacity = firstReader.getNetworkCapacity();
        float totalNetworkStress = firstReader.getNetworkStress();

        float totalRemoteConsumption = 0;
        Set<GlobalBlockPos> allEngines = new LinkedHashSet<>();
        for (GlobalBlockPos rp : networkReaders) {
            Set<GlobalBlockPos> engines = readerToEngine.get(rp);
            if (engines != null) allEngines.addAll(engines);
            totalRemoteConsumption += getEngineConsumptionForReader(rp);
        }

        float localStress = Math.max(0, totalNetworkStress - totalRemoteConsumption);
        float totalBudget = Math.max(0, capacity - localStress);

        Map<Object, List<GlobalBlockPos>> networkToEngines = new LinkedHashMap<>();
        for (GlobalBlockPos ep : allEngines) {
            if (overstrainedEngines.contains(ep)) continue;
            Object networkKey = engineNetworkKey.getOrDefault(ep, ep);
            networkToEngines.computeIfAbsent(networkKey, k -> new ArrayList<>()).add(ep);
        }

        float allocated = 0;
        suppressEngineUpdates = true;
        try {
            for (var entry : networkToEngines.entrySet()) {
                List<GlobalBlockPos> engineList = entry.getValue();

                float networkConsumption = 0;
                if (!engineList.isEmpty()) {
                    networkConsumption = engineLocalStress.getOrDefault(engineList.get(0), 0f);
                }

                float availableBudget = Math.max(0, totalBudget - allocated);

                if (availableBudget >= networkConsumption) {
                    float perEngineBudget = availableBudget / engineList.size();
                    for (GlobalBlockPos enginePos : engineList) {
                        overstrainedEngines.remove(enginePos);
                        engineBudget.put(enginePos, availableBudget);
                        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
                        if (engine != null) {
                            pushToEngineInternal(engine, speed, perEngineBudget, capacity, totalNetworkStress);
                        }
                    }
                    allocated += networkConsumption;
                } else {
                    for (GlobalBlockPos enginePos : engineList) {
                        overstrainedEngines.add(enginePos);
                        engineBudget.put(enginePos, 0f);
                        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
                        if (engine != null) {
                            pushToEngineInternal(engine, 0, 0, capacity, totalNetworkStress);
                        }
                    }
                }
            }
        } finally {
            suppressEngineUpdates = false;
        }

        for (GlobalBlockPos rp : networkReaders) {
            float consumption = getEngineConsumptionForReader(rp);
            StressReaderBlockEntity reader = activeReaders.get(rp);
            if (reader != null) {
                reader.setRemoteEngineConsumption(consumption);
            }
        }
    }

    private void pushToEngineInternal(RemoteEngineBlockEntity engine, float speed, float budget, float capacity, float readerStress) {
        engine.applyRemoteUpdate(speed, budget, capacity, readerStress);
    }


    public void onEngineNetworkChanged(GlobalBlockPos enginePos, float localStress, float localCapacity) {
        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (engine != null && engine.hasNetwork()) {
            engineNetworkKey.put(enginePos, engine.getOrCreateNetwork());
        } else {
            engineNetworkKey.put(enginePos, enginePos);
        }

        if (!overstrainedEngines.contains(enginePos)) {
            engineLocalStress.put(enginePos, localStress);

            if (!suppressEngineUpdates) {
                Float budget = engineBudget.get(enginePos);
                if (budget != null && budget > 0 && localStress > budget) {
                    overstrainedEngines.add(enginePos);
                    engineBudget.put(enginePos, 0f);
                    GlobalBlockPos readerPos = engineToReader.get(enginePos);
                    if (readerPos != null) {
                        dirtyReaders.add(readerPos);
                    }
                }
            }
        }
        engineLocalCapacity.put(enginePos, localCapacity);

        if (suppressEngineUpdates) return;

        GlobalBlockPos readerPos = engineToReader.get(enginePos);
        if (readerPos != null) {
            float totalConsumption = getEngineConsumptionForReader(readerPos);
            StressReaderBlockEntity reader = activeReaders.get(readerPos);
            if (reader != null) reader.setRemoteEngineConsumption(totalConsumption);
        }
    }


    public float getEngineConsumptionForReader(GlobalBlockPos readerPos) {
        Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
        if (engines == null || engines.isEmpty()) return 0;

        Map<Object, Float> networkStress = new HashMap<>();
        for (GlobalBlockPos ep : engines) {
            if (overstrainedEngines.contains(ep)) continue;
            Float stress = engineLocalStress.get(ep);
            if (stress == null) continue;
            Object networkKey = engineNetworkKey.getOrDefault(ep, ep);
            networkStress.put(networkKey, stress);
        }

        float total = 0;
        for (float stress : networkStress.values()) {
            total += stress;
        }
        return total;
    }

    public float getEngineConsumptionForOwner(UUID ownerUUID) {
        float total = 0;
        for (var entry : engineToReader.entrySet()) {
            GlobalBlockPos enginePos = entry.getKey();
            GlobalBlockPos readerPos = entry.getValue();
            UUID readerOwner = readerOwners.get(readerPos);
            if (ownerUUID.equals(readerOwner)) {
                Float stress = engineLocalStress.get(enginePos);
                if (stress != null) total += stress;
            }
        }
        return total;
    }


    public void registerReaderOwner(GlobalBlockPos readerPos, UUID ownerUUID) {
        readerOwners.put(readerPos, ownerUUID);
        markDirty();
        rebuildOwnerIndex();
    }

    public void removeReaderOwner(GlobalBlockPos readerPos) {
        readerOwners.remove(readerPos);
        markDirty();
        rebuildOwnerIndex();
    }

    public List<GlobalBlockPos> getReadersForOwner(UUID ownerUUID) {
        Set<GlobalBlockPos> set = ownerReaders.get(ownerUUID);
        return set != null ? new ArrayList<>(set) : List.of();
    }

    private void rebuildOwnerIndex() {
        ownerReaders.clear();
        for (var entry : readerOwners.entrySet())
            ownerReaders.computeIfAbsent(entry.getValue(), k -> new HashSet<>()).add(entry.getKey());
    }


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

    public boolean hasArk(UUID ownerUUID) {
        return getArkCountForOwner(ownerUUID) > 0;
    }

    public AggregatedReaderData getAggregatedReaderData(UUID ownerUUID) {
        float totalCap = 0;
        float totalStress = 0;
        Set<KineticNetwork> seenNetworks = new HashSet<>();
        int uniqueCount = 0;
        for (GlobalBlockPos readerPos : getReadersForOwner(ownerUUID)) {
            StressReaderBlockEntity liveReader = activeReaders.get(readerPos);
            if (liveReader != null && liveReader.hasNetwork()) {
                KineticNetwork network = liveReader.getOrCreateNetwork();
                if (!seenNetworks.add(network)) continue;
            }

            ReaderSnapshot snap = getCachedReaderData(readerPos);
            if (snap != null) {
                totalCap += snap.capacity();
                totalStress += snap.stress();
                uniqueCount++;
            } else if (liveReader != null) {
                totalCap += liveReader.getNetworkCapacity();
                totalStress += liveReader.getNetworkStress();
                uniqueCount++;
            }
        }
        return new AggregatedReaderData(totalCap, totalStress, uniqueCount);
    }

    public float getAggregatedCapacity(UUID ownerUUID) {
        return getAggregatedReaderData(ownerUUID).totalCapacity();
    }


    private void forceLoadEngineChunk(@Nullable RemoteEngineBlockEntity engine, GlobalBlockPos enginePos) {
        MinecraftServer server = engine != null ? engine.getLevel().getServer() : GoetyMechMod.STRESS_LINK.server;
        if (server == null) return;
        ServerLevel engineLevel = server.getLevel(enginePos.dimension());
        if (engineLevel == null) return;
        ChunkPos chunkPos = new ChunkPos(enginePos.pos());
        engineLevel.setChunkForced(chunkPos.x, chunkPos.z, true);
    }

    private void unforceLoadEngineChunk(@Nullable RemoteEngineBlockEntity engine, GlobalBlockPos enginePos) {
        MinecraftServer server = engine != null ? engine.getLevel().getServer() : GoetyMechMod.STRESS_LINK.server;
        if (server == null) return;
        ServerLevel engineLevel = server.getLevel(enginePos.dimension());
        if (engineLevel == null) return;
        ChunkPos chunkPos = new ChunkPos(enginePos.pos());
        engineLevel.setChunkForced(chunkPos.x, chunkPos.z, false);
    }


    public static class StressLinkSavedData extends SavedData {

        private static final String DATA_NAME = "goetymech_stresslink";

        private Map<GlobalBlockPos, GlobalBlockPos> engineToReader = new LinkedHashMap<>();
        private Map<GlobalBlockPos, Set<GlobalBlockPos>> readerToEngine = new HashMap<>();
        private Map<GlobalBlockPos, UUID> readerOwners = new HashMap<>();

        public StressLinkSavedData() {}

        @Override
        public CompoundTag save(CompoundTag tag) {
            GlobalStressLinkManager manager = GoetyMechMod.STRESS_LINK;

            ListTag bindingsList = new ListTag();
            for (var entry : manager.engineToReader.entrySet()) {
                CompoundTag e = new CompoundTag();
                e.put("engine", entry.getKey().save());
                e.put("reader", entry.getValue().save());
                bindingsList.add(e);
            }
            tag.put("bindings", bindingsList);

            ListTag ownersList = new ListTag();
            for (var entry : manager.readerOwners.entrySet()) {
                CompoundTag e = new CompoundTag();
                e.put("pos", entry.getKey().save());
                e.putUUID("owner", entry.getValue());
                ownersList.add(e);
            }
            tag.put("readerOwners", ownersList);

            return tag;
        }

        @Override
        public void save(@NotNull File file) {
            SavedDataUtil.saveWithDatOld(this, file);
        }

        private static StressLinkSavedData load(CompoundTag nbt) {
            StressLinkSavedData sd = new StressLinkSavedData();

            sd.engineToReader = new LinkedHashMap<>();
            sd.readerToEngine = new HashMap<>();
            ListTag bindingsList = nbt.getList("bindings", Tag.TAG_COMPOUND);
            for (int i = 0; i < bindingsList.size(); i++) {
                CompoundTag e = bindingsList.getCompound(i);
                GlobalBlockPos enginePos = GlobalBlockPos.load(e.getCompound("engine"));
                GlobalBlockPos readerPos = GlobalBlockPos.load(e.getCompound("reader"));
                sd.engineToReader.put(enginePos, readerPos);
                sd.readerToEngine.computeIfAbsent(readerPos, k -> new LinkedHashSet<>()).add(enginePos);
            }

            sd.readerOwners = new HashMap<>();
            ListTag ownersList = nbt.getList("readerOwners", Tag.TAG_COMPOUND);
            for (int i = 0; i < ownersList.size(); i++) {
                CompoundTag e = ownersList.getCompound(i);
                sd.readerOwners.put(GlobalBlockPos.load(e.getCompound("pos")), e.getUUID("owner"));
            }

            return sd;
        }

        public Map<GlobalBlockPos, GlobalBlockPos> getEngineToReader() { return engineToReader; }
        public Map<GlobalBlockPos, Set<GlobalBlockPos>> getReaderToEngine() { return readerToEngine; }
        public Map<GlobalBlockPos, UUID> getReaderOwners() { return readerOwners; }

        public static StressLinkSavedData load(MinecraftServer server) {
            return server.overworld()
                    .getDataStorage()
                    .computeIfAbsent(StressLinkSavedData::load, StressLinkSavedData::new, DATA_NAME);
        }
    }

    public MinecraftServer server;
}
