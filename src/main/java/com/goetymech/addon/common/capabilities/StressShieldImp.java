package com.goetymech.addon.common.capabilities;

import net.minecraft.world.level.Level;

/**
 * IStressShield 默认实现。
 */
public class StressShieldImp implements IStressShield {
    private float shield;
    private float maxShield;
    private long expireTick;
    private long lastHurtTick;

    @Override
    public float getShield() { return shield; }

    @Override
    public void setShield(float value) { this.shield = Math.max(0, value); }

    @Override
    public float getMaxShield() { return maxShield; }

    @Override
    public void setMaxShield(float value) { this.maxShield = Math.max(0, value); }

    @Override
    public long getExpireTick() { return expireTick; }

    @Override
    public void setExpireTick(long tick) { this.expireTick = tick; }

    @Override
    public long getLastHurtTick() { return lastHurtTick; }

    @Override
    public void setLastHurtTick(long tick) { this.lastHurtTick = tick; }

    @Override
    public boolean isActive() {
        return shield > 0;
    }

    @Override
    public void clear() {
        this.shield = 0;
        this.maxShield = 0;
        this.expireTick = 0;
        this.lastHurtTick = 0;
    }
}
