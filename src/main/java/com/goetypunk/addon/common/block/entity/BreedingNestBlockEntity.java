package com.goetypunk.addon.common.block.entity;

import com.goetypunk.addon.config.ModConfig;
import com.goetypunk.addon.init.ModBlocks;
import com.goetypunk.addon.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

public class BreedingNestBlockEntity extends BlockEntity implements WorldlyContainer {

    private int energy;
    private ItemStack inputBuffer = ItemStack.EMPTY;
    private int soundCooldown;

    private final LazyOptional<IItemHandlerModifiable>[] handlers = SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public BreedingNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BREEDING_NEST_ENTITY.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        // 处理输入缓冲
        if (!inputBuffer.isEmpty()) {
            FoodProperties food = inputBuffer.getItem().getFoodProperties(inputBuffer, null);
            if (food != null) {
                energy += food.getNutrition();
                checkProduce();
            }
            inputBuffer = ItemStack.EMPTY;
            markUpdated();
        }

        // 音效冷却衰减
        if (soundCooldown > 0) soundCooldown--;
    }

    private void checkProduce() {
        if (level == null) return;
        int maxEnergy = ModConfig.cachedNestMaxEnergy;
        while (energy >= maxEnergy) {
            energy -= maxEnergy;
            ItemStack embryo = new ItemStack(ModItems.TWISTED_EMBRYO.get(), 1);
            tryOutputToContainer(embryo);
            level.playSound(null, worldPosition, SoundEvents.VILLAGER_CELEBRATE, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
    }

    public void insertFood(ItemStack stack) {
        if (inputBuffer.isEmpty()) {
            FoodProperties food = stack.getItem().getFoodProperties(stack, null);
            if (food != null) {
                inputBuffer = stack.copyWithCount(1);
                markUpdated();
            }
        }
    }

    private void tryOutputToContainer(ItemStack stack) {
        if (level == null) return;
        BlockPos belowPos = worldPosition.below();
        BlockEntity belowBE = level.getBlockEntity(belowPos);
        if (belowBE != null) {
            var handler = belowBE.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).resolve().orElse(null);
            if (handler != null) {
                ItemStack remaining = net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(handler, stack, false);
                if (remaining.isEmpty()) return;
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, remaining);
                return;
            }
        }
        Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, stack);
    }

    public int getEnergy() { return energy; }

    public int getMaxEnergy() { return ModConfig.cachedNestMaxEnergy; }

    public float getProgress() {
        int max = ModConfig.cachedNestMaxEnergy;
        return max <= 0 ? 0 : Math.min(1f, (float) energy / max);
    }

    // ===== NBT =====

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy);
        if (!inputBuffer.isEmpty()) {
            CompoundTag buf = new CompoundTag();
            inputBuffer.save(buf);
            tag.put("InputBuffer", buf);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy = tag.getInt("Energy");
        if (tag.contains("InputBuffer")) {
            inputBuffer = ItemStack.of(tag.getCompound("InputBuffer"));
        }
    }

    // ===== 网络同步 =====

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("Energy", energy);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        energy = tag.getInt("Energy");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ===== WorldlyContainer =====

    private static final int[] SLOTS = {0};

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if (direction == Direction.DOWN) return false;
        if (index != 0 || !inputBuffer.isEmpty()) return false;
        return stack.getItem().getFoodProperties(stack, null) != null;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public int getContainerSize() { return 1; }

    @Override
    public int getMaxStackSize() { return 1; }

    @Override
    public boolean isEmpty() { return inputBuffer.isEmpty(); }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? inputBuffer : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index == 0 && !inputBuffer.isEmpty()) {
            ItemStack result = inputBuffer.split(count);
            if (inputBuffer.isEmpty()) markUpdated();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index == 0) {
            ItemStack result = inputBuffer;
            inputBuffer = ItemStack.EMPTY;
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0 && inputBuffer.isEmpty()) {
            FoodProperties food = stack.getItem().getFoodProperties(stack, null);
            if (food != null) {
                inputBuffer = stack.copyWithCount(1);
                setChanged();
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64.0D;
    }

    @Override
    public void clearContent() {
        inputBuffer = ItemStack.EMPTY;
    }

    // ===== Capability =====

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null && side != Direction.DOWN) {
            LazyOptional<IItemHandlerModifiable> handler = handlers[side.get3DDataValue()];
            if (handler != null) return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onChunkUnloaded() {
        for (LazyOptional<IItemHandlerModifiable> h : handlers) {
            if (h != null) h.invalidate();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void invalidateCaps() {
        for (LazyOptional<IItemHandlerModifiable> h : handlers) {
            if (h != null) h.invalidate();
        }
        super.invalidateCaps();
    }
}
