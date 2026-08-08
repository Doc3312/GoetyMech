package com.goetymech.addon.mixin;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.goetymech.addon.common.event.RitualCompletedEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 仪式完成钩子：DarkAltarBlockEntity.stopRitual(boolean finished) 是所有仪式（含第三方附属）
 * 的统一出口。finished=true（成功完成）时广播 RitualCompletedEvent，供巫法冷凝等机器消费。
 */
@Mixin(DarkAltarBlockEntity.class)
public class DarkAltarBlockEntityMixin {

    @Inject(method = "stopRitual", at = @At("HEAD"), remap = false)
    private void goetymech$onStopRitual(boolean finished, CallbackInfo ci) {
        if (!finished) {
            return;
        }
        DarkAltarBlockEntity altar = (DarkAltarBlockEntity) (Object) this;
        Level level = altar.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        RitualRecipe recipe = altar.getCurrentRitualRecipe();
        if (recipe == null) {
            return;
        }
        MinecraftForge.EVENT_BUS.post(new RitualCompletedEvent(
                level,
                altar.getBlockPos(),
                recipe.getId(),
                recipe.getSoulCost()));
    }
}
