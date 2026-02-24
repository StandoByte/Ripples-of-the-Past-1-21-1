package com.github.standobyte.jojo.client.entityanim.barrage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.TimelineKeys;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.entityrender.stand.HumanoidPart;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityModel;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class BarrageSwings {
	@ApiStatus.Internal public List<BarrageSwing> barrageSwings = new LinkedList<>();
	@ApiStatus.Internal public float loopLast = -1;

	@ApiStatus.Internal public boolean isBarragingAnim = false;
	@ApiStatus.Internal public String barrageType;
	@ApiStatus.Internal public AddBarrageSwing addSwingFunction;
	
	public float barragePrecision = 12;
	public float barrageSwingsPerSecond = 80;
	
	protected float lastTicks = -1;


	public void frameStandBarrage(Minecraft mc, RotpAnimDefinition barrageAnim, 
			float curAnimTimeSecs, LivingEntity entity, float ticks) {
		if (this.lastTicks == ticks) return;
		this.lastTicks = ticks;
		
		if (entity instanceof StandEntity stand) {
			barrageSwingsPerSecond = StandStatFormulas.getBarrageHitsPerSecond(stand.getAttackSpeed());
			barragePrecision = (float) stand.getPrecision();
		}
		frameUpdateSwings(mc);
		String barrageTypeName = barrageAnim.instructionTimelines.getStringTimelineVal(TimelineKeys.BARRAGE, curAnimTimeSecs);
		frameUpdateBarrageType(barrageTypeName);
		if (isBarragingAnim) {
			frameSetValuesAndAddNewSwings(barrageAnim, entity, curAnimTimeSecs);
		}
	}

	public void frameUpdateSwings(Minecraft mc) {
		if (!mc.isPaused() && !barrageSwings.isEmpty()) {
			float timeDelta = mc.getTimer().getGameTimeDeltaTicks();
			Iterator<BarrageSwing> iter = barrageSwings.iterator();
			while (iter.hasNext()) {
				BarrageSwing swing = iter.next();
				swing.addDelta(timeDelta);
				if (swing.removeSwing()) {
					iter.remove();
				}
			}
		}
	}

	public void frameUpdateBarrageType(String barrageTypeName) {
		this.isBarragingAnim = false;
		this.barrageType = barrageTypeName;
		this.addSwingFunction = null;

		if (barrageType != null) {
			AddBarrageSwing addSwingFunction = BARRAGE_SWING_TYPES.get(barrageType);
			if (addSwingFunction != null) {
				this.isBarragingAnim = true;
				this.addSwingFunction = addSwingFunction;
			}
		}
	}

	public void frameSetValuesAndAddNewSwings(RotpAnimDefinition barrageAnim, LivingEntity entity, float curAnimTimeSecs) {
		addSwingFunction.addSwings(this, barrageAnim, entity, curAnimTimeSecs);
	}

	
	public boolean hasSmthToRender() {
		return !barrageSwings.isEmpty();
	}
	
	public void renderLayerBarrage(EntityModel<?> model, 
			PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color) {
		for (BarrageSwing swing : barrageSwings) {
			swing.poseAndRender(model, poseStack, buffer, 
					packedLight, packedOverlay, color);
		}
		restoreVisibility(model);
	}





	public static final Map<String, AddBarrageSwing> BARRAGE_SWING_TYPES = Util.make(new HashMap<>(), map -> {
		map.put("TWO_HANDED", TwoHandedBarrageLoopSwing::addSwings);
		map.put("GRAB_RIGHT", (BarrageSwings swings, RotpAnimDefinition barrageAnim, 
				LivingEntity entity, float animTimeSecs)
				-> GrabBarrageLoopSwing.addSwings(swings, barrageAnim, entity, animTimeSecs, HumanoidArm.RIGHT));
	});

	@FunctionalInterface
	public static interface AddBarrageSwing {
		void addSwings(BarrageSwings swings, RotpAnimDefinition barrageAnim, 
				LivingEntity entity, float curAnimTimeSecs);
	}



	public abstract static class BarrageSwing {
		protected static final Random RANDOM = new Random();
		protected static final LivingEntityRenderState sharedRenderState = new LivingEntityRenderState();
		protected static final EntityActionRenderState sharedActionRenderState = new EntityActionRenderState();
		
		protected RotpAnimDefinition barrageAnim;
		protected float ticks;
		protected float ticksMax;
		
		protected float tickSpeed = 1;

		public BarrageSwing(RotpAnimDefinition barrageAnim, float startingAnim, float animMax) {
			this.barrageAnim = barrageAnim;
			this.ticks = startingAnim;
			this.ticksMax = animMax;
		}

		public void addDelta(float delta) {
			ticks += delta * tickSpeed;
		}

		public boolean removeSwing() {
			return ticks >= ticksMax;
		}

		public abstract void poseAndRender(EntityModel<?> model, 
				PoseStack poseStack, VertexConsumer buffer, 
				int packedLight, int packedOverlay, int color);
	}


	public static ModelPart getNoXRotArm(EntityModel<?> model, HumanoidArm side) {
		return switch (model) {
			case StandEntityModel<?, ?> standModel -> {
				yield switch (side) {
					case LEFT -> standModel.left_arm;
					case RIGHT -> standModel.right_arm;
				};
			}
			case HumanoidModel<?> humanoidModel -> {
				yield switch (side) {
					case LEFT -> humanoidModel.leftArm;
					case RIGHT -> humanoidModel.rightArm;
				};
			}
			default -> null;
		};
	}
	
	public static void setOnlyOneArmVisible(EntityModel<?> model, HumanoidArm side) {
		switch (model) {
			case StandEntityModel<?, ?> standModel -> {
				HumanoidPart.setPartsVisible(standModel, switch (side) {
					case LEFT -> HumanoidPart.LEFT_ARM_ONLY;
					case RIGHT -> HumanoidPart.RIGHT_ARM_ONLY;
				});
			}
			case HumanoidModel<?> humanoidModel -> {
				humanoidModel.setAllVisible(false);
				(switch (side) {
					case LEFT -> humanoidModel.leftArm;
					case RIGHT -> humanoidModel.rightArm;
				}).visible = true;
			}
			default -> {}
		}
	}
	
	public static void restoreVisibility(EntityModel<?> model) {
		switch (model) {
			case StandEntityModel<?, ?> standModel -> {
				standModel.setAllVisible(true);
			}
			case HumanoidModel<?> humanoidModel -> {
				humanoidModel.setAllVisible(true);
			}
			default -> {}
		}
	}
	
	
	@Nullable
	public static BarrageSwings getBarrageSwings(LivingEntityRenderState renderState) {
		EntityActionRenderState action = EntityActionRenderState.getFrom(renderState);
		return action != null ? action.barrageSwings : null;
	}
	
	@Nullable public static BarrageSwings currentlyRendering = null;
	
	public static void setupToRender(BarrageSwings barrage) {
		BarrageSwings.currentlyRendering = barrage;
	}

}

