package com.goetymech.addon.mixin;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.client.ClientLevelExpandedContext;
import com.goetymech.addon.client.CrossDimStockCache;
import com.goetymech.addon.common.LevelEC;
import com.goetymech.addon.common.time.LevelExpandedContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level implements LevelEC {
    @Shadow @Final public EntityTickList tickingEntities;
    @Shadow @Final private TransientEntitySectionManager<Entity> entityStorage;

    @Unique
    private ClientLevelExpandedContext gp$clientEC;

    ClientLevelMixin(WritableLevelData p_270739_, ResourceKey<Level> p_270683_, RegistryAccess p_270200_, Holder<DimensionType> p_270240_, Supplier<ProfilerFiller> p_270692_, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gp$init(ClientPacketListener p_205505_, ClientLevel.ClientLevelData p_205506_, ResourceKey p_205507_, Holder p_205508_, int p_205509_, int p_205510_, Supplier p_205511_, LevelRenderer p_205512_, boolean p_205513_, long p_205514_, CallbackInfo ci) {
        this.gp$setECData(new ClientLevelExpandedContext((ClientLevel) (Object) this));
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void gp$tick(BooleanSupplier p_104727_, CallbackInfo ci) {
        this.gp$levelECData().tickHead(p_104727_, ci);
    }

    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void gp$animateTick(int x, int y, int z, CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            ci.cancel();
    }

    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void gp$tickTime(CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            ci.cancel();
    }

    @Inject(method = "setGameTime", at = @At("HEAD"), cancellable = true)
    private void gp$setGameTime(long time, CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension) {
            if (time - levelData.getGameTime() > 0) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "getBlockEntity", at = @At("RETURN"), cancellable = true)
    private void gp$onGetBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        if (cir.getReturnValue() != null) return;
        StockTickerBlockEntity cached = CrossDimStockCache.get(pos);
        if (cached != null) cir.setReturnValue(cached);
    }

    @Override
    public LevelExpandedContext gp$levelECData() {
        return gp$clientEC;
    }

    @Override
    public void gp$setECData(LevelExpandedContext data) {
        this.gp$clientEC = (ClientLevelExpandedContext) data;
    }
}
