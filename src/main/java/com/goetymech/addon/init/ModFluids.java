package com.goetymech.addon.init;

import java.util.List;
import java.util.function.Supplier;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.fluid.FluidContactEvents;
import com.goetymech.addon.common.fluid.frame.EphemeralFluidConfig;
import com.goetymech.addon.common.fluid.frame.EphemeralLiquidBlock;
import com.goetymech.addon.common.fluid.frame.PermanentLiquidBlock;
import com.goetymech.addon.common.fluid.gale.GaleFluid;
import com.goetymech.addon.common.fluid.gale.GaleFluidType;
import com.goetymech.addon.common.fluid.hunger.HungerFluid;
import com.goetymech.addon.common.fluid.hunger.HungerFluidType;
import com.goetymech.addon.common.fluid.mystic.MysticFluid;
import com.goetymech.addon.common.fluid.mystic.MysticFluidType;
import com.goetymech.addon.common.fluid.soul.SoulFluid;
import com.goetymech.addon.common.fluid.soul.SoulFluidType;
import com.goetymech.addon.common.fluid.vitality.VitalityFluid;
import com.goetymech.addon.common.fluid.vitality.VitalityFluidType;
import com.goetymech.addon.common.item.DrinkableLiquidBottleItem;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.utils.SEHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    // ── 浸泡/饮用效果辅助 ──

    private static final int IMMERSION_TICKS = 100;
    private static final int DRINK_TICKS = 400;

    private static MobEffectInstance immersion(MobEffect effect) {
        return new MobEffectInstance(effect, IMMERSION_TICKS);
    }

    private static MobEffectInstance immersion(MobEffect effect, int amplifier) {
        return new MobEffectInstance(effect, IMMERSION_TICKS, amplifier);
    }

    private static void drink(LivingEntity entity, MobEffect effect) {
        entity.addEffect(new MobEffectInstance(effect, DRINK_TICKS));
    }

    private static void drinkAll(LivingEntity entity, List<MobEffect> effects) {
        for (MobEffect effect : effects) {
            drink(entity, effect);
        }
    }

    // ── 活力之液 ──

    public static final RegistryObject<FluidType> VITALITY_LIQUID_TYPE =
            FLUID_TYPES.register("vitality_liquid", VitalityFluidType::new);

    public static final RegistryObject<FlowingFluid> VITALITY_LIQUID_SOURCE =
            FLUIDS.register("vitality_liquid", () -> new VitalityFluid.Source());

    public static final RegistryObject<FlowingFluid> VITALITY_LIQUID_FLOWING =
            FLUIDS.register("vitality_liquid_flowing", () -> new VitalityFluid.Flowing());

    private static final List<MobEffectInstance> VITALITY_IMMERSION = List.of(
            immersion(MobEffects.REGENERATION, 1),
            immersion(MobEffects.MOVEMENT_SPEED));

    public static final RegistryObject<PermanentLiquidBlock> VITALITY_LIQUID_BLOCK =
            BLOCKS.register("vitality_liquid", () -> new PermanentLiquidBlock(
                    () -> (FlowingFluid) VITALITY_LIQUID_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_GRAY)
                            .noCollission()
                            .randomTicks()
                            .strength(100.0F)
                            .noLootTable(),
                    null, 0.0f
            ));

    public static final RegistryObject<BucketItem> VITALITY_LIQUID_BUCKET =
            ITEMS.register("vitality_liquid_bucket", () -> new BucketItem(
                    VITALITY_LIQUID_SOURCE,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
            ));

    public static final RegistryObject<DrinkableLiquidBottleItem> VITALITY_LIQUID_BOTTLE =
            ITEMS.register("vitality_liquid_bottle", () -> new DrinkableLiquidBottleItem(
                    new Item.Properties().stacksTo(16),
                    entity -> drinkAll(entity, List.of(MobEffects.REGENERATION, MobEffects.MOVEMENT_SPEED))
            ));

    // ── 饥饿之液 ──

    public static final RegistryObject<FluidType> HUNGER_LIQUID_TYPE =
            FLUID_TYPES.register("hunger_liquid", HungerFluidType::new);

    public static final RegistryObject<FlowingFluid> HUNGER_LIQUID_SOURCE =
            FLUIDS.register("hunger_liquid", () -> new HungerFluid.Source());

    public static final RegistryObject<FlowingFluid> HUNGER_LIQUID_FLOWING =
            FLUIDS.register("hunger_liquid_flowing", () -> new HungerFluid.Flowing());

    private static final List<MobEffectInstance> HUNGER_IMMERSION = List.of(
            immersion(MobEffects.HUNGER),
            immersion(MobEffects.POISON));

    public static final RegistryObject<PermanentLiquidBlock> HUNGER_LIQUID_BLOCK =
            BLOCKS.register("hunger_liquid", () -> new PermanentLiquidBlock(
                    () -> (FlowingFluid) HUNGER_LIQUID_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_CYAN)
                            .noCollission()
                            .randomTicks()
                            .strength(100.0F)
                            .noLootTable(),
                    null, 0.0f
            ));

    public static final RegistryObject<BucketItem> HUNGER_LIQUID_BUCKET =
            ITEMS.register("hunger_liquid_bucket", () -> new BucketItem(
                    HUNGER_LIQUID_SOURCE,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
            ));

    public static final RegistryObject<DrinkableLiquidBottleItem> HUNGER_LIQUID_BOTTLE =
            ITEMS.register("hunger_liquid_bottle", () -> new DrinkableLiquidBottleItem(
                    new Item.Properties().stacksTo(16),
                    entity -> drinkAll(entity, List.of(MobEffects.HUNGER, MobEffects.POISON))
            ));

    // ── 神秘之液 ──

    public static final RegistryObject<FluidType> MYSTIC_LIQUID_TYPE =
            FLUID_TYPES.register("mystic_liquid", MysticFluidType::new);

    public static final RegistryObject<FlowingFluid> MYSTIC_LIQUID_SOURCE =
            FLUIDS.register("mystic_liquid", () -> new MysticFluid.Source());

    public static final RegistryObject<FlowingFluid> MYSTIC_LIQUID_FLOWING =
            FLUIDS.register("mystic_liquid_flowing", () -> new MysticFluid.Flowing());

    private static final List<RegistryObject<MobEffect>> MYSTIC_AURAS = List.of(
            GoetyEffects.FIERY_AURA,
            GoetyEffects.REPULSIVE,
            GoetyEffects.FROSTY_AURA,
            GoetyEffects.GRAVITY_PULSE);

    public static final RegistryObject<PermanentLiquidBlock> MYSTIC_LIQUID_BLOCK =
            BLOCKS.register("mystic_liquid", () -> new PermanentLiquidBlock(
                    () -> (FlowingFluid) MYSTIC_LIQUID_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .noCollission()
                            .randomTicks()
                            .strength(100.0F)
                            .noLootTable(),
                    null, 0.0f
            ));

    public static final RegistryObject<BucketItem> MYSTIC_LIQUID_BUCKET =
            ITEMS.register("mystic_liquid_bucket", () -> new BucketItem(
                    MYSTIC_LIQUID_SOURCE,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
            ));

    public static final RegistryObject<DrinkableLiquidBottleItem> MYSTIC_LIQUID_BOTTLE =
            ITEMS.register("mystic_liquid_bottle", () -> new DrinkableLiquidBottleItem(
                    new Item.Properties().stacksTo(16),
                    entity -> drink(entity, MYSTIC_AURAS.get(entity.level().random.nextInt(MYSTIC_AURAS.size())).get())
            ));

    // ── 烈风之液 ──

    public static final RegistryObject<FluidType> GALE_LIQUID_TYPE =
            FLUID_TYPES.register("gale_liquid", GaleFluidType::new);

    public static final RegistryObject<FlowingFluid> GALE_LIQUID_SOURCE =
            FLUIDS.register("gale_liquid", () -> new GaleFluid.Source());

    public static final RegistryObject<FlowingFluid> GALE_LIQUID_FLOWING =
            FLUIDS.register("gale_liquid_flowing", () -> new GaleFluid.Flowing());

    private static final List<MobEffectInstance> GALE_IMMERSION = List.of(
            immersion(MobEffects.LEVITATION));

    public static final RegistryObject<PermanentLiquidBlock> GALE_LIQUID_BLOCK =
            BLOCKS.register("gale_liquid", () -> new PermanentLiquidBlock(
                    () -> (FlowingFluid) GALE_LIQUID_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .noCollission()
                            .randomTicks()
                            .strength(100.0F)
                            .noLootTable(),
                    null, 0.0f
            ));

    public static final RegistryObject<BucketItem> GALE_LIQUID_BUCKET =
            ITEMS.register("gale_liquid_bucket", () -> new BucketItem(
                    GALE_LIQUID_SOURCE,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
            ));

    public static final RegistryObject<DrinkableLiquidBottleItem> GALE_LIQUID_BOTTLE =
            ITEMS.register("gale_liquid_bottle", () -> new DrinkableLiquidBottleItem(
                    new Item.Properties().stacksTo(16),
                    entity -> drink(entity, MobEffects.LEVITATION)
            ));

    // ── 浸泡效果注册（common setup 时调用，此时 registry 已可用） ──
    // 注意：流动层的 FluidState#getType 返回 Flowing 实例而非 Source，两者都必须注册。

    public static void registerContactEffects() {
        registerImmersion(VITALITY_LIQUID_SOURCE.get(), VITALITY_LIQUID_FLOWING.get(), VITALITY_IMMERSION);
        registerImmersion(HUNGER_LIQUID_SOURCE.get(), HUNGER_LIQUID_FLOWING.get(), HUNGER_IMMERSION);
        registerImmersion(GALE_LIQUID_SOURCE.get(), GALE_LIQUID_FLOWING.get(), GALE_IMMERSION);
        FluidContactEvents.ContactEffect soulEffect = entity -> {
            if (entity.tickCount % 20 == 0 && entity instanceof Player player) {
                SEHelper.increaseSouls(player, 50);
            }
        };
        FluidContactEvents.register(SOUL_LIQUID_SOURCE.get(), soulEffect);
        FluidContactEvents.register(SOUL_LIQUID_FLOWING.get(), soulEffect);
    }

    private static void registerImmersion(Fluid source, Fluid flowing, List<MobEffectInstance> effects) {
        FluidContactEvents.ContactEffect apply = entity -> {
            for (MobEffectInstance effect : effects) {
                MobEffectInstance current = entity.getEffect(effect.getEffect());
                if (current == null || current.getDuration() <= 40) {
                    entity.addEffect(
                            new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier()));
                }
            }
        };
        FluidContactEvents.register(source, apply);
        FluidContactEvents.register(flowing, apply);
    }
}
