package com.github.standobyte.jojo.mixin.client.jojoposeui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList;
import com.github.standobyte.jojo.mechanics.jojopose.ClientJojoPoseChatUI;
import com.github.standobyte.jojo.mechanics.jojopose.ClientJojoPoseChatUI.PolShestogoDed;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen implements PolShestogoDed {
	@Unique private ScrolleableButtonList jojoPoseList;

	protected ChatScreenMixin(Component title) {
		super(title);
	}
	
	public void jojo_ripples$setJojoPoseScrolleableList(ScrolleableButtonList list) {
		this.jojoPoseList = list;
	}

	@Inject(method = "mouseScrolled", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/ChatScreen;hasShiftDown()Z"),
			cancellable = true)
	private void scrollPoseList(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> ci) {
		if (jojoPoseList != null && ClientJojoPoseChatUI.scrollPoseList(this, jojoPoseList, 
				mouseX, mouseY, scrollX, scrollY)) {
			ci.cancel();
		}
	}
}
