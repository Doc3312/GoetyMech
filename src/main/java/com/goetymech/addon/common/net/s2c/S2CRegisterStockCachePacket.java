package com.goetymech.addon.common.net.s2c;

import com.goetymech.addon.client.CrossDimStockCache;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRegisterStockCachePacket {
    private final BlockPos pos;

    public S2CRegisterStockCachePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(S2CRegisterStockCachePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static S2CRegisterStockCachePacket decode(FriendlyByteBuf buf) {
        return new S2CRegisterStockCachePacket(buf.readBlockPos());
    }

    public static void handle(S2CRegisterStockCachePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            StockTickerBlockEntity be = new StockTickerBlockEntity(
                    AllBlockEntityTypes.STOCK_TICKER.get(),
                    packet.pos,
                    AllBlocks.STOCK_TICKER.get().defaultBlockState()
            );
            CrossDimStockCache.register(packet.pos, be);
        });
        ctx.get().setPacketHandled(true);
    }
}
