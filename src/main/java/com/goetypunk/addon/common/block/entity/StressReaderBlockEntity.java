package com.goetypunk.addon.common.block.entity;

import java.util.List;

import com.goetypunk.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.item.TooltipHelper;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class StressReaderBlockEntity extends KineticBlockEntity {

    private float consumedRemotely;

    public StressReaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STRESS_READER_ENTITY.get(), pos, state);
    }

    public float getNetworkCapacity() { return capacity; }

    public float getNetworkStress() { return stress; }

    public float getAvailableStress() { return Math.max(0, capacity - stress); }

    @Override
    public float calculateStressApplied() {
        float spd = Math.abs(getSpeed());
        return spd < 0.001f ? 0 : consumedRemotely / spd;
    }

    public void setConsumedRemotely(float amount) {
        if (Math.abs(this.consumedRemotely - amount) <= 0.001f) return;
        this.consumedRemotely = amount;
        if (level != null && !level.isClientSide) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            if (hasNetwork())
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
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
    public void updateFromNetwork(float capacity, float stress, int networkSize) {
        float prevCapacity = this.capacity;
        float prevStress = this.stress;
        super.updateFromNetwork(capacity, stress, networkSize);
        if ((prevCapacity != this.capacity || prevStress != this.stress) && level != null && !level.isClientSide) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            setChanged();
        }
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putFloat("ConsumedRemotely", consumedRemotely);
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        consumedRemotely = compound.getFloat("ConsumedRemotely");
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

            float remaining = getAvailableStress();
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
