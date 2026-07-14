package com.goetymech.addon.common.item;

import com.goetymech.addon.common.magic.spell.SoulDominionSpell;
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

public class SoulDominionFocus extends MagicFocus {

    public SoulDominionFocus() {
        super(new SoulDominionSpell());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("info.goety.focus.cost",
                ModConfig.SOUL_DOMINION_COST.get()));
        tooltip.add(Component.translatable("info.goety.focus.spellType",
                this.spell.getSpellType().getName()));
        tooltip.add(Component.translatable("item.goety.focus.info")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE));

        ItemHelper.addOnShift(tooltip, () -> {
            tooltip.add(Component.translatable("info.goetymech.soul_dominion.shift_info")
                    .withStyle(ChatFormatting.GRAY));
        });
    }
}
