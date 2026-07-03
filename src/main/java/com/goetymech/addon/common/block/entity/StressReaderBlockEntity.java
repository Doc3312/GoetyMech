package com.goetymech.addon.common.block.entity;

import java.util.*;

import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.item.TooltipHelper;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StressReaderBlockEntity extends KineticBlockEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger("TEST");

    public static final Set<StressReaderBlockEntity> ACTIVE_READERS = Collections.synchronizedSet(new HashSet<>());
    public static volatile boolean NETWORK_UPDATED = false;

    private final Map<BlockPos, ConsumerEntry> remoteConsumers = new HashMap<>();

    private static class ConsumerEntry {
        float amount;
        long lastSeenTick;
        ConsumerEntry(float amount, long lastSeenTick) {
            this.amount = amount;
            this.lastSeenTick = lastSeenTick;
        }
    }

    public StressReaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STRESS_READER_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            ACTIVE_READERS.add(this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        ACTIVE_READERS.remove(this);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        ACTIVE_READERS.remove(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide && level.getGameTime() % 100 == 0) {
            purgeStaleEntries();
        }
    }

    public float getNetworkCapacity() { return capacity; }

    public float getNetworkStress() { return stress; }

    public float getAvailableStress() { return Math.max(0, capacity - stress); }

    @Override
    public float calculateStressApplied() {
        float s = Math.abs(getTheoreticalSpeed());
        if (s < 0.01f) return 0;
        return getConsumedRemotely() / s;
    }

    public float getAvailableFor(BlockPos enginePos) {
        float selfAmount = 0;
        ConsumerEntry entry = remoteConsumers.get(enginePos);
        if (entry != null) selfAmount = entry.amount;
        return Math.max(0, getNetworkCapacity() - getNetworkStress() + selfAmount);
    }

    public void setConsumedRemotely(BlockPos enginePos, float amount) {
        purgeStaleEntries();
        if (Math.abs(amount) < 0.001f) {
            remoteConsumers.remove(enginePos);
        } else {
            remoteConsumers.put(enginePos, new ConsumerEntry(amount,
                    level != null ? level.getGameTime() : 0));
        }
        float newTotal = getConsumedRemotely();
        LOGGER.debug("[TEST] SR{} REMOTE | engines={} | total={} | speed={} | hasNet={}",
                fmtPos(worldPosition), remoteConsumers.size(), newTotal, getSpeed(), hasNetwork());
        if (level != null && !level.isClientSide) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            if (hasNetwork())
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
        }
    }

    public void removeConsumer(BlockPos enginePos) {
        float oldTotal = getConsumedRemotely();
        remoteConsumers.remove(enginePos);
        float newTotal = getConsumedRemotely();
        if (Math.abs(oldTotal - newTotal) <= 0.001f) return;
        if (level != null && !level.isClientSide) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            if (hasNetwork())
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
        }
    }

    public float getConsumedRemotely() {
        return (float) remoteConsumers.values().stream()
                .mapToDouble(e -> e.amount).sum();
    }

    private void purgeStaleEntries() {
        if (level == null) return;
        long now = level.getGameTime();
        boolean removed = remoteConsumers.entrySet().removeIf(e -> now - e.getValue().lastSeenTick > 100);
        if (removed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            if (hasNetwork())
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
        }
    }

    private static String fmtPos(BlockPos pos) {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
    }

    public boolean isOverStressed() { return overStressed; }

    public float getCurrentSpeed() { return getSpeed(); }

    public int getComparatorOutput() {
        if (capacity <= 0) return 0;
        float ratio = getAvailableStress() / capacity;
        return Mth.clamp((int)(ratio * 15), 0, 15);
    }

    @Override
    public void updateFromNetwork(float capacity, float stress, int networkSize) {
        float prevCapacity = this.capacity;
        float prevStress = this.stress;
        super.updateFromNetwork(capacity, stress, networkSize);
        LOGGER.debug("[TEST] SR{} NET | cap={}{} | stress={}{} | consumedRemotely={} | networkSize={}",
                fmtPos(worldPosition),
                this.capacity, (prevCapacity != this.capacity ? " (was " + prevCapacity + ")" : ""),
                this.stress, (prevStress != this.stress ? " (was " + prevStress + ")" : ""),
                getConsumedRemotely(), networkSize);
        if ((prevCapacity != this.capacity || prevStress != this.stress) && level != null && !level.isClientSide) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            setChanged();
            NETWORK_UPDATED = true;
        }
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (!clientPacket) {
            ListTag list = new ListTag();
            for (var entry : remoteConsumers.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putLong("Pos", entry.getKey().asLong());
                tag.putFloat("Amount", entry.getValue().amount);
                tag.putLong("LastSeen", entry.getValue().lastSeenTick);
                list.add(tag);
            }
            compound.put("RemoteConsumers", list);
        }
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        remoteConsumers.clear();
        if (!clientPacket && compound.contains("RemoteConsumers")) {
            ListTag list = compound.getList("RemoteConsumers", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                BlockPos pos = BlockPos.of(tag.getLong("Pos"));
                float amount = tag.getFloat("Amount");
                long lastSeen = tag.getLong("LastSeen");
                remoteConsumers.put(pos, new ConsumerEntry(amount, lastSeen));
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled())
            return false;

        float cap = getNetworkCapacity();
        float netStress = getNetworkStress();
        float fraction = cap == 0 ? 0 : netStress / cap;

        if (getTheoreticalSpeed() == 0) {
            CreateLang.text(TooltipHelper.makeProgressBar(3, 0))
                .translate("gui.stressometer.no_rotation")
                .style(ChatFormatting.DARK_GRAY)
                .forGoggles(tooltip);
        } else {
            StressImpact.getFormattedStressText(fraction)
                .forGoggles(tooltip);

            CreateLang.translate("gui.stressometer.capacity")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

            float remaining = Math.max(0, cap - netStress);
            LangBuilder su = CreateLang.translate("generic.unit.stress");
            LangBuilder stressTip = CreateLang.number(remaining)
                .add(su)
                .style(StressImpact.of(fraction).getRelativeColor());

            if (remaining != cap)
                stressTip.text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(cap)
                        .add(su)
                        .style(ChatFormatting.DARK_GRAY));

            stressTip.forGoggles(tooltip, 1);
        }

        return true;
    }
}
