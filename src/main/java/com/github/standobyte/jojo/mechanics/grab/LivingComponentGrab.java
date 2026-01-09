package com.github.standobyte.jojo.mechanics.grab;

import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.utils.ModelUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.UtilFunctions;
import com.github.standobyte.jojo.util.entitycomponent.TickingEntityData;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
	public float yRotDiffWhenGrabbed;
	public float yHeadRotDiffWhenGrabbed;
	public float yBodyRotDiffWhenGrabbed;
	
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
		float yRot = thisEntity.getYRot();
		this.yRotDiffWhenGrabbed = yRot - grabbing.yBodyRot;
		this.yHeadRotDiffWhenGrabbed = thisEntity.getYHeadRot() - yRot;
		this.yBodyRotDiffWhenGrabbed = thisEntity.yBodyRot - yRot;
	}
	
	protected static Vec3 armChokeOffset = new Vec3(0, -0.125, 0);
	@ApiStatus.Internal
	public void setGrabbedPos() {
		if (grabbingEntity != null) {
			HumanoidArm grabbingArm = HumanoidArm.LEFT;
			double neckY = -thisEntity.getBbHeight() * 0.8 - 0.125;
			Vec3 grabOffset = new Vec3(0, neckY, 0);
			
			boolean useModelArmPos = grabbingEntity.level().isClientSide();
			float yRot = grabbingEntity.yBodyRot;
			yRot = -yRot * MathUtil.DEG_TO_RAD;
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
			
			Vec3 grabbedPos = grabbingEntity.position().add(grabOffset);
			thisEntity.setPos(grabbedPos.x, grabbedPos.y, grabbedPos.z);
			thisEntity.setDeltaMovement(Vec3.ZERO);
			for (Entity passenger : thisEntity.getPassengers()) {
				thisEntity.positionRider(passenger);
			}
			
			if (!thisEntity.level().isClientSide()) {
				applyRotationDiff();
			}
		}
	}

	@ApiStatus.Internal
	public void applyRotationDiff() {
		if (grabbingEntity != null) {
			thisEntity.setXRot(this.xRotWhenGrabbed);
			float yRot = grabbingEntity.getYRot() + this.yRotDiffWhenGrabbed;
			thisEntity.setYRot(yRot);
			thisEntity.setYHeadRot(yRot + this.yHeadRotDiffWhenGrabbed);
			thisEntity.setYBodyRot(yRot + this.yBodyRotDiffWhenGrabbed);
		}
	}
	
	
	@SubscribeEvent
	public static void onLevelTickPost(LevelTickEvent.Post event) {
		Level level = event.getLevel();
		var attachmentType = ModDataAttachmentTypes.LIVING_GRAB.get();
		for (Entity entity : UtilFunctions.getEntities(level)) {
			LivingComponentGrab grabComponent = entity.getData(attachmentType);
			if (grabComponent != null) {
				grabComponent.setGrabbedPos();
			}
		}
	}
	
}
