package com.goetymech.addon.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StressShieldProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    public static final Capability<IStressShield> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final IStressShield instance = new StressShieldImp();
    private final LazyOptional<IStressShield> lazyOptional = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? lazyOptional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Shield", instance.getShield());
        tag.putFloat("MaxShield", instance.getMaxShield());
        tag.putLong("ExpireTick", instance.getExpireTick());
        tag.putLong("LastHurtTick", instance.getLastHurtTick());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setShield(nbt.getFloat("Shield"));
        instance.setMaxShield(nbt.getFloat("MaxShield"));
        instance.setExpireTick(nbt.getLong("ExpireTick"));
        instance.setLastHurtTick(nbt.getLong("LastHurtTick"));
    }
}
