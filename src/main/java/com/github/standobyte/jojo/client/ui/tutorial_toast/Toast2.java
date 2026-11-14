package com.github.standobyte.jojo.client.ui.tutorial_toast;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.gui.components.toasts.Toast;

public interface Toast2 extends Toast {
	boolean isShowing();

	boolean isShownThisSession();
	void setShownThisSession(boolean shown);
	boolean isCompleted();
	void setCompleted(boolean completed);
	
	default boolean canShow() { return 
			!isShownThisSession()
			&& !isCompleted()
			&& JojoMod.config.getClient().tutorialEnabled.getAsBoolean()
			&& !isShowing(); }
	void showOnce();
	void showOnceTimed(int durationTicks);
	void hide();
	void updateProgress(float progress);
}
