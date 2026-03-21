package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.input.ClientsideAim;
import com.github.standobyte.jojo.client.ui.hud.BottomLeftNotifications;
import com.github.standobyte.jojo.client.ui.utils.FadeOut;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.client.ClientEntityController;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientTickHandler {
	public static int tickCount;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		ModInteractionUtil.clientTickPre();
		ClientGlobals.tick(mc);
		ClientEntityController.clientTickPre();
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
		if (mc.player != null) {
			limitEntityRotation(mc.player);
		}

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

	@SubscribeEvent
	public static void onLivingRender(RenderLivingEvent.Pre<?, ?> event) {
		LivingEntity entity = event.getEntity();
		limitEntityRotation(entity);
	}

	@SubscribeEvent
	public static void onRenderFrame(RenderLevelStageEvent event) {
		RenderLevelStageEvent.Stage stage = event.getStage();
		if (stage == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			ClientLevel level = Minecraft.getInstance().level;
			var attachmentType = ModDataAttachmentTypes.LIVING_GRAB.get();
			for (Entity entity : level.entitiesForRendering()) {
				LivingComponentGrab grabComponent = entity.getData(attachmentType);
				if (grabComponent != null) {
					grabComponent.setGrabbedPos();
				}
			}
		}
	}
	
	protected static void limitEntityRotation(LivingEntity entity) {
		LivingComponentGrab grabComponent = entity.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
		if (grabComponent != null) {
			grabComponent.applyRotationDiff();
		}
	}
	
}
