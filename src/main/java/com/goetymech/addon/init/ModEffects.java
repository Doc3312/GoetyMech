package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.magic.effect.VoidMarkEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, GoetyMechMod.MOD_ID);

    public static final RegistryObject<MobEffect> VOID_MARK =
            EFFECTS.register("void_mark", VoidMarkEffect::new);
}
