package com.goetymech.addon.common.events;

import com.Polarice3.Goety.common.entities.projectiles.EyeItemEntity;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.utils.GatewayUtil;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EyeCaptureHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!GatewayUtil.pendingEyeCapture) return;
        if (event.getEntity() instanceof EyeOfEnder || event.getEntity() instanceof EyeItemEntity) {
            event.setCanceled(true);
        }
    }
}
