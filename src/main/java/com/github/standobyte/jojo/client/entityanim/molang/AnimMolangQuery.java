package com.github.standobyte.jojo.client.entityanim.molang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.standobyte.jojo.powersystem.entityaction.ActionOBB;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

public class AnimMolangQuery implements ObjectValue {
	public static final String NAMESPACE = "query";
	public static AnimMolangQuery instance = new AnimMolangQuery();
	
	protected AnimMolangQuery() {
		reset();
	}
	
	ObjectProperty head_x_rotation;
	ObjectProperty head_y_rotation;
    ObjectProperty extendablePartLength;
	
	public void fillContext(AnimMolangVariables variables) {
		head_x_rotation = ObjectProperty.property(Value.of(variables.xRot), false);
		head_y_rotation = ObjectProperty.property(Value.of(variables.yRot), false);
		extendablePartLength = ObjectProperty.property(Value.of(variables.extendablePartLength), false);
	}
	
	public void reset() {
		head_x_rotation = ObjectProperty.property(Value.of(0), false);
		head_y_rotation = ObjectProperty.property(Value.of(0), false);
		extendablePartLength = ObjectProperty.property(Value.of(0), false);
	}
	
	@Override
	public @Nullable ObjectProperty getProperty(@NotNull String name) {
		switch (name) {
			case "head_x_rotation": return head_x_rotation;
			case "head_y_rotation": return head_y_rotation;
            case "extendablePartLength": return extendablePartLength;
		}
		return null;
	}
	
	
	public static class AnimMolangVariables {
		public float xRot;
		public float yRot;
		public float extendablePartLength;
		
		public static AnimMolangVariables set(
				float xRot, 
				float yRot, 
				float extendablePartLength
				) {
			reusedState.xRot = xRot;
			reusedState.yRot = yRot;
			reusedState.extendablePartLength = extendablePartLength;
			return reusedState;
		}
		
		public static AnimMolangVariables reusedState = new AnimMolangVariables();
		protected AnimMolangVariables() {}
		
		public static AnimMolangVariables extract(LivingEntity entity, float partialTick) {
			AnimMolangVariables state = AnimMolangVariables.reusedState;
			
			float f = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
			float bodyRot = LivingEntityRenderState.solveBodyRot(entity, f, partialTick);
			state.yRot = Mth.wrapDegrees(f - bodyRot);
			state.xRot = LivingEntityRenderState.getXRot(entity, partialTick);

			state.extendablePartLength = 0;
			if (LivingComponentAction.getCurEntityAction(entity) instanceof ActionOBB obbToRender && obbToRender.extendableOBB() != null){
				state.extendablePartLength = obbToRender.extendableOBB().getAnimLength(partialTick);
	        }
	        return state;
		}
		
	}
	
}

