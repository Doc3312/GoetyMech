package com.goetypunk.addon.compat.caelus;

import net.minecraftforge.fml.ModList;

public class CaelusLoaded {

    private static final String CAELUS_MOD_ID = "caelus";
    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get() != null && ModList.get().isLoaded(CAELUS_MOD_ID);
        }
        return loaded;
    }
}
