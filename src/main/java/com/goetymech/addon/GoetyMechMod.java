package com.goetymech.addon;

import com.goetymech.addon.common.capabilities.IStressShield;
import com.goetymech.addon.common.capabilities.StressShieldProvider;
import com.goetymech.addon.common.events.StressShieldEvents;
import com.goetymech.addon.common.events.VoidBlinkEvents;
import com.goetymech.addon.common.menu.ModMenuTypes;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.compat.caelus.CaelusLoaded;
import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModBlocks;
import com.goetymech.addon.init.ModCreativeTab;
import com.goetymech.addon.init.ModEffects;
import com.goetymech.addon.init.ModEntities;
import com.goetymech.addon.init.ModItems;
import com.goetymech.addon.init.ModRecipeSerializers;
import com.goetymech.addon.init.ModFluids;
import com.goetymech.addon.init.ModRituals;
import com.goetymech.addon.common.entity.CrazedIllagerEntity;
import com.goetymech.addon.common.entity.CrazedIllagerServant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * goetymech 涓绘ā缁勭被
 */
@Mod(GoetyMechMod.MOD_ID)
public class GoetyMechMod {

    public static final String MOD_ID = "goetymech";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public GoetyMechMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC, "goetymech-common.toml");
        ModConfig.loadConfig(ModConfig.SPEC, FMLPaths.CONFIGDIR.get().resolve("goetymech-common.toml").toString());

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
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
        modEventBus.addListener(ModBlocks::registerStressValues);
        modEventBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(ModEntities.CRAZED_ILLAGER.get(), CrazedIllagerEntity.createAttributes().build());
            event.put(ModEntities.CRAZED_ILLAGER_SERVANT.get(), CrazedIllagerServant.setCustomAttributes().build());
        });

        MinecraftForge.EVENT_BUS.register(StressShieldEvents.class);
        MinecraftForge.EVENT_BUS.register(VoidBlinkEvents.class);

        modEventBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.register(IStressShield.class);
        });

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class,
                (AttachCapabilitiesEvent<Entity> event) -> {
                    if (event.getObject() instanceof Player) {
                        event.addCapability(
                                new ResourceLocation(MOD_ID, "stress_shield"),
                                new StressShieldProvider());
                    }
                });

        // 鏉′欢娉ㄥ唽 Caelus 鍏煎 鈥?閬垮厤绫诲姞杞芥壘涓嶅埌 CaelusApi 瀵艰嚧宕╂簝
        if (ModList.get().isLoaded("caelus")) {
            try {
                Class<?> handler = Class.forName("com.goetymech.addon.compat.caelus.CaelusFlightHandler");
                MinecraftForge.EVENT_BUS.register(handler);
                LOGGER.info("Caelus detected 鈥?Prototype Curios flight enabled");
            } catch (Throwable e) {
                LOGGER.warn("CaelusFlightHandler not available: {}", e.getMessage());
            }
        } else {
            LOGGER.info("Caelus not detected 鈥?Prototype chest slot only");
        }
    }
}