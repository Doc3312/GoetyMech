package com.goetymech.addon.common.item;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.goetymech.addon.common.utils.MinionSpawnHelper;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 鎵洸鑳氳儙 - 鍙抽敭鐢熸垚灞炰簬鐜╁鐨勬柊鍒屾皯锛圢eollager锛?
 * 鍙爢鍙?4涓紝鏃犺€愪箙
 * 娑堣€楃伒榄傚彫鍞わ紝鐏甸瓊涓嶈冻鏃跺彫鍞ゅ崐琛€鐗堟湰骞舵彁绀?
 */
public class TwistedEmbryoItem extends Item {

    private static final double REACH_DISTANCE = 4.5D;

    public TwistedEmbryoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(context.getHand());

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Vec3 spawnPos = context.getClickLocation().add(0, MinionSpawnHelper.SPAWN_HEIGHT_OFFSET, 0);
            spawnMinion(serverLevel, player, stack, spawnPos);
        } else if (level.isClientSide) {
            player.swing(context.getHand());
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Vec3 spawnPos = MinionSpawnHelper.findSpawnPosition(player, level, REACH_DISTANCE);
            spawnMinion(serverLevel, player, stack, spawnPos);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /**
     * 鏍稿績鐢熸垚閫昏緫
     */
    private void spawnMinion(ServerLevel level, Player player, ItemStack stack, Vec3 spawnPos) {
        int soulCost = ModConfig.TWISTED_EMBRYO_SOUL_COST.get();

        Mob mob = MinionSpawnHelper.spawnMinion(ModEntityType.NEOLLAGER.get(), level, player, spawnPos);
        if (mob != null) {
            String mobName = mob.getDisplayName().getString();

            switch (MinionSpawnHelper.handleSoulCost(player, soulCost, mob)) {
                case SUFFICIENT -> player.displayClientMessage(
                        Component.translatable("info.goetymech.summon.success", mobName)
                                .withStyle(ChatFormatting.GREEN), true);
                case INSUFFICIENT, DEPLETED -> player.displayClientMessage(
                        Component.translatable("info.goetymech.soul_insufficient", mobName)
                                .withStyle(ChatFormatting.RED), true);
            }

            stack.shrink(1);
        }
    }
}