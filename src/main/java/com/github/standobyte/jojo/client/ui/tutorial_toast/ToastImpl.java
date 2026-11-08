package com.github.standobyte.jojo.client.ui.tutorial_toast;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ToastImpl implements Toast2 {
	protected static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/tutorial");
	protected int PROGRESS_BAR_WIDTH = width() - 6;
	protected int PROGRESS_BAR_HEIGHT = 1;
	protected int PROGRESS_BAR_X = 3;
	protected int PROGRESS_BAR_Y = height() - 4;
	
	@Nullable protected ResourceLocation icon;
	protected Component line1;
	@Nullable protected Component line2;
	protected boolean shownThisSession = false;
	protected boolean completed = false;
	
	protected Toast.Visibility visibility = Toast.Visibility.HIDE;
	protected long lastTime;
	protected long lerpTime;
	protected float lastProgress;
	protected float progress;

	public ToastImpl(@Nullable ResourceLocation icon, Component line1, @Nullable Component line2) {
		this.icon = icon;
		this.line1 = line1;
		this.line2 = line2;
	}
	
	@Override
	public boolean isShowing() {
		return visibility == Toast.Visibility.SHOW;
	}
	
	@Override
	public boolean isShownThisSession() {
		return shownThisSession;
	}
	
	@Override
	public void setShownThisSession(boolean shown) {
		this.shownThisSession = shown;
	}
	
	@Override
	public boolean isCompleted() {
		return completed;
	}
	
	@Override
	public void setCompleted(boolean completed) {
		if (this.completed != completed) {
			this.completed = completed;
			if (completed) {
				hide();
				ToastsHelper.getInstance().saveToFileSystem();
			}
		}
	}
	
	@Override
	public void showOnce() {
		if (!canShow()) return;
		
		visibility = Toast.Visibility.SHOW;
		shownThisSession = true;
		lastProgress = 0;
		progress = 0;
		lastTime = 0;
		lerpTime = 0;
		
		Minecraft.getInstance().getToasts().addToast(this);
	}
	
	@Override
	public void showOnceTimed(int durationTicks) {
		if (!canShow()) return;
		
		showOnce();
		ToastsHelper toastsHelper = ToastsHelper.getInstance();
		toastsHelper.timedToasts.add(new TimedToast(this, durationTicks));
	}

	@Override
	public void hide() {
		visibility = Toast.Visibility.HIDE;
	}

	@Override
	public void updateProgress(float progress) {
		this.lastProgress = this.progress;
		this.lerpTime = 0;
		this.progress = progress;
	}

	@Override
	public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
		Minecraft mc = toastComponent.getMinecraft();
		PoseStack poseStack = guiGraphics.pose();
		guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, width(), height());
		int textX = 30;
		if (icon != null) {
            RenderSystem.enableBlend();
            BlitFloat.blit(poseStack, mc, icon, 6, 6, 20, 20, 0, BlitFloat.NO_TINT);
		}
		else {
			textX -= 22;
		}
		if (line2 == null) {
			guiGraphics.drawString(mc.font, line1, textX, 12, 0xFF000000, false);
		}
		else {
			guiGraphics.drawString(mc.font, line1, textX, 7, 0xFF000000, false);
			guiGraphics.drawString(mc.font, line2, textX, 18, 0xFF000000, false);
		}

		if (progress > 0) {
			guiGraphics.fill(PROGRESS_BAR_X, PROGRESS_BAR_Y, 
					PROGRESS_BAR_X + PROGRESS_BAR_WIDTH, PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT, 
					-1);
			
			long msDiff = timeSinceLastVisible - this.lastTime;
			float partialTick = Math.min(lerpTime / 50f, 1);
			this.lerpTime += msDiff;
			
			float fill = Mth.lerp(partialTick, lastProgress, progress);
			int color = progress >= lastProgress ? 0xFF000000 : 0xff550000;
			
			// maybe this was actually the only thing that was needed to make the bar not look like shit
			// why do they have int parameters in GuiGraphics methods if they then cast to float anyway
			BlitFloat.fill(poseStack, 
					PROGRESS_BAR_X, PROGRESS_BAR_Y, 
					PROGRESS_BAR_X + PROGRESS_BAR_WIDTH * fill, PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT, 
					0, color);
			this.lastTime = timeSinceLastVisible;
		}

		return visibility;
	}
}
