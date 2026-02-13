package com.github.standobyte.jojo.mixin.client.screen;

import java.util.ArrayList;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ui.ScreenCrutches;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.Tickable;

@Mixin(Screen.class)
public class ScreenMixin implements ScreenCrutches {
	@Unique private Collection<Tickable> tickableChildren;

	@Override
	public void jojo_ripples$addTickable(Tickable tickable) {
		if (this.tickableChildren == null) {
			this.tickableChildren = new ArrayList<>();
		}
		tickableChildren.add(tickable);
	}
	
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo ci) {
		if (this.tickableChildren != null) {
			for (Tickable child : tickableChildren) {
				child.tick();
			}
		}
	}
}
