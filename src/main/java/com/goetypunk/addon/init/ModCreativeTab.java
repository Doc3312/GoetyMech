package com.goetypunk.addon.init;

import com.goetypunk.addon.GoetyPunkMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 创造模式标签页注册
 */
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoetyPunkMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> GOETYPUNK_TAB = TABS.register("goetypunk",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goetypunk"))
                    .icon(() -> new ItemStack(ModItems.TWISTED_EMBRYO.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.TWISTED_EMBRYO.get());
                        output.accept(ModItems.MATURE_EMBRYO.get());
                        output.accept(ModItems.FLESH_FURNACE.get());
                        output.accept(ModItems.SCULK_BIND_FOCUS.get());
                        output.accept(ModItems.TIER1_SQUAD_BARRACKS.get());
                        output.accept(ModItems.TIER2_SQUAD_BARRACKS.get());
                        output.accept(ModItems.TIER3_SQUAD_BARRACKS.get());
                        output.accept(ModItems.SQUAD_FOCUS.get());
                        output.accept(ModItems.STRESS_FOCUS.get());
                        output.accept(ModItems.STRESS_READER.get());
                        output.accept(ModItems.REMOTE_ENGINE.get());
                        output.accept(ModItems.UNSTABLE_RIFT_FOCUS.get());
                        output.accept(ModItems.STABLE_RIFT_FOCUS.get());
                        output.accept(ModItems.STRESS_FOCUS.get());
                        output.accept(ModItems.TIME_FOCUS_POUCH.get());
                    })
                    .build()
    );
}