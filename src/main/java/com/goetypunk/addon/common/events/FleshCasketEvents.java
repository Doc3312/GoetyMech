package com.goetypunk.addon.common.events;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.utils.LichdomHelper;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.curio.FleshCasketItem;
import com.goetypunk.addon.common.item.curio.TriggerType;
import com.goetypunk.addon.config.ModConfig;
import com.goetypunk.addon.init.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FleshCasketEvents {
    private static final String TAG_LAST_CAST_TICK = "goetypunk.flesh_casket_last_cast";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer)) return;

        findFleshCasket((ServerPlayer) event.player).ifPresent(casket ->
                FleshCasketItem.tickCooldowns(casket));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Optional<ItemStack> opt = findFleshCasket(player);
        if (opt.isEmpty()) return;

        ItemStack casketStack = opt.get();
        FleshCasketItem casketItem = (FleshCasketItem) casketStack.getItem();
        TriggerType trigger = casketItem.getTriggerType();

        if (trigger == TriggerType.A) {
            tryAutoCast(player, casketStack);
        } else if (trigger == TriggerType.B || trigger == TriggerType.C) {
            float maxHealth = player.getMaxHealth();
            float currentHealth = player.getHealth();
            float threshold = (trigger == TriggerType.B)
                    ? 0.5f
                    : (ModConfig.FLESH_CASKET_THRESHOLD.get() / 100.0f);
            if (currentHealth / maxHealth <= threshold) {
                tryAutoCast(player, casketStack);
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

            String focusId = ForgeRegistries.ITEMS.getKey(focusStack.getItem()).toString();
            if (FleshCasketItem.isFocusOnCooldown(casketStack, focusId)) continue;

            int halfCost = spell.soulCost(player, ItemStack.EMPTY) / 2;
            if (!player.isCreative() && SEHelper.getSoulAmountInt(player) < halfCost) continue;

            ServerLevel serverLevel = (ServerLevel) player.level();
            if (!spell.conditionsMet(serverLevel, player)) continue;

            var spellStat = WandUtil.getStats(player, spell);
            ItemStack staffStack = (tier == 1)
                    ? ItemStack.EMPTY
                    : new ItemStack(ModItems.AUTO_CAST_DUMMY_WAND.get());

            int cooldown = spell.spellCooldown(player);

            player.getPersistentData().putLong(TAG_LAST_CAST_TICK, gameTime);
            spell.SpellResult(serverLevel, player, staffStack, spellStat);

            if (!player.isCreative()) {
                SEHelper.decreaseSouls(player, halfCost);
            }

            FleshCasketItem.setFocusCooldown(casketStack, focusId, cooldown);
            return;
        }
    }

    private static Optional<ItemStack> findFleshCasket(ServerPlayer player) {
        var curios = CuriosApi.getCuriosInventory(player).orElse(null);
        if (curios == null) return Optional.empty();
        return curios.findFirstCurio(s -> s.getItem() instanceof FleshCasketItem)
                .map(r -> r.stack());
    }
}
