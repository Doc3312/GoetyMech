package com.goetypunk.addon.common.time;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.util.TimeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimeContext {

    public static float safeClientFrameTime() {
        return Minecraft.getInstance().getFrameTime();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client {
        public static final Lock lock = new ReentrantLock();
        private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);
        public static Timer timer = new Timer(20.0F, 0L);
        public static long count = 0L;
        public static long timeStopGLFW = 0L;

        public static long generateUniqueSeed() {
            return SEED_UNIQUIFIER.updateAndGet(p -> p * 1181783497276652981L)
                    ^ (Both.timeStopModifyMillis * 1000000L);
        }

        public static float alwaysPartial() {
            return (TimeStopUtils.isTimeStop || Minecraft.getInstance().isPaused())
                    ? Client.timer.partialTick : Minecraft.getInstance().getFrameTime();
        }

        public static float getCommonDegrees() {
            return timeStopGLFW / 100F;
        }

        public static float currentSeconds() {
            return count / 1000F;
        }
    }

    public static class Both {
        public static long timeStopModifyMillis = 0L;
        public static TimeSource.NanoTimeSource timeSource = System::nanoTime;

        public static long getRealMillis() {
            return getNanos() / 1000000L;
        }

        public static long getNanos() {
            return timeSource.getAsLong();
        }

        public static long milliTime() {
            return Util.getMillis();
        }
    }
}
