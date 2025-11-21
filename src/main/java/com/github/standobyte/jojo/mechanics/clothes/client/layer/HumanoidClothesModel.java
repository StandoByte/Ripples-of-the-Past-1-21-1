package com.github.standobyte.jojo.mechanics.clothes.client.layer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerBendModel;
import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerLimbBend;
import com.github.standobyte.jojo.client.entityrender.NamedModelParts;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.v1_21_4_stuff.Reminder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

// TODO (clothes) fix the model z-fighting
// FIXME model bend on left leg
// WHY IS IT ONLY LEFT LEG SPECIFICALLY FOR BOTH????
public class HumanoidClothesModel extends HumanoidModel/*<HumanoidRenderState>*/ {
	private Map<ClothesSlotType, List<ModelPart>> byClothesPart = new EnumMap<>(ClothesSlotType.class);
	public final ModelPart rightArmSlim;
	public final ModelPart leftArmSlim;
	
	private static final String[] BASE_HUMANOID_PARTS = new String[] { "head", "body", "right_arm", "left_arm", "right_leg", "left_leg", "right_arm_slim", "left_arm_slim" };
	protected static ModelPart addMissing(ModelPart root) {
		for (String basePartName : BASE_HUMANOID_PARTS) {
			root.children.putIfAbsent(basePartName, new ModelPart(new ArrayList<>(), new HashMap<>()));
		}
		Reminder.thatHatIsHeadChildNow();
		root/*.getChild("head")*/.children.putIfAbsent("hat", new ModelPart(new ArrayList<>(), new HashMap<>()));
		return root;
	}

	@ApiStatus.Internal
	public HumanoidClothesModel(ModelPart root) {
		super(addMissing(root));
		this.rightArmSlim = root.getChild("right_arm_slim");
		this.leftArmSlim = root.getChild("left_arm_slim");
		IPlayerBendModel thisBends = (IPlayerBendModel) this;
		((IPlayerLimbBend) (Object) rightArmSlim).jojo_ripples$setBendBone(thisBends.jojo_ripples$animRightArmBend(), false);
		((IPlayerLimbBend) (Object) leftArmSlim).jojo_ripples$setBendBone(thisBends.jojo_ripples$animLeftArmBend(), false);
		initClothesSlots();
	}
	
	public void setClothesPartsVisibility(boolean slim, ClothesSlotType... slots) {
		setAllVisible(true);
		
		if (byClothesPart != null) {
			for (Map.Entry<ClothesSlotType, List<ModelPart>> modelPartsBySlot : byClothesPart.entrySet()) {
				boolean visible = ArrayUtils.contains(slots, modelPartsBySlot.getKey());
				List<ModelPart> modelParts = modelPartsBySlot.getValue();
				if (modelParts != null) {
					for (ModelPart modelPart : modelParts) {
						modelPart.visible = visible;
					}
				}
			}
		}
		
		if (slim) {
			leftArm.visible = false;
			rightArm.visible = false;
		}
		else {
			leftArmSlim.visible = false;
			rightArmSlim.visible = false;
		}
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(), ImmutableList.of(rightArmSlim, leftArmSlim));
	}

	@Override
	protected ModelPart getArm(HumanoidArm side) {
		return switch (side) {
			case LEFT -> !leftArm.visible && leftArmSlim.visible ? leftArmSlim : leftArm;
			case RIGHT -> !rightArm.visible && rightArmSlim.visible ? rightArmSlim : rightArm;
		};
	}


	public void poseClothes(HumanoidModel<?> originalModel) {
		this.head.copyFrom(originalModel.head);
		this.body.copyFrom(originalModel.body);
		this.rightArm.copyFrom(originalModel.rightArm);
		this.leftArm.copyFrom(originalModel.leftArm);
		this.rightArmSlim.copyFrom(originalModel.rightArm);
		this.leftArmSlim.copyFrom(originalModel.leftArm);
		this.rightLeg.copyFrom(originalModel.rightLeg);
		this.leftLeg.copyFrom(originalModel.leftLeg);
	}
	
	
	@Override
	public void setAllVisible(boolean visible) {
		super.setAllVisible(visible);
		this.rightArmSlim.visible = visible;
		this.leftArmSlim.visible = visible;
	}
	
	
	public void initClothesSlots() {
		var modelParts = ((NamedModelParts) this).jojo_ripples$getAllNamedParts();
		while (modelParts.hasNext()) {
			var modelPartEntry = modelParts.next();
			String name = modelPartEntry.getKey();
			if (name.length() >= 5 && name.startsWith("slot")) {
				ClothesSlotType clothesPart = null;
				switch (name.charAt(4)) {
				case '0':
					clothesPart = ClothesSlotType.HEAD;
					break;
				case '1':
					clothesPart = ClothesSlotType.CHEST;
					break;
				case '2':
					clothesPart = ClothesSlotType.LEGS;
					break;
				case '3':
					clothesPart = ClothesSlotType.FEET;
					break;
				}
				if (clothesPart != null) {
					ModelPart modelPart = modelPartEntry.getValue().get();
					byClothesPart.computeIfAbsent(clothesPart, __ -> new ArrayList<>()).add(modelPart);
				}
			}
		}
	}

}