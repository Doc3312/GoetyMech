package com.goetymech.addon.common.block.entity;

import com.Polarice3.Goety.api.entities.IChunkLoader;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetymech.addon.common.utils.GlobalBlockPos;
import com.goetymech.addon.common.utils.GlobalStressLinkManager;
import com.goetymech.addon.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class StressArkBlockEntity extends BlockEntity implements IChunkLoader {

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

        GoetyMechMod.STRESS_LINK.registerArk(GlobalBlockPos.of(level, pos), self.ownerUUID);
        self.refreshStats();
    }

    private void refreshStats() {
        if (tickCounter < 0) {
            tickCounter++;
            return;
        }
        if (++tickCounter < 20) return;
        tickCounter = 0;

        float totalCap = 0;
        float totalStress = 0;
        int readerCount = 0;

        List<GlobalBlockPos> readers = GoetyMechMod.STRESS_LINK.getReadersForOwner(ownerUUID);

        for (GlobalBlockPos readerPos : readers) {
            GlobalStressLinkManager.ReaderSnapshot snap = GoetyMechMod.STRESS_LINK.getCachedReaderData(readerPos);
            if (snap != null) {
                totalCap += snap.capacity();
                totalStress += snap.stress();
                readerCount++;
            } else {
                StressReaderBlockEntity reader = GoetyMechMod.STRESS_LINK.activeReaders.get(readerPos);
                if (reader != null) {
                    totalCap += reader.getNetworkCapacity();
                    totalStress += reader.getNetworkStress();
                    readerCount++;
                }
            }
        }

        cachedTotalCapacity = totalCap;
        cachedTotalStress = totalStress;
        cachedNetworkCount = readerCount;
        float consumption = GoetyMechMod.STRESS_LINK.getEngineConsumptionForOwner(ownerUUID);
        syncToClient(consumption);
    }

    // ── Lifecycle ──

    public void setOwner(LivingEntity entity) {
        this.ownerUUID = entity.getUUID();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.registerArk(GlobalBlockPos.of(level, worldPosition), ownerUUID);
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
            GoetyMechMod.STRESS_LINK.unregisterArk(GlobalBlockPos.of(level, worldPosition));
        }
        sendClearPacket();
        ownerUUID = null;
    }

    // ── Getters for external consumers ──

    public float getCachedTotalCapacity() { return cachedTotalCapacity; }
    public float getCachedTotalStress() { return cachedTotalStress; }
    public int getCachedNetworkCount() { return cachedNetworkCount; }

    // ── Packet ──

    private void syncToClient(float consumption) {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        MinecraftServer server = level.getServer();
        if (server == null) return;
        PlayerList players = server.getPlayerList();
        Player player = players.getPlayer(ownerUUID);
        if (player instanceof ServerPlayer sp) {
            ModNetwork.sendTo(sp, new S2CStressArkDataPacket(
                    cachedTotalCapacity, cachedTotalStress, cachedNetworkCount, consumption));
        }
    }

    private void sendClearPacket() {
        if (level == null || level.isClientSide || ownerUUID == null) return;
        MinecraftServer server = level.getServer();
        if (server == null) return;
        PlayerList players = server.getPlayerList();
        Player player = players.getPlayer(ownerUUID);
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
        tickCounter = 0;
    }
}
