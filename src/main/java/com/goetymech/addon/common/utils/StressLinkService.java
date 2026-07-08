package com.goetymech.addon.common.utils;

import com.goetymech.addon.common.block.entity.RemoteEngineBlockEntity;
import com.goetymech.addon.common.block.entity.StressReaderBlockEntity;
import com.simibubi.create.content.kinetics.KineticNetwork;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class StressLinkService extends SavedData {

    private static final String DATA_NAME = "goetymech_stresslink";
    private static final Logger LOGGER = LogManager.getLogger("StressLinkDebug");

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
    // 记录分配给每个引擎的预算（功率单位 su·rpm），用于 onEngineNetworkChanged 中实时检测过载
    private transient final Map<GlobalBlockPos, Float> engineBudget = new HashMap<>();
    // 记录每个引擎所属的 KineticNetwork 对象引用，用于去重：同一网络内的多个引擎只计一次 stress
    private transient final Map<GlobalBlockPos, Object> engineNetworkKey = new HashMap<>();

    // 重入防护：pushToEngine 期间抑制 onEngineNetworkChanged 的回传，
    // 避免 applyRemoteUpdate → engine 网络变化 → onEngineNetworkChanged →
    // setRemoteEngineConsumption → reader 网络 sync → pushToEngine 的死循环
    private transient boolean suppressEngineUpdates = false;
    
    // 服务器引用，用于跨维度区块强加载
    private transient MinecraftServer server;

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
        StressLinkService service = overworld.getDataStorage().computeIfAbsent(
                StressLinkService::loadStatic, StressLinkService::new, DATA_NAME);
        service.server = server;
        return service;
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
        engineBudget.clear();
        engineNetworkKey.clear();
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
        engineBudget.remove(enginePos);
        engineNetworkKey.remove(enginePos);
    }

    // ── Binding management ──

    public void bind(GlobalBlockPos enginePos, GlobalBlockPos readerPos) {
        GlobalBlockPos oldReader = engineToReader.get(enginePos);
        if (oldReader != null) {
            Set<GlobalBlockPos> engines = readerToEngine.get(oldReader);
            if (engines != null) engines.remove(enginePos);
            // 释放旧绑定的区块强加载
            unforceLoadEngineChunk(enginePos);
        }

        engineToReader.put(enginePos, readerPos);
        readerToEngine.computeIfAbsent(readerPos, k -> new LinkedHashSet<>()).add(enginePos);
        setDirty();
        // 强加载引擎所在区块（1x1范围），确保跨维度引擎即使玩家离开也能正常工作
        forceLoadEngineChunk(enginePos);
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
        // 释放区块强加载
        unforceLoadEngineChunk(enginePos);
        overstrainedEngines.remove(enginePos);
        engineLocalStress.remove(enginePos);
        engineLocalCapacity.remove(enginePos);
        engineBudget.remove(enginePos);
        engineNetworkKey.remove(enginePos);
        setDirty();

        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (engine != null) engine.applyRemoteUpdate(0, 0, 0, 0);
    }

    private void forceLoadEngineChunk(GlobalBlockPos enginePos) {
        if (server == null) return;
        ServerLevel engineLevel = server.getLevel(enginePos.dimension());
        if (engineLevel == null) return;
        ChunkPos chunkPos = new ChunkPos(enginePos.pos());
        engineLevel.setChunkForced(chunkPos.x, chunkPos.z, true);
        LOGGER.info("[forceLoadEngineChunk] engine={} chunk=({},{}) dimension={}",
                enginePos, chunkPos.x, chunkPos.z, enginePos.dimension());
    }

    private void unforceLoadEngineChunk(GlobalBlockPos enginePos) {
        if (server == null) return;
        ServerLevel engineLevel = server.getLevel(enginePos.dimension());
        if (engineLevel == null) return;
        ChunkPos chunkPos = new ChunkPos(enginePos.pos());
        engineLevel.setChunkForced(chunkPos.x, chunkPos.z, false);
        LOGGER.info("[unforceLoadEngineChunk] engine={} chunk=({},{}) dimension={}",
                enginePos, chunkPos.x, chunkPos.z, enginePos.dimension());
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
    // 注意：同一 KineticNetwork 内的多个引擎只计一次消耗，但所有引擎都获得相同的预算分配。
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
        // Math.max(0, ...) 防止重复计数或其他异常导致 localStress 为负，
        // 避免 totalBudget 异常膨胀形成正反馈循环
        float localStress = Math.max(0, readerStress - remoteConsumption);
        // 可分配给引擎的总预算 = reader 总容量 - 本地消耗
        float totalBudget = Math.max(0, capacity - localStress);

        LOGGER.info("[distributeBudget] START reader={} speed={} capacity={} readerStress={} remoteConsumption={} localStress={} totalBudget={} engineCount={}",
                readerPos, speed, capacity, readerStress, remoteConsumption, localStress, totalBudget, engines.size());

        // 按网络分组：同一网络的引擎共享消耗和预算
        Map<Object, List<GlobalBlockPos>> networkToEngines = new LinkedHashMap<>();
        for (GlobalBlockPos ep : engines) {
            if (overstrainedEngines.contains(ep)) continue;
            Object networkKey = engineNetworkKey.getOrDefault(ep, ep);
            networkToEngines.computeIfAbsent(networkKey, k -> new ArrayList<>()).add(ep);
        }

        float allocated = 0;

        suppressEngineUpdates = true;
        try {
            int netIdx = 0;
            for (var entry : networkToEngines.entrySet()) {
                Object networkKey = entry.getKey();
                List<GlobalBlockPos> engineList = entry.getValue();

                // 取网络中任意一个引擎的消耗（同网络内所有引擎消耗相同）
                float networkConsumption = 0;
                if (!engineList.isEmpty()) {
                    networkConsumption = engineLocalStress.getOrDefault(engineList.get(0), 0f);
                }

                float availableBudget = Math.max(0, totalBudget - allocated);

                LOGGER.info("[distributeBudget] network#{} key={} consumption={} availableBudget={} engineCount={}",
                        netIdx, networkKey, networkConsumption, availableBudget, engineList.size());

                if (availableBudget >= networkConsumption) {
                    // 预算充足：同网络内所有引擎都正常运转
                    // 同一网络内的引擎共享总预算，每个引擎获得 budget / engineCount，
                    // 这样总容量 = engineCount * (budget / engineCount) / speed = budget / speed，不会叠加
                    float perEngineBudget = availableBudget / engineList.size();
                    for (GlobalBlockPos enginePos : engineList) {
                        overstrainedEngines.remove(enginePos);
                        engineBudget.put(enginePos, availableBudget);
                        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
                        if (engine != null) {
                            pushToEngineInternal(engine, speed, perEngineBudget, capacity, readerStress);
                            LOGGER.info("[distributeBudget]   -> engine={} OK speed={} budget={} (total={}, count={})",
                                    enginePos, speed, perEngineBudget, availableBudget, engineList.size());
                        }
                    }
                    allocated += networkConsumption;
                    LOGGER.info("[distributeBudget]   -> allocatedTotal={}", allocated);
                } else {
                    // 预算不足：同网络内所有引擎都过载停转
                    for (GlobalBlockPos enginePos : engineList) {
                        overstrainedEngines.add(enginePos);
                        engineBudget.put(enginePos, 0f);
                        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
                        if (engine != null) {
                            pushToEngineInternal(engine, 0, 0, capacity, readerStress);
                            LOGGER.info("[distributeBudget]   -> engine={} OVERSTRAIN speed=0 budget=0", enginePos);
                        }
                    }
                }
                netIdx++;
            }
        } finally {
            suppressEngineUpdates = false;
        }

        // 全部推送完成后，统一更新 reader 的远程引擎消耗
        float finalConsumption = getEngineConsumptionForReader(readerPos);
        reader.setRemoteEngineConsumption(finalConsumption);
        LOGGER.info("[distributeBudget] END reader={} finalRemoteConsumption={}",
                readerPos, finalConsumption);
    }

    private void pushToEngineInternal(RemoteEngineBlockEntity engine, float speed, float budget, float capacity, float readerStress) {
        engine.applyRemoteUpdate(speed, budget, capacity, readerStress);
    }

    // ── Engine consumption tracking ──

    public void onEngineNetworkChanged(GlobalBlockPos enginePos, float localStress, float localCapacity) {
        // 更新引擎网络身份：同一 KineticNetwork 内的多个引擎用相同的 key，用于去重
        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (engine != null && engine.hasNetwork()) {
            engineNetworkKey.put(enginePos, engine.getOrCreateNetwork());
        } else {
            engineNetworkKey.put(enginePos, enginePos);
        }

        // 过载引擎的消耗不更新：budget=0 会导致 stress 归零，
        // 但分配预算时需要使用其"额定消耗"来判断是否可以恢复，
        // 否则会在 0>=0 判定正常 → 给 budget → stress 恢复 → 又过载 之间振荡
        if (!overstrainedEngines.contains(enginePos)) {
            float oldStress = engineLocalStress.getOrDefault(enginePos, -1f);
            engineLocalStress.put(enginePos, localStress);

            LOGGER.info("[onEngineNetworkChanged] engine={} oldStress={} newStress={} capacity={} overstrained={}",
                    enginePos, oldStress, localStress, localCapacity, overstrainedEngines.contains(enginePos));

            // 实时过载检测：如果引擎上报的实际消耗超过了分配给它的预算，
            // 立即标记过载并标记 reader 为 dirty，让 distributeBudget 统一推送 speed=0。
            // 注意：不在此方法中直接调用 applyRemoteUpdate，否则会在
            // KineticNetwork.sync 遍历 members 时触发网络结构变化导致 CME。
            if (!suppressEngineUpdates) {
                Float budget = engineBudget.get(enginePos);
                if (budget != null && budget > 0 && localStress > budget) {
                    LOGGER.info("[onEngineNetworkChanged] OVERLOAD DETECTED engine={} stress={} budget={}",
                            enginePos, localStress, budget);
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

        // pushToEngine 期间抑制回传，防止死循环和 CME
        if (suppressEngineUpdates) return;

        GlobalBlockPos readerPos = engineToReader.get(enginePos);
        if (readerPos != null) {
            float totalConsumption = getEngineConsumptionForReader(readerPos);
            LOGGER.info("[onEngineNetworkChanged] update reader={} totalRemoteConsumption={}",
                    readerPos, totalConsumption);
            StressReaderBlockEntity reader = activeReaders.get(readerPos);
            if (reader != null) reader.setRemoteEngineConsumption(totalConsumption);
        }
    }

    public float getEngineConsumptionForReader(GlobalBlockPos readerPos) {
        Set<GlobalBlockPos> engines = readerToEngine.get(readerPos);
        if (engines == null || engines.isEmpty()) return 0;

        // 按引擎所属的 KineticNetwork 分组去重：同一网络内的多个引擎只计一次 stress
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
