package com.goetymech.addon.common.item;

import com.goetymech.addon.init.ModBlocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SoulMonumentBlockItem extends BlockItem {
    public SoulMonumentBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        entity.setInvulnerable(true);
        return false;
    }

    public static boolean isSoulMonument(ItemStack stack) {
        return stack.getItem() instanceof SoulMonumentBlockItem
                && (stack.is(ModBlocks.TIER1_SOUL_MONUMENT.get().asItem())
                || stack.is(ModBlocks.TIER2_SOUL_MONUMENT.get().asItem())
                || stack.is(ModBlocks.TIER3_SOUL_MONUMENT.get().asItem()));
    }
}
