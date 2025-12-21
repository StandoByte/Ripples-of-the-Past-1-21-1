package com.github.standobyte.jojo.mechanics.clothes.client.layer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.HumanoidPlayerModel;
import com.github.standobyte.jojo.client.entityrender.NamedModelParts;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

// TODO (clothes) fix the model z-fighting
// TODO (clothes) fix the way it looks with armor equipped
// FIXME model bend on left leg
// WHY IS IT ONLY LEFT LEG SPECIFICALLY FOR BOTH????
public class HumanoidClothesModel extends HumanoidPlayerModel<LivingEntity>/*<HumanoidRenderState>*/ {
	private Map<ClothesSlotType, List<ModelPart>> byClothesPart = new EnumMap<>(ClothesSlotType.class);

	@ApiStatus.Internal
	public HumanoidClothesModel(ModelPart root) {
		super(root);
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
		
		setSlim(slim);
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