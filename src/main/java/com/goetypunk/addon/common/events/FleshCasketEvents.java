package com.goetypunk.addon.common.events;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler;
import com.Polarice3.Goety.utils.LichdomHelper;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.curio.FleshCasketItem;
import com.goetypunk.addon.common.item.curio.TriggerType;
import com.goetypunk.addon.config.ModConfig;
import com.goetypunk.addon.init.ModItems;
import com.Polarice3.Goety.common.magic.SpellStat;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FleshCasketEvents {
    private static final String TAG_LAST_CAST_TICK = "goetypunk.flesh_casket_last_cast";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer)) return;

        findAllFleshCaskets((ServerPlayer) event.player)
                .forEach(FleshCasketItem::tickCooldowns);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        List<ItemStack> caskets = findAllFleshCaskets(player);
        if (caskets.isEmpty()) return;

        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();

        for (ItemStack casketStack : caskets) {
            TriggerType trigger = FleshCasketItem.getTriggerType(casketStack);

            if (trigger == TriggerType.A) {
                tryAutoCast(player, casketStack);
            } else if (trigger == TriggerType.B || trigger == TriggerType.C) {
                float threshold = (trigger == TriggerType.B)
                        ? 0.5f
                        : (ModConfig.FLESH_CASKET_THRESHOLD.get() / 100.0f);
                if (currentHealth / maxHealth <= threshold) {
                    tryAutoCast(player, casketStack);
                }
            }
        }
    }

    private static void tryAutoCast(ServerPlayer player, ItemStack casketStack) {
        int tier = FleshCasketItem.getTier(casketStack);
        if (tier > 1 && !LichdomHelper.isLich(player)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("info.goetypunk.flesh_casket.no_lich"), true);
            return;
        }

        long gameTime = player.level().getGameTime();
        long lastCast = player.getPersistentData().getLong(TAG_LAST_CAST_TICK);
        if (gameTime - lastCast < 5) return;

        var handler = casketStack.getCapability(
                net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handler.isEmpty()) return;

        IItemHandler inv = handler.get();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack focusStack = inv.getStackInSlot(i);
            if (focusStack.isEmpty()) continue;
            if (!(focusStack.getItem() instanceof IFocus iFocus)) continue;

            ISpell spell = iFocus.getSpell();
            if (spell == null) continue;

            if (FleshCasketItem.isOnCooldown(casketStack)) continue;

            int halfCost = spell.soulCost(player, ItemStack.EMPTY) / 2;
            if (!player.isCreative() && SEHelper.getSoulAmountInt(player) < halfCost) continue;

            ServerLevel serverLevel = (ServerLevel) player.level();
            if (!spell.conditionsMet(serverLevel, player)) continue;

            var spellStat = WandUtil.getStats(player, spell);
            if (tier >= 2) spellStat.potency += 2;
            if (tier >= 3) { spellStat.potency += 2; spellStat.duration += 1; }
            ItemStack staffStack = new ItemStack(
                    tier >= 2 ? ModItems.AUTO_CAST_DUMMY_WAND.get()
                              : ModItems.AUTO_CAST_DUMMY_WAND_T1.get());
            SoulUsingItemHandler.get(staffStack).setStackInSlot(0, focusStack);

            player.getPersistentData().putLong(TAG_LAST_CAST_TICK, gameTime);
            ItemStack oldOffhand = player.getOffhandItem();
            player.getInventory().offhand.set(0, staffStack);
            try {
                spell.SpellResult(serverLevel, player, staffStack, spellStat);
            } finally {
                player.getInventory().offhand.set(0, oldOffhand);
            }

            if (!player.isCreative()) {
                SEHelper.decreaseSouls(player, halfCost);
            }

            int maxCooldown = 0;
            for (int j = 0; j < inv.getSlots(); j++) {
                ItemStack fs = inv.getStackInSlot(j);
                if (fs.isEmpty() || !(fs.getItem() instanceof IFocus iF)) continue;
                ISpell s = iF.getSpell();
                if (s != null) {
                    int cd = s.spellCooldown(player);
                    if (cd > maxCooldown) maxCooldown = cd;
                }
            }
            FleshCasketItem.setCooldown(casketStack, maxCooldown);
            return;
        }
    }

    private static List<ItemStack> findAllFleshCaskets(ServerPlayer player) {
        var curios = CuriosApi.getCuriosInventory(player).orElse(null);
        if (curios == null) return List.of();
        List<ItemStack> result = new ArrayList<>();
        curios.getCurios().forEach((ident, handler) -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStacks().getStackInSlot(i);
                if (stack.getItem() instanceof FleshCasketItem) {
                    result.add(stack);
                }
            }
        });
        return result;
    }
}
