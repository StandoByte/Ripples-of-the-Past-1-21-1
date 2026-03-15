package com.github.standobyte.jojo.mixin.container.open_as_non_player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.entity_opencontainer.OpenContainerAsNonPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;

@Mixin(ClientPayloadHandler.class)
public class ClientContainerScreenFactoryMixin {

	// How else am I supposed to read it? 
	// Why does ServerPlayer#openMenu method with a Consumer<RegistryFriendlyByteBuf> parameter exist, I don't get it, someone explain
	@Inject(method = "lambda$createMenuScreen$0", at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
	// dementia note - the injector args are all the stuff the lambda gets into its closure (which is cool af)
	private static void onContainerScreenCreated(MenuType<?> menuType, int windowId, Minecraft mc,  
			RegistryFriendlyByteBuf buf, Component name,
			MenuScreens.ScreenConstructor<?, ?> screenFactory, 
			CallbackInfo ci) {
		AbstractContainerMenu containerMenu = Minecraft.getInstance().player.containerMenu;
		OpenContainerAsNonPlayer.readClient(buf, containerMenu);
	}
}
