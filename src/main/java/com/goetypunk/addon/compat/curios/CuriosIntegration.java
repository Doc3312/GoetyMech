package com.goetypunk.addon.compat.curios;

import com.goetypunk.addon.GoetyPunkMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CuriosIntegration {

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void sendImc(InterModEnqueueEvent event) {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BELT.getMessageBuilder().build());
        // StormWing 需要背部槽和身体槽
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BACK.getMessageBuilder().build());
    }
}
