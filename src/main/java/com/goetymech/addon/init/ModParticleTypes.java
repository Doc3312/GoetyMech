package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, GoetyMechMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> SCALED_GOD_RAY =
            PARTICLE_TYPES.register("scaled_god_ray", () -> new SimpleParticleType(true));
}
