package com.github.standobyte.jojo.event.client;

import org.jetbrains.annotations.ApiStatus;

import net.neoforged.neoforge.common.NeoForge;

@ApiStatus.Internal
public abstract class ModClientEventHooks {

	public static boolean onKeyboardInputPre(int key, int scanCode, int action, int modifiers) {
		return NeoForge.EVENT_BUS.post(new PreKeyInputEvent(key, scanCode, action, modifiers)).isCanceled();
	}
}
