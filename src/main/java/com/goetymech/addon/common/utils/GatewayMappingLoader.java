package com.goetymech.addon.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class GatewayMappingLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, ResourceLocation> MAPPINGS = new HashMap<>();

    public GatewayMappingLoader() {
        super(GSON, "structure_locator_mappings");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
        MAPPINGS.clear();
        for (var entry : entries.entrySet()) {
            if (!entry.getValue().isJsonObject()) continue;
            JsonObject obj = entry.getValue().getAsJsonObject();
            if (!obj.has("values") || !obj.get("values").isJsonObject()) continue;
            JsonObject values = obj.get("values").getAsJsonObject();
            for (String key : values.keySet()) {
                ResourceLocation itemId = ResourceLocation.tryParse(key);
                ResourceLocation tagPath = ResourceLocation.tryParse(values.get(key).getAsString());
                if (itemId != null && tagPath != null) {
                    MAPPINGS.put(itemId, tagPath);
                }
            }
        }
    }

    public static ResourceLocation getTagForItem(ResourceLocation itemId) {
        return MAPPINGS.get(itemId);
    }
}
