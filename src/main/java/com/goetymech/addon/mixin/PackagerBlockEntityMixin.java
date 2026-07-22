package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackagerBlockEntity.class)
/**
 * Create 打包器 Mixin。适配远程仓储。
 */
public class PackagerBlockEntityMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/logistics/packager/PackagerBlockEntity;wakeTheFrogs()V",
            ordinal = 0), cancellable = true, remap = false)
    private void gp$redirectPlayerPackage(CallbackInfo ci) {
        PackagerBlockEntity self = (PackagerBlockEntity) (Object) this;
        ItemStack box = self.heldBox;
        if (box.isEmpty()) return;

        String address = PackageItem.getAddress(box);
        if (address == null || address.isEmpty()) return;

        Level level = self.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        ServerPlayer target = serverLevel.getServer().getPlayerList().getPlayerByName(address);
        if (target == null) {
            GoetyMechMod.LOGGER.info("STOCK_DIAG:PACK packager=({}:{},{},{}) address='{}' → player offline",
                    level.dimension().location(), self.getBlockPos().getX(), self.getBlockPos().getY(),
                    self.getBlockPos().getZ(), address);
            return;
        }

        GoetyMechMod.LOGGER.info("STOCK_DIAG:PACK packager=({}:{},{},{}) → player=({}:{},{},{}) address='{}' items={}",
                level.dimension().location(), self.getBlockPos().getX(), self.getBlockPos().getY(),
                self.getBlockPos().getZ(),
                target.level().dimension().location(), target.getBlockX(), target.getBlockY(), target.getBlockZ(),
                address, box.getCount());
        try {
            ItemHandlerHelper.giveItemToPlayer(target, box.copy());
            self.heldBox = ItemStack.EMPTY;
            self.sendData();
            ci.cancel();
            GoetyMechMod.LOGGER.info("STOCK_DIAG:PACK delivered OK, wakeTheFrogs cancelled");
        } catch (Exception e) {
            GoetyMechMod.LOGGER.error("STOCK_DIAG:PACK delivery FAILED", e);
        }
    }
}
