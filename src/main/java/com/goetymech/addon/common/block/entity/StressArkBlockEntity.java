package com.goetymech.addon.common.block.entity;

import com.Polarice3.Goety.api.entities.IChunkLoader;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetymech.addon.common.utils.GlobalBlockPos;
import com.goetymech.addon.common.utils.StressLinkService;
import com.goetymech.addon.init.ModBlocks;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StressArkBlockEntity extends BlockEntity implements IChunkLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("TEST");

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
    public long ticketTime;

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
        self.refreshStats();
    }

    public int getEngineCount() { return 0; }

    public float getRepresentativeRPM() {
        StressLinkService service = StressLinkService.get(level);
        if (service == null || ownerUUID == null) return 0;
        float bestRpm = 0;
        float bestAvailable = -1;
        for (GlobalBlockPos readerPos : service.getReadersForOwner(ownerUUID)) {
            BlockEntity be = level instanceof ServerLevel sl
                    ? sl.getBlockEntity(readerPos.pos())
                    : null;
            if (be instanceof StressReaderBlockEntity reader) {
                float avail = reader.getAvailableStress();
                if (avail > bestAvailable) {
                    bestAvailable = avail;
                    bestRpm = reader.getSpeed();
                }
            }
        }
        return bestRpm;
    }

    public float getTotalAvailableStress() {
        StressLinkService service = StressLinkService.get(level);
        if (service == null || ownerUUID == null) return 0;
        float total = 0;
        for (GlobalBlockPos readerPos : service.getReadersForOwner(ownerUUID)) {
            BlockEntity be = level instanceof ServerLevel sl
                    ? sl.getBlockEntity(readerPos.pos())
                    : null;
            if (be instanceof StressReaderBlockEntity reader) {
                total += reader.getAvailableStress();
            }
        }
        return total;
    }

    public float getTotalConsumedByEngines() { return 0; }

    public float getRawAvailableStress() { return getTotalAvailableStress(); }

    private void refreshStats() {
        if (++tickCounter < 20) return;
        tickCounter = 0;

        StressLinkService service = StressLinkService.get(level);
        if (service == null || ownerUUID == null) return;

        float totalCap = 0, totalStress = 0;
        int netCount = 0;
        Set<String> seenNetworks = new HashSet<>();
        for (GlobalBlockPos readerPos : service.getReadersForOwner(ownerUUID)) {
            BlockEntity be = level instanceof ServerLevel sl
                    ? sl.getBlockEntity(readerPos.pos())
                    : null;
            if (be instanceof StressReaderBlockEntity reader && reader.hasNetwork()) {
                float cap = reader.getNetworkCapacity();
                float stress = reader.getNetworkStress();
                totalCap += cap;
                totalStress += stress;
                if (!seenNetworks.contains(readerPos.dimension().location() + "@" + readerPos.pos())) {
                    seenNetworks.add(readerPos.dimension().location() + "@" + readerPos.pos());
                    netCount++;
                }
            }
        }

        boolean changed = Math.abs(totalCap - cachedTotalCapacity) > 0.01f
                || Math.abs(totalStress - cachedTotalStress) > 0.01f
                || netCount != cachedNetworkCount;

        if (changed) {
            cachedTotalCapacity = totalCap;
            cachedTotalStress = totalStress;
            cachedNetworkCount = netCount;
            syncToClient();
        }
    }

    private void syncToClient() {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        Player player = level.getPlayerByUUID(ownerUUID);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CStressArkDataPacket(cachedTotalCapacity, cachedTotalStress,
                    cachedNetworkCount, 0));
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
                PLAYER_ACTIVE_ARK.putIfAbsent(ownerUUID, new ArkLocation(level.dimension(), worldPosition));
            }
        }
    }

    private static String fmtPos(BlockPos pos) {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
    }
}
