package com.goetymech.addon.common.block.entity;

import java.util.List;
import java.util.UUID;

import com.goetymech.addon.common.utils.EngineStatus;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class RemoteEngineBlockEntity extends GeneratingKineticBlockEntity {

    @Nullable private BlockPos linkedPos;
    @Nullable private ResourceKey<Level> linkedDim;
    @Nullable private UUID placerUUID;

    private float remoteSpeed;
    private float remoteBudget;
    private float remoteCapacity;

    private int consumeReportTick;
    private int staleTick;
    private EngineStatus status = EngineStatus.NO_BUDGET;

    public RemoteEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REMOTE_ENGINE_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RemoteEngineBlockEntity self) {
        if (level.isClientSide) return;
        if (++self.consumeReportTick < 20) return;
        self.consumeReportTick = 0;

        // P1: report actual network consumption
        StressLinkService service = StressLinkService.get(level);
        if (service != null) {
            service.reportEngineConsumption(GlobalBlockPos.of(level, pos), self.stress);
        }

        // P3: check for stale reader (each callback = 20gt, threshold 2 = 40gt)
        if (++self.staleTick > 2) {
            self.status = EngineStatus.READER_UNLOADED;
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            GlobalBlockPos gpos = GlobalBlockPos.of(level, worldPosition);
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.onEngineLoad(this);
                if (placerUUID != null) {
                    service.registerPlayerEngine(gpos, placerUUID);
                }
            }
        }
    }

    @Override
    public void invalidate() {
        if (level != null && !level.isClientSide) {
            GlobalBlockPos gpos = GlobalBlockPos.of(level, worldPosition);
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.unbind(gpos);
                service.onEngineUnload(gpos);
            }
        }
        super.invalidate();
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

    public void applyRemoteUpdate(float speed, float budget, float capacity) {
        this.remoteSpeed = Math.abs(speed);
        this.remoteBudget = Math.max(0, budget);
        this.remoteCapacity = Math.max(0, capacity);
        this.staleTick = 0;
        this.status = budget > 0 ? EngineStatus.ACTIVE
                : capacity > 0 ? EngineStatus.IDLE
                : EngineStatus.NO_BUDGET;
        updateGeneratedRotation();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            setChanged();
            sendData();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        if (linkedPos != null) return remoteSpeed;
        if (placerUUID != null && remoteBudget > 0) return 16;
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
        if (level != null && !level.isClientSide) {
            StressLinkService service = StressLinkService.get(level);
            if (service != null) {
                service.registerPlayerEngine(GlobalBlockPos.of(level, worldPosition), uuid);
            }
        }
    }

    @Nullable
    public UUID getPlacerUUID() { return placerUUID; }

    public void clearLinkedPos() {
        this.linkedPos = null;
        this.linkedDim = null;
        this.remoteSpeed = 0;
        this.remoteBudget = 0;
        this.remoteCapacity = 0;
        this.status = EngineStatus.NO_BUDGET;
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
        if (!StressImpact.isEnabled()) return false;

        LangBuilder su = CreateLang.translate("generic.unit.stress");
        float rpm = Math.abs(getSpeed());

        CreateLang.text("RPM: ")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.number(rpm)
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        float added = Math.abs(calculateAddedStressCapacity());

        CreateLang.translate("gui.stressometer.capacity")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CreateLang.number(added)
                .add(su)
                .style(ChatFormatting.GREEN)
                .forGoggles(tooltip, 1);

        // Status
        if (placerUUID != null) {
            CreateLang.text("Link: ")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            ChatFormatting color = switch (status) {
                case ACTIVE -> ChatFormatting.GREEN;
                case IDLE -> ChatFormatting.GRAY;
                case NO_BUDGET -> ChatFormatting.YELLOW;
                case READER_UNLOADED -> ChatFormatting.RED;
            };
            CreateLang.translate("status." + status.name().toLowerCase())
                    .style(color)
                    .forGoggles(tooltip, 1);
        }

        if (remoteCapacity > 0) {
            CreateLang.text("Source: ")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            CreateLang.number(remoteBudget)
                    .add(CreateLang.translate("generic.unit.stress"))
                    .style(ChatFormatting.DARK_GREEN)
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(remoteCapacity)
                            .add(CreateLang.translate("generic.unit.stress"))
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);
        }

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
        compound.putFloat("RemoteSpeed", remoteSpeed);
        compound.putFloat("RemoteBudget", remoteBudget);
        compound.putFloat("RemoteCapacity", remoteCapacity);
        compound.putInt("Status", status.ordinal());
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
        remoteSpeed = compound.getFloat("RemoteSpeed");
        remoteBudget = compound.getFloat("RemoteBudget");
        remoteCapacity = compound.getFloat("RemoteCapacity");
        if (compound.contains("Status")) {
            int si = compound.getInt("Status");
            status = si >= 0 && si < EngineStatus.values().length ? EngineStatus.values()[si] : EngineStatus.NO_BUDGET;
        }
    }
}
