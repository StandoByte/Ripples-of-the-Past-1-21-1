package com.github.standobyte.jojo.client.ui.utils.tooltip;

import java.util.OptionalInt;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/**
 * {@link RenderTooltipEvent.Color} works well out of the box when you want to change the tooltip for an item,
 * but my case of changing the colors in my UI is more specific
 */
@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class TooltipParams {
	public OptionalInt backgroundStart;
	public OptionalInt backgroundEnd;
	public OptionalInt borderStart;
	public OptionalInt borderEnd;
	public boolean disableShadow;

	public TooltipParams(OptionalInt backgroundStart, OptionalInt backgroundEnd, OptionalInt borderStart, OptionalInt borderEnd, boolean disableShadow) {
		this.backgroundStart = backgroundStart;
		this.backgroundEnd = backgroundEnd;
		this.borderStart = borderStart;
		this.borderEnd = borderEnd;
		this.disableShadow = disableShadow;
	}
	
	public static TooltipParams paperStyle() { // white background with black border
		return new TooltipParams(
				OptionalInt.of(0xc0FFFFFF), 
				OptionalInt.of(0xc0FFFFFF),
				OptionalInt.of(0xc0000000),
				OptionalInt.of(0xc0000000), true);
	}
	
	public static TooltipParams paperStyle(float bgAlpha) {
		int alphaChannel = FastColor.as8BitChannel(bgAlpha)<< 24 ;
		return new TooltipParams(
				OptionalInt.of(alphaChannel | 0x00FFFFFF), 
				OptionalInt.of(alphaChannel | 0x00FFFFFF),
				OptionalInt.of(0xc0000000),
				OptionalInt.of(0xc0000000), true);
	}

	protected static TooltipParams instance;
	public static void set(TooltipParams params) {
		instance = params;
	}
	

	
	@SubscribeEvent
	public static void tooltipColor(RenderTooltipEvent.Color event) {
		if (instance != null) {
			instance.backgroundStart.ifPresent(event::setBackgroundStart);
			instance.backgroundEnd.ifPresent(event::setBackgroundEnd);
			instance.borderStart.ifPresent(event::setBorderStart);
			instance.borderEnd.ifPresent(event::setBorderEnd);
		}
	}
	
	public static boolean disableShadow() {
		return instance != null && instance.disableShadow;
	}
	
	public static void renderTooltipPost() {
		instance = null;
	}
}
