package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.input.ClientsideAim;
import com.github.standobyte.jojo.client.ui.hud_misc.BottomLeftNotifications;
import com.github.standobyte.jojo.client.ui.utils.FadeOut;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.voiceline.VoiceLineClientSoundTracker;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientTickHandler {
	public static int tickCount;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		ModInteractionUtil.clientTickPre();
		ClientGlobals.tick(mc);
		ClientEntityController.clientTickPre();
		VoiceLineClientSoundTracker.tick();
		++tickCount;
	}

	@SubscribeEvent
	public static void onClientTickPost(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		ClientsideAim.updateTarget(mc, 1);
		ClientsideAim.updateTargetWithServer(mc);
		ClientEntityController.clientTickPost();
		if (!mc.isPaused()) {
			for (var fadeOut : FadeOut.__TO_TICK) fadeOut.__tick();
		}
		BottomLeftNotifications._tick();
		ClientTickables._tick();
	}

	@SubscribeEvent
	public static void onFrameRender(RenderFrameEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			float tickDelta = mc.getTimer().getGameTimeDeltaTicks();
			for (Entity entity : mc.level.entitiesForRendering()) {
				if (entity instanceof LivingEntity living) {
					StandPower standPower = entity == mc.player ? ClientPowerCache.getPower(PowerClass.STAND) : StandPower.get(living);
					if (standPower != null) {
						for (StandEffectInstance standEffect : standPower.userStandEffects.getEffects()) {
							standEffect.onFrame(tickDelta);
						}
					}
				}
			}
		}
	}
	
}
