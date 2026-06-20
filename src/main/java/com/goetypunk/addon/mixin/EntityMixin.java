package com.goetypunk.addon.mixin;

import com.goetypunk.addon.common.time.TimeStopEntityData;
import com.goetypunk.addon.common.time.TimeStopUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public Level level;

    @Shadow public abstract Level level();

    @Shadow public abstract void setPosRaw(double x, double y, double z);

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void gp$setRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop) {
            if (((Object) this) instanceof LivingEntity living) {
                if (TimeStopEntityData.getTimeStopCount(living) > 0 && !level.isClientSide) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    TimeStopUtils.use(false, living);
                    if (serverLevel.players().isEmpty())
                        TimeStopUtils.isTimeStop = true;
                }
            }
        }
    }

    @Inject(method = "setPos", at = @At("HEAD"), cancellable = true)
    private void gp$setPos(net.minecraft.world.phys.Vec3 pos, CallbackInfo ci) {
        if (level.isClientSide) return;
        Entity self = (Entity) (Object) this;
        if (self instanceof Player) return;
        if (TimeStopUtils.isTimeStop && !TimeStopUtils.canMove(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "setPosRaw", at = @At("HEAD"), cancellable = true)
    private void gp$setPosRaw(double x, double y, double z, CallbackInfo ci) {
        if (level.isClientSide) return;
        Entity self = (Entity) (Object) this;
        if (self instanceof Player) return;
        if (TimeStopUtils.isTimeStop && !TimeStopUtils.canMove(self)) {
            ci.cancel();
        }
    }
}
