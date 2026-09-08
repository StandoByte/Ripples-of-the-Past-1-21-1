package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.core.JojoMod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.RegisterStageEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class CustomLevelRenderStages {
	public static RenderLevelStageEvent.Stage BEFORE_SPECTATOR_SHADER;
	public static RenderLevelStageEvent.Stage AFTER_HAND_RENDER;
	
	@SubscribeEvent
	public static void whyMakeAnEventSubcriberForStagesWithNullRenderTypeToA(RegisterStageEvent whichDoesntActuallyRegister/*?*/) {
		BEFORE_SPECTATOR_SHADER = whichDoesntActuallyRegister.register(JojoMod.resLoc("before_spectator_shader"), null);
		AFTER_HAND_RENDER = whichDoesntActuallyRegister.register(JojoMod.resLoc("after_hand_render"), null);
	}
}
