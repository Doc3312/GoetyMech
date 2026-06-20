package com.goetypunk.addon.common.time;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import java.util.concurrent.atomic.AtomicLong;

public class TimeStopRandom extends LegacyRandomSource {
    private final AtomicLong seed = new AtomicLong();

    public TimeStopRandom(long seed) {
        super(seed);
    }

    @Override
    public int next(int bits) {
        long i = this.seed.get();
        long j = i * 25214903917L + 11L & 281474976710655L;
        if (TimeStopUtils.isTimeStop) {
            return (int) (j >> 48 - bits);
        } else {
            this.seed.compareAndSet(i, j);
            return (int) (j >> 48 - bits);
        }
    }

    @Override
    public RandomSource fork() {
        return new LegacyRandomSource(this.nextLong());
    }
}
