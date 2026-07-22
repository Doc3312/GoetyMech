package com.goetymech.addon.common.item;

import com.goetymech.addon.common.magic.spell.RemoteStorageSpell;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.common.items.magic.MagicFocus;
import com.Polarice3.Goety.utils.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 远程仓储聚晶。绑定 Stock Ticker 后远程打开 GUI。
 */
public class RemoteStorageFocus extends MagicFocus {

    private static final String TAG_BOUND = "goetymech.st_bound";
    private static final String TAG_BLOCK_X = "goetymech.st_bx";
    private static final String TAG_BLOCK_Y = "goetymech.st_by";
    private static final String TAG_BLOCK_Z = "goetymech.st_bz";
    private static final String TAG_DIMENSION = "goetymech.st_dim";
    private static final String TAG_FREQ_ID = "goetymech.st_freq";

    public RemoteStorageFocus() {
        super(new RemoteStorageSpell());
    }

    public static void bindToStockTicker(ItemStack stack, Level level, BlockPos pos, UUID freqId) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_BOUND, true);
        tag.putInt(TAG_BLOCK_X, pos.getX());
        tag.putInt(TAG_BLOCK_Y, pos.getY());
        tag.putInt(TAG_BLOCK_Z, pos.getZ());
        tag.putString(TAG_DIMENSION, level.dimension().location().toString());
        if (freqId != null) {
            tag.putUUID(TAG_FREQ_ID, freqId);
        }
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
    public static ResourceLocation getBoundDimension(ItemStack stack) {
        if (!isBound(stack) || !stack.hasTag()) return null;
        return ResourceLocation.tryParse(stack.getTag().getString(TAG_DIMENSION));
    }

    @Nullable
    public static UUID getBoundFreqId(ItemStack stack) {
        if (!isBound(stack) || !stack.hasTag() || !stack.getTag().hasUUID(TAG_FREQ_ID)) return null;
        return stack.getTag().getUUID(TAG_FREQ_ID);
    }

    public static void clearBinding(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            tag.remove(TAG_BOUND);
            tag.remove(TAG_BLOCK_X);
            tag.remove(TAG_BLOCK_Y);
            tag.remove(TAG_BLOCK_Z);
            tag.remove(TAG_DIMENSION);
            tag.remove(TAG_FREQ_ID);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && isBound(stack)) {
                clearBinding(stack);
                player.displayClientMessage(
                        Component.translatable("info.goetymech.remote_storage_focus.unbound"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("info.goety.focus.cost",
                ModConfig.STOCK_TICKER_FOCUS_COST.get()));
        tooltip.add(Component.translatable("info.goety.focus.spellType",
                this.spell.getSpellType().getName()));
        tooltip.add(Component.translatable("item.goety.focus.info")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE));

        if (isBound(stack)) {
            BlockPos pos = getBoundPos(stack);
            ResourceLocation dim = getBoundDimension(stack);
            String dimName = dim != null ? dim.toString() : "?";
            tooltip.add(Component.translatable("info.goetymech.remote_storage_focus.bound")
                    .withStyle(ChatFormatting.GREEN));
            if (pos != null) {
                tooltip.add(Component.translatable("info.goetymech.remote_storage_focus.bound_pos",
                                pos.getX(), pos.getY(), pos.getZ(), dimName)
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("info.goetymech.remote_storage_focus.not_bound")
                    .withStyle(ChatFormatting.RED));
        }

        ItemHelper.addOnShift(tooltip, () -> {
            if (isBound(stack)) {
                tooltip.add(Component.translatable("info.goetymech.remote_storage_focus.shift_bound")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("info.goetymech.remote_storage_focus.shift_unbound")
                        .withStyle(ChatFormatting.GRAY));
            }
        });
    }
}
