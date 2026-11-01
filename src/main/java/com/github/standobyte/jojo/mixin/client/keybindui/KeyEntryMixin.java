package com.github.standobyte.jojo.mixin.client.keybindui;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.ui.VanillaKeybindsEntryModification;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class KeyEntryMixin implements VanillaKeybindsEntryModification.KeyEntryDuck {
	@Shadow @Final private KeyMapping key;
	@Shadow @Final private Button changeButton;
	@Shadow private boolean hasCollision = false;

	@Unique protected List<Button> extraButtons;
	@Unique protected Button buttonToTheLeft;
	@Unique protected Component tooltipDescription;

	@Shadow abstract void refreshEntry();

	@Override
	public void jojo_ripples$addButtonToTheLeftOfKey(Button button) {
		jojo_ripples$addButton(button);
		buttonToTheLeft = button;
		changeButton.setWidth(changeButton.getWidth() - buttonToTheLeft.getWidth());
	}
	
	private void jojo_ripples$addButton(Button button) {
		if (extraButtons == null) extraButtons = new ArrayList<>();
		extraButtons.add(button);
	}
	
	@Inject(method = "render", at = @At("TAIL"))
	public void jojo_ripples$renderButtons(GuiGraphics guiGraphics, int index,
            int top, int left, int width, int height,
            int mouseX, int mouseY, boolean hovering, float partialTick, 
            CallbackInfo ci) {
		if (buttonToTheLeft != null) {
			int x = changeButton.getX() - buttonToTheLeft.getWidth();
			int y = top - 2;
			buttonToTheLeft.setPosition(x, y);
			buttonToTheLeft.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}
	
	@Inject(method = "children", at = @At("RETURN"), cancellable = true)
	public void jojo_ripples$THIS_IS_SO_FUCKING_STUPID(CallbackInfoReturnable<List<? extends GuiEventListener>> ci) {
		if (extraButtons != null) {
			List<GuiEventListener> mutable = new ArrayList<>(ci.getReturnValue());
			mutable.addAll(extraButtons);
			ci.setReturnValue(mutable);
		}
	}
	
	@Inject(method = "narratables", at = @At("RETURN"), cancellable = true)
	public void jojo_ripples$WHY_DID_THEY_NOT_JUST_MAKE_A_FUCKING_FIELD(CallbackInfoReturnable<List<? extends GuiEventListener>> ci) {
		if (extraButtons != null) {
			List<GuiEventListener> mutable = new ArrayList<>(ci.getReturnValue());
			mutable.addAll(extraButtons);
			ci.setReturnValue(mutable);
		}
	}
	
	
	@Override
	public void jojo_ripples$addTooltipDescription(Component description) {
		this.tooltipDescription = description;
	}

	@Inject(method = "refreshEntry", at = @At("TAIL"))
	public void jojo_ripples$modifyKeybindEntry(CallbackInfo ci) {
		if (tooltipDescription != null) {
			MutableComponent description = tooltipDescription.copy();
			Tooltip tooltip = changeButton.getTooltip();
			if (tooltip != null) {
				description = description
						.append(CommonComponents.NEW_LINE)
						.append(CommonComponents.NEW_LINE)
						.append(tooltip.message.copy().withStyle(ChatFormatting.DARK_GRAY));
			}
			changeButton.setTooltip(Tooltip.create(description));
		}
	}
	
	@Override
	public void jojo_ripples$invokeRefreshEntry() {
		refreshEntry();
	}
	
}
