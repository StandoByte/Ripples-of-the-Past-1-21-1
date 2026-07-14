package com.github.standobyte.jojo.client.entityrender;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimVariantsList;
import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.LivingAnimState;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.AnimWithId;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery.AnimMolangVariables;
import com.github.standobyte.jojo.client.entityanim.player.HumanoidModelPartsWithBends;
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
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.mechanics.jojopose.resource.ClientJojoPoseLoader;
import com.github.standobyte.jojo.mechanics.jojopose.resource.JojoPoseAnimSet;
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
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class PreFrameEntityRenderCallback {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onFrameRender(RenderFrameEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level != null) {
			DeltaTracker deltaTracker = mc.getTimer();
			TickRateManager tickRateManager = level.tickRateManager();
			for (Entity entity : level.entitiesForRendering()) {
				float partialTick = ClientUtil.partialTick(entity, deltaTracker, tickRateManager);
				AnimFramePose pose = PreFrameEntityRenderCallback.makeEntityPose(entity, partialTick);
				((AnimatedEntity) entity).jojo_ripples$setModelPose(AnimatedEntity.PoseType.FINAL, pose);
			}
		}
	}
	
	@Nullable
	public static AnimFramePose makeEntityPose(Entity entity, float partialTick) {
		if (entity instanceof LivingEntity living) {
			return PreFrameEntityRenderCallback.makeLivingPose(living, partialTick, true);
		}
		return null;
	}
	
	// TODO get rid of instanceof
	// TODO get rid of newFrame argument
	@Nullable
	public static AnimFramePose makeLivingPose(LivingEntity living, float partialTick, boolean newFrame) {
		LivingComponentAction actionComponent = LivingComponentAction.getExistingComponent(living);
		EntityActionInstance action = actionComponent != null ? actionComponent.getAction() : null;
		@Nullable StandEntity stand = living instanceof StandEntity __ ? __ : null;
		LivingEntityRenderer renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(living) instanceof LivingEntityRenderer __ ? __ : null;
		if (renderer == null) return null;
		EntityModel model = renderer.getModel();
		
		LivingAnimState animVariables = LivingAnimState.reusedInstance;
		if (action != null) {
			action.extractAnim(animVariables, living, partialTick);
		}
		else {
			animVariables.reset();
		}

		List<RotpAnimDefinition> animsPre = null;
		RotpAnimDefinition anim = null;
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
			if (newFrame && !animVariables.animId.isIdle) {
				stand.nonIdlePoseTimeStamp = stand.tickCount;
			}
			
			AnimWithId standSummonAnim = getCurSummonAnimAndAdjustFreezeTime(stand, standSkin, animVariables);
			AnimWithId animPossiblyReplaced = standSummonAnim != null ? standSummonAnim : getStandAnim(standSkin, animVariables.animId, idleAnim);
			anim = animPossiblyReplaced.anim;
			animVariables.animId = animPossiblyReplaced.animId;
			animsPre = standSkin.getStandAlwaysAnimations();
			
			if (!stand.clientStuff.summonAnimStopped && !animVariables.animId.isSummon) {
				stand.clientStuff.summonAnimStopped = true;
			}
		}
		else {
			if (animVariables.animFromJojoPosesLoader) {
				if (animVariables.animSet != null && animVariables.animId != null) {
					JojoPoseAnimSet animSet = ClientJojoPoseLoader.getInstance().getAnimSet(animVariables.animSet);
					if (animSet != null) {
						anim = animSet.anims().getNamedAnim(animVariables.animId);
					}
				}
			}
			else {
				if (animVariables.animSet != null && animVariables.animId != null) {
					AnimationSet animSet = AnimationLoader.getInstance().getAnimSet(animVariables.animSet);
					if (animSet != null) {
						anim = animSet.getNamedAnim(animVariables.animId);
					}
				}
			}
		}
		
		boolean adjustComplexBends = false;
		if (model instanceof PlayerModel playerModel) {
			ReplacePlayerModelEvent event = ModClientEventHooks.preRenderReplacePlayerModel(
					living, renderer, partialTick, animVariables);
			event.afterEvent();
			if (event.animation != null) {
				anim = event.animation;
			}
			adjustComplexBends = event.replacingModel == null;
		}
		
		AnimFramePose pose = AnimFramePose.reused.clear();
		if (animsPre != null) {
			float time = living.tickCount + partialTick;
			for (RotpAnimDefinition animPre : animsPre) {
				float timeSeconds = animPre.getAnimTime(time);
				animPre.calcAnimPose(pose, timeSeconds, 1, 
						AnimMolangVariables.extract(living, partialTick), 
						actionComponent != null ? actionComponent.clPrevPunchPose : null);
			}
		}
		if (anim != null) {
			float timeSeconds = anim.getAnimTime(animVariables);
			anim.calcAnimPose(pose, timeSeconds, 1, 
					AnimMolangVariables.extract(living, partialTick), 
					actionComponent != null ? actionComponent.clPrevPunchPose : null);
			if (adjustComplexBends) {
				HumanoidModelPartsWithBends.adjustComplexBends(pose);
			}
			
			if (newFrame) {
				BarrageSwings barrageSwings = EntityActionRenderState.getBarrageSwings(living);
				if (barrageSwings != null) {
					barrageSwings.frameStandBarrage(Minecraft.getInstance(), anim, timeSeconds, living, living.tickCount + partialTick);
				}
			}
			
			if (JojoMod.config.getClient().standMotionTilt.getAsBoolean() && stand != null
					&& (animVariables.animId == null || !animVariables.animId.isSummon)) {
				// XXX interpolate motion tilt from summon pose
				// FIXME save the pose without motion tilt separately (fixes punch combo interpolation)
				if (renderer instanceof StandEntityRenderer standEntityRenderer) {
					StandEntityModel standModel = standEntityRenderer.getEntityModel(stand);
					if (standModel != null) {
						Vec3 motionTiltVec = standModel.prepareMotionTilt(stand, partialTick);
						boolean idlePose = animVariables.animId != null && animVariables.animId.isIdle;
						standModel.doMotionTilt(motionTiltVec, pose, idlePose);
					}
				}
			}
			
			return pose;
		}
		
		return null;
	}
	
	@Nullable
	private static AnimWithId getCurSummonAnimAndAdjustFreezeTime(StandEntity stand, StandSkin standSkin, LivingAnimState animVariables) {
		if (!stand.clientStuff.summonAnimStopped) {
			if (!animVariables.animId.isIdle) {
				stand.clientStuff.summonAnimStopped = true;
			}
			else {
				EntityActionInstance userJojoPose = null;
				LivingEntity user = stand.getUser();
				if (user != null) {
					var userAction = LivingComponentAction.getCurEntityAction(user);
					if (userAction != null && userAction.ability == ModSpecialActions.JOJO_POSE.get()) {
						userJojoPose = userAction;
					}
				}
				
				int summonAnimIndex = stand.summonPoseRandomByte;
				AnimWithId standSummonAnim = getStandSummonAnim(standSkin, summonAnimIndex);
				
				if (standSummonAnim != null) {
					boolean freezeAtSummonPose = userJojoPose != null;
					if (freezeAtSummonPose && !standSummonAnim.anim.poses.isEmpty()) {
						float freezeAtSummonPoseTimestamp = standSummonAnim.anim.poses.values().iterator().next().timeInSeconds() * 20;
						
						if (animVariables.time > freezeAtSummonPoseTimestamp) {
							stand.clientStuff.freezeAtSummonPoseTime = animVariables.time - freezeAtSummonPoseTimestamp;
						}
					}
					
					if (stand.clientStuff.freezeAtSummonPoseTime > 0) {
						animVariables.time -= stand.clientStuff.freezeAtSummonPoseTime;
					}
					
					if (standSummonAnim.anim.lengthInSeconds * 20 > animVariables.time) {
						return standSummonAnim;
					}
				}
			}
		}
		
		return null;
	}
	
	
	@Nullable
	public static AnimWithId getStandSummonAnim(StandSkin skin, int index) {
		AnimVariantsList summonAnims = skin.getStandAnimations("summon");
		if (summonAnims != null) {
			index %= summonAnims.anims.size();
			RotpAnimDefinition summonAnim = summonAnims.get(index);
			return AnimWithId.with(ActionAnimIdentifier.getOrCreate("summon", index).setSummon(), summonAnim);
		}
		return null;
	}
	
	public static AnimWithId getStandAnim(StandSkin skin, ActionAnimIdentifier animId, ActionAnimIdentifier curIdleAnim) {
		if (skin != null) {
			if (animId != null) {
				RotpAnimDefinition anim = skin.getStandAnimation(animId);
				if (anim != null) {
					return AnimWithId.with(animId, anim);
				}
				else {
					anim = skin.getStandAnimation(curIdleAnim);
					return AnimWithId.with(curIdleAnim, anim);
				}
			}
		}
		return AnimWithId.with(null, null);
	}
}
