package com.goetypunk.addon.common.block.entity;

import com.goetypunk.addon.config.ModConfig;
import com.goetypunk.addon.init.ModBlocks;
import com.goetypunk.addon.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class BreedingNestBlockEntity extends BlockEntity implements WorldlyContainer {

    private int energy;
    private ItemStack inputBuffer = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int processingTicks;
    private int outputWaitTicks;
    private int soundCooldown;

    private final LazyOptional<IItemHandlerModifiable>[] handlers = SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public BreedingNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BREEDING_NEST_ENTITY.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        // 音效冷却
        if (soundCooldown > 0) soundCooldown--;

        // === 加工延时 ===
        if (!inputBuffer.isEmpty()) {
            processingTicks++;
            // 红石粒子
            if (level instanceof ServerLevel server && level.getGameTime() % 3 == 0) {
                double x = worldPosition.getX() + 0.5D;
                double y = worldPosition.getY() + 0.8D;
                double z = worldPosition.getZ() + 0.5D;
                server.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 0.8F),
                        x, y, z, 1, 0.3D, 0.2D, 0.3D, 0.01D);
            }
            // 满 2s → 消化
            if (processingTicks >= 40) {
                FoodProperties food = inputBuffer.getItem().getFoodProperties(inputBuffer, null);
                if (food != null) {
                    energy += food.getNutrition();
                    level.playSound(null, worldPosition, SoundEvents.PLAYER_BURP, SoundSource.BLOCKS, 0.6F, 0.9F + level.random.nextFloat() * 0.2F);
                    checkProduce();
                }
                inputBuffer = ItemStack.EMPTY;
                processingTicks = 0;
                markUpdated();
            }
        }

        // === 输出等待窗口：产物等待侧面漏斗提取 ===
        if (!outputStack.isEmpty()) {
            outputWaitTicks++;
            if (outputWaitTicks > 20) {
                tryOutputToContainer(outputStack);
                outputStack = ItemStack.EMPTY;
                outputWaitTicks = 0;
                markUpdated();
            }
        }
    }

    private void checkProduce() {
        if (level == null) return;
        int maxEnergy = ModConfig.cachedNestMaxEnergy;
        while (energy >= maxEnergy) {
            energy -= maxEnergy;
            if (!outputStack.isEmpty()) return;
            outputStack = new ItemStack(ModItems.TWISTED_EMBRYO.get(), 1);
            outputWaitTicks = 0;
            level.playSound(null, worldPosition, SoundEvents.VILLAGER_CELEBRATE, SoundSource.BLOCKS, 0.8F, 1.0F);
            markUpdated();
        }
    }

    // ===== 输入控制 =====

    public boolean canAcceptInput() {
        return inputBuffer.isEmpty() && outputStack.isEmpty();
    }

    public boolean canPlaySound() {
        return soundCooldown <= 0;
    }

    public void onSoundPlayed() {
        soundCooldown = 30;
    }

    public void insertFood(ItemStack stack) {
        if (inputBuffer.isEmpty()) {
            FoodProperties food = stack.getItem().getFoodProperties(stack, null);
            if (food != null) {
                inputBuffer = stack.copyWithCount(1);
                processingTicks = 0;
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
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, stack, false);
                if (remaining.isEmpty()) return;
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, remaining);
                return;
            }
        }
        Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, stack);
    }

    // ===== HUD 数据 =====

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
        tag.putInt("ProcessingTicks", processingTicks);
        tag.putInt("OutputWaitTicks", outputWaitTicks);
        if (!inputBuffer.isEmpty()) {
            CompoundTag buf = new CompoundTag();
            inputBuffer.save(buf);
            tag.put("InputBuffer", buf);
        }
        if (!outputStack.isEmpty()) {
            CompoundTag buf = new CompoundTag();
            outputStack.save(buf);
            tag.put("OutputStack", buf);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy = tag.getInt("Energy");
        processingTicks = tag.getInt("ProcessingTicks");
        outputWaitTicks = tag.getInt("OutputWaitTicks");
        if (tag.contains("InputBuffer")) {
            inputBuffer = ItemStack.of(tag.getCompound("InputBuffer"));
        }
        if (tag.contains("OutputStack")) {
            outputStack = ItemStack.of(tag.getCompound("OutputStack"));
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
        if (index != 0 || !inputBuffer.isEmpty() || !outputStack.isEmpty()) return false;
        return stack.getItem().getFoodProperties(stack, null) != null;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (direction == Direction.UP || direction == Direction.DOWN) return false;
        return !outputStack.isEmpty();
    }

    @Override
    public int getContainerSize() { return 1; }

    @Override
    public int getMaxStackSize() { return 1; }

    @Override
    public boolean isEmpty() { return inputBuffer.isEmpty() && outputStack.isEmpty(); }

    @Override
    public ItemStack getItem(int index) {
        if (index == 0) {
            return outputStack.isEmpty() ? inputBuffer : outputStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index == 0) {
            if (!outputStack.isEmpty()) {
                ItemStack result = outputStack.split(count);
                if (outputStack.isEmpty()) {
                    outputWaitTicks = 0;
                    markUpdated();
                }
                return result;
            }
            if (!inputBuffer.isEmpty()) {
                ItemStack result = inputBuffer.split(count);
                if (inputBuffer.isEmpty()) {
                    processingTicks = 0;
                    markUpdated();
                }
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index == 0) {
            if (!outputStack.isEmpty()) {
                ItemStack result = outputStack;
                outputStack = ItemStack.EMPTY;
                outputWaitTicks = 0;
                return result;
            }
            if (!inputBuffer.isEmpty()) {
                ItemStack result = inputBuffer;
                inputBuffer = ItemStack.EMPTY;
                processingTicks = 0;
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0 && inputBuffer.isEmpty() && outputStack.isEmpty()) {
            FoodProperties food = stack.getItem().getFoodProperties(stack, null);
            if (food != null) {
                inputBuffer = stack.copyWithCount(1);
                processingTicks = 0;
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
        outputStack = ItemStack.EMPTY;
        processingTicks = 0;
        outputWaitTicks = 0;
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
