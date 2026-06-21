package com.goetypunk.addon.common.block.entity;

import com.Polarice3.Goety.api.entities.IChunkLoader;
import com.goetypunk.addon.common.net.ModNetwork;
import com.goetypunk.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetypunk.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.KineticNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

    private static final Map<UUID, BlockPos> PLAYER_ACTIVE_ARK = new HashMap<>();

    @Nullable private UUID ownerUUID;
    private float cachedTotalCapacity;
    private float cachedTotalStress;
    private int cachedNetworkCount;
    private int tickCounter;
    private boolean forceRefresh = true;
    private boolean hasEverSynced;
    public long ticketTime;

    public StressArkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STRESS_ARK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StressArkBlockEntity self) {
        if (!(level instanceof ServerLevel)) return;
        self.chunkLoadBlock();
        if (self.ownerUUID == null) return;
        if (!Objects.equals(PLAYER_ACTIVE_ARK.get(self.ownerUUID), pos)) {
            BlockPos active = PLAYER_ACTIVE_ARK.get(self.ownerUUID);
            if (active == null) {
                PLAYER_ACTIVE_ARK.put(self.ownerUUID, pos);
            } else {
                return;
            }
        }
        self.recomputeIfNeeded();
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

        Set<KineticNetwork> seen = new HashSet<>();
        float totalCap = 0, totalStress = 0;
        int netCount = 0;

        synchronized (StressReaderBlockEntity.ACTIVE_READERS) {
            for (StressReaderBlockEntity reader : StressReaderBlockEntity.ACTIVE_READERS) {
                if (reader.hasNetwork() && reader.getLevel() != null && reader.getLevel().isLoaded(reader.getBlockPos())) {
                    KineticNetwork net = reader.getOrCreateNetwork();
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
    }

    private void syncToClient() {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        Player player = level.getPlayerByUUID(ownerUUID);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CStressArkDataPacket(cachedTotalCapacity, cachedTotalStress, cachedNetworkCount));
        }
    }

    private void sendClearPacket() {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        Player player = level.getPlayerByUUID(ownerUUID);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CStressArkDataPacket(-1, -1, 0));
        }
    }

    public void setOwner(LivingEntity entity) {
        this.ownerUUID = entity.getUUID();
        PLAYER_ACTIVE_ARK.put(this.ownerUUID, worldPosition);
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
            if (level != null && !level.isClientSide) {
                PLAYER_ACTIVE_ARK.putIfAbsent(ownerUUID, worldPosition);
            }
        }
        forceRefresh = true;
    }
}
