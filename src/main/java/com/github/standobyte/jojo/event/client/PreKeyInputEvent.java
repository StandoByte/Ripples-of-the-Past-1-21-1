package com.github.standobyte.jojo.event.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Fired when a keyboard key input occurs, such as pressing, releasing, or repeating a key, before being processed by vanilla.
 *
 * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * If the event is cancelled, then the key event will not be processed by vanilla (e.g. keymappings and screens) </p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 * @see <a href="https://www.glfw.org/docs/latest/group__keys.html" target="_top">the online GLFW documentation</a>
 */
public class PreKeyInputEvent extends Event implements ICancellableEvent {
	private final int key;
	private final int scanCode;
	private final int action;
	private final int modifiers;
	
	public PreKeyInputEvent(int key, int scanCode, int action, int modifiers) {
		this.key = key;
		this.scanCode = scanCode;
		this.action = action;
		this.modifiers = modifiers;
	}

	/**
	 * {@return the {@code GLFW} (platform-agnostic) key code}
	 *
	 * @see InputConstants input constants starting with {@code KEY_}
	 * @see GLFW key constants starting with {@code GLFW_KEY_}
	 * @see <a href="https://www.glfw.org/docs/latest/group__keys.html" target="_top">the online GLFW documentation</a>
	 */
	public int getKey() {
		return this.key;
	}

	/**
	 * {@return the platform-specific scan code}
	 * <p>
	 * The scan code is unique for every key, regardless of whether it has a key code.
	 * Scan codes are platform-specific but consistent over time, so keys will have different scan codes depending
	 * on the platform but they are safe to save to disk as custom key bindings.
	 *
	 * @see InputConstants#getKey(int, int)
	 */
	public int getScanCode() {
		return this.scanCode;
	}

	/**
	 * {@return the mouse button's action}
	 *
	 * @see InputConstants#PRESS
	 * @see InputConstants#RELEASE
	 * @see InputConstants#REPEAT
	 */
	public int getAction() {
		return this.action;
	}

	/**
	 * {@return a bit field representing the active modifier keys}
	 *
	 * @see InputConstants#MOD_CONTROL CTRL modifier key bit
	 * @see GLFW#GLFW_MOD_SHIFT SHIFT modifier key bit
	 * @see GLFW#GLFW_MOD_ALT ALT modifier key bit
	 * @see GLFW#GLFW_MOD_SUPER SUPER modifier key bit
	 * @see GLFW#GLFW_KEY_CAPS_LOCK CAPS LOCK modifier key bit
	 * @see GLFW#GLFW_KEY_NUM_LOCK NUM LOCK modifier key bit
	 * @see <a href="https://www.glfw.org/docs/latest/group__mods.html" target="_top">the online GLFW documentation</a>
	 */
	public int getModifiers() {
		return this.modifiers;
	}
	
}
