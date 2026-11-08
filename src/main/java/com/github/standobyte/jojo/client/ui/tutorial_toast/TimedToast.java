package com.github.standobyte.jojo.client.ui.tutorial_toast;

public class TimedToast {
	public static final int USUAL_DURATION = 160;
	public final Toast2 toast;
	public final int durationTicks;
	public int progress;

	public TimedToast(Toast2 toast, int durationTicks) {
		this.toast = toast;
		this.durationTicks = durationTicks;
	}

	public boolean updateProgress() {
		toast.updateProgress(Math.min((float)(++progress) / (float)durationTicks, 1.0F));
		if (progress > durationTicks) {
			toast.hide();
			return true;
		}
		else {
			return false;
		}
	}
}
