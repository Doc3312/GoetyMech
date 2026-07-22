package com.goetymech.addon.common.item.curio;

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

/**
 * 灵质捕手饰品。击杀时拦截灵魂转化为灵质。
 */
public class EctoplasmCatcherCurio extends Item implements ICurioItem {

    private static final String TAG_SOULS = "ectoplasm_souls";

    public EctoplasmCatcherCurio() {
        super(new Properties().stacksTo(1));
    }

    public static int getSouls(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_SOULS);
    }

    public static void setSouls(ItemStack stack, int souls) {
        stack.getOrCreateTag().putInt(TAG_SOULS, Math.max(0, souls));
    }

    public static int addSouls(ItemStack stack, int amount) {
        int current = getSouls(stack);
        setSouls(stack, current + amount);
        return current + amount;
    }

    public static void resetSouls(ItemStack stack) {
        stack.getOrCreateTag().remove(TAG_SOULS);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int souls = getSouls(stack);
        tooltip.add(Component.translatable("info.goetymech.ectoplasm_catcher.progress", souls)
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}