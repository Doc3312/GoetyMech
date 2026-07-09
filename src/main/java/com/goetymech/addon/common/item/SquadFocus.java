package com.goetymech.addon.common.item;

import com.goetymech.addon.common.block.entity.SoulMonumentBlockEntity;
import com.goetymech.addon.common.block.entity.SquadBarracksBlockEntity;
import com.goetymech.addon.common.magic.spell.SquadSpell;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.items.magic.MagicFocus;
import com.Polarice3.Goety.utils.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SquadFocus extends MagicFocus {

    private static final String TAG_BOUND = "goetymech.bound";
    private static final String TAG_BOUND_TYPE = "goetymech.bound_type";
    private static final String TAG_BLOCK_X = "goetymech.bound_x";
    private static final String TAG_BLOCK_Y = "goetymech.bound_y";
    private static final String TAG_BLOCK_Z = "goetymech.bound_z";
    private static final String TAG_DIMENSION = "goetymech.bound_dim";
    private static final String TAG_CACHED_TIER = "goetymech.cached_tier";
    private static final String TAG_CACHED_COUNT = "goetymech.cached_count";
    private static final String TAG_MONUMENT_CAPACITY = "goetymech.monument_capacity";
    private static final String TAG_MONUMENT_USED = "goetymech.monument_used";

    public static final String TAG_SUMMONER = "goetymech.squad_summoner";
    public static final String BOUND_TYPE_BARRACKS = "barracks";
    public static final String BOUND_TYPE_MONUMENT = "monument";

    public SquadFocus() {
        super(new SquadSpell());
    }

    public void bindToBarracks(ItemStack stack, Level level, BlockPos pos, SquadBarracksBlockEntity barracks) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_BOUND, true);
        tag.putString(TAG_BOUND_TYPE, BOUND_TYPE_BARRACKS);
        tag.putInt(TAG_BLOCK_X, pos.getX());
        tag.putInt(TAG_BLOCK_Y, pos.getY());
        tag.putInt(TAG_BLOCK_Z, pos.getZ());
        tag.putString(TAG_DIMENSION, level.dimension().location().toString());
        tag.putInt(TAG_CACHED_TIER, barracks.getTier());
        tag.putInt(TAG_CACHED_COUNT, (int) barracks.getConfigSlots().stream().filter(java.util.Objects::nonNull).count());
    }

    public void bindToMonument(ItemStack stack, Level level, BlockPos pos, SoulMonumentBlockEntity monument) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_BOUND, true);
        tag.putString(TAG_BOUND_TYPE, BOUND_TYPE_MONUMENT);
        tag.putInt(TAG_BLOCK_X, pos.getX());
        tag.putInt(TAG_BLOCK_Y, pos.getY());
        tag.putInt(TAG_BLOCK_Z, pos.getZ());
        tag.putString(TAG_DIMENSION, level.dimension().location().toString());
        tag.putInt(TAG_CACHED_TIER, monument.getTier());
        tag.putInt(TAG_MONUMENT_CAPACITY, monument.getCapacity());
        tag.putInt(TAG_MONUMENT_USED, monument.getUsedSlots());
    }

    public static String getBoundType(ItemStack stack) {
        if (!isBound(stack) || !stack.hasTag()) return "";
        CompoundTag tag = stack.getTag();
        return tag.contains(TAG_BOUND_TYPE) ? tag.getString(TAG_BOUND_TYPE) : BOUND_TYPE_BARRACKS;
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

    public static int getCachedTier(ItemStack stack) {
        if (!isBound(stack) || !stack.hasTag()) return 1;
        return stack.getTag().getInt(TAG_CACHED_TIER);
    }

    public static int getCachedCount(ItemStack stack) {
        if (!isBound(stack) || !stack.hasTag()) return 0;
        if (BOUND_TYPE_MONUMENT.equals(getBoundType(stack))) {
            return stack.getTag().getInt(TAG_MONUMENT_USED);
        }
        return stack.getTag().getInt(TAG_CACHED_COUNT);
    }

    public static boolean isBoundTo(ItemStack stack, BlockPos pos, ResourceLocation dimension) {
        if (!isBound(stack) || !stack.hasTag()) return false;
        CompoundTag tag = stack.getTag();
        return tag.getInt(TAG_BLOCK_X) == pos.getX()
                && tag.getInt(TAG_BLOCK_Y) == pos.getY()
                && tag.getInt(TAG_BLOCK_Z) == pos.getZ()
                && dimension.toString().equals(tag.getString(TAG_DIMENSION));
    }

    public static void clearBinding(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            tag.remove(TAG_BOUND);
            tag.remove(TAG_BOUND_TYPE);
            tag.remove(TAG_BLOCK_X);
            tag.remove(TAG_BLOCK_Y);
            tag.remove(TAG_BLOCK_Z);
            tag.remove(TAG_DIMENSION);
            tag.remove(TAG_CACHED_TIER);
            tag.remove(TAG_CACHED_COUNT);
            tag.remove(TAG_MONUMENT_CAPACITY);
            tag.remove(TAG_MONUMENT_USED);
        }
    }

    public static String makeSummonerKey(BlockPos pos, String dimension) {
        return dimension + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
    }

    public static void updateCachedData(ItemStack stack, SquadBarracksBlockEntity barracks) {
        if (!isBound(stack) || !stack.hasTag()) return;
        CompoundTag tag = stack.getTag();
        tag.putInt(TAG_CACHED_TIER, barracks.getTier());
        tag.putInt(TAG_CACHED_COUNT, (int) barracks.getConfigSlots().stream()
                .filter(Objects::nonNull).count());
    }

    public static List<ItemStack> findAllBoundFoci(Player player, BlockPos pos, ResourceLocation dimension) {
        List<ItemStack> results = new ArrayList<>();
        for (ItemStack stack : player.getInventory().items) {
            checkStackForBoundFocus(stack, pos, dimension, results);
        }
        for (ItemStack stack : player.getInventory().armor) {
            checkStackForBoundFocus(stack, pos, dimension, results);
        }
        checkStackForBoundFocus(player.getInventory().offhand.get(0), pos, dimension, results);
        return results;
    }

    private static void checkStackForBoundFocus(ItemStack stack, BlockPos pos, ResourceLocation dimension, List<ItemStack> results) {
        if (stack.isEmpty()) return;
        if (stack.getItem() instanceof SquadFocus && isBoundTo(stack, pos, dimension)) {
            results.add(stack);
        }
        if (stack.getItem() instanceof IWand) {
            ItemStack focus = IWand.getFocus(stack);
            if (!focus.isEmpty() && focus.getItem() instanceof SquadFocus && isBoundTo(focus, pos, dimension)) {
                results.add(focus);
            }
        }
    }

    public static void refreshBoundFoci(ServerLevel level, BlockPos pos, SquadBarracksBlockEntity barracks) {
        ResourceLocation dimId = level.dimension().location();
        for (ServerPlayer player : level.players()) {
            for (ItemStack stack : findAllBoundFoci(player, pos, dimId)) {
                updateCachedData(stack, barracks);
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && isBound(stack)) {
                clearBinding(stack);
                player.displayClientMessage(Component.translatable("info.goetymech.squad_focus.unbound"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("info.goety.focus.cost", 0));
        tooltip.add(Component.translatable("info.goety.focus.spellType", this.spell.getSpellType().getName()));
        tooltip.add(Component.translatable("item.goety.focus.info").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE));

        int baseSoulCost = ModConfig.MATURE_EMBRYO_SOUL_COST.get();

        if (isBound(stack)) {
            int tier = getCachedTier(stack);
            int count = getCachedCount(stack);
            boolean isMonument = BOUND_TYPE_MONUMENT.equals(getBoundType(stack));

            if (isMonument) {
                int capacity = stack.getTag().getInt(TAG_MONUMENT_CAPACITY);
                tooltip.add(Component.translatable("info.goetymech.squad_focus.bound_success_monument")
                        .withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("info.goetymech.squad_focus.squad_info_monument", count, capacity, tier)
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("info.goetymech.squad_focus.estimated_cost", 0)
                        .withStyle(ChatFormatting.AQUA));
            } else {
                double discount = tier == 1 ? 1.0 : tier == 2 ? ModConfig.SQUAD_TIER2_DISCOUNT.get() : ModConfig.SQUAD_TIER3_DISCOUNT.get();
                int estimatedCost = (int) (count * baseSoulCost * discount);

                tooltip.add(Component.translatable("info.goetymech.squad_focus.bound")
                        .withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("info.goetymech.squad_focus.squad_info", count, tier)
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("info.goetymech.squad_focus.estimated_cost", estimatedCost)
                        .withStyle(ChatFormatting.AQUA));
            }
        } else {
            tooltip.add(Component.translatable("info.goetymech.squad_focus.not_bound")
                    .withStyle(ChatFormatting.RED));
        }

        ItemHelper.addOnShift(tooltip, () -> {
            if (isBound(stack)) {
                tooltip.add(Component.translatable("info.goetymech.squad_focus.shift_bound")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("info.goetymech.squad_focus.shift_unbound")
                        .withStyle(ChatFormatting.GRAY));
            }
        });
    }
}
