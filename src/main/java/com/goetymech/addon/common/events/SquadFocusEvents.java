package com.goetymech.addon.common.events;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.SquadFocus;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SquadFocusEvents {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) return;

        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IWand) {
            ItemStack focus = IWand.getFocus(stack);
            if (focus.getItem() instanceof SquadFocus && SquadFocus.isBound(focus)) {
                if (player.level() instanceof ServerLevel) {
                    SEHelper.getFocusCoolDown(player).removeCooldown(player, player.level(), focus.getItem());
                }
            }
        }
    }
}
