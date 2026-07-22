package com.goetymech.addon.common.item;

import com.goetymech.addon.common.magic.spell.GatewaySpell;
import com.goetymech.addon.config.ModConfig;
import com.Polarice3.Goety.common.items.magic.MagicFocus;
import com.Polarice3.Goety.utils.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 定点聚晶。同维度结构传送。
 */
public class GatewayFocus extends MagicFocus {

    public GatewayFocus() {
        super(new GatewaySpell());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("info.goety.focus.cost",
                ModConfig.GATEWAY_COST.get()));
        tooltip.add(Component.translatable("info.goety.focus.spellType",
                this.spell.getSpellType().getName()));
        tooltip.add(Component.translatable("item.goety.focus.info")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE));

        ItemHelper.addOnShift(tooltip, () -> {
            tooltip.add(Component.translatable("info.goetymech.gateway_focus.shift_info")
                    .withStyle(ChatFormatting.GRAY));
        });
    }
}
