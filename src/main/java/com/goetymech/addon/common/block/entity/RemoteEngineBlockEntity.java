package com.goetymech.addon.common.block.entity;

import java.util.List;
import java.util.UUID;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.utils.GlobalBlockPos;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class RemoteEngineBlockEntity extends GeneratingKineticBlockEntity {

    private static final Logger LOGGER = LogManager.getLogger("StressLinkDebug");

    @Nullable private BlockPos linkedPos;
    @Nullable private ResourceKey<Level> linkedDim;

    @Nullable private UUID placerUUID;

    private float remoteSpeed;
    private float remoteBudget;
    private float remoteCapacity;
    // Reader 网络的已使用应力（含本地消耗 + 所有远程引擎消耗），用于 Goggle 显示网络应力状况
    private float remoteReaderStress;

    public RemoteEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REMOTE_ENGINE_ENTITY.get(), pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.onEngineLoad(this);
            KineticNetwork network = getOrCreateNetwork();
            if (network != null) {
                GoetyMechMod.STRESS_LINK.onEngineNetworkChanged(
                        GlobalBlockPos.of(level, worldPosition),
                        network.calculateStress(),
                        network.calculateCapacity());
            }
            if (linkedPos == null && placerUUID != null) {
                tryAutoBindArk(placerUUID);
            }
        }
    }

    @Override
    public void invalidate() {
        if (level != null && !level.isClientSide) {
            GlobalBlockPos gpos = GlobalBlockPos.of(level, worldPosition);
            GoetyMechMod.STRESS_LINK.unbind(gpos);
            GoetyMechMod.STRESS_LINK.onEngineUnload(gpos);
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.onEngineUnload(GlobalBlockPos.of(level, worldPosition));
        }
    }

    public void applyRemoteUpdate(float speed, float budget, float capacity, float readerStress) {
        this.remoteSpeed = Math.abs(speed);
        this.remoteBudget = Math.max(0, budget);
        this.remoteCapacity = Math.max(0, capacity);
        this.remoteReaderStress = Math.max(0, readerStress);
        updateGeneratedRotation();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            setChanged();
            sendData();
        }
    }

    @Override
    public void updateFromNetwork(float capacity, float stress, int networkSize) {
        super.updateFromNetwork(capacity, stress, networkSize);
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.onEngineNetworkChanged(
                    GlobalBlockPos.of(level, worldPosition),
                    this.stress,
                    this.capacity);
        }
    }

    @Override
    public float getGeneratedSpeed() {
        if (linkedPos != null) return remoteSpeed;
        return 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float s = Math.abs(getGeneratedSpeed());
        if (s < 0.01f) return 0;
        return remoteBudget / s;
    }

    public void setLinkedPos(BlockPos pos, ResourceKey<Level> dim) {
        this.linkedPos = pos;
        this.linkedDim = dim;
        setChanged();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.bind(GlobalBlockPos.of(level, worldPosition),
                    new GlobalBlockPos(dim, pos));
        }
    }

    public void clearLinkedPos() {
        this.linkedPos = null;
        this.linkedDim = null;
        this.remoteSpeed = 0;
        this.remoteBudget = 0;
        this.remoteCapacity = 0;
        this.remoteReaderStress = 0;
        setChanged();
        if (level != null && !level.isClientSide) {
            GoetyMechMod.STRESS_LINK.unbind(GlobalBlockPos.of(level, worldPosition));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            sendData();
        }
    }

    @Nullable
    public UUID getPlacerUUID() { return placerUUID; }

    public void setPlacerUUID(UUID uuid) {
        this.placerUUID = uuid;
        setChanged();
    }

    private void tryAutoBindArk(UUID ownerUUID) {
        if (linkedPos != null || level == null || level.isClientSide) return;
        List<GlobalBlockPos> readers = GoetyMechMod.STRESS_LINK.getReadersForOwner(ownerUUID);
        if (!readers.isEmpty()) {
            GlobalBlockPos first = readers.get(0);
            setLinkedPos(first.pos(), first.dimension());
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled()) return false;

        LangBuilder su = CreateLang.translate("generic.unit.stress");
        float rpm = Math.abs(getSpeed());

        CreateLang.text("RPM: ")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.number(rpm)
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        if (linkedPos != null) {
            float localStr = this.stress;
            float localCap = this.capacity;
            boolean over = localCap > 0 && localStr > localCap;
            // 已分配应力：引擎网络的消耗/容量（原「本地消耗」改名，语义更清晰）
            CreateLang.text("已分配应力: ")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            CreateLang.number(localStr)
                    .add(su)
                    .style(over ? ChatFormatting.RED : ChatFormatting.GOLD)
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(localCap)
                            .add(su)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

            // 网络应力：Reader 网络总使用/总容量（含本地消耗 + 所有远程引擎消耗）
            float netStr = this.remoteReaderStress;
            float netCap = this.remoteCapacity;
            boolean netOver = netCap > 0 && netStr > netCap;
            CreateLang.text("网络应力: ")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            CreateLang.number(netStr)
                    .add(su)
                    .style(netOver ? ChatFormatting.RED : ChatFormatting.AQUA)
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(netCap)
                            .add(su)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);
        }

        return true;
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (linkedPos != null) {
            compound.putInt("LinkedX", linkedPos.getX());
            compound.putInt("LinkedY", linkedPos.getY());
            compound.putInt("LinkedZ", linkedPos.getZ());
            if (linkedDim != null) {
                compound.putString("LinkedDim", linkedDim.location().toString());
            }
        }
        compound.putFloat("RemoteSpeed", remoteSpeed);
        compound.putFloat("RemoteBudget", remoteBudget);
        compound.putFloat("RemoteCapacity", remoteCapacity);
        compound.putFloat("RemoteReaderStress", remoteReaderStress);
        if (placerUUID != null) {
            compound.putUUID("PlacerUUID", placerUUID);
        }
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        linkedPos = null;
        linkedDim = null;
        if (compound.contains("LinkedX")) {
            linkedPos = new BlockPos(compound.getInt("LinkedX"),
                    compound.getInt("LinkedY"),
                    compound.getInt("LinkedZ"));
            if (compound.contains("LinkedDim")) {
                linkedDim = ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.tryParse(compound.getString("LinkedDim")));
            }
        }
        remoteSpeed = compound.getFloat("RemoteSpeed");
        remoteBudget = compound.getFloat("RemoteBudget");
        remoteCapacity = compound.getFloat("RemoteCapacity");
        remoteReaderStress = compound.getFloat("RemoteReaderStress");
        if (compound.hasUUID("PlacerUUID")) {
            placerUUID = compound.getUUID("PlacerUUID");
        } else {
            placerUUID = null;
        }
    }
}
