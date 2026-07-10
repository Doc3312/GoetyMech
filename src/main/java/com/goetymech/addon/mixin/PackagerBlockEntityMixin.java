package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PackagerBlockEntity.class)
public class PackagerBlockEntityMixin {

    @Inject(method = "attemptToSend", at = @At("TAIL"), remap = false)
    private void gp$redirectToPlayer(List<PackagingRequest> queuedRequests, CallbackInfo ci) {
        PackagerBlockEntity self = (PackagerBlockEntity) (Object) this;
        ItemStack box = self.heldBox;
        if (box.isEmpty()) return;

        String address = PackageItem.getAddress(box);
        GoetyMechMod.LOGGER.info("PACKAGER_STOCK: attemptToSend TAIL, address='{}', box={}",
                address, box.getDisplayName().getString());

        if (address == null || address.isEmpty()) {
            GoetyMechMod.LOGGER.warn("PACKAGER_STOCK: address empty, skipping player redirect");
            return;
        }

        Level level = self.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        ServerPlayer target = serverLevel.getServer().getPlayerList().getPlayerByName(address);
        if (target == null) {
            GoetyMechMod.LOGGER.warn("PACKAGER_STOCK: player '{}' not found online", address);
            return;
        }

        GoetyMechMod.LOGGER.info("PACKAGER_STOCK: redirecting box to player {}", target.getName().getString());
        try {
            ItemHandlerHelper.giveItemToPlayer(target, box.copy());
            self.heldBox = ItemStack.EMPTY;
            GoetyMechMod.LOGGER.info("PACKAGER_STOCK: delivered successfully");
        } catch (Exception e) {
            GoetyMechMod.LOGGER.error("PACKAGER_STOCK: delivery failed", e);
            self.heldBox = box;
        }
    }
}
