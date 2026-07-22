package com.goetymech.addon.common;

import com.goetymech.addon.common.data.TimeStopSavedData;
import com.goetymech.addon.common.time.TimeStopEntityData;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务端扩展上下文。管理时停状态和实体 tick。
 */
public class ServerExpandedContext {
    public final MinecraftServer server;
    private TimeStopSavedData timeStopSavedData;

    public ServerExpandedContext(MinecraftServer server) {
        this.server = server;
    }

    public TimeStopSavedData getTimeStopSavedData() {
        if (this.timeStopSavedData == null)
            this.timeStopSavedData = TimeStopSavedData.readOrCreate(server);
        return timeStopSavedData;
    }

    public void serverTickHead(ServerLevel serverLevel) {
        if (!TimeStopUtils.isTimeStop) return;
        boolean can = getTimeStopSavedData().dimensions.contains(serverLevel.dimension().location());
        if (!can) return;

        if (serverLevel.getGameTime() % 60 == 0) {
            AtomicBoolean has = new AtomicBoolean(false);
            for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class,
                    new AABB(new BlockPos(0, 0, 0)).inflate(30000000))) {
                if (TimeStopEntityData.getTimeStopCount(living) > 0 && living.isAlive()) {
                    has.set(true);
                    break;
                }
            }
            if (!has.get()) {
                Player player = serverLevel.getRandomPlayer();
                if (player != null) {
                    TimeStopUtils.use(false, player, true);
                } else {
                    TimeStopUtils.isTimeStop = false;
                }
            }
        }
    }
}
