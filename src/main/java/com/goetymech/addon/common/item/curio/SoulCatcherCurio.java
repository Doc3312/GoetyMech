package com.goetymech.addon.common.item.curio;

import com.goetymech.addon.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class SoulCatcherCurio extends Item implements ICurioItem {

    private static final String TAG_KILLS = "soul_catcher_kills";

    public SoulCatcherCurio() {
        super(new Properties().stacksTo(1));
    }

    public static int getKills(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_KILLS);
    }

    public static void addKill(ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_KILLS, getKills(stack) + 1);
    }

    public static void resetKills(ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_KILLS, 0);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int kills = getKills(stack);
        int threshold = ModConfig.SOUL_CATCHER_KILLS_THRESHOLD.get();
        tooltip.add(Component.translatable("info.goetymech.soul_catcher.progress", kills, threshold)
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
