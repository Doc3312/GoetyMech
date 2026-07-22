package com.goetymech.addon.client;

import com.goetymech.addon.common.time.TimeContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端时停包装器。管理 enable/disable 及 GLFW 时间锚点。
 */
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
