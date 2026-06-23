package com.goetypunk.addon.mixin;

import com.goetypunk.addon.GoetyPunkMod;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Inject(method = "handlePlayerInput", at = @At("HEAD"))
    private void gp$logHandlePlayerInput(ServerboundPlayerInputPacket pPacket, CallbackInfo ci) {
        GoetyPunkMod.LOGGER.info("HANDLE_INPUT_CALLED: xxa={}, zza={}, jump={}, sneak={}",
                pPacket.getXxa(), pPacket.getZza(), pPacket.isJumping(), pPacket.isShiftKeyDown());
    }

    @Inject(method = "handlePlayerInput", at = @At("RETURN"))
    private void gp$logHandlePlayerInputReturn(ServerboundPlayerInputPacket pPacket, CallbackInfo ci) {
        GoetyPunkMod.LOGGER.info("HANDLE_INPUT_RETURN");
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void gp$logTick(CallbackInfo ci) {
        GoetyPunkMod.LOGGER.info("SGPLI_TICK called");
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void gp$logTickReturn(CallbackInfo ci) {
        GoetyPunkMod.LOGGER.info("SGPLI_TICK returned");
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void gp$logHandleMovePlayer(CallbackInfo ci) {
        GoetyPunkMod.LOGGER.info("HANDLE_MOVE_PLAYER called");
    }

    @Inject(method = "handlePlayerCommand", at = @At("HEAD"))
    private void gp$logHandlePlayerCommand(CallbackInfo ci) {
        GoetyPunkMod.LOGGER.info("HANDLE_PLAYER_COMMAND called");
    }
}
