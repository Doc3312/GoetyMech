package com.goetypunk.addon.client;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.client.gui.SquadBarracksScreen;
import com.goetypunk.addon.client.render.CrushingWheelRenderer;
import com.goetypunk.addon.client.render.RemoteEngineRenderer;
import com.goetypunk.addon.client.render.SculkBidingModel;
import com.goetypunk.addon.client.render.SculkBidingRenderer;
import com.goetypunk.addon.common.menu.ModMenuTypes;
import com.goetypunk.addon.init.ModBlocks;
import com.goetypunk.addon.init.ModEntities;
import com.goetypunk.addon.init.ModItems;
import com.goetypunk.addon.common.item.SquadFocus;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.SQUAD_BARRACKS.get(), SquadBarracksScreen::new);
            net.minecraft.client.renderer.item.ItemProperties.register(
                    ModItems.SQUAD_FOCUS.get(),
                    new net.minecraft.resources.ResourceLocation(com.goetypunk.addon.GoetyPunkMod.MOD_ID, "bound"),
                    (stack, world, entity, seed) -> SquadFocus.isBound(stack) ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SculkBidingRenderer.LAYER, SculkBidingModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(GoetyPunkMod.MOD_ID, "entity/crushing_wheel"));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SCULK_SPIKE.get(), SculkBidingRenderer::new);
        event.registerEntityRenderer(ModEntities.CRUSHING_WHEEL.get(), CrushingWheelRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.STRESS_READER_ENTITY.get(),
                KineticBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.REMOTE_ENGINE_ENTITY.get(),
                RemoteEngineRenderer::new);
    }
}