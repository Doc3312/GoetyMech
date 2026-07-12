package com.goetymech.addon.common.ritual.type;

import com.Polarice3.Goety.api.ritual.IRitualType;
import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.RitualBlockEntity;
import com.Polarice3.Goety.common.ritual.Ritual;
import com.Polarice3.Goety.common.ritual.RitualChecker;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class MechanicalRitualType implements IRitualType {
    @Override
    public String getName() {
        return "mechanical";
    }

    @Override
    public ItemStack getJeiIcon() {
        return new ItemStack(AllBlocks.MECHANICAL_PRESS.get());
    }

    @Override
    public boolean getRequirement(RitualBlockEntity pTileEntity, Player pPlayer, BlockPos pPos, Level pLevel) {
        int range = Ritual.RANGE;
        RitualChecker finder = new RitualChecker(pLevel, pPos, s -> true, range, 0);

        Predicate<BlockState> andesiteCasing = s -> s.is(AllBlocks.ANDESITE_CASING.get());
        if (!finder.hasBlocks(andesiteCasing, 16)) {
            if (pPlayer != null) {
                pPlayer.displayClientMessage(Component.translatable("info.goety.ritual.structure.noBlocks",
                        AllBlocks.ANDESITE_CASING.get().getName()), true);
            }
            return false;
        }

        Predicate<BlockState> cauldron = s -> s.getBlock() instanceof AbstractCauldronBlock;
        if (!finder.hasBlocks(cauldron, 2)) {
            if (pPlayer != null) {
                pPlayer.displayClientMessage(Component.translatable("info.goety.ritual.structure.noBlocks",
                        Blocks.CAULDRON.getName()), true);
            }
            return false;
        }

        Predicate<BlockState> press = s -> s.is(AllBlocks.MECHANICAL_PRESS.get());
        if (!finder.hasBlocks(press, 1)) {
            if (pPlayer != null) {
                pPlayer.displayClientMessage(Component.translatable("info.goety.ritual.structure.noBlocks",
                        AllBlocks.MECHANICAL_PRESS.get().getName()), true);
            }
            return false;
        }

        Predicate<BlockState> anvil = s -> s.getBlock() instanceof AnvilBlock;
        if (!finder.hasBlocks(anvil, 1)) {
            if (pPlayer != null) {
                pPlayer.displayClientMessage(Component.translatable("info.goety.ritual.structure.noBlocks",
                        Blocks.ANVIL.getName()), true);
            }
            return false;
        }

        Predicate<BlockState> smithingTable = s -> s.getBlock() instanceof SmithingTableBlock;
        if (!finder.hasBlocks(smithingTable, 1)) {
            if (pPlayer != null) {
                pPlayer.displayClientMessage(Component.translatable("info.goety.ritual.structure.noBlocks",
                        Blocks.SMITHING_TABLE.getName()), true);
            }
            return false;
        }

        Predicate<BlockState> sawOrStonecutter = s ->
                s.is(AllBlocks.MECHANICAL_SAW.get()) || s.getBlock() instanceof StonecutterBlock;
        if (!finder.hasBlocks(sawOrStonecutter, 1)) {
            if (pPlayer != null) {
                pPlayer.displayClientMessage(Component.translatable("info.goety.ritual.structure.noBlocks",
                        AllBlocks.MECHANICAL_SAW.get().getName()), true);
            }
            return false;
        }

        return true;
    }

    @Override
    public void onFinishRitual(Level world, BlockPos darkAltarPos, DarkAltarBlockEntity tileEntity,
                               Player castingPlayer, ItemStack activationItem) {
        world.playSound(null, darkAltarPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS,
                1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
    }
}
