package com.goetymech.addon.common.block.entity;

import com.goetymech.addon.init.ModBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class CraterBlockEntity extends BlockEntity {

    private BlockState originalState = Blocks.AIR.defaultBlockState();
    private Quaternionf rotation = new Quaternionf();
    private float liftHeight = 0;
    private int lifetime = 0;
    private int maxLifetime = 60;

    private static final int BOUNCE_UP_TICKS = 6;
    private static final int FINAL_DISSOLVE_TICKS = 5;

    public CraterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CRATER_ENTITY.get(), pos, state);
    }

    public void setData(BlockState originalState, Quaternionf rotation, float liftHeight, int maxLifetime) {
        this.originalState = originalState;
        this.rotation = rotation;
        this.liftHeight = liftHeight;
        this.maxLifetime = maxLifetime;
        this.lifetime = 0;
        setChanged();
    }

    public BlockState getOriginalState() { return originalState; }
    public Quaternionf getRotation() { return rotation; }
    public float getLiftHeight() { return liftHeight; }
    public int getLifetime() { return lifetime; }
    public int getMaxLifetime() { return maxLifetime; }

    public float getBounce(float partialTick) {
        if (lifetime < BOUNCE_UP_TICKS) {
            float t = (lifetime + partialTick) / BOUNCE_UP_TICKS;
            return liftHeight * (1 - (1 - t) * (1 - t));
        }
        int postBounce = lifetime - BOUNCE_UP_TICKS;
        float remain = maxLifetime - BOUNCE_UP_TICKS;
        if (remain <= 0) return liftHeight;
        float decay = 1 - (postBounce + partialTick) / remain;
        return liftHeight * Math.max(0, decay);
    }

    public float getDissolveFade(float partialTick) {
        int remaining = maxLifetime - lifetime;
        if (remaining > FINAL_DISSOLVE_TICKS) return 1;
        return Math.max(0, (remaining - partialTick) / FINAL_DISSOLVE_TICKS);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CraterBlockEntity be) {
        be.lifetime++;

        if (be.lifetime >= be.maxLifetime) {
            level.setBlock(pos, be.originalState, 3);
            return;
        }

        int remaining = be.maxLifetime - be.lifetime;
        if (remaining <= FINAL_DISSOLVE_TICKS && level instanceof ClientLevel cl) {
            if (cl.random.nextInt(3) == 0) {
                cl.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, be.originalState),
                        pos.getX() + cl.random.nextDouble(),
                        pos.getY() + 0.5,
                        pos.getZ() + cl.random.nextDouble(),
                        (cl.random.nextDouble() - 0.5) * 0.3,
                        cl.random.nextDouble() * 0.3 + 0.1,
                        (cl.random.nextDouble() - 0.5) * 0.3);
            }
        }
    }
}
