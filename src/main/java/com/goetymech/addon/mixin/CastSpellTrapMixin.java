package com.goetymech.addon.mixin;

import com.Polarice3.Goety.common.entities.util.CastSpellTrap;
import com.Polarice3.Goety.utils.MobUtil;
import com.goetymech.addon.common.utils.PhantomWandManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CastSpellTrap.class)
public class CastSpellTrapMixin {

    @Redirect(
        method = "m_8119_",
        at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/utils/MobUtil;isSpellCasting(Lnet/minecraft/world/entity/LivingEntity;)Z"),
        remap = false
    )
    private boolean gp$bypassIsSpellCasting(LivingEntity entity) {
        if (entity instanceof Player player) {
            ItemStack phantom = PhantomWandManager.get(player.getUUID());
            if (!phantom.isEmpty()) {
                return true;
            }
        }
        return MobUtil.isSpellCasting(entity);
    }
}
