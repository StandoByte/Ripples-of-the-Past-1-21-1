package com.github.standobyte.jojo.mixin.client.keybindui;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.VanillaKeybinds;
import com.github.standobyte.jojo.client.ui.screen_widgets.IconButton;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MultiLineScreenTooltip;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.core.JojoMod;

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
public abstract class KeyEntryMixin {
	@Shadow @Final private KeyMapping key;
	@Shadow @Final private Button changeButton;
	@Shadow private boolean hasCollision = false;

	protected Button jojo_ripples$holdToggleButton;
	protected Button jojo_ripples$left20x20Button;
	protected List<Button> jojo_ripples$extraButtons;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void jojo_rippples$addButtons(KeyBindsList keybindsList, KeyMapping key, Component name, CallbackInfo ci) {
		String keyName = key.getName();
		boolean refreshAgain = false;
		
		ConfigBool holdOrToggle = VanillaKeybinds.HOLD_OR_TOGGLE.get(keyName);
		if (holdOrToggle != null) {
			jojo_ripples$holdToggleButton = Button.builder(name, button -> {
				holdOrToggle.set(!holdOrToggle.get());
				JojoMod.config.saveClient();
			})
			.bounds(0, 0, 90, 20)
			.build();
			
			if (jojo_ripples$extraButtons == null) jojo_ripples$extraButtons = new ArrayList<>();
			jojo_ripples$extraButtons.add(jojo_ripples$holdToggleButton);
			refreshAgain = true;
		}
		
		if (key == InputHandler.getInstance().vanillaKeybinds.switchSpecial) {
			jojo_ripples$left20x20Button = new IconButton(0, 0, 20, 20, 
					new GuiIcon(ConfigGuiHelper.toIconPath("ability_selection_wheel"), 16, 16), 
					button -> {
						ConfigBool abilitySelectionWheel = JojoMod.config.getClient().abilitySelectionWheel;
						abilitySelectionWheel.set(!abilitySelectionWheel.getAsBoolean());
						JojoMod.config.saveClient();
					},
					new MultiLineScreenTooltip(
							Component.translatable("jojo_ripples.config.client.abilitySelectionWheel"),
							Component.translatable("jojo_ripples.config.client.abilitySelectionWheel.tooltip")));
			
			if (jojo_ripples$extraButtons == null) jojo_ripples$extraButtons = new ArrayList<>();
			jojo_ripples$extraButtons.add(jojo_ripples$left20x20Button);
			refreshAgain = true;
		}
		
		if (refreshAgain) refreshEntry();
	}
	
	@Shadow abstract void refreshEntry();

	@Inject(method = "refreshEntry", at = @At("TAIL"))
	public void jojo_ripples$modifyKeybindEntry(CallbackInfo ci) {
		String keyName = this.key.getName();
		if (VanillaKeybinds.ADD_DESC_TOOLTIP.contains(keyName)) {
			MutableComponent description = Component.translatable(keyName + ".desc");
			Tooltip tooltip = changeButton.getTooltip();
			if (tooltip != null) {
				description = description
						.append(CommonComponents.NEW_LINE)
						.append(CommonComponents.NEW_LINE)
						.append(tooltip.message.copy());
			}
			changeButton.setTooltip(Tooltip.create(description));
		}
	}
	
	@Inject(method = "render", at = @At("TAIL"))
	public void jojo_ripples$renderButtons(GuiGraphics guiGraphics, int index,
            int top, int left, int width, int height,
            int mouseX, int mouseY, boolean hovering, float partialTick, 
            CallbackInfo ci) {
		if (jojo_ripples$holdToggleButton != null) {
			String keyName = this.key.getName();
			ConfigBool holdOrToggle = VanillaKeybinds.HOLD_OR_TOGGLE.get(keyName);
			Component message = holdOrToggle != null ? Component.translatable(holdOrToggle.get() ? "options.key.toggle" : "options.key.hold") : null;
			jojo_ripples$holdToggleButton.setMessage(message != null ? message : CommonComponents.EMPTY);
			
			int x = left - jojo_ripples$holdToggleButton.getWidth() - 10;
			int y = top - 2;
			jojo_ripples$holdToggleButton.setPosition(x, y);
			jojo_ripples$holdToggleButton.render(guiGraphics, mouseX, mouseY, partialTick);
			if (x < 0) {
				jojo_ripples$holdToggleButton.setTooltip(message != null ? Tooltip.create(message) : null);
			}
		}
		
		if (jojo_ripples$left20x20Button != null) {
			int x = left - jojo_ripples$left20x20Button.getWidth() - 10;
			int y = top - 2;
			jojo_ripples$left20x20Button.setPosition(x, y);
			jojo_ripples$left20x20Button.render(guiGraphics, mouseX, mouseY, partialTick);
		}
		
		if (this.key == InputHandler.getInstance().vanillaKeybinds.switchSpecial) {
			IconButton.renderCheckmarkOrCross(jojo_ripples$left20x20Button, 
					JojoMod.config.getClient().abilitySelectionWheel.getAsBoolean(), guiGraphics.pose());
		}
	}
	
	@Inject(method = "children", at = @At("RETURN"), cancellable = true)
	public void jojo_ripples$THIS_IS_SO_FUCKING_STUPID(CallbackInfoReturnable<List<? extends GuiEventListener>> ci) {
		if (jojo_ripples$extraButtons != null) {
			List<GuiEventListener> mutable = new ArrayList<>(ci.getReturnValue());
			mutable.addAll(jojo_ripples$extraButtons);
			ci.setReturnValue(mutable);
		}
	}
	
	@Inject(method = "narratables", at = @At("RETURN"), cancellable = true)
	public void jojo_ripples$WHY_DID_THEY_NOT_JUST_MAKE_A_FUCKING_FIELD(CallbackInfoReturnable<List<? extends GuiEventListener>> ci) {
		if (jojo_ripples$extraButtons != null) {
			List<GuiEventListener> mutable = new ArrayList<>(ci.getReturnValue());
			mutable.addAll(jojo_ripples$extraButtons);
			ci.setReturnValue(mutable);
		}
	}
	
}
