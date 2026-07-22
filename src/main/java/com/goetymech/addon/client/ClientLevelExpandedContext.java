package com.goetymech.addon.client;

import com.goetymech.addon.common.LevelEC;
import com.goetymech.addon.common.time.LevelExpandedContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

/**
 * 客户端 Level 扩展。更新时停维度并取消时停中的世界 tick。
 */
public class ClientLevelExpandedContext extends LevelExpandedContext {
    public final ClientLevel clientLevel;
    public ResourceKey<Level> currentTimeStopDimension = null;

    public ClientLevelExpandedContext(ClientLevel clientLevel) {
        super(clientLevel);
        this.clientLevel = clientLevel;
    }

    public static ClientLevelExpandedContext get() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            return (ClientLevelExpandedContext) ((LevelEC) level).gp$levelECData();
        }
        return null;
    }

    public boolean isCurrentTS() {
        return currentTimeStopDimension != null;
    }

    @Override
    public void tickHead(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        ClientContext.isTimeStop_andSameDimension = isCurrentTS();
        if (ClientContext.isTimeStop_andSameDimension) {
            ci.cancel();
        }
    }
}
