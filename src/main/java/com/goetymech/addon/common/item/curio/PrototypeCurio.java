package com.goetymech.addon.common.item.curio;

import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.goetymech.addon.compat.caelus.CaelusLoaded;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = com.goetymech.addon.GoetyMechMod.MOD_ID)
public class PrototypeCurio {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (!PrototypeItem.isPrototype(stack)) return;

        LazyOptional<ICurio> curio = LazyOptional.of(() -> new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                LivingEntity entity = slotContext.entity();
                if (entity.isFallFlying()) {
                    int ticks = entity.getFallFlyingTicks();
                    if (ticks > 0) {
                        stack.elytraFlightTick(entity, ticks);
                    }
                }
            }

            @Override
            public boolean canEquip(SlotContext slotContext) {
                // 无Caelus时禁止放入Curios背/胸槽
                if (!CaelusLoaded.isLoaded()) return false;
                // 防止与胸甲槽冲突
                ItemStack chest = slotContext.entity().getItemBySlot(EquipmentSlot.CHEST);
                return !PrototypeItem.isPrototype(chest);
            }

            @Override
            public boolean canEquipFromUse(SlotContext slotContext) {
                return CaelusLoaded.isLoaded();
            }

            @Nonnull
            @Override
            public SoundInfo getEquipSound(SlotContext slotContext) {
                return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_ELYTRA, 1.0F, 1.0F);
            }
        });

        event.addCapability(CuriosCapability.ID_ITEM, new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
                return CuriosCapability.ITEM.orEmpty(cap, curio);
            }
        });
        event.addListener(curio::invalidate);
    }
}
