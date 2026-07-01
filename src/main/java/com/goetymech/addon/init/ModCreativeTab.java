package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 鍒涢€犳ā寮忔爣绛鹃〉娉ㄥ唽
 */
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoetyMechMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> goetymech_TAB = TABS.register("goetymech",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goetymech"))
                    .icon(() -> new ItemStack(ModItems.TWISTED_EMBRYO.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.TWISTED_EMBRYO.get());
                        output.accept(ModItems.MATURE_EMBRYO.get());
                        output.accept(ModItems.FLESH_FURNACE.get());
                        output.accept(ModItems.BREEDING_NEST.get());
                        output.accept(ModItems.SCULK_BIND_FOCUS.get());
                        output.accept(ModItems.TIER1_SQUAD_BARRACKS.get());
                        output.accept(ModItems.TIER2_SQUAD_BARRACKS.get());
                        output.accept(ModItems.TIER3_SQUAD_BARRACKS.get());
                        output.accept(ModItems.SQUAD_FOCUS.get());
                        output.accept(ModItems.STRESS_FOCUS.get());
                        output.accept(ModItems.STRESS_SHIELD_FOCUS.get());
                        output.accept(ModItems.STRESS_READER.get());
                        output.accept(ModItems.REMOTE_ENGINE.get());
                        output.accept(ModItems.UNSTABLE_RIFT_FOCUS.get());
                        output.accept(ModItems.STABLE_RIFT_FOCUS.get());
                        output.accept(ModItems.TIME_FOCUS_POUCH.get());
                        output.accept(ModItems.SOUL_CATCHER.get());
                        output.accept(ModItems.PROTOTYPE.get());
                        output.accept(ModItems.STRESS_ARK.get());
                        output.accept(ModItems.FLESH_CASKET_1.get());
                        output.accept(ModItems.FLESH_CASKET_2.get());
                        output.accept(ModItems.FLESH_CASKET_3.get());
                        output.accept(ModItems.CHASE_FOCUS.get());
                        output.accept(ModItems.CRAZED_SUMMON_FOCUS.get());
                        output.accept(ModItems.CRAZED_ILLAGER_SPAWN_EGG.get());
                        output.accept(ModFluids.SOUL_LIQUID_BUCKET.get());
                    })
                    .build()
    );
}