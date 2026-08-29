package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.client.gui.FleshCasketScreen;
import com.goetymech.addon.client.gui.SquadBarracksScreen;
import com.goetymech.addon.client.particles.ScaledGodRayParticle;
import com.goetymech.addon.client.render.CraterBlockRenderer;
import com.goetymech.addon.client.render.CrazedIllagerModel;
import com.goetymech.addon.client.render.CrazedIllagerRenderer;
import com.goetymech.addon.client.render.CrushingWheelRenderer;
import com.goetymech.addon.client.render.WitchCondenserRenderer;
import com.goetymech.addon.client.render.RemoteEngineRenderer;
import com.goetymech.addon.client.render.SculkBidingModel;
import com.goetymech.addon.client.render.SculkBidingRenderer;
import com.goetymech.addon.client.particles.ScaledGodRayParticle;
import com.goetymech.addon.client.render.StressShieldLayer;
import com.goetymech.addon.client.render.StressReaderRenderer;
import com.goetymech.addon.common.menu.ModMenuTypes;
import com.goetymech.addon.init.ModBlocks;
import com.goetymech.addon.init.ModEntities;
import com.goetymech.addon.init.ModFluids;
import com.goetymech.addon.init.ModItems;
import com.goetymech.addon.init.ModParticleTypes;
import com.goetymech.addon.common.item.SquadFocus;
import com.goetymech.addon.common.item.curio.FleshCasketItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.ArrayUtils;

import com.goetymech.addon.client.GoetyMechPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModClientEvents");

    public static boolean freecamActive = false;
    public static KeyMapping fleshCasketKey;
    public static KeyMapping freecamExitKey;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        event.enqueueWork(() -> {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.SOUL_LIQUID_SOURCE.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.SOUL_LIQUID_FLOWING.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.VITALITY_LIQUID_SOURCE.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.VITALITY_LIQUID_FLOWING.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.HUNGER_LIQUID_SOURCE.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.HUNGER_LIQUID_FLOWING.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.MYSTIC_LIQUID_SOURCE.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.MYSTIC_LIQUID_FLOWING.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.GALE_LIQUID_SOURCE.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModFluids.GALE_LIQUID_FLOWING.get(),
                    net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.SQUAD_BARRACKS.get(), SquadBarracksScreen::new);
            net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.FLESH_CASKET.get(), FleshCasketScreen::new);
            net.minecraft.client.renderer.item.ItemProperties.register(
                    ModItems.SQUAD_FOCUS.get(),
                    new net.minecraft.resources.ResourceLocation(com.goetymech.addon.GoetyMechMod.MOD_ID, "bound"),
                    (stack, world, entity, seed) -> SquadFocus.isBound(stack) ? 1.0F : 0.0F);

            var triggerProp = new net.minecraft.resources.ResourceLocation(GoetyMechMod.MOD_ID, "trigger_type");
            net.minecraft.client.renderer.item.ItemProperties.register(
                    ModItems.FLESH_CASKET_1.get(), triggerProp,
                    (stack, world, entity, seed) -> switch (FleshCasketItem.getTriggerType(stack)) {
                        case A -> 0.0F;
                        case B -> 0.5F;
                        case C -> 1.0F;
                    });
            net.minecraft.client.renderer.item.ItemProperties.register(
                    ModItems.FLESH_CASKET_2.get(), triggerProp,
                    (stack, world, entity, seed) -> switch (FleshCasketItem.getTriggerType(stack)) {
                        case A -> 0.0F;
                        case B -> 0.5F;
                        case C -> 1.0F;
                    });
            net.minecraft.client.renderer.item.ItemProperties.register(
                    ModItems.FLESH_CASKET_3.get(), triggerProp,
                    (stack, world, entity, seed) -> switch (FleshCasketItem.getTriggerType(stack)) {
                        case A -> 0.0F;
                        case B -> 0.5F;
                        case C -> 1.0F;
                    });

            fleshCasketKey = new KeyMapping("key.goetymech.open_flesh_casket",
                    GLFW.GLFW_KEY_Z, "key.goetymech.category");
            Minecraft.getInstance().options.keyMappings =
                    ArrayUtils.add(Minecraft.getInstance().options.keyMappings, fleshCasketKey);

            freecamExitKey = new KeyMapping("key.goetymech.freecam_exit",
                    GLFW.GLFW_KEY_R, "key.goetymech.category");
            Minecraft.getInstance().options.keyMappings =
                    ArrayUtils.add(Minecraft.getInstance().options.keyMappings, freecamExitKey);

            // 巫术冷凝机：双轨 Flywheel Visual（背面 2px 短轴帽，与 BER 同源）
            SimpleBlockEntityVisualizer.builder(ModBlocks.WITCH_CONDENSER_ENTITY.get())
                    .factory((context, be, pt) -> {
                        Direction facing = be.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
                        return new OrientedRotatingVisual<>(context, be, pt, Direction.SOUTH, facing,
                                Models.partial(GoetyMechPartialModels.WITCH_CONDENSER_SHAFT_STUB));
                    })
                    .skipVanillaRender(be -> true)
                    .apply();

        });

        LOGGER.info("[MOD_CLIENT] Flywheel Visuals DISABLED for diagnosis - using BESR only");
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SculkBidingRenderer.LAYER, SculkBidingModel::createBodyLayer);
        event.registerLayerDefinition(CrazedIllagerModel.LAYER, CrazedIllagerModel::createBodyLayer);
        event.registerLayerDefinition(StressShieldLayer.LAYER, StressShieldLayer::createBodyLayer);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(skin -> {
            var renderer = event.getSkin(skin);
            if (renderer != null) {
                addStressShieldLayer(renderer, event.getEntityModels());
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addStressShieldLayer(net.minecraft.client.renderer.entity.LivingEntityRenderer renderer,
                                              net.minecraft.client.model.geom.EntityModelSet models) {
        renderer.addLayer(new StressShieldLayer<>(renderer, models));
    }

    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(GoetyMechMod.MOD_ID, "entity/crushing_wheel"));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SCULK_SPIKE.get(), SculkBidingRenderer::new);
        event.registerEntityRenderer(ModEntities.CRUSHING_WHEEL.get(), CrushingWheelRenderer::new);
        event.registerEntityRenderer(ModEntities.CRAZED_ILLAGER.get(), CrazedIllagerRenderer::new);
        event.registerEntityRenderer(ModEntities.CRAZED_ILLAGER_SERVANT.get(), CrazedIllagerRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.STRESS_READER_ENTITY.get(),
                StressReaderRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.REMOTE_ENGINE_ENTITY.get(),
                RemoteEngineRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CRATER_ENTITY.get(),
                CraterBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.WITCH_CONDENSER_ENTITY.get(),
                WitchCondenserRenderer::new);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "stress_ark_hud", StressArkHUD.OVERLAY);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "freecam_hud", FreecamHUD.OVERLAY);
        event.registerAboveAll("stress_shield_overlay", StressShieldOverlay.OVERLAY);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.SCALED_GOD_RAY.get(), ScaledGodRayParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(ModItems.PROTOTYPE_MIMIC.get(), new MimicItemDecorator());
    }

    @SubscribeEvent
    public static void registerItemColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
        registerLiquidBottleColor(event, ModFluids.VITALITY_LIQUID_BOTTLE.get(), ModFluids.VITALITY_LIQUID_SOURCE.get());
        registerLiquidBottleColor(event, ModFluids.HUNGER_LIQUID_BOTTLE.get(), ModFluids.HUNGER_LIQUID_SOURCE.get());
        registerLiquidBottleColor(event, ModFluids.MYSTIC_LIQUID_BOTTLE.get(), ModFluids.MYSTIC_LIQUID_SOURCE.get());
        registerLiquidBottleColor(event, ModFluids.GALE_LIQUID_BOTTLE.get(), ModFluids.GALE_LIQUID_SOURCE.get());

        registerLiquidBucketColor(event, ModFluids.SOUL_LIQUID_BUCKET.get());
        registerLiquidBucketColor(event, ModFluids.VITALITY_LIQUID_BUCKET.get());
        registerLiquidBucketColor(event, ModFluids.HUNGER_LIQUID_BUCKET.get());
        registerLiquidBucketColor(event, ModFluids.MYSTIC_LIQUID_BUCKET.get());
        registerLiquidBucketColor(event, ModFluids.GALE_LIQUID_BUCKET.get());
    }

    private static void registerLiquidBottleColor(
            net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event,
            net.minecraft.world.item.Item bottle, net.minecraft.world.level.material.Fluid fluid) {
        event.register((stack, layer) -> layer == 0
                ? fluidTint(fluid)
                : 0xFFFFFFFF, bottle);
    }

    private static void registerLiquidBucketColor(
            net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event,
            net.minecraft.world.item.Item bucket) {
        event.register((stack, layer) -> {
            if (layer != 1) {
                return 0xFFFFFFFF;
            }
            return net.minecraftforge.fluids.FluidUtil.getFluidContained(stack)
                    .filter(contained -> !contained.isEmpty())
                    .map(contained -> net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(contained.getFluid()).getTintColor(contained))
                    .orElse(0xFFFFFFFF);
        }, bucket);
    }

    private static int fluidTint(net.minecraft.world.level.material.Fluid fluid) {
        return net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid)
                .getTintColor(net.minecraftforge.fluids.FluidStack.EMPTY);
    }

}