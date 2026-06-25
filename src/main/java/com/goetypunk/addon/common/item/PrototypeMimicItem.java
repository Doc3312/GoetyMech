package com.goetypunk.addon.common.item;

import com.goetypunk.addon.init.ModItems;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PrototypeMimicItem extends ElytraItem {

    private static final String TAG_SOURCE_ID = "goetypunk.mimic_source_id";
    private static final String TAG_SOURCE_NAME = "goetypunk.mimic_source_name";
    private static final String TAG_SOURCE_ATTRIBUTES = "goetypunk.mimic_attributes";
    private static final String TAG_SOURCE_ENCHANTS = "goetypunk.mimic_enchants";
    private static final String TAG_SOURCE_TAG = "goetypunk.mimic_source_tag";

    public PrototypeMimicItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
    }

    public static ItemStack createFrom(ItemStack source) {
        ItemStack result = new ItemStack(ModItems.PROTOTYPE_MIMIC.get());
        CompoundTag tag = result.getOrCreateTag();

        tag.putString(TAG_SOURCE_ID, source.getItem().getDescriptionId());
        tag.putString(TAG_SOURCE_NAME, Component.Serializer.toJson(source.getHoverName()));

        CompoundTag attrTag = new CompoundTag();
        Multimap<Attribute, AttributeModifier> modifiers = source.getAttributeModifiers(EquipmentSlot.CHEST);
        int i = 0;
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            CompoundTag entryTag = new CompoundTag();
            ResourceLocation attrId = BuiltInRegistries.ATTRIBUTE.getKey(entry.getKey());
            if (attrId != null) {
                entryTag.putString("attribute", attrId.toString());
                CompoundTag modTag = entry.getValue().save();
                entryTag.put("modifier", modTag);
                attrTag.put("mod_" + (i++), entryTag);
            }
        }
        tag.put(TAG_SOURCE_ATTRIBUTES, attrTag);

        CompoundTag enchTag = new CompoundTag();
        Map<net.minecraft.world.item.enchantment.Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(source);
        for (Map.Entry<net.minecraft.world.item.enchantment.Enchantment, Integer> e : enchants.entrySet()) {
            String key = net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getKey(e.getKey()).toString();
            enchTag.putInt(key, e.getValue());
        }
        tag.put(TAG_SOURCE_ENCHANTS, enchTag);

        if (source.getTag() != null) {
            tag.put(TAG_SOURCE_TAG, source.getTag().copy());
        }

        result.setTag(tag);
        return result;
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_SOURCE_NAME)) {
            Component sourceName = Component.Serializer.fromJson(tag.getString(TAG_SOURCE_NAME));
            if (sourceName != null) {
                return Component.translatable("item.goetypunk.prototype_mimic", sourceName);
            }
        }
        return super.getName(stack);
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.CHEST) {
            return super.getAttributeModifiers(slot, stack);
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_SOURCE_ATTRIBUTES)) {
            return super.getAttributeModifiers(slot, stack);
        }
        Multimap<Attribute, AttributeModifier> result = HashMultimap.create();
        CompoundTag attrTag = tag.getCompound(TAG_SOURCE_ATTRIBUTES);
        for (String key : attrTag.getAllKeys()) {
            CompoundTag entryTag = attrTag.getCompound(key);
            ResourceLocation attrId = ResourceLocation.tryParse(entryTag.getString("attribute"));
            if (attrId == null) continue;
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(attrId);
            if (attribute != null) {
                AttributeModifier modifier = AttributeModifier.load(entryTag.getCompound("modifier"));
                if (modifier != null) {
                    result.put(attribute, modifier);
                }
            }
        }
        return result;
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return !PrototypeItem.isSlamming(entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_SOURCE_NAME)) {
            Component sourceName = Component.Serializer.fromJson(tag.getString(TAG_SOURCE_NAME));
            if (sourceName != null) {
                tooltip.add(Component.translatable("info.goetypunk.prototype_mimic.source", sourceName));
            }
        }
        int attrCount = tag != null && tag.contains(TAG_SOURCE_ATTRIBUTES)
                ? tag.getCompound(TAG_SOURCE_ATTRIBUTES).getAllKeys().size() : 0;
        int enchCount = tag != null && tag.contains(TAG_SOURCE_ENCHANTS)
                ? tag.getCompound(TAG_SOURCE_ENCHANTS).getAllKeys().size() : 0;
        if (attrCount > 0 || enchCount > 0) {
            tooltip.add(Component.translatable("info.goetypunk.prototype_mimic.inherited", attrCount, enchCount));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
