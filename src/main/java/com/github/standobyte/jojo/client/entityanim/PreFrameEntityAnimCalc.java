package com.github.standobyte.jojo.client.entityanim;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.AnimWithId;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery.AnimMolangVariables;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimatedEntity;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityModel;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.config.client.ClientModSettings;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_grab.LivingComponentGrab;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PreFrameEntityAnimCalc {
	
	public static class LivingAnimState {
		@Nullable public ResourceLocation animSet;
		@Nullable public ActionAnimIdentifier animId;
		public float time;
		@Nullable public ActionPhase actionPhase;
		public float phaseTime;
		public float phaseCompletion;
		
		public void reset() {
			this.animSet = null;
			this.animId = null;
			this.time = -1;
			this.actionPhase = null;
			this.phaseTime = -1;
			this.phaseCompletion = -1;
		}
		
		public static LivingAnimState reusedInstance = new LivingAnimState();
	}

	public static void onBeforeEntitiesRender(ClientLevel level) {
		Minecraft mc = Minecraft.getInstance();
		DeltaTracker deltaTracker = mc.getTimer();
		TickRateManager tickRateManager = level.tickRateManager();
		for (Entity entity : level.entitiesForRendering()) {
			float partialTick = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
			@Nullable AnimFramePose pose = null;
			if (entity instanceof LivingEntity living) {
				pose = getLivingPose(living, partialTick, true);
			}
			((AnimatedEntity) entity).jojo_ripples$setModelPose(AnimatedEntity.PoseType.FINAL, pose);
		}
	}
	
	// TODO get rid of instanceof
	// TODO get rid of newFrame argument
	public static AnimFramePose getLivingPose(LivingEntity living, float partialTick, boolean newFrame) {
		LivingComponentAction actionComponent = LivingComponentAction.getExistingComponent(living);
		EntityActionInstance action = actionComponent != null ? actionComponent.getAction() : null;
		@Nullable StandEntity stand = living instanceof StandEntity __ ? __ : null;
		
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
		
		if (anim != null) {
			float timeSeconds = anim.getAnimTime(animVariables);
			AnimFramePose pose = anim.calcAnimPose(AnimMolangVariables.extract(living, partialTick), 
					actionComponent != null ? actionComponent.clPrevPunchPose : null, timeSeconds, 1);
			
			if (newFrame) {
				BarrageSwings barrageSwings = getBarrageSwings(living);
				if (barrageSwings != null) {
					barrageSwings.frameStandBarrage(Minecraft.getInstance(), anim, timeSeconds, living, living.tickCount + partialTick);
				}
			}
			
			if (ClientModSettings.getSettingsReadOnly().standMotionTilt && stand != null) {
				// FIXME save the pose without motion tilt separately (fixes punch combo interpolation)
				EntityRenderer renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(living);
				if (renderer instanceof StandEntityRenderer standEntityRenderer) {
					StandEntityModel model = standEntityRenderer.getEntityModel(stand);
					if (model != null) {
						Vec3 motionTiltVec = model.prepareMotionTilt(stand, partialTick);
						boolean idlePose = animVariables.animId != null && animVariables.animId.isIdle();
						model.doMotionTilt(motionTiltVec, pose, idlePose);
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
	
	@Nullable
	public static BarrageSwings getBarrageSwings(LivingEntity entity) {
		if (entity instanceof StandEntity stand) {
			return stand.clientStuff.barrageSwings;
		}
		return null;
	}

}
