package com.goetymech.addon.common.fluid;

import java.util.IdentityHashMap;
import java.util.Map;

import com.goetymech.addon.GoetyMechMod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 液体浸泡效果。参照原版熔岩着火机制（Entity#tick 每 tick 检测 isInLava 立即生效），
 * 每 tick 检测实体所在流体并立即施效，无 onGround 限制、无延迟。
 * 1.20.1 中 Block#entityInside 仅在实体 onGround 时被调用，故不采用。
 */
@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID)
public class FluidContactEvents {

    @FunctionalInterface
    public interface ContactEffect {
        void apply(LivingEntity entity);
    }

    private static final Map<Fluid, ContactEffect> CONTACT_EFFECTS = new IdentityHashMap<>();

    public static void register(Fluid fluid, ContactEffect effect) {
        CONTACT_EFFECTS.put(fluid, effect);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        ContactEffect effect = CONTACT_EFFECTS.get(immersedFluid(entity));
        if (effect != null) {
            effect.apply(entity);
        }
    }

    private static Fluid immersedFluid(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        Fluid foot = level.getFluidState(pos).getType();
        if (!foot.isSame(Fluids.EMPTY)) {
            return foot;
        }
        Fluid body = level.getFluidState(pos.above()).getType();
        if (!body.isSame(Fluids.EMPTY)) {
            return body;
        }
        return Fluids.EMPTY;
    }
}
