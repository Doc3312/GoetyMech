package com.goetypunk.addon.common.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class PrototypeMimicItem extends ElytraItem {

    public PrototypeMimicItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return !PrototypeItem.isSlamming(entity);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
