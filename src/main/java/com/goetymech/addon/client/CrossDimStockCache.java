package com.goetymech.addon.client;

import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class CrossDimStockCache {
    private static final Map<BlockPos, StockTickerBlockEntity> CACHE = new HashMap<>();

    public static void register(BlockPos pos, StockTickerBlockEntity be) {
        CACHE.put(pos, be);
    }

    public static StockTickerBlockEntity get(BlockPos pos) {
        return CACHE.get(pos);
    }

    public static void clear() {
        CACHE.clear();
    }
}
