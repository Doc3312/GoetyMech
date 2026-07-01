package com.goetymech.addon.common.menu;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPE =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, GoetyMechMod.MOD_ID);

    public static final RegistryObject<MenuType<SquadBarracksMenu>> SQUAD_BARRACKS =
            CONTAINER_TYPE.register("squad_barracks",
                    () -> IForgeMenuType.create(SquadBarracksMenu::new));

    public static final RegistryObject<MenuType<FleshCasketContainer>> FLESH_CASKET =
            CONTAINER_TYPE.register("flesh_casket",
                    () -> IForgeMenuType.create(FleshCasketContainer::createContainerClientSide));
}
