package com.goetypunk.addon.common.utils;

import com.goetypunk.addon.common.item.PrototypeItem;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosHelper {

    public static boolean hasPrototypeInCurios(Player player) {
        try {
            var inv = CuriosApi.getCuriosInventory(player);
            return inv.map(i -> i.findFirstCurio(
                    s -> s.getItem() instanceof PrototypeItem).isPresent()).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
