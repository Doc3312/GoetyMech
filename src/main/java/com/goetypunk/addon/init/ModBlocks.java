package com.goetypunk.addon.init;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.block.BreedingNestBlock;
import com.goetypunk.addon.common.block.FleshFurnaceBlock;
import com.goetypunk.addon.common.block.RemoteEngineBlock;
import com.goetypunk.addon.common.block.StressArkBlock;
import com.goetypunk.addon.common.block.StressReaderBlock;
import com.goetypunk.addon.common.block.Tier1SquadBarracksBlock;
import com.goetypunk.addon.common.block.Tier2SquadBarracksBlock;
import com.goetypunk.addon.common.block.Tier3SquadBarracksBlock;
import com.goetypunk.addon.common.block.entity.BreedingNestBlockEntity;
import com.goetypunk.addon.common.block.entity.FleshFurnaceBlockEntity;
import com.goetypunk.addon.common.block.entity.RemoteEngineBlockEntity;
import com.goetypunk.addon.common.block.entity.SquadBarracksBlockEntity;
import com.goetypunk.addon.common.block.entity.StressArkBlockEntity;
import com.goetypunk.addon.common.block.entity.StressReaderBlockEntity;
import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 方块注册
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GoetyPunkMod.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GoetyPunkMod.MOD_ID);

    public static final RegistryObject<Block> FLESH_FURNACE =
            BLOCKS.register("flesh_furnace", FleshFurnaceBlock::new);

    public static final RegistryObject<BlockEntityType<FleshFurnaceBlockEntity>> FLESH_FURNACE_ENTITY =
            BLOCK_ENTITIES.register("flesh_furnace",
                    () -> BlockEntityType.Builder.of(FleshFurnaceBlockEntity::new, FLESH_FURNACE.get()).build(null));

    // ── 育巢 ──

    public static final RegistryObject<Block> BREEDING_NEST =
            BLOCKS.register("breeding_nest", BreedingNestBlock::new);

    public static final RegistryObject<BlockEntityType<BreedingNestBlockEntity>> BREEDING_NEST_ENTITY =
            BLOCK_ENTITIES.register("breeding_nest",
                    () -> BlockEntityType.Builder.of(BreedingNestBlockEntity::new, BREEDING_NEST.get()).build(null));

    // ── 点将台三阶方块 ──

    public static final RegistryObject<Block> TIER1_SQUAD_BARRACKS =
            BLOCKS.register("tier1_squad_barracks", Tier1SquadBarracksBlock::new);

    public static final RegistryObject<Block> TIER2_SQUAD_BARRACKS =
            BLOCKS.register("tier2_squad_barracks", Tier2SquadBarracksBlock::new);

    public static final RegistryObject<Block> TIER3_SQUAD_BARRACKS =
            BLOCKS.register("tier3_squad_barracks", Tier3SquadBarracksBlock::new);

    public static final RegistryObject<BlockEntityType<SquadBarracksBlockEntity>> SQUAD_BARRACKS_ENTITY =
            BLOCK_ENTITIES.register("squad_barracks",
                    () -> BlockEntityType.Builder.of(SquadBarracksBlockEntity::new,
                            TIER1_SQUAD_BARRACKS.get(),
                            TIER2_SQUAD_BARRACKS.get(),
                            TIER3_SQUAD_BARRACKS.get()).build(null));

    // ── 应力读取器 ──

    public static final RegistryObject<Block> STRESS_READER =
            BLOCKS.register("stress_reader", StressReaderBlock::new);

    public static final RegistryObject<BlockEntityType<StressReaderBlockEntity>> STRESS_READER_ENTITY =
            BLOCK_ENTITIES.register("stress_reader",
                    () -> BlockEntityType.Builder.of(
                            StressReaderBlockEntity::new,
                            STRESS_READER.get()).build(null));

    // ── 远程引擎 ──

    public static final RegistryObject<Block> REMOTE_ENGINE =
            BLOCKS.register("remote_engine", RemoteEngineBlock::new);

    public static final RegistryObject<BlockEntityType<RemoteEngineBlockEntity>> REMOTE_ENGINE_ENTITY =
            BLOCK_ENTITIES.register("remote_engine",
                    () -> BlockEntityType.Builder.of(
                            RemoteEngineBlockEntity::new,
                            REMOTE_ENGINE.get()).build(null));

    // ── 应力方舟 ──

    public static final RegistryObject<Block> STRESS_ARK =
            BLOCKS.register("stress_ark", StressArkBlock::new);

    public static final RegistryObject<BlockEntityType<StressArkBlockEntity>> STRESS_ARK_ENTITY =
            BLOCK_ENTITIES.register("stress_ark",
                    () -> BlockEntityType.Builder.of(
                            StressArkBlockEntity::new,
                            STRESS_ARK.get()).build(null));

    public static void registerStressValues(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockStressValues.IMPACTS.register(STRESS_READER.get(), () -> 0.0);
            BlockStressValues.CAPACITIES.register(REMOTE_ENGINE.get(), () -> 0.0);
            BlockStressValues.IMPACTS.register(REMOTE_ENGINE.get(), () -> 0.0);
        });
    }
}
