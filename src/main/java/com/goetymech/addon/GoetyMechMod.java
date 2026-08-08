package com.goetymech.addon;

import com.goetymech.addon.common.block.entity.WitchCondenserBlockEntity;
import com.goetymech.addon.common.capabilities.IStressShield;
import com.goetymech.addon.common.capabilities.StressShieldProvider;
import com.goetymech.addon.common.capabilities.ISoulDominion;
import com.goetymech.addon.common.capabilities.SoulDominionProvider;
import com.goetymech.addon.common.data.CondenserProductManager;
import com.goetymech.addon.common.events.SoulDominionEvents;
import com.goetymech.addon.common.events.StressLinkEvents;
import com.goetymech.addon.common.events.StressShieldEvents;
import com.goetymech.addon.common.events.ChaseEvents;
import com.Polarice3.Goety.api.magic.SpellType;
import com.goetymech.addon.common.menu.ModMenuTypes;
import com.goetymech.addon.common.utils.GlobalStressLinkManager;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModBlocks;
import com.goetymech.addon.init.ModCreativeTab;
import com.goetymech.addon.init.ModEffects;
import com.goetymech.addon.init.ModEntities;
import com.goetymech.addon.init.ModItems;
import com.goetymech.addon.init.ModParticleTypes;
import com.goetymech.addon.init.ModRecipeSerializers;
import com.goetymech.addon.init.ModFluids;
import com.goetymech.addon.init.ModRituals;
import com.goetymech.addon.init.ModRitualTypes;
import com.goetymech.addon.common.entity.CrazedIllagerEntity;
import com.goetymech.addon.common.entity.CrazedIllagerServant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * GoetyMech 主模组类
 */
@Mod(GoetyMechMod.MOD_ID)
public class GoetyMechMod {

    public static final String MOD_ID = "goetymech";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final GlobalStressLinkManager STRESS_LINK = new GlobalStressLinkManager();
    public static SpellType MECHANICAL;

    public GoetyMechMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC, "goetymech-common.toml");
        ModConfig.loadConfig(ModConfig.SPEC, FMLPaths.CONFIGDIR.get().resolve("goetymech-common.toml").toString());

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModCreativeTab.TABS.register(modEventBus);
        ModMenuTypes.CONTAINER_TYPE.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModRituals.RITUALS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.BLOCKS.register(modEventBus);
        ModFluids.ITEMS.register(modEventBus);

        ModNetwork.init();
        modEventBus.addListener((FMLCommonSetupEvent event) -> {
            event.enqueueWork(() -> {
                MECHANICAL = SpellType.create("mechanical", "mechanical");
                ModRitualTypes.register();
                ModFluids.registerContactEffects();
            });
        });
        modEventBus.addListener(ModBlocks::registerStressValues);
        modEventBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(ModEntities.CRAZED_ILLAGER.get(), CrazedIllagerEntity.createAttributes().build());
            event.put(ModEntities.CRAZED_ILLAGER_SERVANT.get(), CrazedIllagerServant.setCustomAttributes().build());
        });

        MinecraftForge.EVENT_BUS.register(ChaseEvents.class);
        MinecraftForge.EVENT_BUS.register(SoulDominionEvents.class);
        MinecraftForge.EVENT_BUS.register(WitchCondenserBlockEntity.class);
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) ->
                event.addListener(CondenserProductManager.INSTANCE));
        modEventBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.register(IStressShield.class);
            event.register(ISoulDominion.class);
        });

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class,
                (AttachCapabilitiesEvent<Entity> event) -> {
                    if (event.getObject() instanceof Player) {
                        event.addCapability(
                                new ResourceLocation(MOD_ID, "stress_shield"),
                                new StressShieldProvider());
                        event.addCapability(
                                new ResourceLocation(MOD_ID, "soul_dominion"),
                                new SoulDominionProvider());
                    }
                });
        LOGGER.info("Prototype chest slot only — Curios support removed");
    }
}