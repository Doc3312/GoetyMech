package com.goetymech.addon.common.block.entity;

import com.goetymech.addon.common.data.CondenserProductManager;
import com.goetymech.addon.common.event.RitualCompletedEvent;
import com.goetymech.addon.init.ModBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 巫法冷凝。Create 应力驱动蒸馏机：
 *  - 群系蒸馏：转速非零时按 max(20, |speed|×0.625) mb/s 累积，满 50mb 脉冲产出（群系概率表独立判定）
 *  - 仪式联动：仪式成功完成后按仪式专属表产液体（soulCost×5 mb），距离 ≤ 8 格
 *  - 存储 3 槽 × 1000mb，只出不进（管道可抽，不可灌）
 */
public class LengNingQiBlockEntity extends KineticBlockEntity {

    public static final int TANK_COUNT = 3;
    public static final int TANK_CAPACITY = 1000;
    public static final int RITUAL_RANGE = 8;
    public static final float PULSE_AMOUNT = 50f;
    public static final float RATE_BASE = 0.625f;
    public static final float MIN_RATE = 20f;

    /** 服务端活动实例集合（仪式事件派发用） */
    private static final Set<LengNingQiBlockEntity> ACTIVE = new HashSet<>();

    protected SmartFluidTankBehaviour fluidBehaviour;
    private float progress;

    public LengNingQiBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.LENGNINGQI_ENTITY.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        fluidBehaviour = new SmartFluidTankBehaviour(
                SmartFluidTankBehaviour.INPUT, this, TANK_COUNT, TANK_CAPACITY, true)
                .allowExtraction()
                .forbidInsertion()
                .whenFluidUpdates(this::onFluidChanged);
        behaviours.add(fluidBehaviour);
    }

    private void onFluidChanged() {
        setChanged();
        sendData();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            ACTIVE.add(this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (level != null && !level.isClientSide) {
            ACTIVE.remove(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            ACTIVE.remove(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        float speed = Math.abs(getSpeed());
        if (speed < 0.01f) {
            return;
        }
        float mbPerTick = Math.max(MIN_RATE, speed * RATE_BASE) / 20f;
        progress += mbPerTick;
        if (progress >= PULSE_AMOUNT) {
            progress -= PULSE_AMOUNT;
            produceFromBiome();
        }
    }

    /** 群系蒸馏脉冲：条目内每个液体独立掷骰，可同时产出多个液体 */
    private void produceFromBiome() {
        Holder<Biome> biome = level.getBiome(worldPosition);
        CondenserProductManager.BiomeEntry entry = CondenserProductManager.matchBiome(biome);
        if (entry == null) {
            return;
        }
        for (CondenserProductManager.FluidChance fc : entry.getLiquids()) {
            if (level.random.nextFloat() * 100f < fc.getChance()) {
                fillFluid(new FluidStack(fc.getFluid(), (int) PULSE_AMOUNT));
            }
        }
    }

    /** 内部写入（ItemDrain 模式：临时放开灌入 → 写入 → 立即收回）；槽满时丢弃 */
    private void fillFluid(FluidStack stack) {
        if (stack.isEmpty() || fluidBehaviour == null) {
            return;
        }
        fluidBehaviour.allowInsertion();
        try {
            LazyOptional<? extends IFluidHandler> cap = fluidBehaviour.getCapability();
            if (cap.isPresent()) {
                cap.orElse(null).fill(stack, FluidAction.EXECUTE);
            }
        } finally {
            fluidBehaviour.forbidInsertion();
        }
    }

    @SubscribeEvent
    public static void onRitualCompleted(RitualCompletedEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        int rangeSq = RITUAL_RANGE * RITUAL_RANGE;
        for (LengNingQiBlockEntity be : new ArrayList<>(ACTIVE)) {
            if (be.level != event.getLevel()) {
                continue;
            }
            if (be.worldPosition.distSqr(event.getAltarPos()) <= rangeSq) {
                be.onRitualCompleted(event);
            }
        }
    }

    private void onRitualCompleted(RitualCompletedEvent event) {
        Fluid fluid = CondenserProductManager.getRitualFluid(event.getRecipeId());
        if (fluid == null) {
            return;
        }
        int amount = event.getSoulCost() * 5;
        if (amount > 0) {
            fillFluid(new FluidStack(fluid, amount));
        }
    }

    public SmartFluidTankBehaviour getFluidBehaviour() {
        return fluidBehaviour;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && fluidBehaviour != null) {
            return fluidBehaviour.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }
}
