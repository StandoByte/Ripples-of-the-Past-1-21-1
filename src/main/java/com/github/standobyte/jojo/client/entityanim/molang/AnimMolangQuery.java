package com.github.standobyte.jojo.client.entityanim.molang;

import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
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
	
	public void fillContext(LivingEntityRenderState renderState) {
		head_x_rotation = ObjectProperty.property(Value.of(renderState.xRot), false);
		head_y_rotation = ObjectProperty.property(Value.of(renderState.yRot), false);
        if (renderState instanceof StandEntityRenderState standState){
            extendablePartLength = ObjectProperty.property(Value.of(standState.extendablePartLength), false);
        }
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
	
}

