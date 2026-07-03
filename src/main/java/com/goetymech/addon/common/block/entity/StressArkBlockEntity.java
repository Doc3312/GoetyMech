package com.goetymech.addon.common.block.entity;

import com.Polarice3.Goety.api.entities.IChunkLoader;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetymech.addon.common.utils.GlobalBlockPos;
import com.goetymech.addon.common.utils.StressLinkService;
import com.goetymech.addon.init.ModBlocks;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StressArkBlockEntity extends BlockEntity implements IChunkLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("TEST");

    @Nullable private UUID ownerUUID;
    private float cachedTotalCapacity;
    private float cachedTotalStress;
    private int cachedNetworkCount;
    private int tickCounter;
    public long ticketTime;

    public StressArkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STRESS_ARK_ENTITY.get(), pos, state);
    }

    // ── Tick ──

    public static void tick(Level level, BlockPos pos, BlockState state, StressArkBlockEntity self) {
        if (!(level instanceof ServerLevel)) return;
        self.chunkLoadBlock();
        if (self.ownerUUID == null) return;

        StressLinkService service = StressLinkService.get(level);
        if (service == null) return;

        // Register this Ark — all Arks for one owner are active simultaneously
        service.registerArk(GlobalBlockPos.of(level, pos), self.ownerUUID);
        self.refreshStats(service);
    }

    // ── Stats via Service cache (no direct getBlockEntity) ──

    private void refreshStats(StressLinkService service) {
        if (++tickCounter < 20) return;
        tickCounter = 0;

        float totalCap = 0, totalStress = 0;
        int readerCount = 0;

        for (GlobalBlockPos readerPos : service.getReadersForOwner(ownerUUID)) {
            StressLinkService.ReaderSnapshot snap = service.getCachedReaderData(readerPos);
            if (snap == null) continue;
            totalCap += snap.capacity();
            totalStress += snap.stress();
            readerCount++;
        }

        boolean changed = Math.abs(totalCap - cachedTotalCapacity) > 0.01f
                || Math.abs(totalStress - cachedTotalStress) > 0.01f
                || readerCount != cachedNetworkCount;

        if (changed) {
            cachedTotalCapacity = totalCap;
            cachedTotalStress = totalStress;
            cachedNetworkCount = readerCount;
            syncToClient();
        }
    }

    // ── Lifecycle ──

    public void setOwner(LivingEntity entity) {
        this.ownerUUID = entity.getUUID();
        if (level != null && !level.isClientSide) {
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.registerArk(GlobalBlockPos.of(level, worldPosition), ownerUUID);
            }
        }
        setChanged();
    }

    public boolean isOwner(Player player) {
        return ownerUUID != null && player.getUUID().equals(ownerUUID);
    }

    @Nullable
    public UUID getOwnerUUID() { return ownerUUID; }

    public void onArkRemoved() {
        if (level != null && !level.isClientSide) {
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.unregisterArk(GlobalBlockPos.of(level, worldPosition));
            }
        }
        sendClearPacket();
        ownerUUID = null;
    }

    // ── Getters for external consumers ──

    public float getCachedTotalCapacity() { return cachedTotalCapacity; }
    public float getCachedTotalStress() { return cachedTotalStress; }
    public int getCachedNetworkCount() { return cachedNetworkCount; }

    // ── Packet ──

    private void syncToClient() {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        Player player = level.getPlayerByUUID(ownerUUID);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CStressArkDataPacket(
                    cachedTotalCapacity, cachedTotalStress, cachedNetworkCount, 0));
        }
    }

    private void sendClearPacket() {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        Player player = level.getPlayerByUUID(ownerUUID);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CStressArkDataPacket(-1, -1, 0, 0));
        }
    }

    // ── Chunk loader ──

    @Override public boolean shouldChunkLoad() { return true; }
    @Override public long getTicketTime() { return this.ticketTime; }
    @Override public void setTicketTime(long ticketTime) { this.ticketTime = ticketTime; }
    @Override public int selfLoadRadius() { return 5; }

    // ── NBT ──

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
    }
}
