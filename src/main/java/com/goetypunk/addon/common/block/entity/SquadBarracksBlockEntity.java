package com.goetypunk.addon.common.block.entity;

import com.goetypunk.addon.common.block.SquadBarracksBlock;
import com.goetypunk.addon.common.block.Tier1SquadBarracksBlock;
import com.goetypunk.addon.common.block.Tier2SquadBarracksBlock;
import com.goetypunk.addon.common.block.Tier3SquadBarracksBlock;
import com.goetypunk.addon.common.utils.SquadChunkTickets;
import com.goetypunk.addon.init.ModBlocks;
import com.goetypunk.addon.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SquadBarracksBlockEntity extends BlockEntity implements WorldlyContainer {

    private final int configSlotCount;
    private final int reserveSlotCount;
    private final List<ResourceLocation> configSlots;
    private final List<ItemStack> reserveSlots;
    private UUID ownerUUID;
    private String ownerName = "";

    private final LazyOptional<IItemHandlerModifiable>[] handlers =
            SidedInvWrapper.create(this, Direction.values());

    public SquadBarracksBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SQUAD_BARRACKS_ENTITY.get(), pos, state);
        Block block = state.getBlock();
        if (block instanceof SquadBarracksBlock barracksBlock) {
            this.configSlotCount = barracksBlock.getConfigSlots();
            this.reserveSlotCount = barracksBlock.getReserveSlots();
        } else {
            this.configSlotCount = 0;
            this.reserveSlotCount = 0;
        }
        this.configSlots = new ArrayList<>(configSlotCount);
        for (int i = 0; i < configSlotCount; i++) configSlots.add(null);
        this.reserveSlots = new ArrayList<>(reserveSlotCount);
        for (int i = 0; i < reserveSlotCount; i++) reserveSlots.add(ItemStack.EMPTY);
    }

    public int getConfigSlotCount() { return configSlotCount; }

    public int getReserveSlotCount() { return reserveSlotCount; }

    public int getTier() {
        Block block = getBlockState().getBlock();
        if (block instanceof Tier3SquadBarracksBlock) return 3;
        if (block instanceof Tier2SquadBarracksBlock) return 2;
        return 1;
    }

    // === Squad Config (virtual slots) ===

    public ResourceLocation getConfigSlot(int index) {
        if (index < 0 || index >= configSlots.size()) return null;
        return configSlots.get(index);
    }

    public List<ResourceLocation> getConfigSlots() {
        return new ArrayList<>(configSlots);
    }

    public void setConfigSlot(int index, ResourceLocation entityType) {
        if (index >= 0 && index < configSlots.size()) {
            configSlots.set(index, entityType);
            markUpdated();
        }
    }

    public void clearConfigSlot(int index) {
        if (index >= 0 && index < configSlots.size()) {
            configSlots.set(index, null);
            markUpdated();
        }
    }

    // === Squad Composition ===

    public List<Map.Entry<ResourceLocation, Integer>> getSquadComposition() {
        Map<ResourceLocation, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < configSlots.size(); i++) {
            ResourceLocation rl = configSlots.get(i);
            if (rl != null) {
                map.merge(rl, 1, Integer::sum);
            }
        }
        return new ArrayList<>(map.entrySet());
    }

    public int countAvailableUnits() {
        Map<ResourceLocation, Integer> composition = new LinkedHashMap<>();
        for (ResourceLocation rl : configSlots) {
            if (rl != null) {
                composition.merge(rl, 1, Integer::sum);
            }
        }
        int total = 0;
        for (Map.Entry<ResourceLocation, Integer> entry : composition.entrySet()) {
            ResourceLocation type = entry.getKey();
            int needed = entry.getValue();
            int available = 0;
            for (ItemStack stack : reserveSlots) {
                if (!stack.isEmpty() && stack.hasTag()) {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && tag.contains("EntityTypeId")) {
                        if (tag.getString("EntityTypeId").equals(type.toString())) {
                            available += stack.getCount();
                        }
                    }
                }
            }
            total += Math.min(needed, available);
        }
        return total;
    }

    public List<Map.Entry<ResourceLocation, Integer>> consumeSquad(ServerLevel level, Player player) {
        return consumeSquad(level, player, Integer.MAX_VALUE);
    }

    public List<Map.Entry<ResourceLocation, Integer>> consumeSquad(ServerLevel level, Player player, int maxUnits) {
        Map<ResourceLocation, Integer> consumedMap = new LinkedHashMap<>();
        int remaining = maxUnits;
        for (Map.Entry<ResourceLocation, Integer> entry : getSquadComposition()) {
            if (remaining <= 0) break;
            ResourceLocation type = entry.getKey();
            int needed = Math.min(entry.getValue(), remaining);
            int consumed = 0;
            for (int r = 0; r < reserveSlots.size() && needed > 0; r++) {
                ItemStack stack = reserveSlots.get(r);
                if (!stack.isEmpty() && stack.hasTag()) {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && tag.contains("EntityTypeId")) {
                        String rlStr = tag.getString("EntityTypeId");
                        if (rlStr.equals(type.toString())) {
                            int toConsume = Math.min(needed, stack.getCount());
                            stack.shrink(toConsume);
                            needed -= toConsume;
                            consumed += toConsume;
                        }
                    }
                }
            }
            if (consumed > 0) {
                consumedMap.put(type, consumed);
                remaining -= consumed;
            }
        }
        if (!consumedMap.isEmpty()) markUpdated();
        return new ArrayList<>(consumedMap.entrySet());
    }

    // === Owner ===

    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID uuid) { this.ownerUUID = uuid; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String name) { this.ownerName = name; }

    public boolean isOwner(Player player) {
        return ownerUUID != null && player.getUUID().equals(ownerUUID);
    }

    public boolean tryClaim(Player player) {
        if (this.ownerUUID == null) {
            this.ownerUUID = player.getUUID();
            this.ownerName = player.getDisplayName().getString();
            setChanged();
            return true;
        }
        return false;
    }

    // === Chunk Ticket ===

    public void registerChunkTicket(ServerLevel serverLevel) {
        ChunkPos chunkPos = new ChunkPos(worldPosition);
        serverLevel.getChunkSource().addRegionTicket(
                SquadChunkTickets.TICKET_TYPE, chunkPos, 3, worldPosition);
    }

    public void removeChunkTicket(ServerLevel serverLevel) {
        ChunkPos chunkPos = new ChunkPos(worldPosition);
        serverLevel.getChunkSource().removeRegionTicket(
                SquadChunkTickets.TICKET_TYPE, chunkPos, 3, worldPosition);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            registerChunkTicket(serverLevel);
        }
    }

    // === Container / WorldlyContainer ===

    @Override
    public int[] getSlotsForFace(Direction side) {
        int total = getContainerSize();
        int[] slots = new int[total];
        for (int i = 0; i < total; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if (index < configSlotCount) return false;
        return stack.is(ModItems.MATURE_EMBRYO.get());
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public int getContainerSize() { return configSlotCount + reserveSlotCount; }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : reserveSlots) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        if (index < configSlotCount) return ItemStack.EMPTY;
        int ri = index - configSlotCount;
        if (ri < reserveSlots.size()) return reserveSlots.get(ri);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index < configSlotCount) return ItemStack.EMPTY;
        int ri = index - configSlotCount;
        ItemStack stack = reserveSlots.get(ri);
        if (!stack.isEmpty() && count > 0) {
            ItemStack result = stack.split(count);
            if (stack.isEmpty()) reserveSlots.set(ri, ItemStack.EMPTY);
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index < configSlotCount) return ItemStack.EMPTY;
        int ri = index - configSlotCount;
        ItemStack stack = reserveSlots.get(ri);
        reserveSlots.set(ri, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index >= configSlotCount) {
            int ri = index - configSlotCount;
            if (ri < reserveSlots.size()) {
                reserveSlots.set(ri, stack.copy());
                setChanged();
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < configSlots.size(); i++) configSlots.set(i, null);
        for (int i = 0; i < reserveSlots.size(); i++) reserveSlots.set(i, ItemStack.EMPTY);
        setChanged();
    }

    public void dropAllContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(reserveSlots.size());
        for (int i = 0; i < reserveSlots.size(); i++) {
            container.setItem(i, reserveSlots.get(i));
        }
        Containers.dropContents(level, pos, container);
    }

    // === NBT Persistence ===

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putString("OwnerName", ownerName);

        ListTag configList = new ListTag();
        for (ResourceLocation rl : configSlots) {
            configList.add(StringTag.valueOf(rl != null ? rl.toString() : ""));
        }
        tag.put("ConfigSlots", configList);

        ListTag reserveList = new ListTag();
        for (ItemStack stack : reserveSlots) {
            CompoundTag slotTag = new CompoundTag();
            if (!stack.isEmpty()) stack.save(slotTag);
            reserveList.add(slotTag);
        }
        tag.put("ReserveSlots", reserveList);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
        ownerName = tag.getString("OwnerName");

        configSlots.clear();
        if (tag.contains("ConfigSlots")) {
            ListTag configList = tag.getList("ConfigSlots", Tag.TAG_STRING);
            for (int i = 0; i < configList.size() && i < configSlotCount; i++) {
                String s = configList.getString(i);
                configSlots.add(s.isEmpty() ? null : ResourceLocation.tryParse(s));
            }
        }
        while (configSlots.size() < configSlotCount) configSlots.add(null);

        reserveSlots.clear();
        if (tag.contains("ReserveSlots")) {
            ListTag reserveList = tag.getList("ReserveSlots", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(reserveSlotCount, reserveList.size()); i++) {
                CompoundTag slotTag = reserveList.getCompound(i);
                reserveSlots.add(slotTag.isEmpty() ? ItemStack.EMPTY : ItemStack.of(slotTag));
            }
        }
        while (reserveSlots.size() < reserveSlotCount) reserveSlots.add(ItemStack.EMPTY);
    }

    // === Network Sync ===

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // === Capabilities ===

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return handlers[side.get3DDataValue()].cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel serverLevel) {
            removeChunkTicket(serverLevel);
        }
        for (LazyOptional<IItemHandlerModifiable> h : handlers) {
            if (h != null) h.invalidate();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void invalidateCaps() {
        for (LazyOptional<IItemHandlerModifiable> h : handlers) {
            if (h != null) h.invalidate();
        }
        super.invalidateCaps();
    }
}
