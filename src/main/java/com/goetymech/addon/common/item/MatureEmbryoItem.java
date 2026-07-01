package com.goetymech.addon.common.item;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.goetymech.addon.common.utils.MinionSpawnHelper;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 鎴愮啛鑳氳儙 - 澶氭€佸埛鎬泲
 * 鐗╁搧 NBT 涓繚瀛?EntityTypeId 纭畾鍏电绫诲瀷
 * 鍙抽敭鐢熸垚瀵瑰簲鍏电鐨勭帺瀹朵粏浠庯紝娑堣€楃伒榄? * 闇€鍏堢粡杩囪鑲夌啍鐐夌児鍒舵壄鏇茶儦鑳庝骇鍑? */
public class MatureEmbryoItem extends Item {

    private static final String TAG_ENTITY_TYPE = "EntityTypeId";

    private static final double REACH_DISTANCE = 4.5D;

    public MatureEmbryoItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_ENTITY_TYPE)) {
            String typeId = tag.getString(TAG_ENTITY_TYPE);
            ResourceLocation rl = ResourceLocation.tryParse(typeId);
            if (rl != null) {
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(rl);
                if (entityType != null) {
                    // 浣跨敤鏈湴鍖栧疄浣撳悕锛堜腑鏂囨樉绀?鍔帬鍏?锛岃嫳鏂囨樉绀?Vindicator"锛?
                    return Component.translatable("item.goetymech.mature_embryo.named", entityType.getDescription());
                }
            }
        }
        return super.getName(stack);
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
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ENTITY_TYPE)) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.no_entity_type").withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        String entityTypeId = tag.getString(TAG_ENTITY_TYPE);
        ResourceLocation rl = ResourceLocation.tryParse(entityTypeId);
        if (rl == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.invalid_entity_type").withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(rl);
        if (entityType == null) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.unknown_entity_type").withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        if (!(entityType.create(level) instanceof Mob)) {
            player.displayClientMessage(
                    Component.translatable("info.goetymech.not_mob_entity").withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        // 浣跨敤娉涘瀷鍒涘缓瀹炰綋
        Mob mob = MinionSpawnHelper.spawnMinion((EntityType<? extends Mob>) entityType, level, player, spawnPos);
        if (mob != null) {
            int soulCost = ModConfig.MATURE_EMBRYO_SOUL_COST.get();
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