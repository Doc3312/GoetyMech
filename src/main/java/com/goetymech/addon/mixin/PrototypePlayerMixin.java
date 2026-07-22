package com.goetymech.addon.mixin;

import com.goetymech.addon.common.item.PrototypeItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
/**
 * 原型玩家 Mixin。扩展 PlayerNBT 储存原型数据。
 */
public abstract class PrototypePlayerMixin extends LivingEntity {

    protected PrototypePlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void gp$cancelJumpWhileCharging(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (PrototypeItem.getJumpChargeTick(player) > 0) {
            ci.cancel();
            return;
        }
        if (this.jumping && PrototypeItem.hasAnyPrototype(player)) {
            ci.cancel();
        }
    }
}
