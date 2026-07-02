package com.goetymech.addon.common.events;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.api.magic.IChargingSpell;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.LichdomHelper;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.curio.FleshCasketItem;
import com.goetymech.addon.common.item.curio.TriggerType;
import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FleshCasketEvents {
    private static final String TAG_LAST_CAST_TICK = "goetymech.flesh_casket_last_cast";

    // --- channel task: holds player in "casting" state for IChargingSpell ---
    private static final Map<UUID, ChannelTask> ACTIVE_CHANNELS = new ConcurrentHashMap<>();

    private static class ChannelTask {
        final ServerPlayer player;
        final ISpell spell;
        final ItemStack staffStack;
        final ItemStack oldOffhand;
        final SpellStat spellStat;
        final int castUp;
        final int maxShots;
        final ItemStack focusStack;
        int castTicks = 0;
        int coolAcc = 0;

        ChannelTask(ServerPlayer player, ISpell spell, ItemStack staffStack,
                    ItemStack oldOffhand, SpellStat spellStat, ItemStack focusStack) {
            this.player = player;
            this.spell = spell;
            this.staffStack = staffStack;
            this.oldOffhand = oldOffhand;
            this.spellStat = new SpellStat(spellStat.potency, spellStat.duration,
                    spellStat.range, spellStat.radius, spellStat.burning, spellStat.velocity);
            this.focusStack = focusStack;
            if (spell instanceof IChargingSpell cs) {
                this.castUp = cs.castUp(player, staffStack);
                this.maxShots = Math.max(cs.shotsNumber(player, staffStack), 60);
            } else {
                this.castUp = 0;
                this.maxShots = 60;
            }
        }

        boolean tick() {
            if (!player.isAlive()) return false;
            castTicks++;
            ServerLevel serverLevel = (ServerLevel) player.level();
            player.getInventory().offhand.set(0, staffStack);
            if (castTicks == 1 && !player.isUsingItem()) {
                player.startUsingItem(InteractionHand.OFF_HAND);
            }
            spell.useSpell(serverLevel, player, staffStack, castTicks, spellStat);
            if (spell instanceof IChargingSpell cs) {
                if (castTicks >= castUp || castUp <= 0) {
                    coolAcc++;
                    int cd = cs.Cooldown(player, staffStack, maxShots);
                    if (coolAcc >= cd) {
                        coolAcc = 0;
                        spell.SpellResult(serverLevel, player, staffStack, spellStat);
                    }
                }
            }
            return castTicks < maxShots;
        }

        void stop() {
            ServerLevel serverLevel = (ServerLevel) player.level();
            spell.stopSpell(serverLevel, player, staffStack,
                    focusStack.isEmpty() ? ItemStack.EMPTY : focusStack,
                    castTicks, spellStat);
            if (player.isUsingItem()) player.stopUsingItem();
            player.getInventory().offhand.set(0, oldOffhand);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ACTIVE_CHANNELS.entrySet().removeIf(entry -> {
            ChannelTask task = entry.getValue();
            if (!task.tick()) {
                task.stop();
                return true;
            }
            return false;
        });
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
                    net.minecraft.network.chat.Component.translatable("info.goetymech.flesh_casket.no_lich"), true);
            return;
        }

        long gameTime = player.level().getGameTime();
        long lastCast = player.getPersistentData().getLong(TAG_LAST_CAST_TICK);
        if (gameTime - lastCast < 5) return;

        if (player.getCooldowns().isOnCooldown(casketStack.getItem())) return;
        if (ACTIVE_CHANNELS.containsKey(player.getUUID())) return;

        var handler = casketStack.getCapability(
                net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handler.isEmpty()) return;

        IItemHandler inv = handler.get();
        boolean anyFired = false;
        int maxCooldown = 0;

        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack focusStack = inv.getStackInSlot(i);
            if (focusStack.isEmpty()) continue;
            if (!(focusStack.getItem() instanceof IFocus iFocus)) continue;

            ISpell spell = iFocus.getSpell();
            if (spell == null) continue;

            int halfCost = spell.soulCost(player, ItemStack.EMPTY) / 2;
            if (tier == 2) halfCost = (int)(halfCost * 0.5);
            if (tier == 3) halfCost = (int)(halfCost * 0.1);
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

            ItemStack oldOffhand = player.getOffhandItem();
            player.getInventory().offhand.set(0, staffStack);

            boolean channelStarted = false;
            try {
                spell.startSpell(serverLevel, player, staffStack, spellStat);
                spell.useSpell(serverLevel, player, staffStack, 60, spellStat);

                if (spell instanceof IChargingSpell && !ACTIVE_CHANNELS.containsKey(player.getUUID())) {
                    ACTIVE_CHANNELS.put(player.getUUID(), new ChannelTask(
                            player, spell, staffStack, oldOffhand, spellStat, focusStack));
                    channelStarted = true;
                } else {
                    spell.SpellResult(serverLevel, player, staffStack, spellStat);
                    ItemStack currentFocus = WandUtil.findFocus(player);
                    spell.stopSpell(serverLevel, player, staffStack,
                            currentFocus.isEmpty() ? ItemStack.EMPTY : currentFocus,
                            60, spellStat);
                }
            } finally {
                if (!channelStarted) {
                    player.getInventory().offhand.set(0, oldOffhand);
                }
            }

            if (!player.isCreative()) {
                SEHelper.decreaseSouls(player, halfCost);
            }

            int cd = spell.spellCooldown(player);
            if (tier == 2) cd = (int)(cd * 0.8);
            if (tier == 3) cd = (int)(cd * 0.4);
            if (cd > maxCooldown) maxCooldown = cd;
            anyFired = true;

            if (ACTIVE_CHANNELS.containsKey(player.getUUID())) break;
        }

        if (anyFired) {
            player.getPersistentData().putLong(TAG_LAST_CAST_TICK, gameTime);
            int configCd = ModConfig.FLESH_CASKET_COOLDOWN.get();
            int finalCd = configCd > 0 ? configCd : maxCooldown;
            player.getCooldowns().addCooldown(casketStack.getItem(), finalCd);
            FleshCasketItem.setCooldown(casketStack, finalCd);
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
