package com.goetymech.addon.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
/**
 * Entity.onGround 访问器。
 */
public interface EntityOnGroundAccessor {
    @Accessor("onGround")
    void setOnGround(boolean onGround);
}
