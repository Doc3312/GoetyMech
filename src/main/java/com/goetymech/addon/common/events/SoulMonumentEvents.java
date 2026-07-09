package com.goetymech.addon.common.events;

import com.goetymech.addon.common.block.entity.SoulMonumentBlockEntity;
import com.Polarice3.Goety.common.items.magic.TaglockKit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SoulMonumentEvents {

    @SubscribeEvent
    public static void onVillagerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        Level level = villager.level;
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        AABB searchBox = villager.getBoundingBox().inflate(SoulMonumentBlockEntity.DETECTION_RANGE);

        for (SoulMonumentBlockEntity monument : serverLevel.getEntitiesOfClass(
                SoulMonumentBlockEntity.class, searchBox,
                be -> true)) {
            if (monument.isRecording()) {
                monument.onVillagerDeath(serverLevel, villager.blockPosition());
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide) return;

        ItemStack held = event.getItemStack();
        if (!(held.getItem() instanceof TaglockKit)) return;

        if (!TaglockKit.hasEntity(held)) return;

        var blockEntity = level.getBlockEntity(event.getPos());
        if (!(blockEntity instanceof SoulMonumentBlockEntity monument)) return;

        if (monument.isRecording()) {
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.already_recording"), true);
            }
            return;
        }

        if (monument.isFull()) {
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.full"), true);
            }
            return;
        }

        java.util.UUID targetUUID = TaglockKit.getEntityId(held);
        if (targetUUID == null) return;

        if (monument.tryStartRecording(player, targetUUID)) {
            TaglockKit.removeEntity(held);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("info.goetymech.soul_monument.recording_started"), true);
            }
            event.setCanceled(true);
        }
    }
}
