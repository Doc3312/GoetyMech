package com.goetymech.addon.common.block.entity;

import com.goetymech.addon.client.net.ClientPacketHandlers;
import com.goetymech.addon.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 撞击坑方块实体。持有变换数据（旋转/平移/弹跳）和生命周期。
 */
public class CraterBlockEntity extends BlockEntity {

    private BlockState originalState = Blocks.AIR.defaultBlockState();
    private Quaternionf rotation = new Quaternionf();
    private Vector3f translate = new Vector3f();
    private double bouncing = 0;
    private int lifetime = 0;
    private int maxLifetime = 60;

    public CraterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CRATER_ENTITY.get(), pos, state);
    }

    public void setData(BlockState originalState, Quaternionf rotation, Vector3f translate, double bouncing, int maxLifetime) {
        this.originalState = originalState;
        this.rotation = rotation;
        this.translate = translate;
        this.bouncing = bouncing;
        this.maxLifetime = maxLifetime;
        this.lifetime = 0;
        setChanged();
    }

    public BlockState getOriginalState() { return originalState; }
    public Quaternionf getRotation() { return rotation; }
    public Vector3f getTranslate() { return translate; }
    public double getBouncing() { return bouncing; }
    public int getLifetime() { return lifetime; }
    public int getMaxLifetime() { return maxLifetime; }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CraterBlockEntity be) {
        be.lifetime++;

        if (be.lifetime >= be.maxLifetime) {
            level.removeBlockEntity(pos);
            level.setBlock(pos, be.originalState, 3);
            return;
        }

        if (be.originalState.shouldSpawnParticlesOnBreak() && be.maxLifetime - be.lifetime < 10) {
            ClientPacketHandlers.spawnCraterBreakParticles(level, pos, be.originalState, be.maxLifetime, be.lifetime);
        }
    }
}
