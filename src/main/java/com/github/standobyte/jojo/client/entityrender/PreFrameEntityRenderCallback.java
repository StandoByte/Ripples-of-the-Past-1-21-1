package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.LivingAnimState;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.AnimWithId;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery.AnimMolangVariables;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimatedEntity;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityModel;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.event.client.ModClientEventHooks;
import com.github.standobyte.jojo.event.client.ReplacePlayerModelEvent;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_grab.LivingComponentGrab;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PreFrameEntityRenderCallback {

	public static void onBeforeEntitiesRender(ClientLevel level) {
		Minecraft mc = Minecraft.getInstance();
		DeltaTracker deltaTracker = mc.getTimer();
		TickRateManager tickRateManager = level.tickRateManager();
		for (Entity entity : level.entitiesForRendering()) {
			float partialTick = ClientUtil.partialTick(entity, deltaTracker, tickRateManager);
			AnimFramePose pose = PreFrameEntityRenderCallback.makeEntityPose(entity, partialTick);
			((AnimatedEntity) entity).jojo_ripples$setModelPose(AnimatedEntity.PoseType.FINAL, pose);
		}
	}
	
	public static AnimFramePose makeEntityPose(Entity entity, float partialTick) {
		if (entity instanceof LivingEntity living) {
			return PreFrameEntityRenderCallback.makeLivingPose(living, partialTick, true);
		}
		return null;
	}
	
	// TODO get rid of instanceof
	// TODO get rid of newFrame argument
	public static AnimFramePose makeLivingPose(LivingEntity living, float partialTick, boolean newFrame) {
		LivingComponentAction actionComponent = LivingComponentAction.getExistingComponent(living);
		EntityActionInstance action = actionComponent != null ? actionComponent.getAction() : null;
		@Nullable StandEntity stand = living instanceof StandEntity __ ? __ : null;
		LivingEntityRenderer renderer = (LivingEntityRenderer) Minecraft.getInstance()
				.getEntityRenderDispatcher().getRenderer(living);
		EntityModel model = renderer.getModel();
		
		LivingAnimState animVariables = LivingAnimState.reusedInstance;
		if (action != null) {
			action.extractAnim(animVariables, living, partialTick);
		}
		else {
			animVariables.reset();
		}

		RotpAnimDefinition anim;
		if (stand != null) {
			StandSkin standSkin = StandSkinsLoader.getInstance().getSkin(stand);
			boolean isGrabbing = LivingComponentGrab.getEntityGrabbedBy(stand) != null;
			ActionAnimIdentifier idleAnim = isGrabbing ? StandEntityRenderer.GRAB_IDLE_ANIM : StandEntityRenderer.IDLE_ANIM;

			if (animVariables.animId == null) {
				float idleTime = stand.tickCount - stand.nonIdlePoseTimeStamp + partialTick;
				// FIXME for a bit after grabbing, the grabbed entity is not yet synced to the client, causing it to use regular idle anim for a few frames
				if (isGrabbing) {
					animVariables.animId = idleAnim;
					animVariables.actionPhase = ActionPhase.PERFORM;
					animVariables.phaseTime = idleTime;
				}
				else {
					animVariables.animId = idleAnim;
					animVariables.time = idleTime;
				}
			}
			if (newFrame && !animVariables.animId.isIdle()) {
				stand.nonIdlePoseTimeStamp = stand.tickCount;
			}

			AnimWithId animPossiblyReplaced = getStandAnim(standSkin, animVariables.animId, idleAnim);
			anim = animPossiblyReplaced.anim;
			animVariables.animId = animPossiblyReplaced.animId;
		}
		else {
			anim = getPlayerAnim(animVariables.animSet, animVariables.animId);
		}
		
		if (model instanceof PlayerModel playerModel) {
			ReplacePlayerModelEvent event = ModClientEventHooks.preRenderReplacePlayerModel(
					living, renderer, partialTick, animVariables);
			event.afterEvent();
			if (event.animation != null) {
				anim = event.animation;
			}
		}
		
		if (anim != null) {
			float timeSeconds = anim.getAnimTime(animVariables);
			AnimFramePose pose = anim.calcAnimPose(AnimMolangVariables.extract(living, partialTick), 
					actionComponent != null ? actionComponent.clPrevPunchPose : null, timeSeconds, 1);
			
			if (newFrame) {
				BarrageSwings barrageSwings = EntityActionRenderState.getBarrageSwings(living);
				if (barrageSwings != null) {
					barrageSwings.frameStandBarrage(Minecraft.getInstance(), anim, timeSeconds, living, living.tickCount + partialTick);
				}
			}
			
			if (JojoMod.config.getClient().standMotionTilt.getAsBoolean() && stand != null) {
				// FIXME save the pose without motion tilt separately (fixes punch combo interpolation)
				if (renderer instanceof StandEntityRenderer standEntityRenderer) {
					StandEntityModel standModel = standEntityRenderer.getEntityModel(stand);
					if (standModel != null) {
						Vec3 motionTiltVec = standModel.prepareMotionTilt(stand, partialTick);
						boolean idlePose = animVariables.animId != null && animVariables.animId.isIdle();
						standModel.doMotionTilt(motionTiltVec, pose, idlePose);
					}
				}
			}
			
			return pose;
		}
		
		return null;
	}
	
	public static RotpAnimDefinition getPlayerAnim(ResourceLocation animSetPath, ActionAnimIdentifier animId) {
		if (animSetPath != null && animId != null) {
			AnimationSet animSet = AnimationLoader.getInstance().getAnimSet(animSetPath);
			if (animSet != null) {
				RotpAnimDefinition anim = animSet.getNamedAnim(animId);
				return anim;
			}
		}
		return null;
	}
	
	public static AnimWithId getStandAnim(StandSkin skin, ActionAnimIdentifier animId, ActionAnimIdentifier curIdleAnim) {
		if (skin != null) {
			if (animId != null) {
				RotpAnimDefinition anim = skin.getStandAnimation(anims -> anims.getNamedAnim(animId));
				if (anim == null) {
					anim = skin.getStandAnimation(anims -> anims.getNamedAnim(curIdleAnim));
					if (anim != null) {
						return AnimWithId.with(curIdleAnim, anim);
					}
				}
				return AnimWithId.with(animId, anim);
			}
		}
		return AnimWithId.with(null, null);
	}
}
