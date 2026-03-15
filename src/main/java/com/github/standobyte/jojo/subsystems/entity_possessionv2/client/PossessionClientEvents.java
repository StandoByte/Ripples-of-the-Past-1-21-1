package com.github.standobyte.jojo.subsystems.entity_possessionv2.client;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.entity_possessionv2.LivingComponentPossession;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class PossessionClientEvents {
	
	@SubscribeEvent
	public static void cancelEntityRender(RenderLivingEvent.Pre<?, ?> event) {
		if (LivingComponentPossession.isPossessingSomeone(event.getEntity())) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void cancelAttackOrInteraction(InteractionKeyMappingTriggered event) {
		if ((event.isAttack() || event.isUseItem()) && LivingComponentPossession.isPossessingSomeone(Minecraft.getInstance().player)) {
			event.setCanceled(true);
			event.setSwingHand(false);
		}
	}
}
