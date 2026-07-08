package com.goetymech.addon.common.block.entity;

import java.util.*;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.utils.GlobalBlockPos;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class StressReaderBlockEntity extends KineticBlockEntity {

    @Nullable private UUID placerUUID;
    private float remoteEngineConsumption;

    public StressReaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STRESS_READER_ENTITY.get(), pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.onReaderLoad(this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.onReaderDestroyed(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.onReaderUnload(
                    GlobalBlockPos.of(level, worldPosition));
        }
    }

    @Override
    public void updateFromNetwork(float capacity, float stress, int networkSize) {
        float prevCap = this.capacity;
        float prevStress = this.stress;
        super.updateFromNetwork(capacity, stress, networkSize);
        if (level != null && !level.isClientSide
                && (prevCap != this.capacity || prevStress != this.stress)) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            setChanged();
            GoetyMechMod.STRESS_LINK.onReaderNetworkChanged(this);
        }
    }

    public float getNetworkCapacity() { return capacity; }

    public float getNetworkStress() { return stress; }

    public float getAvailableStress() { return Math.max(0, capacity - getNetworkStress()); }

    public float getRemoteEngineConsumption() { return remoteEngineConsumption; }

    public void setRemoteEngineConsumption(float consumption) {
        if (Float.compare(this.remoteEngineConsumption, consumption) == 0) {
            return;
        }
        this.remoteEngineConsumption = consumption;
        setChanged();
        if (level != null && !level.isClientSide) {
            if (hasNetwork()) {
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
            }
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            sendData();
        }
    }

    @Override
    public float calculateStressApplied() {
        float speed = Math.abs(getTheoreticalSpeed());
        if (speed < 0.01f) {
            this.lastStressApplied = 0;
            return 0;
        }
        float impact = remoteEngineConsumption / speed;
        this.lastStressApplied = impact;
        return impact;
    }

    @Nullable
    public UUID getPlacerUUID() { return placerUUID; }

    public void setPlacerUUID(UUID uuid) {
        this.placerUUID = uuid;
        setChanged();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.registerReaderOwner(
                    GlobalBlockPos.of(level, worldPosition),
                    uuid);
        }
    }

    public boolean isOverStressed() { return overStressed; }
    public float getCurrentSpeed() { return getSpeed(); }

    public int getComparatorOutput() {
        if (capacity <= 0) return 0;
        float ratio = getAvailableStress() / capacity;
        return Mth.clamp((int)(ratio * 15), 0, 15);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (placerUUID != null) {
            compound.putUUID("PlacerUUID", placerUUID);
        }
        compound.putFloat("RemoteEngineConsumption", remoteEngineConsumption);
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (compound.hasUUID("PlacerUUID")) {
            placerUUID = compound.getUUID("PlacerUUID");
        } else {
            placerUUID = null;
        }
        remoteEngineConsumption = compound.getFloat("RemoteEngineConsumption");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled())
            return false;

        float cap = getNetworkCapacity();
        float netStress = getNetworkStress();
        float fraction = cap == 0 ? 0 : netStress / cap;

        if (getTheoreticalSpeed() == 0) {
            CreateLang.text(com.simibubi.create.foundation.item.TooltipHelper.makeProgressBar(3, 0))
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
