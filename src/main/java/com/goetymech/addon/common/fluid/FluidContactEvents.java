package com.goetymech.addon.common.fluid;

import java.util.IdentityHashMap;
import java.util.List;
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
 * 液体浸泡效果。1.20.1 中 Block#entityInside 仅在实体 onGround 时被调用，
 * 玩家在液体中游泳/漂浮时不会触发，故改用每 tick 的 LivingTickEvent 检测流体。
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
        ContactEffect effect = CONTACT_EFFECTS.get(immersedFluid(entity));
        if (effect != null) {
            effect.apply(entity);
        }
    }

    private static Fluid immersedFluid(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        for (BlockPos candidate : List.of(pos, pos.above())) {
            Fluid fluid = level.getFluidState(candidate).getType();
            if (!fluid.isSame(Fluids.EMPTY)) {
                return fluid;
            }
        }
        return Fluids.EMPTY;
    }
}
