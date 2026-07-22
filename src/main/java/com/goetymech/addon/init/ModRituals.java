package com.goetymech.addon.init;

import com.Polarice3.Goety.common.ritual.ModRitualFactory;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.ritual.UpgradeRitual;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组仪式注册。
 */
public class ModRituals {
    public static final ResourceLocation RITUAL_FACTORY_REGISTRY =
            new ResourceLocation("goety", "ritual_factory");

    public static final DeferredRegister<ModRitualFactory> RITUALS =
            DeferredRegister.create(RITUAL_FACTORY_REGISTRY, GoetyMechMod.MOD_ID);

    public static final RegistryObject<ModRitualFactory> UPGRADE =
            RITUALS.register("upgrade",
                    () -> new ModRitualFactory(UpgradeRitual::new));
}
