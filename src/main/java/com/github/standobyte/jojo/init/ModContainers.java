package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.container.PlayerClothesMenu;
import com.github.standobyte.jojo.mechanics.clothes.sewing.SewingMachineContainer;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.StandHandsContainerMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModContainers {
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, JojoMod.MOD_ID);


	public static final DeferredHolder<MenuType<?>, MenuType<PlayerClothesMenu>> PLAYER_CLOTHES = CONTAINERS.register("clothes", 
			key -> new MenuType<>(PlayerClothesMenu::new, FeatureFlags.DEFAULT_FLAGS));

	public static final DeferredHolder<MenuType<?>, MenuType<SewingMachineContainer>> SEWING_MACHINE = CONTAINERS.register("sewing_machine", 
			key -> new MenuType<>((id, inventory) -> new SewingMachineContainer(id, inventory, ContainerLevelAccess.NULL), FeatureFlags.DEFAULT_FLAGS));

	public static final DeferredHolder<MenuType<?>, MenuType<StandHandsContainerMenu>> STAND_HANDS = CONTAINERS.register("stand_hands", 
			key -> new MenuType<>(StandHandsContainerMenu.CLIENT_FACTORY, FeatureFlags.DEFAULT_FLAGS));
}
