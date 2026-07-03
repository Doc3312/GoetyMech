package com.goetymech.addon.common.block.entity;

import java.util.List;
import java.util.UUID;

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
            LOGGER.debug("[REMOTE_ENGINE] init at {}: bindingType={} cachedRpm={} getSpeed={} hasNetwork={} stress={}",
                    worldPosition, bindingType, cachedRpm, getSpeed(), hasNetwork(), stress);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level instanceof ServerLevel && ++tickCounter >= 20) {
            tickCounter = 0;
            tryRefreshRpm();
            LOGGER.warn("[TICK] ENGINE {} ARK={} cap={} stress={} speed={} thSpeed={} over={} genSpeed={}",
                    fmtPos(worldPosition), bindingType, this.capacity, this.stress, this.speed, getTheoreticalSpeed(), overStressed, getGeneratedSpeed());
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
        if (arkLevel == null || !arkLevel.isLoaded(arkPos)) {
            LOGGER.warn("[ARK_BE] ENGINE{} arkLevel={} loaded={}", fmtPos(worldPosition), arkLevel != null, arkLevel != null && arkLevel.isLoaded(arkPos));
            return;
        }

        BlockEntity be = arkLevel.getBlockEntity(arkPos);
        if (!(be instanceof StressArkBlockEntity ark)) {
            LOGGER.warn("[ARK_BE] ENGINE{} expected StressArkBlockEntity but got {}", fmtPos(worldPosition), be != null ? be.getClass().getSimpleName() : "null");
            return;
        }

        cachedRpm = ark.getRepresentativeRPM();
        cachedArkTotalAvailable = ark.getTotalAvailableStress();
        cachedArkEnginesConsumption = ark.getTotalConsumedByEngines();

        float rawTotal = ark.getRawAvailableStress();
        int engineCount = ark.getEngineCount();
        float prevConsumed = cachedConsumed;
        cachedConsumed = engineCount > 0 ? Math.max(0, rawTotal) / engineCount : 0;
        ark.reportConsumption(this.worldPosition, cachedConsumed);
        LOGGER.warn("[ARK_REF] ENGINE {} availDisp={} availRaw={} engineCount={} prevConsumed={} newConsumed={} rpm={}",
                fmtPos(worldPosition), (int)cachedArkTotalAvailable, (int)rawTotal, engineCount, (int)prevConsumed, (int)cachedConsumed, cachedRpm);

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

        float rawAvailable = reader.getAvailableFor(this.worldPosition);
        float prevConsumed = cachedConsumed;
        cachedConsumed = Math.max(rawAvailable, 0);
        LOGGER.warn("[READER_REF] ENGINE {} Reader{} cap={} stress={} availForMe={} prevConsumed={} newConsumed={} rpm={}",
                fmtPos(worldPosition), fmtPos(linkedPos), (int)cachedNetworkCapacity, (int)cachedNetworkStress,
                (int)rawAvailable, (int)prevConsumed, (int)cachedConsumed, cachedRpm);

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
        if (speed < 0.01f) {
            LOGGER.warn("[CALC_CAP] ENGINE {} speed=0 -> return 0", fmtPos(worldPosition));
            return 0;
        }
        if (bindingType != BindingType.NONE && cachedConsumed > 0) {
            float result = cachedConsumed / speed;
            LOGGER.warn("[CALC_CAP] ENGINE {} cachedConsumed={} speed={} -> result={}", fmtPos(worldPosition), (int)cachedConsumed, speed, (int)result);
            return result;
        }
        LOGGER.warn("[CALC_CAP] ENGINE {} binding={} cachedConsumed={} -> return 0", fmtPos(worldPosition), bindingType, (int)cachedConsumed);
        return 0;
    }

    @Override
    public void invalidate() {
        LOGGER.warn("[INVALIDATE] ENGINE{} binding={}", fmtPos(worldPosition), bindingType);
        cleanupConsumption("invalidate");
        super.invalidate();
    }

    @Override
    public void onChunkUnloaded() {
        LOGGER.warn("[CHUNK_UNLOAD] ENGINE{} binding={}", fmtPos(worldPosition), bindingType);
        cleanupConsumption("chunk_unload");
        super.onChunkUnloaded();
    }

    private void cleanupConsumption(String source) {
        if (level == null || level.isClientSide) return;
        MinecraftServer server = ((ServerLevel) level).getServer();

        if (bindingType == BindingType.ARK && arkPos != null && arkDim != null) {
            ServerLevel arkLevel = server.getLevel(arkDim);
            if (arkLevel != null && arkLevel.getBlockEntity(arkPos) instanceof StressArkBlockEntity ark) {
                ark.unregisterEngine(worldPosition);
            } else {
                LOGGER.warn("[CLEANUP] ENGINE{} src={} arkLevelExists={} arkBE_found={}", fmtPos(worldPosition), source, arkLevel != null,
                        arkLevel != null && arkLevel.getBlockEntity(arkPos) instanceof StressArkBlockEntity);
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

    public void clearLinkedPos() {
        if (bindingType == BindingType.READER && level != null && !level.isClientSide) {
            MinecraftServer server = ((ServerLevel) level).getServer();
            if (linkedDim != null) {
                ServerLevel targetLevel = server.getLevel(linkedDim);
                if (targetLevel != null && targetLevel.getBlockEntity(linkedPos) instanceof StressReaderBlockEntity reader) {
                    reader.removeConsumer(worldPosition);
                }
            }
        }
        this.bindingType = BindingType.NONE;
        this.linkedPos = null;
        this.linkedDim = null;
        this.arkPos = null;
        this.arkDim = null;
        this.cachedConsumed = 0;
        this.cachedRpm = 0;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            sendData();
        }
    }

    @Override
    public void updateGeneratedRotation() {
        super.updateGeneratedRotation();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled()) return false;
        if (bindingType == BindingType.NONE) return false;

        LangBuilder su = CreateLang.translate("generic.unit.stress");
        float rpm = Math.abs(getSpeed());

        CreateLang.text("RPM: ")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.number(rpm)
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        if (bindingType == BindingType.ARK) {
            float total = Math.max(0, cachedArkTotalAvailable);
            float used = cachedArkEnginesConsumption;
            float remaining = Math.max(0, total - used);

            CreateLang.translate("gui.stressometer.capacity")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            float displayUsed = Math.max(used, 0);
            CreateLang.number(displayUsed)
                    .add(su)
                    .style(used > total ? ChatFormatting.RED : ChatFormatting.GREEN)
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(total)
                            .add(su).style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

            return true;
        }

        if (linkedPos == null) return false;

        float rawAvailable = cachedNetworkCapacity;
        float rawStress = cachedNetworkStress;
        float total = Math.max(0, rawAvailable - rawStress + cachedConsumed);
        float consumed = cachedConsumed;
        float remaining = Math.max(0, rawAvailable - rawStress);

        CreateLang.translate("gui.stressometer.capacity")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CreateLang.number(consumed)
                .add(su)
                .style(consumed > remaining + consumed ? ChatFormatting.RED : ChatFormatting.GREEN)
                .text(ChatFormatting.GRAY, " / ")
                .add(CreateLang.number(total)
                        .add(su).style(ChatFormatting.DARK_GRAY))
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
