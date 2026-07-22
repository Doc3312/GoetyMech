package com.goetymech.addon.init;

import java.util.function.Supplier;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.fluid.frame.EphemeralFluidConfig;
import com.goetymech.addon.common.fluid.frame.EphemeralLiquidBlock;
import com.goetymech.addon.common.fluid.soul.SoulFluid;
import com.goetymech.addon.common.fluid.soul.SoulFluidType;
import com.Polarice3.Goety.utils.SEHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, GoetyMechMod.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, GoetyMechMod.MOD_ID);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GoetyMechMod.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GoetyMechMod.MOD_ID);

    // ── Soul Liquid FluidType ──

    public static final RegistryObject<FluidType> SOUL_LIQUID_TYPE =
            FLUID_TYPES.register("soul_liquid", SoulFluidType::new);

    // ── Soul Liquid Fluids ──

    public static final RegistryObject<FlowingFluid> SOUL_LIQUID_SOURCE =
            FLUIDS.register("soul_liquid", () -> new SoulFluid.Source());

    public static final RegistryObject<FlowingFluid> SOUL_LIQUID_FLOWING =
            FLUIDS.register("soul_liquid_flowing", () -> new SoulFluid.Flowing());

    // ── Soul Liquid Block ──

    private static final EphemeralFluidConfig SOUL_LIQUID_CONFIG = EphemeralFluidConfig.builder()
            .particle(ParticleTypes.SCULK_SOUL)
            .particleProbability(0.025f)
            .contactEffect(player -> SEHelper.increaseSouls(player, 50))
            .build();

    public static final RegistryObject<EphemeralLiquidBlock> SOUL_LIQUID_BLOCK =
            BLOCKS.register("soul_liquid", () -> new EphemeralLiquidBlock(
                    () -> (FlowingFluid) SOUL_LIQUID_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_CYAN)
                            .noCollission()
                            .randomTicks()
                            .strength(100.0F)
                            .noLootTable(),
                    SOUL_LIQUID_CONFIG,
                    100,
                    2
            ));

    // ── Soul Liquid Bucket ──

    public static final RegistryObject<BucketItem> SOUL_LIQUID_BUCKET =
            ITEMS.register("soul_liquid_bucket", () -> new BucketItem(
                    SOUL_LIQUID_SOURCE,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
            ));
}
