package com.goetymech.addon.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SoulDominionProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    public static final Capability<ISoulDominion> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final ISoulDominion instance = new SoulDominionImp();
    private final LazyOptional<ISoulDominion> lazyOptional = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? lazyOptional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Active", instance.isActive());
        tag.putLong("ExpireTick", instance.getExpireTick());
        tag.putInt("Range", instance.getRange());
        tag.putInt("Duration", instance.getDuration());
        ListTag servantsList = new ListTag();
        for (UUID uuid : instance.getLinkedServants()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("UUID", uuid);
            servantsList.add(entry);
        }
        tag.put("LinkedServants", servantsList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setActive(nbt.getBoolean("Active"));
        instance.setExpireTick(nbt.getLong("ExpireTick"));
        instance.setRange(nbt.getInt("Range"));
        instance.setDuration(nbt.getInt("Duration"));
        ListTag servantsList = nbt.getList("LinkedServants", Tag.TAG_COMPOUND);
        List<UUID> uuids = new ArrayList<>();
        for (int i = 0; i < servantsList.size(); i++) {
            CompoundTag entry = servantsList.getCompound(i);
            uuids.add(entry.getUUID("UUID"));
        }
        instance.setLinkedServants(uuids);
    }
}
