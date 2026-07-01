package com.goetymech.addon.common.fluid.frame;

import java.util.function.Consumer;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;

public class EphemeralFluidConfig {

    private final ParticleOptions particle;
    private final Consumer<Player> contactEffect;
    private final float particleProbability;

    private EphemeralFluidConfig(Builder builder) {
        this.particle = builder.particle;
        this.contactEffect = builder.contactEffect;
        this.particleProbability = builder.particleProbability;
    }

    public ParticleOptions getParticle() {
        return particle;
    }

    public Consumer<Player> getContactEffect() {
        return contactEffect;
    }

    public float getParticleProbability() {
        return particleProbability;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ParticleOptions particle;
        private Consumer<Player> contactEffect = p -> {};
        private float particleProbability = 0.025f;

        public Builder particle(ParticleOptions particle) {
            this.particle = particle;
            return this;
        }

        public Builder contactEffect(Consumer<Player> contactEffect) {
            this.contactEffect = contactEffect;
            return this;
        }

        public Builder particleProbability(float probability) {
            this.particleProbability = probability;
            return this;
        }

        public EphemeralFluidConfig build() {
            return new EphemeralFluidConfig(this);
        }
    }
}
