package com.goetymech.addon.common.block.entity;

import java.util.List;
import java.util.UUID;

import com.goetymech.addon.common.utils.GlobalBlockPos;
import com.goetymech.addon.common.utils.StressLinkService;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class RemoteEngineBlockEntity extends GeneratingKineticBlockEntity {

    @Nullable private BlockPos linkedPos;
    @Nullable private ResourceKey<Level> linkedDim;
    @Nullable private UUID placerUUID;

    private float remoteSpeed;
    private float remoteBudget;

    public RemoteEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REMOTE_ENGINE_ENTITY.get(), pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide && linkedPos != null && linkedDim != null) {
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.onEngineLoad(this);
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (level != null && !level.isClientSide) {
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.onEngineUnload(GlobalBlockPos.of(level, worldPosition));
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.onEngineUnload(GlobalBlockPos.of(level, worldPosition));
            }
        }
    }

    public void applyRemoteUpdate(float speed, float budget) {
        this.remoteSpeed = Math.abs(speed);
        this.remoteBudget = Math.max(0, budget);
        updateGeneratedRotation();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            setChanged();
            sendData();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        return (linkedPos != null && linkedDim != null) ? remoteSpeed : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float s = Math.abs(remoteSpeed);
        if (s < 0.01f || linkedPos == null) return 0;
        return remoteBudget / s;
    }

    public void setLinkedPos(BlockPos pos, ResourceKey<Level> dim) {
        this.linkedPos = pos;
        this.linkedDim = dim;
        setChanged();
        if (level != null && !level.isClientSide) {
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.bind(GlobalBlockPos.of(level, worldPosition),
                        new GlobalBlockPos(dim, pos));
            }
        }
    }

    public void setPlacerUUID(UUID uuid) {
        this.placerUUID = uuid;
        setChanged();
    }

    public void clearLinkedPos() {
        this.linkedPos = null;
        this.linkedDim = null;
        this.remoteSpeed = 0;
        this.remoteBudget = 0;
        setChanged();
        if (level != null && !level.isClientSide) {
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.unbind(GlobalBlockPos.of(level, worldPosition));
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            sendData();
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled() || linkedPos == null) return false;

        LangBuilder su = CreateLang.translate("generic.unit.stress");
        float rpm = Math.abs(getSpeed());

        CreateLang.text("RPM: ")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.number(rpm)
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        float total = remoteBudget;
        float consumed = remoteBudget;

        CreateLang.translate("gui.stressometer.capacity")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CreateLang.number(consumed)
                .add(su)
                .style(ChatFormatting.GREEN)
                .text(ChatFormatting.GRAY, " / ")
                .add(CreateLang.number(total)
                        .add(su).style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (placerUUID != null) {
            compound.putUUID("PlacerUUID", placerUUID);
        }
        if (linkedPos != null) {
            compound.putInt("LinkedX", linkedPos.getX());
            compound.putInt("LinkedY", linkedPos.getY());
            compound.putInt("LinkedZ", linkedPos.getZ());
            if (linkedDim != null) {
                compound.putString("LinkedDim", linkedDim.location().toString());
            }
        }
        if (!clientPacket) {
            compound.putFloat("RemoteSpeed", remoteSpeed);
            compound.putFloat("RemoteBudget", remoteBudget);
        }
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (compound.hasUUID("PlacerUUID")) {
            placerUUID = compound.getUUID("PlacerUUID");
        } else {
            placerUUID = null;
        }
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
        if (!clientPacket) {
            remoteSpeed = compound.getFloat("RemoteSpeed");
            remoteBudget = compound.getFloat("RemoteBudget");
        }
    }
}
