package com.goetymech.addon.common.block.entity;

import java.util.List;
import java.util.UUID;

import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteEngineBlockEntity extends GeneratingKineticBlockEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger("TEST");

    public enum BindingType {
        NONE, ARK, READER
    }

    private BindingType bindingType = BindingType.NONE;

    @Nullable private BlockPos arkPos;
    @Nullable private ResourceKey<Level> arkDim;

    @Nullable private BlockPos linkedPos;
    @Nullable private ResourceKey<Level> linkedDim;

    @Nullable private UUID placerUUID;

    private float cachedNetworkCapacity;
    private float cachedNetworkStress;
    private float cachedRpm;
    private float cachedConsumed;
    private float cachedArkTotalAvailable;
    private float cachedArkEnginesConsumption;
    private int tickCounter;

    public RemoteEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REMOTE_ENGINE_ENTITY.get(), pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            tryAutoBindArk();
            tryRefreshRpm();
            if (cachedRpm > 0 && getSpeed() == 0)
                updateGeneratedRotation();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level instanceof ServerLevel && ++tickCounter >= 20) {
            tickCounter = 0;
            tryRefreshRpm();
        }
    }

    private void tryAutoBindArk() {
        if (placerUUID == null || level == null) return;

        StressArkBlockEntity.ArkLocation arkLoc = StressArkBlockEntity.getActiveArkLocation(placerUUID);
        if (arkLoc == null) return;

        // Already bound to this exact Ark
        if (bindingType == BindingType.ARK && arkPos != null && arkPos.equals(arkLoc.pos())) return;

        MinecraftServer server = ((ServerLevel) level).getServer();
        ServerLevel arkLevel = server.getLevel(arkLoc.dimension());
        if (arkLevel == null || !arkLevel.isLoaded(arkLoc.pos())) return;

        BlockEntity be = arkLevel.getBlockEntity(arkLoc.pos());
        if (be instanceof StressArkBlockEntity ark) {
            // Clean up old reader binding if switching to Ark
            if (bindingType == BindingType.READER && linkedPos != null && linkedDim != null) {
                ServerLevel targetLevel = server.getLevel(linkedDim);
                if (targetLevel != null && targetLevel.getBlockEntity(linkedPos) instanceof StressReaderBlockEntity reader) {
                    reader.removeConsumer(worldPosition);
                }
            }
            this.arkPos = arkLoc.pos();
            this.arkDim = arkLoc.dimension();
            this.linkedPos = null;
            this.linkedDim = null;
            this.bindingType = BindingType.ARK;
            ark.registerEngine(this.worldPosition);
            setChanged();
        }
    }

    private void tryRefreshRpm() {
        switch (bindingType) {
            case ARK:
                refreshFromArk();
                break;
            case READER:
                refreshFromReader();
                break;
            case NONE:
                break;
        }
    }

    private void refreshFromArk() {
        if (arkPos == null || arkDim == null || level == null) return;
        MinecraftServer server = ((ServerLevel) level).getServer();
        ServerLevel arkLevel = server.getLevel(arkDim);
        if (arkLevel == null || !arkLevel.isLoaded(arkPos)) return;

        BlockEntity be = arkLevel.getBlockEntity(arkPos);
        if (!(be instanceof StressArkBlockEntity ark)) return;

        cachedRpm = ark.getRepresentativeRPM();
        cachedArkTotalAvailable = ark.getTotalAvailableStress();
        cachedArkEnginesConsumption = ark.getTotalConsumedByEngines();
        cachedConsumed = Mth.clamp(this.stress, 0, cachedArkTotalAvailable);
        ark.reportConsumption(this.worldPosition, cachedConsumed);

        updateGeneratedRotation();

        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            setChanged();
            sendData();
        }
    }

    private void refreshFromReader() {
        if (linkedPos == null || linkedDim == null) return;

        MinecraftServer server = ((ServerLevel) level).getServer();
        ServerLevel targetLevel = server.getLevel(linkedDim);
        if (targetLevel == null) return;
        if (!targetLevel.isLoaded(linkedPos)) return;

        BlockEntity be = targetLevel.getBlockEntity(linkedPos);
        if (!(be instanceof StressReaderBlockEntity reader)) return;

        cachedRpm = reader.getSpeed();
        cachedNetworkCapacity = reader.getNetworkCapacity();
        cachedNetworkStress = reader.getNetworkStress();

        float rawAvailable = cachedNetworkCapacity - cachedNetworkStress;
        float trueAvailable = Math.max(0, rawAvailable + reader.getConsumedRemotely());

        cachedConsumed = Mth.clamp(this.stress, 0, trueAvailable);

        reader.setConsumedRemotely(this.worldPosition, cachedConsumed);

        updateGeneratedRotation();

        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            setChanged();
            sendData();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        return (bindingType == BindingType.ARK) ? (arkPos != null ? cachedRpm : 0)
                : (linkedPos != null ? cachedRpm : 0);
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(this.speed);
        if (speed < 0.01f) return 0;
        return ModConfig.REMOTE_ENGINE_STRESS_TO_RPM_RATIO.get().floatValue();
    }

    @Override
    public void invalidate() {
        cleanupConsumption();
        super.invalidate();
    }

    @Override
    public void onChunkUnloaded() {
        cleanupConsumption();
        super.onChunkUnloaded();
    }

    private void cleanupConsumption() {
        if (level == null || level.isClientSide) return;
        MinecraftServer server = ((ServerLevel) level).getServer();

        if (bindingType == BindingType.ARK && arkPos != null && arkDim != null) {
            ServerLevel arkLevel = server.getLevel(arkDim);
            if (arkLevel != null && arkLevel.getBlockEntity(arkPos) instanceof StressArkBlockEntity ark) {
                ark.unregisterEngine(worldPosition);
            }
        } else if (bindingType == BindingType.READER && linkedPos != null && linkedDim != null) {
            ServerLevel targetLevel = server.getLevel(linkedDim);
            if (targetLevel != null && targetLevel.getBlockEntity(linkedPos) instanceof StressReaderBlockEntity reader) {
                reader.removeConsumer(worldPosition);
            }
        }
    }

    public void setLinkedPos(BlockPos pos, ResourceKey<Level> dim) {
        this.bindingType = BindingType.READER;
        this.linkedPos = pos;
        this.linkedDim = dim;
        this.arkPos = null;
        setChanged();
        if (level != null && !level.isClientSide)
            tryRefreshRpm();
    }

    public void setPlacerUUID(UUID uuid) {
        this.placerUUID = uuid;
        setChanged();
    }

    @Override
    public void updateGeneratedRotation() {
        super.updateGeneratedRotation();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled()) return false;
        if (bindingType == BindingType.NONE) return false;

        if (bindingType == BindingType.ARK) {
            CreateLang.text("Remote Engine (Ark)")
                    .style(ChatFormatting.WHITE)
                    .forGoggles(tooltip);

            LangBuilder su = CreateLang.translate("generic.unit.stress");
            float rpm = Math.abs(getSpeed());

            CreateLang.text("RPM: ")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            CreateLang.number(rpm)
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 1);

            CreateLang.text("Source: ")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            CreateLang.number(cachedArkEnginesConsumption)
                    .add(su)
                    .style(ChatFormatting.GREEN)
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(cachedArkTotalAvailable)
                            .add(su).style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

            return true;
        }

        if (linkedPos == null) return false;

        CreateLang.text("Remote Engine: ["
                + linkedPos.getX() + ", "
                + linkedPos.getY() + ", "
                + linkedPos.getZ() + "]")
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);

        LangBuilder su = CreateLang.translate("generic.unit.stress");
        float rpm = Math.abs(getSpeed());
        float cap = cachedNetworkCapacity;
        float consumed = cachedConsumed;

        CreateLang.text("RPM: ")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.number(rpm)
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        CreateLang.text("Source: ")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.number(consumed)
                .add(su)
                .style(ChatFormatting.GREEN)
                .text(ChatFormatting.GRAY, " / ")
                .add(CreateLang.number(cap).add(su).style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putByte("BindingType", (byte) bindingType.ordinal());
        if (placerUUID != null) {
            compound.putUUID("PlacerUUID", placerUUID);
        }
        if (bindingType == BindingType.ARK && arkPos != null && arkDim != null) {
            compound.putInt("ArkX", arkPos.getX());
            compound.putInt("ArkY", arkPos.getY());
            compound.putInt("ArkZ", arkPos.getZ());
            compound.putString("ArkDim", arkDim.location().toString());
        }
        if (bindingType == BindingType.READER && linkedPos != null) {
            compound.putInt("LinkedX", linkedPos.getX());
            compound.putInt("LinkedY", linkedPos.getY());
            compound.putInt("LinkedZ", linkedPos.getZ());
            compound.putString("LinkedDim", linkedDim.location().toString());
        }
        if (!clientPacket) {
            compound.putFloat("CachedCapacity", cachedNetworkCapacity);
            compound.putFloat("CachedStressVal", cachedNetworkStress);
            compound.putFloat("CachedRpm", cachedRpm);
            compound.putFloat("CachedConsumed", cachedConsumed);
        }
        // Sync Ark data to client for tooltip display
        compound.putFloat("ArkTotalAvail", cachedArkTotalAvailable);
        compound.putFloat("ArkEnginesConsumption", cachedArkEnginesConsumption);
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        int bt = compound.getByte("BindingType") & 0xFF;
        bindingType = bt < BindingType.values().length ? BindingType.values()[bt] : BindingType.NONE;

        if (compound.hasUUID("PlacerUUID")) {
            placerUUID = compound.getUUID("PlacerUUID");
        }

        arkPos = null;
        arkDim = null;
        linkedPos = null;
        linkedDim = null;

        if (bindingType == BindingType.ARK && compound.contains("ArkX")) {
            arkPos = new BlockPos(compound.getInt("ArkX"),
                    compound.getInt("ArkY"),
                    compound.getInt("ArkZ"));
            if (compound.contains("ArkDim")) {
                arkDim = ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.tryParse(compound.getString("ArkDim")));
            }
        }

        if (bindingType == BindingType.READER && compound.contains("LinkedX")) {
            linkedPos = new BlockPos(compound.getInt("LinkedX"),
                    compound.getInt("LinkedY"),
                    compound.getInt("LinkedZ"));
            linkedDim = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.tryParse(compound.getString("LinkedDim")));
        }

        if (!clientPacket) {
            cachedNetworkCapacity = compound.getFloat("CachedCapacity");
            cachedNetworkStress = compound.getFloat("CachedStressVal");
            cachedRpm = compound.getFloat("CachedRpm");
            cachedConsumed = compound.getFloat("CachedConsumed");
        }
        cachedArkTotalAvailable = compound.getFloat("ArkTotalAvail");
        cachedArkEnginesConsumption = compound.getFloat("ArkEnginesConsumption");
    }

    private static String fmtPos(BlockPos pos) {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
    }
}
