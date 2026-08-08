package com.goetymech.addon.common.item;

import java.util.function.Consumer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Drinkable;
import net.minecraft.world.level.Level;

/**
 * 可饮用液体瓶。右键饮用，施效后返还空玻璃瓶。
 */
public class DrinkableLiquidBottleItem extends Item implements Drinkable {

    private final Consumer<LivingEntity> drinkEffect;

    public DrinkableLiquidBottleItem(Properties properties, Consumer<LivingEntity> drinkEffect) {
        super(properties);
        this.drinkEffect = drinkEffect;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            drinkEffect.accept(entity);
        }
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (entity instanceof Player player) {
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }
}
