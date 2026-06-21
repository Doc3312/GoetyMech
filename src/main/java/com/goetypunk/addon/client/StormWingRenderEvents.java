package com.goetypunk.addon.client;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.StormWingItem;
import com.goetypunk.addon.common.net.ModNetwork;
import com.goetypunk.addon.common.net.c2s.C2SCancelSlamPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class StormWingRenderEvents {

    private static final ResourceLocation STORM_WING_TEXTURE =
            new ResourceLocation(GoetyPunkMod.MOD_ID, "textures/entity/storm_wing.png");

    private static boolean prevJumpDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;
        boolean jumpDown = Minecraft.getInstance().options.keyJump.isDown();
        boolean pressed = jumpDown && !prevJumpDown;
        prevJumpDown = jumpDown;
        if (!pressed) return;
        ModNetwork.CHANNEL.sendToServer(new C2SCancelSlamPacket());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasInChest = chestStack.getItem() instanceof StormWingItem;

        if (!hasInChest) {
            try {
                var curiosInv = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);
                hasInChest = curiosInv.map(inv -> inv.findFirstCurio(
                        s -> s.getItem() instanceof StormWingItem).isPresent()).orElse(false);
            } catch (Exception ignored) {}
        }

        if (!hasInChest) return;

        poseStack.pushPose();
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

        model.body.translateAndRotate(poseStack);
        poseStack.translate(0, 0.25, -0.15);
        poseStack.scale(1.8F, 1.8F, 1.8F);

        VertexConsumer consumer = ItemRenderer.getFoilBuffer(
                buffer, RenderType.entityCutout(STORM_WING_TEXTURE), false, chestStack.hasFoil());
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}
