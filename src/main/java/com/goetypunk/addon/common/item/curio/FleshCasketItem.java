package com.goetypunk.addon.common.item.curio;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.goetypunk.addon.common.item.capability.FleshCasketItemCapability;
import com.goetypunk.addon.common.item.capability.FleshCasketItemHandler;
import com.goetypunk.addon.common.menu.FleshCasketContainer;
import net.minecraft.nbt.CompoundTag;
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
import java.util.HashMap;
import java.util.Map;

public class FleshCasketItem extends Item implements ICurioItem {
    private static final String TAG_TIER = "flesh_casket_tier";
    private static final String TAG_TRIGGER = "flesh_casket_trigger";
    private static final String TAG_COOLDOWNS = "flesh_casket_cooldowns";

    private final TriggerType triggerType;

    public FleshCasketItem(TriggerType triggerType) {
        super(new Properties().stacksTo(1).setNoRepair());
        this.triggerType = triggerType;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            int tier = getTier(stack);
            SimpleMenuProvider provider = new SimpleMenuProvider(
                    (id, inventory, p) -> new FleshCasketContainer(id, inventory, FleshCasketItemHandler.get(stack), stack, tier),
                    getName(stack));
            NetworkHooks.openScreen((ServerPlayer) player, provider, buf -> buf.writeInt(tier));
        }
        return InteractionResultHolder.success(stack);
    }

    public static int getTier(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_TIER);
    }

    public static void setTier(ItemStack stack, int tier) {
        stack.getOrCreateTag().putInt(TAG_TIER, tier);
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
        FleshCasketItemHandler handler = FleshCasketItemHandler.get(stack);
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt) {
        int tier = getTier(stack);
        if (tier == 0) {
            setTier(stack, 1);
            tier = 1;
        }
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

    public static int getFocusCooldown(ItemStack casketStack, String focusId) {
        CompoundTag cdTag = casketStack.getOrCreateTagElement(TAG_COOLDOWNS);
        return cdTag.getInt(focusId);
    }

    public static void setFocusCooldown(ItemStack casketStack, String focusId, int ticks) {
        casketStack.getOrCreateTagElement(TAG_COOLDOWNS).putInt(focusId, ticks);
    }

    public static boolean isFocusOnCooldown(ItemStack casketStack, String focusId) {
        return getFocusCooldown(casketStack, focusId) > 0;
    }

    public static void tickCooldowns(ItemStack casketStack) {
        CompoundTag cdTag = casketStack.getOrCreateTagElement(TAG_COOLDOWNS);
        if (cdTag.isEmpty()) return;
        Map<String, Integer> toRemove = new HashMap<>();
        for (String key : cdTag.getAllKeys()) {
            int val = cdTag.getInt(key) - 1;
            if (val <= 0) {
                toRemove.put(key, 0);
            } else {
                cdTag.putInt(key, val);
            }
        }
        for (String key : toRemove.keySet()) {
            cdTag.remove(key);
        }
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
