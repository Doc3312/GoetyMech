package com.goetymech.addon.common.ritual;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.Polarice3.Goety.common.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

public class UpgradeMonumentRitual extends Ritual {
    public UpgradeMonumentRitual(RitualRecipe recipe) {
        super(recipe);
    }

    @Override
    public void finish(Level world, BlockPos blockPos, DarkAltarBlockEntity tileEntity,
                       Player castingPlayer, ItemStack activationItem) {
        super.finish(world, blockPos, tileEntity, castingPlayer, activationItem);
        ItemStack result = this.recipe.getResultItem(world.registryAccess()).copy();
        if (activationItem.hasTag()) {
            CompoundTag tag = activationItem.getTag().copy();
            tag.remove("flesh_casket_cooldown");
            result.setTag(tag);
        }
        activationItem.shrink(1);
        result.onCraftedBy(world, castingPlayer, 1);
        IItemHandler handler = tileEntity.itemStackHandler.orElseThrow(RuntimeException::new);
        handler.insertItem(0, result, false);
    }
}
