package com.github.standobyte.jojo.mixin.container.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.ClientStandHeldItemsUI;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.client.ClientExternalContainerUI.ExternalContainerScreenCrutches;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenCrutches extends Screen implements ExternalContainerScreenCrutches {
	
	protected ContainerScreenCrutches(Component title) {
		super(title);
	}

	@Unique private boolean cancelMouseReleaseHandling;
	@Override
	public void jojo_ripples$preventMouseRelease() {
		this.cancelMouseReleaseHandling = true;
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	@Unique private void cancelMouseReleaseHandling(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
		if (this.cancelMouseReleaseHandling) {
			this.cancelMouseReleaseHandling = false;
			ci.setReturnValue(true);
		}
	}
	
	
	@Override
	public void jojo_ripples$onAddedExternalContainerUI(GuiEventListener child) {
		if (child instanceof ClientStandHeldItemsUI standHandsUI) {
			standArmsExtContainer = standHandsUI;
		}
		alwaysHandleKeyPress.add(child);
	}


	@Unique private List<GuiEventListener> alwaysHandleKeyPress = new ArrayList<>();
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> ci) {
		for (GuiEventListener extContainerUI : alwaysHandleKeyPress) {
			if (extContainerUI.keyPressed(keyCode, scanCode, modifiers)) {
				ci.setReturnValue(true);
			}
		}
	}
	

	@Unique private ClientStandHeldItemsUI standArmsExtContainer;
	@Override
	public ClientStandHeldItemsUI jojo_ripples$getStandArmsExtContainer() {
		return standArmsExtContainer;
	}
	
}
