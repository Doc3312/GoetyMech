package com.goetymech.addon.common.item.curio;

import com.Polarice3.Goety.init.ModAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class MagicSkullNecklace extends Item implements ICurioItem {

    private static final UUID COOLDOWN_DISCOUNT_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000001");
    private static final UUID SOUL_DISCOUNT_BASE_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000002");
    private static final UUID SOUL_DISCOUNT_ABYSS_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000003");
    private static final UUID SOUL_DISCOUNT_FROST_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000004");
    private static final UUID SOUL_DISCOUNT_GEOMANCY_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000005");
    private static final UUID SOUL_DISCOUNT_NECROMANCY_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000006");
    private static final UUID SOUL_DISCOUNT_NETHER_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000007");
    private static final UUID SOUL_DISCOUNT_STORM_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000008");
    private static final UUID SOUL_DISCOUNT_VOID_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-100000000009");
    private static final UUID SOUL_DISCOUNT_WILD_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-10000000000a");
    private static final UUID SOUL_DISCOUNT_WIND_ID = UUID.fromString("a1b2c3d4-1001-4001-8001-10000000000b");

    public MagicSkullNecklace() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();

        map.put(ModAttributes.COOLDOWN_DISCOUNT.get(),
                new AttributeModifier(COOLDOWN_DISCOUNT_ID, "msn_cooldown_discount",
                        0.9, AttributeModifier.Operation.ADDITION));

        map.put(ModAttributes.SOUL_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_BASE_ID, "msn_soul_discount_base",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.ABYSS_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_ABYSS_ID, "msn_soul_discount_abyss",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.FROST_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_FROST_ID, "msn_soul_discount_frost",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.GEOMANCY_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_GEOMANCY_ID, "msn_soul_discount_geomancy",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.NECROMANCY_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_NECROMANCY_ID, "msn_soul_discount_necromancy",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.NETHER_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_NETHER_ID, "msn_soul_discount_nether",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.STORM_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_STORM_ID, "msn_soul_discount_storm",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.VOID_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_VOID_ID, "msn_soul_discount_void",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.WILD_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_WILD_ID, "msn_soul_discount_wild",
                        0.2, AttributeModifier.Operation.ADDITION));
        map.put(ModAttributes.WIND_DISCOUNT.get(),
                new AttributeModifier(SOUL_DISCOUNT_WIND_ID, "msn_soul_discount_wind",
                        0.2, AttributeModifier.Operation.ADDITION));

        return map;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("info.goetymech.magic_skull_necklace.cooldown")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("info.goetymech.magic_skull_necklace.soul")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
