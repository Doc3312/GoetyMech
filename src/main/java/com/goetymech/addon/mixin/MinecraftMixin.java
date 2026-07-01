package com.goetymech.addon.mixin;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.common.time.TimeContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Nullable public net.minecraft.client.player.LocalPlayer player;
    @Shadow @Nullable public net.minecraft.client.multiplayer.ClientLevel level;
    @Shadow @Final public net.minecraft.client.gui.Gui gui;
    @Shadow @Final public net.minecraft.client.renderer.GameRenderer gameRenderer;
    @Shadow @Nullable public Screen screen;
    @Shadow @Final public net.minecraft.client.Options options;
    @Shadow @Final public net.minecraft.client.KeyboardHandler keyboardHandler;
    @Shadow @Final private Timer timer;
    @Shadow private ProfilerFiller profiler;
    @Shadow private int rightClickDelay;
    @Shadow @Nullable private Overlay overlay;
    @Shadow private boolean pause;
    @Shadow @Final private net.minecraft.client.sounds.SoundManager soundManager;
    @Shadow @Final private net.minecraft.client.tutorial.Tutorial tutorial;
    @Shadow @Nullable public net.minecraft.world.phys.HitResult hitResult;
    @Shadow @Nullable public net.minecraft.client.multiplayer.MultiPlayerGameMode gameMode;
    @Shadow @Final private net.minecraft.client.multiplayer.chat.ChatListener chatListener;
    @Shadow @Nullable private net.minecraft.client.server.IntegratedServer singleplayerServer;
    @Shadow private float pausePartialTick;
    @Shadow public abstract void tick();
    @Shadow public abstract void setScreen(@Nullable Screen screen);
    @Shadow protected abstract void handleKeybinds();
    @Unique private boolean gp$isTimeStop = false;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void gp$runTickHead(boolean renderLevel, CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension) {
            TimerAccessor acc = (TimerAccessor) (Object) this.timer;
            acc.gp$setMsPerTick(1.0e32F);
        }
    }

    @Inject(method = "getPartialTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void gp$getPartialTick(CallbackInfoReturnable<Float> cir) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            cir.setReturnValue(timer.partialTick);
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;onRenderTickStart(F)V", remap = false))
    private void gp$runTick_modifyPartial(boolean renderLevel, CallbackInfo ci) {
        if (!renderLevel) return;

        long l = TimeContext.Client.timer.advanceTime(Util.getMillis());

        if (level == null && TimeStopUtils.isTimeStop) {
            ClientContext.isTimeStop_andSameDimension = false;
            TimeStopUtils.isTimeStop = false;
        }

        TimerAccessor acc = (TimerAccessor) (Object) this.timer;
        gp$isTimeStop = TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension;

        if (TimeStopUtils.isTimeStop && gameMode != null && player != null) {
            if (gp$isTimeStop && !pause) {
                for (int i = 0; i < l; i++) {
                    if (this.overlay == null && this.screen == null) {
                        this.profiler.push("keybindings");
                        this.handleKeybinds();
                        this.profiler.pop();
                    }
                    this.gameRenderer.tick();
                    this.profiler.push("keyboard");
                    this.keyboardHandler.tick();
                    this.profiler.pop();

                    if (level != null) {
                        if (this.screen == null && this.player != null) {
                            if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
                                this.setScreen(null);
                            } else if (this.player.isSleeping() && !(this.screen instanceof InBedChatScreen)) {
                                this.setScreen(new InBedChatScreen());
                            }
                        } else if (this.screen instanceof InBedChatScreen inBed) {
                            if (this.player != null && !this.player.isSleeping()) {
                                inBed.onPlayerWokeUp();
                            }
                        }

                        if (this.screen != null) {
                            Screen.wrapScreenError(() -> this.screen.tick(),
                                    "Ticking screen", this.screen.getClass().getCanonicalName());
                        }

                        if (player != null && !player.isRemoved()
                                && !player.isDeadOrDying()
                                && (player.isSpectator() || player.isCreative()
                                || TimeStopUtils.canMove(player))) {
                            if (this.rightClickDelay > 0) --this.rightClickDelay;
                            this.profiler.push("gui");
                            this.chatListener.tick();
                            this.gui.tick(false);
                            this.profiler.pop();
                            this.gameRenderer.pick(1.0F);
                            this.tutorial.onLookAt(this.level, this.hitResult);
                            this.profiler.push("gameMode");
                            if (this.gameMode != null) this.gameMode.tick();
                            this.profiler.pop();
                            if (!this.options.renderDebug) this.gui.clearCache();
                        }

                        if (this.level != null && TimeStopUtils.canMove(player)) {
                            this.level.guardEntityTick(this.level::tickNonPassenger, player);
                        }
                    }
                }

                if (this.screen instanceof DeathScreen) this.screen.tick();
                for (int i = 0; i < l; i++) {
                    this.soundManager.tick(true);
                }
            }
        } else {
            if (Math.abs(acc.gp$getMsPerTick() - 1.0e32F) < 0.001F)
                acc.gp$setMsPerTick(50.0F);
        }
    }
}
