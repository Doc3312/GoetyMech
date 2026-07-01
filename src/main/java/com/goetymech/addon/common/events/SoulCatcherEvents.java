package com.goetymech.addon.common.events;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MobUtil;
import com.goetymech.addon.goetymechMod;
import com.goetymech.addon.common.item.curio.SoulCatcherCurio;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = goetymechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulCatcherEvents {

    private static final String TAG_LAST_SOUND = "goetymech.soul_catcher_last_sound";

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        Entity source = event.getSource().getEntity();
        if (source == null) return;

        Player player = resolvePlayerOwner(source);
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        var curios = CuriosApi.getCuriosInventory(serverPlayer).orElse(null);
        if (curios == null) return;

        var slotResult = curios.findFirstCurio(s -> s.getItem() instanceof SoulCatcherCurio);
        if (slotResult.isEmpty()) return;

        ItemStack stack = slotResult.get().stack();
        int threshold = ModConfig.SOUL_CATCHER_KILLS_THRESHOLD.get();

        SoulCatcherCurio.addKill(stack);
        int kills = SoulCatcherCurio.getKills(stack);

        if (kills >= threshold) {
            SoulCatcherCurio.resetKills(stack);

            long now = serverPlayer.level().getGameTime();
            long lastSound = serverPlayer.getPersistentData().getLong(TAG_LAST_SOUND);
            int cooldown = ModConfig.SOUL_CATCHER_SOUND_COOLDOWN.get();
            if (now - lastSound >= cooldown) {
                serverPlayer.getPersistentData().putLong(TAG_LAST_SOUND, now);
                serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        ModSounds.SOUL_EAT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            ItemStack ectoplasm = new ItemStack(ModItems.ECTOPLASM.get());
            if (!serverPlayer.addItem(ectoplasm)) {
                serverPlayer.drop(ectoplasm, false);
            }
        }
    }

    private static Player resolvePlayerOwner(Entity source) {
        if (source instanceof Player player) return player;
        LivingEntity owner = MobUtil.getOwner(source);
        return owner instanceof Player ? (Player) owner : null;
    }
}
