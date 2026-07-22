package com.goetymech.addon.common.capabilities;

/**
 * 应力护盾 Capability 接口。管理护盾值。
 */
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
