package com.goetymech.addon.mixin;

import com.goetymech.addon.common.utils.GatewayUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
/**
 * 服务端找结构 Mixin。Gateway 跨维度搜索。
 */
public class ServerLevelFindStructureMixin {

    @Inject(method = "findNearestMapStructure", at = @At("HEAD"))
    private void gp$captureTag(TagKey<Structure> tagKey, BlockPos blockPos, int i, boolean bl, CallbackInfoReturnable<BlockPos> cir) {
        if (GatewayUtil.pendingEyeCapture) {
            GatewayUtil.pendingEyeTag = tagKey;
        }
    }
}
