package com.goetypunk.addon.client;

import com.goetypunk.addon.common.time.TimeStopUtils;
import net.minecraft.client.Minecraft;

public class TimeStopUtilsWrapped {
    public static void enable(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        mc.getSoundManager().pause();
        TimeStopUtils.isTimeStop = true;
        ClientContext.isTimeStop_andSameDimension = true;
        ClientLevelExpandedContext ctx = ClientLevelExpandedContext.get();
        if (ctx != null) {
            ctx.currentTimeStopDimension = mc.level.dimension();
        }
    }

    public static void disable() {
        TimeStopUtils.isTimeStop = false;
        Minecraft.getInstance().getSoundManager().resume();
    }
}
