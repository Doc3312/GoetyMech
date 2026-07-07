package com.goetymech.addon.common.events;

import com.goetymech.addon.common.utils.StressLinkService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class StressLinkEvents {

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel sl && sl.dimension() == Level.OVERWORLD) {
            StressLinkService service = StressLinkService.get(sl);
            if (service != null) service.clear();
        }
    }

    // 服务端 tick 末尾统一推送 dirty readers，避免在网络 sync 遍历期间
    // 因修改 engine 网络 members 导致 ConcurrentModificationException
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        StressLinkService service = StressLinkService.get(event.getServer());
        if (service != null) service.tick();
    }
}
