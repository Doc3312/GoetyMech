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

    // 延迟推送标记：onReaderNetworkChanged 不立即推送，只标记 dirty，
    // 在 tick() 中统一推送，避免在网络 sync 遍历期间因 detachKinetics 修改 members
    // 导致 ConcurrentModificationException
    private transient final Set<GlobalBlockPos> dirtyReaders = new HashSet<>();

    // 过载引擎集合：应力不足时，后加入的引擎被标记为过载，budget=0，
    // 其消耗不计入 reader 网络，保护先加入的引擎正常运转
    private transient final Set<GlobalBlockPos> overstrainedEngines = new HashSet<>();

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
        overstrainedEngines.clear();
        dirtyReaders.clear();
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
        if (engines != null && !engines.isEmpty()) {
            // reader 加载后标记 dirty，由 tick 统一推送，
            // 避免在加载流程中触发网络结构修改
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
        if (readerPos != null) {
            // 引擎加载后标记 reader dirty，由 tick 统一推送
            dirtyReaders.add(readerPos);
        }
    }

    public void onEngineUnload(GlobalBlockPos enginePos) {
        activeEngines.remove(enginePos);
        overstrainedEngines.remove(enginePos);
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
        // 新引擎加入：标记 reader dirty，由 tick 统一分配预算
        dirtyReaders.add(readerPos);
    }

    public void unbind(GlobalBlockPos enginePos) {
        GlobalBlockPos readerPos = engineToReader.remove(enginePos);
        if (readerPos != null) {
            Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
            if (engines != null) {
                engines.remove(enginePos);
                if (engines.isEmpty()) readerToEngine.remove(readerPos);
            }
            // 解绑后重新分配剩余引擎的预算
            dirtyReaders.add(readerPos);
        }
        overstrainedEngines.remove(enginePos);
        engineLocalStress.remove(enginePos);
        engineLocalCapacity.remove(enginePos);
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

        // 延迟推送：不立即 pushToEngine，标记为 dirty 后由 tick() 统一推送。
        // 原因：此方法在 reader 网络 sync 遍历中被调用，
        // 立即 push 可能触发 engine 的 applyRemoteUpdate → updateGeneratedRotation
        // → detachKinetics，修改 engine 网络 members，若此时 engine 网络也在
        // sync 遍历则会导致 ConcurrentModificationException。
        dirtyReaders.add(readerKey);
    }

    @Nullable
    public ReaderSnapshot getCachedReaderData(GlobalBlockPos readerPos) {
        return readerCache.get(readerPos);
    }

    // ── Tick: 延迟推送 + 批量分配 ──

    public void tick() {
        if (dirtyReaders.isEmpty()) return;
        Set<GlobalBlockPos> snapshot = new HashSet<>(dirtyReaders);
        dirtyReaders.clear();
        for (GlobalBlockPos readerPos : snapshot) {
            distributeBudget(readerPos);
        }
    }

    // 按引擎加入顺序（LinkedHashSet）分配 reader 的应力预算：
    // 先加入的引擎优先获得预算，后加入的引擎若预算不足则标记为过载（budget=0）。
    // 过载引擎的消耗不计入 reader 网络，保护已有引擎正常运转。
    private void distributeBudget(GlobalBlockPos readerPos) {
        StressReaderBlockEntity reader = activeReaders.get(readerPos);
        if (reader == null) return;

        Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
        if (engines == null || engines.isEmpty()) return;

        float speed = reader.getSpeed();
        float capacity = reader.getNetworkCapacity();
        float readerStress = reader.getNetworkStress();
        float remoteConsumption = getEngineConsumptionForReader(readerPos);

        // reader 本地消耗 = reader 总 stress - 远程引擎消耗（非过载的）
        float localStress = readerStress - remoteConsumption;
        // 可分配给引擎的总预算 = reader 总容量 - 本地消耗
        float totalBudget = Math.max(0, capacity - localStress);

        float allocated = 0;

        // 抑制回传：批量推送过程中引擎网络变化不应回传修改 reader 消耗，
        // 全部推送完成后统一设置一次
        suppressEngineUpdates = true;
        try {
            for (GlobalBlockPos enginePos : engines) {
                RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
                if (engine == null) continue;

                float engineConsumption = engineLocalStress.getOrDefault(enginePos, 0f);
                float availableBudget = Math.max(0, totalBudget - allocated);

                if (availableBudget >= engineConsumption) {
                    // 预算充足：正常运转
                    overstrainedEngines.remove(enginePos);
                    pushToEngineInternal(engine, speed, availableBudget, capacity, readerStress);
                    allocated += engineConsumption;
                } else {
                    // 预算不足：标记过载，speed=0 让引擎完全停转。
                    // 若只设 budget=0 但 speed>0，Create 会判定引擎"有速度无应力"而过载，
                    // 导致连接方块被破坏。speed=0 让引擎安全离线。
                    overstrainedEngines.add(enginePos);
                    pushToEngineInternal(engine, 0, 0, capacity, readerStress);
                }
            }
        } finally {
            suppressEngineUpdates = false;
        }

        // 全部推送完成后，统一更新 reader 的远程引擎消耗
        reader.setRemoteEngineConsumption(getEngineConsumptionForReader(readerPos));
    }

    private void pushToEngineInternal(RemoteEngineBlockEntity engine, float speed, float budget, float capacity, float readerStress) {
        engine.applyRemoteUpdate(speed, budget, capacity, readerStress);
    }

    // ── Engine consumption tracking ──

    public void onEngineNetworkChanged(GlobalBlockPos enginePos, float localStress, float localCapacity) {
        // 过载引擎的消耗不更新：budget=0 会导致 stress 归零，
        // 但分配预算时需要使用其"额定消耗"来判断是否可以恢复，
        // 否则会在 0>=0 判定正常 → 给 budget → stress 恢复 → 又过载 之间振荡
        if (!overstrainedEngines.contains(enginePos)) {
            engineLocalStress.put(enginePos, localStress);
        }
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
            // 过载引擎不计入消耗：其 budget=0，引擎网络已过载停转，
            // 不应再占用 reader 的应力配额
            if (overstrainedEngines.contains(ep)) continue;
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
