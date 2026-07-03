package com.goetymech.addon.common.utils;

import com.goetymech.addon.common.block.entity.RemoteEngineBlockEntity;
import com.goetymech.addon.common.block.entity.StressReaderBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;

public class StressLinkService extends SavedData {

    private static final String DATA_NAME = "goetymech_stresslink";

    // === Persisted ===
    // enginePos → readerPos   (1:1 binding)
    private final Map<GlobalBlockPos, GlobalBlockPos> engineToReader = new LinkedHashMap<>();
    // Inverse (optimization, rebuilt on load)
    private transient final Map<GlobalBlockPos, GlobalBlockPos> readerToEngine = new HashMap<>();

    // readerPos → ownerUUID (for Ark discovery, persisted)
    private final Map<GlobalBlockPos, UUID> readerOwners = new HashMap<>();
    // ownerUUID → Set<readerPos> (inverse, rebuilt on load)
    private transient final Map<UUID, Set<GlobalBlockPos>> ownerReaders = new HashMap<>();

    // === Runtime-only (not persisted) ===
    private transient final Map<GlobalBlockPos, StressReaderBlockEntity> activeReaders = new HashMap<>();
    private transient final Map<GlobalBlockPos, RemoteEngineBlockEntity> activeEngines = new HashMap<>();

    public StressLinkService() {
        super();
    }

    // ──────────────────────────────────────────────
    //  Static accessor
    // ──────────────────────────────────────────────

    @Nullable
    public static StressLinkService get(@Nullable Level level) {
        if (level == null || level.isClientSide) return null;
        return get(((ServerLevel) level).getServer());
    }

    public static StressLinkService get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        return overworld.getDataStorage().computeIfAbsent(
                StressLinkService::loadStatic,
                StressLinkService::new,
                DATA_NAME);
    }

    private static StressLinkService loadStatic(CompoundTag tag) {
        return new StressLinkService().load(tag);
    }

    // ──────────────────────────────────────────────
    //  Reader lifecycle
    // ──────────────────────────────────────────────

    public void onReaderLoad(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());
        activeReaders.put(key, reader);
        rebuildOwnerIndex();

        // If there's an engine bound to this reader, push initial data
        GlobalBlockPos enginePos = readerToEngine.get(key);
        if (enginePos != null) {
            pushToEngine(enginePos);
        }
    }

    public void onReaderUnload(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());
        activeReaders.remove(key);
    }

    // ──────────────────────────────────────────────
    //  Engine lifecycle
    // ──────────────────────────────────────────────

    public void onEngineLoad(RemoteEngineBlockEntity engine) {
        if (engine.getLevel() == null) return;
        GlobalBlockPos key = GlobalBlockPos.of(engine.getLevel(), engine.getBlockPos());
        activeEngines.put(key, engine);

        // If a binding exists in saved data, push initial data
        GlobalBlockPos readerPos = engineToReader.get(key);
        if (readerPos != null) {
            pushToEngine(key);
        }
    }

    public void onEngineUnload(GlobalBlockPos enginePos) {
        activeEngines.remove(enginePos);
    }

    // ──────────────────────────────────────────────
    //  Binding management
    // ──────────────────────────────────────────────

    public void bind(GlobalBlockPos enginePos, GlobalBlockPos readerPos) {
        // Remove old binding for this engine, if any
        GlobalBlockPos oldReader = engineToReader.get(enginePos);
        if (oldReader != null) {
            readerToEngine.remove(oldReader);
        }
        // If this reader is already bound to another engine, disconnect the old one
        GlobalBlockPos oldEngine = readerToEngine.get(readerPos);
        if (oldEngine != null && !oldEngine.equals(enginePos)) {
            engineToReader.remove(oldEngine);
            RemoteEngineBlockEntity stale = activeEngines.get(oldEngine);
            if (stale != null) {
                stale.applyRemoteUpdate(0, 0);
            }
        }
        engineToReader.put(enginePos, readerPos);
        readerToEngine.put(readerPos, enginePos);
        setDirty();

        pushToEngine(enginePos);
    }

    public void unbind(GlobalBlockPos enginePos) {
        GlobalBlockPos readerPos = engineToReader.remove(enginePos);
        if (readerPos != null) {
            readerToEngine.remove(readerPos);
        }
        setDirty();

        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (engine != null) {
            engine.applyRemoteUpdate(0, 0);
        }
    }

    @Nullable
    public GlobalBlockPos getReaderForEngine(GlobalBlockPos enginePos) {
        return engineToReader.get(enginePos);
    }

    @Nullable
    public RemoteEngineBlockEntity getLoadedEngine(GlobalBlockPos enginePos) {
        return activeEngines.get(enginePos);
    }

    // ──────────────────────────────────────────────
    //  Event-driven update (called by Reader)
    // ──────────────────────────────────────────────

    public void onReaderNetworkChanged(StressReaderBlockEntity reader) {
        if (reader.getLevel() == null) return;
        GlobalBlockPos readerKey = GlobalBlockPos.of(reader.getLevel(), reader.getBlockPos());
        GlobalBlockPos enginePos = readerToEngine.get(readerKey);
        if (enginePos != null) {
            pushToEngine(enginePos);
        }
    }

    private void pushToEngine(GlobalBlockPos enginePos) {
        GlobalBlockPos readerPos = engineToReader.get(enginePos);
        if (readerPos == null) return;

        StressReaderBlockEntity reader = activeReaders.get(readerPos);
        RemoteEngineBlockEntity engine = activeEngines.get(enginePos);
        if (reader == null || engine == null) return;

        float speed = reader.getSpeed();
        float budget = Math.max(0, reader.getNetworkCapacity() - reader.getNetworkStress());
        engine.applyRemoteUpdate(speed, budget);
    }

    // ──────────────────────────────────────────────
    //  Reader ownership registry (for Ark)
    // ──────────────────────────────────────────────

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
        for (var entry : readerOwners.entrySet()) {
            ownerReaders.computeIfAbsent(entry.getValue(), k -> new HashSet<>()).add(entry.getKey());
        }
    }

    // ──────────────────────────────────────────────
    //  SavedData serialization
    // ──────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag bindingsList = new ListTag();
        for (var entry : engineToReader.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("engine", entry.getKey().save());
            entryTag.put("reader", entry.getValue().save());
            bindingsList.add(entryTag);
        }
        tag.put("bindings", bindingsList);

        ListTag ownersList = new ListTag();
        for (var entry : readerOwners.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("pos", entry.getKey().save());
            entryTag.putUUID("owner", entry.getValue());
            ownersList.add(entryTag);
        }
        tag.put("readerOwners", ownersList);

        return tag;
    }

    public StressLinkService load(CompoundTag tag) {
        engineToReader.clear();
        ListTag bindingsList = tag.getList("bindings", Tag.TAG_COMPOUND);
        for (int i = 0; i < bindingsList.size(); i++) {
            CompoundTag entryTag = bindingsList.getCompound(i);
            GlobalBlockPos enginePos = GlobalBlockPos.load(entryTag.getCompound("engine"));
            GlobalBlockPos readerPos = GlobalBlockPos.load(entryTag.getCompound("reader"));
            engineToReader.put(enginePos, readerPos);
            readerToEngine.put(readerPos, enginePos);
        }

        readerOwners.clear();
        ListTag ownersList = tag.getList("readerOwners", Tag.TAG_COMPOUND);
        for (int i = 0; i < ownersList.size(); i++) {
            CompoundTag entryTag = ownersList.getCompound(i);
            GlobalBlockPos pos = GlobalBlockPos.load(entryTag.getCompound("pos"));
            UUID owner = entryTag.getUUID("owner");
            readerOwners.put(pos, owner);
        }
        rebuildOwnerIndex();

        return this;
    }
}
