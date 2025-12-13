package com.github.standobyte.jojo.client.entityrender.stand;

import org.apache.commons.lang3.ArrayUtils;

public enum HumanoidPart {
	HEAD,
	BODY,
	LEFT_ARM,
	RIGHT_ARM,
	LEGS;
	
	public static final HumanoidPart[] ALL = HumanoidPart.values();
	public static final HumanoidPart[] ARMS_ONLY = new HumanoidPart[] { LEFT_ARM, RIGHT_ARM };
	public static final HumanoidPart[] LEFT_ARM_ONLY = new HumanoidPart[] { LEFT_ARM };
	public static final HumanoidPart[] RIGHT_ARM_ONLY = new HumanoidPart[] { RIGHT_ARM };
	
	public static void setPartsVisible(StandEntityModel<?, ?> model, HumanoidPart... parts) {
		StandEntityModel.setVisible(model.head, true);
		StandEntityModel.setVisible(model.torso_no_arms, true);
		StandEntityModel.setVisible(model.torso_lower, true);
		StandEntityModel.setVisible(model.left_arm, true);
		StandEntityModel.setVisible(model.right_arm, true);
		StandEntityModel.setVisible(model.left_leg, true);
		StandEntityModel.setVisible(model.right_leg, true);
		
		byte mask = 0;
		for (HumanoidPart part : parts) {
			mask |= (1 << part.ordinal());
		}
		for (HumanoidPart part : HumanoidPart.values()) {
			if ((mask & (1 << part.ordinal())) == 0) {
				switch (part) {
					case HEAD -> {
						StandEntityModel.setVisible(model.head, false);
					}
					case BODY -> {
						StandEntityModel.setVisible(model.torso_no_arms, false);
						StandEntityModel.setVisible(model.torso_lower, false);
					}
					case LEFT_ARM -> {
						StandEntityModel.setVisible(model.left_arm, false);
					}
					case RIGHT_ARM -> {
						StandEntityModel.setVisible(model.right_arm, false);
					}
					case LEGS -> {
						StandEntityModel.setVisible(model.left_leg, false);
						StandEntityModel.setVisible(model.right_leg, false);
					}
				}
			}
		}
	}
	
	private static HumanoidPart[] temp = new HumanoidPart[HumanoidPart.values().length];
	public static HumanoidPart[] reduce(HumanoidPart[] parts, HumanoidPart[] limit) {
		int i = 0;
		for (HumanoidPart part : parts) {
			if (ArrayUtils.contains(limit, part)) {
				temp[i++] = part;
			}
		}
		HumanoidPart[] arr = new HumanoidPart[i];
		for (int j = 0; j < i; j++) {
			arr[j] = temp[j];
		}
		return arr;
	}
	
}
