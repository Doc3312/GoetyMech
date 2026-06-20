package com.goetypunk.addon.client;

import com.goetypunk.addon.common.time.TimeContext;
import com.goetypunk.addon.common.time.TimeStopUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

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
        if (TimeContext.Client.timeStopGLFW == 0L)
            TimeContext.Client.timeStopGLFW = (long)(GLFW.glfwGetTime() * 1000L);
    }

    public static void disable() {
        TimeStopUtils.isTimeStop = false;
        Minecraft.getInstance().getSoundManager().resume();
        ClientContext.isTimeStop_andSameDimension = false;
        ClientLevelExpandedContext ctx = ClientLevelExpandedContext.get();
        if (ctx != null) {
            ctx.currentTimeStopDimension = null;
        }
    }
}
