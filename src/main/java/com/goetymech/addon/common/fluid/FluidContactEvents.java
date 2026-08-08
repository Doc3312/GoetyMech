package com.goetymech.addon.common.fluid;

import java.util.IdentityHashMap;
import java.util.Map;

import com.goetymech.addon.GoetyMechMod;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 液体浸泡效果。1.20.1 中 Block#entityInside 仅在实体 onGround 时被调用，
 * 玩家在液体中游泳/漂浮时不会触发，故改用每 tick 的 LivingTickEvent 检测脚部流体。
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
        if (entity.level().isClientSide || entity.tickCount % 20 != 0) {
            return;
        }
        Fluid fluid = entity.level().getFluidState(entity.blockPosition()).getType();
        ContactEffect effect = CONTACT_EFFECTS.get(fluid);
        if (effect != null) {
            effect.apply(entity);
        }
    }
}
