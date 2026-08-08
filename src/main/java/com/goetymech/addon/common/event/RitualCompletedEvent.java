package com.goetymech.addon.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * 仪式成功完成事件。由 DarkAltarBlockEntityMixin 在 stopRitual(true) 时广播。
 * 失败/打断不触发。
 */
public class RitualCompletedEvent extends Event {

    private final Level level;
    private final BlockPos altarPos;
    private final ResourceLocation recipeId;
    private final int soulCost;

    public RitualCompletedEvent(Level level, BlockPos altarPos, ResourceLocation recipeId, int soulCost) {
        this.level = level;
        this.altarPos = altarPos;
        this.recipeId = recipeId;
        this.soulCost = soulCost;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getAltarPos() {
        return altarPos;
    }

    /** 仪式配方的注册 ID（RitualRecipe.getId()），用于匹配 ritual_products.json */
    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    /** 仪式配方的总灵魂消耗（RitualRecipe.getSoulCost()） */
    public int getSoulCost() {
        return soulCost;
    }
}
