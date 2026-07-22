package com.goetymech.addon.common.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ISoulDominion 默认实现。
 */
public class SoulDominionImp implements ISoulDominion {
    private boolean active;
    private long expireTick;
    private List<UUID> linkedServants = new ArrayList<>();
    private int range;
    private int duration;

    @Override
    public boolean isActive() { return active; }

    @Override
    public void setActive(boolean active) { this.active = active; }

    @Override
    public long getExpireTick() { return expireTick; }

    @Override
    public void setExpireTick(long tick) { this.expireTick = tick; }

    @Override
    public List<UUID> getLinkedServants() { return linkedServants; }

    @Override
    public void setLinkedServants(List<UUID> uuids) { this.linkedServants = uuids != null ? uuids : new ArrayList<>(); }

    @Override
    public int getRange() { return range; }

    @Override
    public void setRange(int range) { this.range = range; }

    @Override
    public int getDuration() { return duration; }

    @Override
    public void setDuration(int duration) { this.duration = duration; }

    @Override
    public void clear() {
        this.active = false;
        this.expireTick = 0;
        this.linkedServants.clear();
        this.range = 0;
        this.duration = 0;
    }
}
