package com.goetymech.addon.common.block.entity;

import java.util.List;

import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModBlocks;
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
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteEngineBlockEntity extends GeneratingKineticBlockEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger("TEST");

    @Nullable private BlockPos linkedPos;
    @Nullable private ResourceKey<Level> linkedDim;
    private float cachedNetworkCapacity;
    private float cachedNetworkStress;
    private float cachedRpm;
    private float cachedConsumed;
    private int tickCounter;

    public RemoteEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REMOTE_ENGINE_ENTITY.get(), pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        LOGGER.debug("[TEST] RE{} INIT | linked={} | rpm={} | speed={}",
                fmtPos(worldPosition), linkedPos != null ? fmtPos(linkedPos) : "null", cachedRpm, getSpeed());
        if (linkedPos != null && !level.isClientSide) {
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

    private void tryRefreshRpm() {
        if (linkedPos == null || linkedDim == null)
            return;

        MinecraftServer server = ((ServerLevel) level).getServer();
        ServerLevel targetLevel = server.getLevel(linkedDim);
        if (targetLevel == null) return;
        if (!targetLevel.isLoaded(linkedPos)) return;

        BlockEntity be = targetLevel.getBlockEntity(linkedPos);
        if (!(be instanceof StressReaderBlockEntity reader)) return;

        // ① Inherit RPM from source network
        cachedRpm = reader.getSpeed();
        cachedNetworkCapacity = reader.getNetworkCapacity();
        cachedNetworkStress = reader.getNetworkStress();

        // ② Compute true available stress (excluding our own previous consumption)
        float rawAvailable = cachedNetworkCapacity - cachedNetworkStress;
        float trueAvailable = Math.max(0, rawAvailable + reader.getConsumedRemotely());

        // ③ Actual consumption = local network load, capped by availability
        //    (no machines attached → this.stress=0 → consumes 0)
        cachedConsumed = Mth.clamp(this.stress, 0, trueAvailable);

        LOGGER.debug("[TEST] RE{} REFRESH | netSpeed={} | rawAvail={} | trueAvail={} | localStress={} | consumed={} | linkedTarget={}",
                fmtPos(worldPosition), cachedRpm, rawAvailable, trueAvailable, this.stress, cachedConsumed, fmtPos(linkedPos));

        // ④ Write consumption back to reader (multi-engine safe)
        reader.setConsumedRemotely(this.worldPosition, cachedConsumed);

        // ⑤ Always repropagate to keep speed/capacity in sync
        updateGeneratedRotation();

        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            setChanged();
            sendData();
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
        if (linkedPos == null || level == null || level.isClientSide) return;
        MinecraftServer server = ((ServerLevel) level).getServer();
        ServerLevel targetLevel = server.getLevel(linkedDim);
        if (targetLevel != null && targetLevel.getBlockEntity(linkedPos) instanceof StressReaderBlockEntity reader) {
            reader.removeConsumer(worldPosition);
            LOGGER.debug("[TEST] RE{} CLEANUP | removed consumer from reader at {}",
                    fmtPos(worldPosition), fmtPos(linkedPos));
        }
    }

    public void setLinkedPos(BlockPos pos, ResourceKey<Level> dim) {
        BlockPos prevLinked = this.linkedPos;
        this.linkedPos = pos;
        this.linkedDim = dim;
        setChanged();
        LOGGER.debug("[TEST] RE{} LINK | target={} | prevTarget={} | rpm={}",
                fmtPos(worldPosition), fmtPos(pos), prevLinked != null ? fmtPos(prevLinked) : "null", cachedRpm);
        if (level != null && !level.isClientSide)
            tryRefreshRpm();
    }

    @Override
    public void updateGeneratedRotation() {
        float prevSpeed = this.speed;
        super.updateGeneratedRotation();
        LOGGER.debug("[TEST] RE{} GEN | speed={} | prevSpeed={} | hasNet={} | rpm={}",
                fmtPos(worldPosition), this.speed, prevSpeed, hasNetwork(), cachedRpm);
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
        float fraction = cap == 0 ? 0 : cachedConsumed / cap;

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

            float remaining = cap - cachedConsumed;
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
        if (!clientPacket) {
            compound.putFloat("CachedCapacity", cachedNetworkCapacity);
            compound.putFloat("CachedStressVal", cachedNetworkStress);
            compound.putFloat("CachedRpm", cachedRpm);
            compound.putFloat("CachedConsumed", cachedConsumed);
        }
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
        if (!clientPacket) {
            cachedNetworkCapacity = compound.getFloat("CachedCapacity");
            cachedNetworkStress = compound.getFloat("CachedStressVal");
            cachedRpm = compound.getFloat("CachedRpm");
            cachedConsumed = compound.getFloat("CachedConsumed");
        }
    }

    private static String fmtPos(BlockPos pos) {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
    }
}
