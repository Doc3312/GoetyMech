package com.goetymech.addon.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class DepositSavedData extends SavedData {

    private static final String DATA_NAME = "goetymech_deposit";

    private final Map<UUID, Map<UUID, List<ItemStack>>> playerData = new HashMap<>();

    public static DepositSavedData create(CompoundTag tag) {
        DepositSavedData data = new DepositSavedData();
        ListTag playerList = tag.getList("PlayerEntries", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag pt = playerList.getCompound(i);
            UUID playerUUID = pt.getUUID("PlayerUUID");
            Map<UUID, List<ItemStack>> freqMap = new HashMap<>();
            ListTag freqList = pt.getList("Frequencies", Tag.TAG_COMPOUND);
            for (int j = 0; j < freqList.size(); j++) {
                CompoundTag ft = freqList.getCompound(j);
                UUID freqId = ft.getUUID("FreqUUID");
                List<ItemStack> items = new ArrayList<>();
                ListTag itemsList = ft.getList("Items", Tag.TAG_COMPOUND);
                for (int k = 0; k < itemsList.size(); k++) {
                    ItemStack stack = ItemStack.of(itemsList.getCompound(k));
                    if (!stack.isEmpty()) items.add(stack);
                }
                if (!items.isEmpty()) freqMap.put(freqId, items);
            }
            if (!freqMap.isEmpty()) data.playerData.put(playerUUID, freqMap);
        }
        return data;
    }

    public static DepositSavedData readOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(DepositSavedData::create, DepositSavedData::new, DATA_NAME);
    }

    public List<ItemStack> getItems(UUID playerUUID, UUID freqId) {
        Map<UUID, List<ItemStack>> freqMap = playerData.get(playerUUID);
        if (freqMap == null) return List.of();
        List<ItemStack> items = freqMap.get(freqId);
        return items != null ? items : List.of();
    }

    public boolean hasItems(UUID playerUUID, UUID freqId) {
        Map<UUID, List<ItemStack>> freqMap = playerData.get(playerUUID);
        return freqMap != null && freqMap.containsKey(freqId) && !freqMap.get(freqId).isEmpty();
    }

    public void setItems(UUID playerUUID, UUID freqId, List<ItemStack> items) {
        List<ItemStack> nonEmpty = items.stream().filter(s -> !s.isEmpty()).toList();
        if (nonEmpty.isEmpty()) {
            clear(playerUUID, freqId);
            return;
        }
        playerData.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(freqId, new ArrayList<>(nonEmpty));
        setDirty();
    }

    public void clear(UUID playerUUID, UUID freqId) {
        Map<UUID, List<ItemStack>> freqMap = playerData.get(playerUUID);
        if (freqMap != null) {
            freqMap.remove(freqId);
            if (freqMap.isEmpty()) playerData.remove(playerUUID);
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, Map<UUID, List<ItemStack>>> pe : playerData.entrySet()) {
            CompoundTag pt = new CompoundTag();
            pt.putUUID("PlayerUUID", pe.getKey());
            ListTag freqList = new ListTag();
            for (Map.Entry<UUID, List<ItemStack>> fe : pe.getValue().entrySet()) {
                CompoundTag ft = new CompoundTag();
                ft.putUUID("FreqUUID", fe.getKey());
                ListTag itemsList = new ListTag();
                for (ItemStack stack : fe.getValue()) {
                    itemsList.add(stack.save(new CompoundTag()));
                }
                ft.put("Items", itemsList);
                freqList.add(ft);
            }
            pt.put("Frequencies", freqList);
            playerList.add(pt);
        }
        tag.put("PlayerEntries", playerList);
        return tag;
    }
}
