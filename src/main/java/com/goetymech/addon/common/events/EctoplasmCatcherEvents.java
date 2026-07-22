package com.goetymech.addon.common.events;

import com.Polarice3.Goety.common.events.spell.ChangeSoulEnergyEvent;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.SEHelper;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.curio.EctoplasmCatcherCurio;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
/**
 * 灵质捕手事件。拦截击杀灵魂并转化为灵质。
 */
public class EctoplasmCatcherEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (source == null) return;

        Player player = resolvePlayerOwner(source);
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        var curios = CuriosApi.getCuriosInventory(serverPlayer).orElse(null);
        if (curios == null) return;

        var slotResults = curios.findCurios(s -> s.getItem() instanceof EctoplasmCatcherCurio);
        if (slotResults.isEmpty()) return;

        List<ItemStack> catchers = slotResults.stream().map(SlotResult::stack).toList();

        int soulsPerEctoplasm = ModConfig.ECTOPLASM_CATCHER_SOULS_PER_ECTOPLASM.get();
        int soundCooldown = ModConfig.ECTOPLASM_CATCHER_SOUND_COOLDOWN.get();

        int soulsGiven = SEHelper.getSoulGiven(victim);
        if (soulsGiven <= 0) return;

        int remainingSouls = soulsGiven;
        int totalConverted = 0;

        for (ItemStack stack : catchers) {
            if (remainingSouls <= 0) break;
            int current = EctoplasmCatcherCurio.getSouls(stack);
            int newTotal = current + remainingSouls;
            int ectoplasmCount = newTotal / soulsPerEctoplasm;
            int remainder = newTotal % soulsPerEctoplasm;

            EctoplasmCatcherCurio.setSouls(stack, remainder);
            totalConverted += ectoplasmCount;
            remainingSouls = 0;
        }

        if (totalConverted > 0) {
            long now = serverPlayer.level().getGameTime();
            long lastSound = serverPlayer.getPersistentData().getLong("goetymech.ectoplasm_catcher_last_sound");
            if (now - lastSound >= soundCooldown) {
                serverPlayer.getPersistentData().putLong("goetymech.ectoplasm_catcher_last_sound", now);
                serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        ModSounds.SOUL_EAT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            ItemStack ectoplasmStack = new ItemStack(ModItems.ECTOPLASM.get(), totalConverted);
            if (!serverPlayer.addItem(ectoplasmStack)) {
                serverPlayer.drop(ectoplasmStack, false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSoulEnergyGain(ChangeSoulEnergyEvent.Gain event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        var curios = CuriosApi.getCuriosInventory(player).orElse(null);
        if (curios == null) return;
        if (!curios.findCurios(s -> s.getItem() instanceof EctoplasmCatcherCurio).isEmpty()) {
            event.setCanceled(true);
        }
    }

    private static Player resolvePlayerOwner(Entity source) {
        if (source instanceof Player player) return player;
        LivingEntity owner = MobUtil.getOwner(source);
        return owner instanceof Player ? (Player) owner : null;
    }
}