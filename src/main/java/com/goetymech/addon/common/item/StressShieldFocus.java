package com.goetymech.addon.common.item;

import com.goetymech.addon.common.magic.spell.StressShieldSpell;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.common.items.magic.MagicFocus;
import com.Polarice3.Goety.utils.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class StressShieldFocus extends MagicFocus {

    private static final String TAG_BOUND = "goetymech.shield_bound";
    private static final String TAG_BLOCK_X = "goetymech.shield_bx";
    private static final String TAG_BLOCK_Y = "goetymech.shield_by";
    private static final String TAG_BLOCK_Z = "goetymech.shield_bz";
    private static final String TAG_DIMENSION = "goetymech.shield_dim";

    public StressShieldFocus() {
        super(new StressShieldSpell());
    }

    public static void bindToReader(ItemStack stack, Level level, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_BOUND, true);
        tag.putInt(TAG_BLOCK_X, pos.getX());
        tag.putInt(TAG_BLOCK_Y, pos.getY());
        tag.putInt(TAG_BLOCK_Z, pos.getZ());
        tag.putString(TAG_DIMENSION, level.dimension().location().toString());
    }

    public static boolean isBound(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_BOUND);
    }

    @Nullable
    public static BlockPos getBoundPos(ItemStack stack) {
        if (!isBound(stack) || !stack.hasTag()) return null;
        CompoundTag tag = stack.getTag();
        return new BlockPos(tag.getInt(TAG_BLOCK_X), tag.getInt(TAG_BLOCK_Y), tag.getInt(TAG_BLOCK_Z));
    }

    @Nullable
    public static String getBoundDimension(ItemStack stack) {
        if (!isBound(stack) || !stack.hasTag()) return null;
        return stack.getTag().getString(TAG_DIMENSION);
    }

    public static void clearBinding(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            tag.remove(TAG_BOUND);
            tag.remove(TAG_BLOCK_X);
            tag.remove(TAG_BLOCK_Y);
            tag.remove(TAG_BLOCK_Z);
            tag.remove(TAG_DIMENSION);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && isBound(stack)) {
                clearBinding(stack);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.stress_shield.unbound"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("info.goety.focus.cost",
                ModConfig.STRESS_SHIELD_COST.get()));
        tooltip.add(Component.translatable("info.goety.focus.spellType",
                this.spell.getSpellType().getName()));
        tooltip.add(Component.translatable("item.goety.focus.info")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE));

        if (isBound(stack)) {
            tooltip.add(Component.translatable("info.goetymech.stress_shield.bound")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("info.goetymech.stress_shield.not_bound")
                    .withStyle(ChatFormatting.RED));
        }

        ItemHelper.addOnShift(tooltip, () -> {
            tooltip.add(Component.translatable("info.goetymech.stress_shield.shift_info")
                    .withStyle(ChatFormatting.GRAY));
        });
    }
}
