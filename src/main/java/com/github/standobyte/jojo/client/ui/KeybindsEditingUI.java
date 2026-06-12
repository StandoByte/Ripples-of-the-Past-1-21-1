package com.github.standobyte.jojo.client.ui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class KeybindsEditingUI {
	@Nullable public KeyMapping selectedKey;
	public List<KeybindEntry> entries = new ArrayList<>();
	public Runnable save;
	
	public KeybindEntry addKeybind(KeyMapping keybind, Runnable save, 
			int x, int y, int width, int height) {
		KeybindEntry entry = new KeybindEntry(this, keybind, x, y, width, height);
		entries.add(entry);
		this.save = save;
		return entry;
	}
	
	public void refresh() {
        KeyMapping.resetMapping();
        entries.forEach(KeybindEntry::refreshEntry);
	}
	
	public static class KeybindEntry {
		public final KeybindsEditingUI parent;
		public final KeybindButton button;
		public final KeyMapping keybind;
		
		public KeybindEntry(KeybindsEditingUI parent, KeyMapping keybind, 
				int x, int y, int width, int height) {
			this.parent = parent;
			this.button = new KeybindButton(parent, this, x, y, width, height);
			this.keybind = keybind;
		}
		
		public void refreshEntry() {
			button.setMessage(keybind.getTranslatedKeyMessage());
			this.button.setTooltip(null);
			if (parent.selectedKey == keybind) {
				button.setMessage(
						Component.literal("> ")
						.append(button.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
						.append(" <")
						.withStyle(ChatFormatting.YELLOW));
			}
		}
	}
	
	public static class KeybindButton extends Button {
		public final KeyMapping keyMapping;

		public KeybindButton(KeybindsEditingUI keybindsHandler, KeybindEntry keybindEntry, 
				int x, int y, int width, int height) {
			super(x, y, width, height, CommonComponents.EMPTY, 
					b -> {
						keybindsHandler.selectedKey = keybindEntry.keybind;
						keybindsHandler.refresh();
					}, 
					Button.DEFAULT_NARRATION);
			this.keyMapping = keybindEntry.keybind;
		}
		
	}
	
	
	// copypaste from KeyBindsScreen

	public long lastKeySelection;
	protected InputConstants.Key lastPressedKey = InputConstants.UNKNOWN;
	protected InputConstants.Key lastPressedModifier = InputConstants.UNKNOWN;
	protected boolean isLastKeyHeldDown = false;
	protected boolean isLastModifierHeldDown = false;
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.selectedKey != null) {
			var key = InputConstants.getKey(keyCode, scanCode);
			if (lastPressedModifier == InputConstants.UNKNOWN && KeyModifier.isKeyCodeModifier(key)) {
				lastPressedModifier = key;
				isLastModifierHeldDown = true;
			} else {
				lastPressedKey = key;
				isLastKeyHeldDown = true;
			}
			return true;
		}
		
		return false;
	}
	
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if (this.selectedKey != null && (!Minecraft.ON_OSX || scanCode != 63)) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				this.selectedKey.setKeyModifierAndCode(KeyModifier.NONE, InputConstants.UNKNOWN);
				setKey(this.selectedKey, InputConstants.UNKNOWN);
				lastPressedKey = InputConstants.UNKNOWN;
				lastPressedModifier = InputConstants.UNKNOWN;
				isLastKeyHeldDown = false;
				isLastModifierHeldDown = false;
			} else {
				var key = InputConstants.getKey(keyCode, scanCode);
				if (lastPressedKey.equals(key)) {
					isLastKeyHeldDown = false;
				} else if (lastPressedModifier.equals(key)) {
					isLastModifierHeldDown = false;
				}

				if (!isLastKeyHeldDown && !isLastModifierHeldDown) {
					if (!lastPressedKey.equals(InputConstants.UNKNOWN)) {
						this.selectedKey.setKeyModifierAndCode(
								KeyModifier.getKeyModifier(lastPressedModifier),
								lastPressedKey);
						setKey(this.selectedKey, lastPressedKey);
					} else {
						this.selectedKey.setKeyModifierAndCode(
								KeyModifier.NONE,
								lastPressedModifier);
						setKey(this.selectedKey, lastPressedModifier);
					}
					lastPressedKey = InputConstants.UNKNOWN;
					lastPressedModifier = InputConstants.UNKNOWN;
				} else {
					return true;
				}
			}
			this.selectedKey = null;
			this.lastKeySelection = Util.getMillis();
			refresh();
			return true;
		} else {
			return false;
		}
	}
	
	public void setKey(KeyMapping keyBinding, InputConstants.Key input) {
		keyBinding.setKey(input);
		if (save != null) {
			save.run();
		}
	}
	
}
