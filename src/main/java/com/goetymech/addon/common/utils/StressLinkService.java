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
    private transient final Map<GlobalBlockPos, Set<GlobalBlockPos>> readerToEngine = new HashMap<>();
    private final Map<GlobalBlockPos, UUID> readerOwners = new HashMap<>();
    private transient final Map<UUID, Set<GlobalBlockPos>> ownerReaders = new HashMap<>();

    // === Runtime-only ===
    private transient final Map<GlobalBlockPos, StressReaderBlockEntity> activeReaders = new HashMap<>();
    private transient final Map<GlobalBlockPos, RemoteEngineBlockEntity> activeEngines = new HashMap<>();
    private transient final Map<GlobalBlockPos, ReaderSnapshot> readerCache = new HashMap<>();
    private transient final Map<GlobalBlockPos, UUID> arkRegistry = new HashMap<>();
    private transient final Map<UUID, Set<GlobalBlockPos>> ownerArks = new HashMap<>();
    private transient final Map<GlobalBlockPos, Float> engineLocalStress = new HashMap<>();
    private transient final Map<GlobalBlockPos, Float> engineLocalCapacity = new HashMap<>();

    // 重入防护：pushToEngine 期间抑制 onEngineNetworkChanged 的回传，
    // 避免 applyRemoteUpdate → engine 网络变化 → onEngineNetworkChanged →
    // setRemoteEngineConsumption → reader 网络 sync → pushToEngine 的死循环
    private transient boolean suppressEngineUpdates = false;

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

    // ── Clear all data (for world unload to prevent cross-save) ──

    public void clear() {
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
        setDirty();
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

        Set<GlobalBlockPos> engines = readerToEngine.get(key);
        if (engines != null) engines.forEach(this::pushToEngine);
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
                RemoteEngineBlockEntity engine = activeEngines.get(ep);
                if (engine != null) engine.applyRemoteUpdate(0, 0, 0, 0);
            }
            setDirty();
        }
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
        engineLocalStress.remove(enginePos);
        engineLocalCapacity.remove(enginePos);
    }

    // ── Binding management ──

    public void bind(GlobalBlockPos enginePos, GlobalBlockPos readerPos) {
        GlobalBlockPos oldReader = engineToReader.get(enginePos);
        if (oldReader != null) {
            Set<GlobalBlockPos> engines = readerToEngine.get(oldReader);
            if (engines != null) engines.remove(enginePos);
        }

        engineToReader.put(enginePos, readerPos);
        readerToEngine.computeIfAbsent(readerPos, k -> new LinkedHashSet<>()).add(enginePos);
        setDirty();
        pushToEngine(enginePos);
    }

    public void unbind(GlobalBlockPos enginePos) {
        GlobalBlockPos readerPos = engineToReader.remove(enginePos);
        if (readerPos != null) {
            Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
            if (engines != null) {
                engines.remove(enginePos);
                if (engines.isEmpty()) readerToEngine.remove(readerPos);
            }
        }
        setDirty();

        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (engine != null) engine.applyRemoteUpdate(0, 0, 0, 0);
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

        Set<GlobalBlockPos> engines = readerToEngine.get(readerKey);
        if (engines != null) engines.forEach(this::pushToEngine);
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
        float readerStress = reader.getNetworkStress();
        // budget 需加回当前引擎自身消耗：getNetworkStress() 已含所有远程引擎消耗（ΣXi），
        // 加回 Xi 得到 capacity - localStress - Σ(j≠i)Xj，避免双重计入与正反馈振荡
        float thisEngineConsumption = engineLocalStress.getOrDefault(enginePos, 0f);
        float budget = Math.max(0, capacity - readerStress + thisEngineConsumption);

        // 抑制回传：applyRemoteUpdate 会触发 engine 自己的网络更新，
        // 进而触发 onEngineNetworkChanged 回传 reader，形成递归。
        // 用 suppress 标志在 push 期间阻断回传，末尾统一同步最终状态。
        suppressEngineUpdates = true;
        try {
            engine.applyRemoteUpdate(speed, budget, capacity, readerStress);
        } finally {
            suppressEngineUpdates = false;
        }

        reader.setRemoteEngineConsumption(getEngineConsumptionForReader(readerPos));
    }

    // ── Engine consumption tracking ──

    public void onEngineNetworkChanged(GlobalBlockPos enginePos, float localStress, float localCapacity) {
        engineLocalStress.put(enginePos, localStress);
        engineLocalCapacity.put(enginePos, localCapacity);

        // pushToEngine 期间抑制回传，防止死循环
        if (suppressEngineUpdates) return;

        GlobalBlockPos readerPos = engineToReader.get(enginePos);
        if (readerPos != null) {
            StressReaderBlockEntity reader = activeReaders.get(readerPos);
            if (reader != null) reader.setRemoteEngineConsumption(getEngineConsumptionForReader(readerPos));
        }
    }

    public float getEngineConsumptionForReader(GlobalBlockPos readerPos) {
        Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
        if (engines == null || engines.isEmpty()) return 0;
        float total = 0;
        for (GlobalBlockPos ep : engines) {
            Float stress = engineLocalStress.get(ep);
            if (stress != null) total += stress;
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
            readerToEngine.computeIfAbsent(readerPos, k -> new LinkedHashSet<>()).add(enginePos);
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
