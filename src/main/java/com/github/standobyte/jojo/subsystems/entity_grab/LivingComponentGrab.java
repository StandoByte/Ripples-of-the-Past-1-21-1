package com.github.standobyte.jojo.subsystems.entity_grab;

import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.entityrender.ModelUtil;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.jojo.util.functions.UtilFunctions;
import com.github.standobyte.jojo.util.objects_mc.EntityResolver;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class LivingComponentGrab implements TickingEntityData {
	static final AttributeModifier GRABBED_NO_ATTACK_POWER = new AttributeModifier(
			JojoMod.resLoc("grabbed_no_attack"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	static final AttributeModifier GRABBED_NO_GRAVITY = new AttributeModifier(
			JojoMod.resLoc("grabbed_no_gravity"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	
	private final LivingEntity thisEntity;
	private LivingEntity grabbingEntity = null;
	private LivingEntity grabbedTarget = null;
	
	public float xRotWhenGrabbed;
	public float yRotWhenGrabbed;
	public float yRotDiffWhenGrabbed;
	public float yHeadRotDiffWhenGrabbed;
	public float yBodyRotDiffWhenGrabbed;
	public EntityResolver standUser = new EntityResolver();
	@Nullable public Vec3 userOffsetFromStandWhenGrabbed;
	
	public LivingComponentGrab(LivingEntity entity) {
		this.thisEntity = entity;
		addTicking(entity);
	}
	
	
	@Nullable
	public static LivingEntity getEntityGrabbedBy(LivingEntity grabbing) {
		if (!grabbing.hasData(ModDataAttachmentTypes.LIVING_GRAB.get())) return null;;
		
		LivingComponentGrab grabbing_ = grabbing.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
		return grabbing_.grabbedTarget;
	}
	
	@Nullable
	public static LivingEntity getEntityGrabbing(LivingEntity target) {
		if (!target.hasData(ModDataAttachmentTypes.LIVING_GRAB.get())) return null;;
		
		LivingComponentGrab target_ = target.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
		return target_.grabbingEntity;
	}

	
	@Override
	public void tick() {
		if (grabbedTarget != null) {
			if (!grabbedTarget.isAlive()) {
				this.setGrabTarget(null);
			}
			else if (thisEntity instanceof StandEntity stand) {
				LivingEntity user = stand.getUser();
				if (user != null && grabbedTarget.isPassengerOfSameVehicle(user)) {
					this.setGrabTarget(null);
				}
			}
		}

		if (grabbingEntity != null) {
			if (!grabbingEntity.isAlive()) {
				grabbingEntity
				.getData(ModDataAttachmentTypes.LIVING_GRAB.get())
				.setGrabTarget(null);
			}
			else {
				thisEntity.fallDistance = 0;
			}
		}
	}
	
	public void setGrabTarget(LivingEntity target) {
		if (grabbedTarget != null && grabbedTarget != target) {
			grabbedTarget
			.getData(ModDataAttachmentTypes.LIVING_GRAB.get())
			.setGrabbedBy(null);
		}

		if (target != null) {
			LivingComponentGrab target_ = target.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
			boolean canBeGrabbed = !target_.isGrabbed();
			if (canBeGrabbed) {
				target_.setGrabbedBy(thisEntity);
			}
		}

		grabbedTarget = target;
		if (!thisEntity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(thisEntity, new TrSetGrabbedEntityPacket(thisEntity.getId(), target != null ? target.getId() : -1));
		}
	}
	
	@ApiStatus.Internal
	public void setGrabbedBy(LivingEntity grabbing) {
		boolean clientSide = thisEntity.level().isClientSide();
		if (!clientSide) {
			Optional.ofNullable(thisEntity.getAttribute(Attributes.ATTACK_DAMAGE)).ifPresent(attackDamage -> {
				if (grabbing != null)	attackDamage.addTransientModifier(GRABBED_NO_ATTACK_POWER);
				else					attackDamage.removeModifier(GRABBED_NO_ATTACK_POWER);
			});
			Optional.ofNullable(thisEntity.getAttribute(Attributes.GRAVITY)).ifPresent(gravity -> {
				if (grabbing != null)	gravity.addTransientModifier(GRABBED_NO_GRAVITY);
				else					gravity.removeModifier(GRABBED_NO_GRAVITY);
			});

		}
		if (grabbing != null) {
			if (!clientSide && thisEntity.isPassenger()) {
				thisEntity.stopRiding();
			}
			saveRotationDiff(grabbing);
		}

		this.grabbingEntity = grabbing;
	}
	
	
	public boolean isGrabbed() {
		return grabbingEntity != null && grabbingEntity.isAlive();
	}

	public LivingEntity getGrabbedEntity() {
		return grabbedTarget;
	}
	
	
	protected void saveRotationDiff(LivingEntity grabbing) {
		this.xRotWhenGrabbed = thisEntity.getXRot();
		this.yRotWhenGrabbed = thisEntity.getYRot();
		this.yRotDiffWhenGrabbed = yRotWhenGrabbed - grabbing.yBodyRot;
		this.yHeadRotDiffWhenGrabbed = thisEntity.getYHeadRot() - yRotWhenGrabbed;
		this.yBodyRotDiffWhenGrabbed = thisEntity.yBodyRot - yRotWhenGrabbed;
		if (thisEntity instanceof StandEntity standEntity) {
			LivingEntity user = standEntity.getUser();
			if (user != null) {
				standUser.setEntity(user);
				userOffsetFromStandWhenGrabbed = user.position().subtract(thisEntity.position());
			}
		}
	}
	
	
	protected void setGrabbedPos(float partialTick) {
		if (grabbingEntity != null) {
			float yRot = getGrabYRot(grabbingEntity, partialTick);
			Vec3 grabOffset = getGrabOffset(grabbingEntity, partialTick, yRot);
			
			Vec3 grabbedPos = grabbingEntity.position().add(grabOffset);
			thisEntity.setPos(grabbedPos.x, grabbedPos.y, grabbedPos.z);
			thisEntity.setDeltaMovement(Vec3.ZERO);
			for (Entity passenger : thisEntity.getPassengers()) {
				thisEntity.positionRider(passenger);
			}
			JojoMod.LOGGER.debug("{} {} {} {}", yRot * MathUtil.RAD_TO_DEG, this.yRotDiffWhenGrabbed, this.yHeadRotDiffWhenGrabbed, this.yBodyRotDiffWhenGrabbed);
			if (userOffsetFromStandWhenGrabbed != null) {
				Entity standUser = this.standUser.getEntity(thisEntity.level());
				if (standUser != null) {
					float grabbedStandYRot = partialTick == 1.0F ? thisEntity.getYRot() : Mth.lerp(partialTick, thisEntity.yRotO, thisEntity.getYRot());
					Vec3 absoluteOffset = userOffsetFromStandWhenGrabbed.yRot((this.yRotWhenGrabbed - grabbedStandYRot) * MathUtil.DEG_TO_RAD);
					Vec3 standUserPos = grabbedPos.add(absoluteOffset);
					standUser.setPos(standUserPos.x, standUserPos.y, standUserPos.z);
					standUser.setDeltaMovement(Vec3.ZERO);
				}
			}
		}
	}
	
	protected float getGrabYRot(LivingEntity grabbingEntity, float partialTick) {
		//float yRot = partialTick == 1 ? grabbingEntity.yBodyRot : Mth.lerp(partialTick, grabbingEntity.yBodyRotO, grabbingEntity.yBodyRot);
		float yRot = grabbingEntity.yBodyRot;
		yRot = -yRot * MathUtil.DEG_TO_RAD;
		return yRot;
	}
	
	protected static Vec3 armChokeOffset = new Vec3(0, -0.125, 0);
	// TODO add a way to override grab offset for external grab methods (Hermit Purple, Sticky Fingers, etc.)
	protected Vec3 getGrabOffset(LivingEntity grabbingEntity, float partialTick, float yRot) {
		HumanoidArm grabbingArm = HumanoidArm.LEFT;
		double neckY = -thisEntity.getBbHeight() * 0.8 - 0.125;
		Vec3 grabOffset = new Vec3(0, neckY, 0);
		
		boolean useModelArmPos = grabbingEntity.level().isClientSide();
		if (useModelArmPos) {
			Vec3 animOffset = ModelUtil.getModelPartPos(
					grabbingEntity, 
					grabbingArm == HumanoidArm.LEFT ? "left_item" : "right_item", armChokeOffset);
			if (animOffset != null) {
				animOffset = animOffset.yRot(yRot);
				grabOffset = grabOffset.add(animOffset);
				// TODO (grab) sync the offset to the server?
			}
			else {
				useModelArmPos = false;
			}
		}
		
		if (!useModelArmPos) {
			grabOffset = grabOffset.add(new Vec3(grabbingArm == HumanoidArm.LEFT ? 0.2 : -0.2, 1.5, 0.875)
					/* lifting the target up and down a bit from x rotation would be cool, 
					 * but we'd have to also adjust the grab animations for this and it's a PITA, 
					 * so unfortunately this goes into the "commented out" hell
					 */
					// .xRot(-grabbingEntity.getXRot() * MathUtil.DEG_TO_RAD)
					.yRot(yRot));
		}
		return grabOffset;
	}


	protected void applyRotationDiff(float partialTick) {
		if (grabbingEntity != null) {
			applyRotationDiff(thisEntity, partialTick);
		}
	}
	
	protected void applyRotationDiff(LivingEntity grabbedEntity, float partialTick) {
		float _grabbingYRot0 = grabbingEntity.yBodyRotO;
		float _grabbingYRot1 = grabbingEntity.yBodyRot;
		float grabbingYRot;
		if (partialTick == 1) {
			grabbingYRot = _grabbingYRot1;
		}
		else {
			while (_grabbingYRot0 - _grabbingYRot1 < 180) _grabbingYRot1 -= 360;
			while (_grabbingYRot0 - _grabbingYRot1 > 180) _grabbingYRot1 += 360;
			grabbingYRot = Mth.lerp(partialTick, _grabbingYRot0, _grabbingYRot1);
		}
		
		float yRot = Mth.wrapDegrees(grabbingYRot + this.yRotDiffWhenGrabbed);
		float yHeadRot = Mth.wrapDegrees(yRot + this.yHeadRotDiffWhenGrabbed);
		float yBodyRot = Mth.wrapDegrees(yRot + this.yBodyRotDiffWhenGrabbed);
		grabbedEntity.setYRot(yRot);
		grabbedEntity.setYHeadRot(yHeadRot);
		grabbedEntity.setYBodyRot(yBodyRot);
		grabbedEntity.yRotO = yRot;
		grabbedEntity.yHeadRotO = grabbedEntity.yHeadRot;
		grabbedEntity.yBodyRotO = grabbedEntity.yBodyRot;
		grabbedEntity.setXRot(this.xRotWhenGrabbed);
		
		if (userOffsetFromStandWhenGrabbed != null) {
			LivingEntity standUser = this.standUser.getEntityLiving(grabbedEntity.level());
			if (standUser != null) {
				standUser.setYRot(yRot);
				standUser.setYHeadRot(yHeadRot);
				standUser.setYBodyRot(yBodyRot);
				standUser.yRotO = yRot;
				standUser.yHeadRotO = standUser.yHeadRot;
				standUser.yBodyRotO = standUser.yBodyRot;
				standUser.setXRot(this.xRotWhenGrabbed);
			}
		}
	}
	
	
	@SubscribeEvent
	public static void onLevelTickPost(LevelTickEvent.Post event) {
		Level level = event.getLevel();
		var attachmentType = ModDataAttachmentTypes.LIVING_GRAB.get();
		for (Entity entity : UtilFunctions.getEntities(level)) {
			LivingComponentGrab grabComponent = entity.getData(attachmentType);
			if (grabComponent != null) {
				grabComponent.setGrabbedPos(1);
				if (!level.isClientSide()) {
					grabComponent.applyRotationDiff(1);
				}
			}
		}
	}
	

	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
	public static class ClientEvents {

		@SubscribeEvent
		public static void onFrameRender(RenderFrameEvent.Pre event) {
			Minecraft mc = Minecraft.getInstance();
			
			if (ClientGlobals.playerStandEntity != null) {
				LivingComponentGrab grabComponent = ClientGlobals.playerStandEntity.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
				if (grabComponent != null && grabComponent.grabbingEntity != null) {
					float partialTick = ClientUtil.partialTick(ClientGlobals.playerStandEntity, 
							event.getPartialTick(), mc.level.tickRateManager());
					grabComponent.applyRotationDiff(partialTick);
				}
			}
			
			if (mc.player != null) {
				LivingComponentGrab grabComponent = mc.player.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
				if (grabComponent != null && grabComponent.grabbingEntity != null) {
					float partialTick = ClientUtil.partialTick(grabComponent.grabbingEntity, 
							event.getPartialTick(), mc.level.tickRateManager());
					grabComponent.applyRotationDiff(partialTick);
				}
			}
		}

		@SubscribeEvent
		public static void onLivingRender(RenderLivingEvent.Pre<?, ?> event) {
			LivingEntity entity = event.getEntity();
			LivingComponentGrab grabComponent = entity.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
			if (grabComponent != null && grabComponent.grabbingEntity != null) {
				float partialTick = event.getPartialTick();
				grabComponent.applyRotationDiff(partialTick);
			}
		}

		@SubscribeEvent
		public static void onRenderFrame(RenderLevelStageEvent event) {
			RenderLevelStageEvent.Stage stage = event.getStage();
			if (stage == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
				Minecraft mc = Minecraft.getInstance();
				ClientLevel level = mc.level;
				var attachmentType = ModDataAttachmentTypes.LIVING_GRAB.get();
				for (Entity entity : level.entitiesForRendering()) {
					LivingComponentGrab grabComponent = entity.getData(attachmentType);
					if (grabComponent != null && grabComponent.grabbingEntity != null) {
						float partialTick = ClientUtil.partialTick(grabComponent.grabbingEntity, 
								event.getPartialTick(), level.tickRateManager());
						grabComponent.setGrabbedPos(partialTick);
					}
				}
			}
		}
	}
	
}
