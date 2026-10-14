package com.github.standobyte.jojo.mechanics.sunburn;

import static com.github.standobyte.jojo.mechanics.sunburn.SunBurn.DAY_LENGTH;
import static com.github.standobyte.jojo.mechanics.sunburn.SunBurn.TIME_SUNRISE_START;
import static com.github.standobyte.jojo.mechanics.sunburn.SunBurn.TIME_SUNSET_END;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class WaitAMinuteIsClockActuallyKindaUseful {

	@SubscribeEvent
	public static void addTextToClock(ItemTooltipEvent event) {
		ItemStack item = event.getItemStack();
		if (item.getItem() == Items.CLOCK) {
			Level level = event.getContext().level();
			if (level != null) {
				int time = (int) level.getDayTime() % DAY_LENGTH;
				
				String tlKey;
				int ticks;
				
				// remaining daytime
				if (time > TIME_SUNRISE_START + DAY_LENGTH || time <= TIME_SUNSET_END) {
					tlKey = "jojo_ripples.clock.daytime";
					ticks = TIME_SUNSET_END - time;
					if (ticks < 0) {
						ticks += DAY_LENGTH;
					}
				}
				// remaining nighttime
				else {
					tlKey = "jojo_ripples.clock.nighttime";
					ticks = TIME_SUNRISE_START + DAY_LENGTH - time;
				}
				
				String timeFormatted = StringUtil.formatTickDuration(ticks, level.tickRateManager().tickrate());
				MutableComponent timeTooltip = Component.translatable(tlKey, timeFormatted);
				event.getToolTip().add(timeTooltip.withStyle(ChatFormatting.GRAY));
			}
		}
	}
	
}
