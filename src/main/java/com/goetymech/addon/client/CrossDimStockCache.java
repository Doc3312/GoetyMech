package com.goetymech.addon.client;

import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class CrossDimStockCache {
    private static final Map<BlockPos, StockTickerBlockEntity> CLIENT_CACHE = new HashMap<>();
    private static final Map<BlockPos, StockTickerBlockEntity> SERVER_CACHE = new HashMap<>();

    public static void register(BlockPos pos, StockTickerBlockEntity be) {
        CLIENT_CACHE.put(pos, be);
    }

    public static StockTickerBlockEntity get(BlockPos pos) {
        StockTickerBlockEntity cached = CLIENT_CACHE.get(pos);
        if (cached != null) return cached;
        return SERVER_CACHE.get(pos);
    }

    public static void registerServer(BlockPos pos, StockTickerBlockEntity be) {
        SERVER_CACHE.put(pos, be);
    }

    public static void clear() {
        CLIENT_CACHE.clear();
        SERVER_CACHE.clear();
    }
}
