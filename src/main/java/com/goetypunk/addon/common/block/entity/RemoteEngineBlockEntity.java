package com.goetypunk.addon.common.block.entity;

import java.util.List;

import com.goetypunk.addon.config.ModConfig;
import com.goetypunk.addon.init.ModBlocks;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.item.TooltipHelper;
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

public class RemoteEngineBlockEntity extends GeneratingKineticBlockEntity {

    @Nullable private BlockPos linkedPos;
    @Nullable private ResourceKey<Level> linkedDim;
    private float cachedNetworkCapacity;
    private float cachedNetworkStress;
    private float cachedAvailableStress;
    private float cachedEffectiveAvailable;
    private float cachedRpm;
    private int tickCounter;

    public RemoteEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REMOTE_ENGINE_ENTITY.get(), pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (linkedPos != null && cachedRpm > 0 && getSpeed() == 0)
            updateGeneratedRotation();
    }

    @Override
    public void tick() {
        super.tick();
        if (level instanceof ServerLevel sl && ++tickCounter >= 20) {
            tickCounter = 0;
            tryRefreshRpm();
        }
    }

    private void tryRefreshRpm() {
        if (linkedPos == null || linkedDim == null)
            return;

        MinecraftServer server = ((ServerLevel) level).getServer();
        ServerLevel targetLevel = server.getLevel(linkedDim);
        if (targetLevel == null) return;
        if (!targetLevel.isLoaded(linkedPos)) return;

        BlockEntity be = targetLevel.getBlockEntity(linkedPos);
        if (!(be instanceof StressReaderBlockEntity reader)) return;

        // Tell reader how much stress this engine's local network is consuming
        float prevEffective = cachedEffectiveAvailable;
        reader.setConsumedRemotely(this.stress);
        cachedEffectiveAvailable = reader.getAvailableStress();

        // Sync HUD value to client even if reader raw values haven't changed
        if (Math.abs(cachedEffectiveAvailable - prevEffective) > 0.01f && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            setChanged();
        }

        float newCapacity = reader.getNetworkCapacity();
        float newStress = reader.getNetworkStress();
        float rawAvailable = newCapacity - newStress;

        if (Math.abs(rawAvailable - cachedAvailableStress) <= 0.01f
                && Math.abs(newCapacity - cachedNetworkCapacity) <= 0.01f)
            return;

        cachedNetworkCapacity = newCapacity;
        cachedNetworkStress = newStress;
        cachedAvailableStress = rawAvailable;

        float maxRpm = AllConfigs.server().kinetics.maxRotationSpeed.get().floatValue();
        float newRpm = rawAvailable <= 0 ? 0 : Math.min(rawAvailable / ModConfig.REMOTE_ENGINE_STRESS_TO_RPM_RATIO.get(), maxRpm);

        if (Math.abs(newRpm - cachedRpm) > 0.01f) {
            cachedRpm = newRpm;
            updateGeneratedRotation();
            if (!level.isClientSide) sendData();
        }

        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            setChanged();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        return linkedPos != null ? cachedRpm : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(this.speed);
        if (speed < 0.01f) return 0;
        return cachedAvailableStress / speed;
    }

    public void setLinkedPos(BlockPos pos, ResourceKey<Level> dim) {
        this.linkedPos = pos;
        this.linkedDim = dim;
        setChanged();
        if (level != null && !level.isClientSide)
            tryRefreshRpm();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (linkedPos == null) return false;
        if (!StressImpact.isEnabled()) return false;

        CreateLang.text("Remote Engine: ["
                + linkedPos.getX() + ", "
                + linkedPos.getY() + ", "
                + linkedPos.getZ() + "]")
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);

        float cap = cachedNetworkCapacity;
        float effectiveStress = cap - cachedEffectiveAvailable;
        float fraction = cap == 0 ? 0 : effectiveStress / cap;

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

            float remaining = cachedEffectiveAvailable;
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

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (linkedPos != null) {
            compound.putInt("LinkedX", linkedPos.getX());
            compound.putInt("LinkedY", linkedPos.getY());
            compound.putInt("LinkedZ", linkedPos.getZ());
            compound.putString("LinkedDim", linkedDim.location().toString());
        }
        compound.putFloat("CachedCapacity", cachedNetworkCapacity);
        compound.putFloat("CachedStressVal", cachedNetworkStress);
        compound.putFloat("CachedAvailable", cachedAvailableStress);
        compound.putFloat("CachedEffectiveAvailable", cachedEffectiveAvailable);
        compound.putFloat("CachedRpm", cachedRpm);
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (compound.contains("LinkedX")) {
            linkedPos = new BlockPos(compound.getInt("LinkedX"),
                    compound.getInt("LinkedY"),
                    compound.getInt("LinkedZ"));
            linkedDim = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.tryParse(compound.getString("LinkedDim")));
        }
        cachedNetworkCapacity = compound.getFloat("CachedCapacity");
        cachedNetworkStress = compound.getFloat("CachedStressVal");
        cachedAvailableStress = compound.getFloat("CachedAvailable");
        cachedEffectiveAvailable = compound.getFloat("CachedEffectiveAvailable");
        cachedRpm = compound.getFloat("CachedRpm");
    }
}
