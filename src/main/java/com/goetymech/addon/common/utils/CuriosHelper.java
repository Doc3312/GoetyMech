package com.goetymech.addon.common.utils;

import com.goetymech.addon.common.item.PrototypeItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosHelper {

    public static boolean hasPrototypeInCurios(Player player) {
        try {
            var inv = CuriosApi.getCuriosInventory(player);
            return inv.map(i -> i.findFirstCurio(
                    s -> PrototypeItem.isPrototype(s)).isPresent()).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasMimicInCurios(Player player) {
        try {
            var inv = CuriosApi.getCuriosInventory(player);
            return inv.map(i -> i.findFirstCurio(
                    s -> s.getItem() instanceof com.goetymech.addon.common.item.PrototypeMimicItem).isPresent()).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
