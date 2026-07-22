package com.goetymech.addon.common.capabilities;

import java.util.List;
import java.util.UUID;

/**
 * 灵魂统御 Capability 接口。管理链接的仆从列表。
 */
public interface ISoulDominion {
    boolean isActive();
    void setActive(boolean active);
    long getExpireTick();
    void setExpireTick(long tick);
    List<UUID> getLinkedServants();
    void setLinkedServants(List<UUID> uuids);
    int getRange();
    void setRange(int range);
    int getDuration();
    void setDuration(int duration);
    void clear();
}
