package com.goetypunk.addon.common.utils;

import com.goetypunk.addon.common.item.StormWingItem;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosHelper {

    public static boolean hasStormWingInCurios(Player player) {
        try {
            var inv = CuriosApi.getCuriosInventory(player);
            return inv.map(i -> i.findFirstCurio(
                    s -> s.getItem() instanceof StormWingItem).isPresent()).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
