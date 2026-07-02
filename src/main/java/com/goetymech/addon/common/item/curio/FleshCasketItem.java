package com.goetymech.addon.common.item.curio;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.goetymech.addon.common.item.capability.FleshCasketItemCapability;
import com.goetymech.addon.common.item.capability.FleshCasketItemHandler;
import com.goetymech.addon.common.menu.FleshCasketContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FleshCasketItem extends Item implements ICurioItem {
    private static final String TAG_COOLDOWN = "flesh_casket_cooldown";
    private static final String TAG_TRIGGER_TYPE = "flesh_casket_trigger_type";
    private static final int MAX_COOLDOWN_TICKS = 600;

    private final int tier;

    public FleshCasketItem(int tier) {
        super(new Properties().stacksTo(1).setNoRepair());
        this.tier = tier;
    }

    @Override
    public Component getName(ItemStack stack) {
        TriggerType type = getTriggerType(stack);
        String suffix = switch (type) {
            case A -> "fullness";
            case B -> "balance";
            case C -> "deathbed";
        };
        String key = "item.goetymech.flesh_casket_" + tier + "." + suffix;
        return Component.translatable(key);
    }

    public static int getTier(ItemStack stack) {
        if (stack.getItem() instanceof FleshCasketItem casket) {
            return casket.tier;
        }
        return 1;
    }

    public static TriggerType getTriggerType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_TRIGGER_TYPE)) {
            return TriggerType.fromName(tag.getString(TAG_TRIGGER_TYPE));
        }
        return TriggerType.A;
    }

    public static void setTriggerType(ItemStack stack, TriggerType type) {
        stack.getOrCreateTag().putString(TAG_TRIGGER_TYPE, type.name());
    }

    public static void cycleTriggerType(ItemStack stack) {
        TriggerType current = getTriggerType(stack);
        setTriggerType(stack, current.next());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            SimpleMenuProvider provider = new SimpleMenuProvider(
                    (id, inventory, p) -> new FleshCasketContainer(id, inventory, FleshCasketItemHandler.get(stack), stack, tier),
                    getName(stack));
            NetworkHooks.openScreen((ServerPlayer) player, provider, buf -> buf.writeInt(tier));
        }
        return InteractionResultHolder.success(stack);
    }

    public static int getSlotCount(int tier) {
        return switch (tier) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 5;
            default -> 1;
        };
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return slotContext.entity().isCrouching();
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt) {
        return new FleshCasketItemCapability(stack, getSlotCount(tier));
    }

    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        var cap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        CompoundTag nbt = stack.getTag() != null ? stack.getTag() : new CompoundTag();
        if (cap.isPresent() && cap.get() instanceof ItemStackHandler handler) {
            nbt.put("cap", handler.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt == null) {
            stack.setTag(null);
            return;
        }
        var cap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (cap.isPresent() && cap.get() instanceof ItemStackHandler handler && nbt.contains("cap")) {
            handler.deserializeNBT(nbt.getCompound("cap"));
        }
        stack.setTag(nbt);
    }

    public static int getCooldown(ItemStack casketStack) {
        return casketStack.getOrCreateTag().getInt(TAG_COOLDOWN);
    }

    public static void setCooldown(ItemStack casketStack, int ticks) {
        casketStack.getOrCreateTag().putInt(TAG_COOLDOWN, Math.max(0, ticks));
    }

    public static boolean isOnCooldown(ItemStack casketStack) {
        return getCooldown(casketStack) > 0;
    }

    public static boolean hasAnyFocus(ItemStack casketStack) {
        var handler = casketStack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handler.isEmpty()) return false;
        for (int i = 0; i < handler.get().getSlots(); i++) {
            if (!handler.get().getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }
}
