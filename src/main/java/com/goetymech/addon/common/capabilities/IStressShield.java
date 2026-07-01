package com.goetymech.addon.common.capabilities;

public interface IStressShield {
    float getShield();
    void setShield(float value);
    float getMaxShield();
    void setMaxShield(float value);
    long getExpireTick();
    void setExpireTick(long tick);
    long getLastHurtTick();
    void setLastHurtTick(long tick);
    boolean isActive();
    void clear();
}
