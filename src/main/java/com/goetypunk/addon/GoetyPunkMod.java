package com.goetypunk.addon;

import com.goetypunk.addon.common.capabilities.IStressShield;
import com.goetypunk.addon.common.capabilities.StressShieldProvider;
import com.goetypunk.addon.common.events.StressShieldEvents;
import com.goetypunk.addon.common.events.VoidBlinkEvents;
import com.goetypunk.addon.common.menu.ModMenuTypes;
import com.goetypunk.addon.common.net.ModNetwork;
import com.goetypunk.addon.compat.caelus.CaelusLoaded;
import com.goetypunk.addon.config.ModConfig;
import com.goetypunk.addon.init.ModBlocks;
import com.goetypunk.addon.init.ModCreativeTab;
import com.goetypunk.addon.init.ModEffects;
import com.goetypunk.addon.init.ModEntities;
import com.goetypunk.addon.init.ModItems;
import com.goetypunk.addon.init.ModRecipeSerializers;
import com.goetypunk.addon.common.entity.CrazedIllagerEntity;
import com.goetypunk.addon.common.entity.CrazedIllagerServant;
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
 * GoetyPunk 主模组类
 */
@Mod(GoetyPunkMod.MOD_ID)
public class GoetyPunkMod {

    public static final String MOD_ID = "goetypunk";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public GoetyPunkMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC, "goetypunk-common.toml");
        ModConfig.loadConfig(ModConfig.SPEC, FMLPaths.CONFIGDIR.get().resolve("goetypunk-common.toml").toString());

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModCreativeTab.TABS.register(modEventBus);
        ModMenuTypes.CONTAINER_TYPE.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

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

        // 条件注册 Caelus 兼容 — 避免类加载找不到 CaelusApi 导致崩溃
        if (ModList.get().isLoaded("caelus")) {
            try {
                Class<?> handler = Class.forName("com.goetypunk.addon.compat.caelus.CaelusFlightHandler");
                MinecraftForge.EVENT_BUS.register(handler);
                LOGGER.info("Caelus detected — Prototype Curios flight enabled");
            } catch (Throwable e) {
                LOGGER.warn("CaelusFlightHandler not available: {}", e.getMessage());
            }
        } else {
            LOGGER.info("Caelus not detected — Prototype chest slot only");
        }
    }
}