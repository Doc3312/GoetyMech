package com.goetymech.addon.init;

import com.Polarice3.Goety.common.ritual.ModRitualFactory;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.ritual.UpgradeCasketRitual;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModRituals {
    public static final ResourceLocation RITUAL_FACTORY_REGISTRY =
            new ResourceLocation("goety", "ritual_factory");

    public static final DeferredRegister<ModRitualFactory> RITUALS =
            DeferredRegister.create(RITUAL_FACTORY_REGISTRY, GoetyMechMod.MOD_ID);

    public static final RegistryObject<ModRitualFactory> UPGRADE_CASKET =
            RITUALS.register("upgrade_casket",
                    () -> new ModRitualFactory(UpgradeCasketRitual::new));
}
