package com.github.standobyte.jojo.mixin.standskin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojo.client.standskin.text.StandSkinTranslatableContents;

import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;

@Mixin(ComponentSerialization.class)
public class ComponentSerializationMixin {

	@ModifyVariable(method = "createCodec", at = @At("STORE"), ordinal = 0)
	private static ComponentContents.Type<?>[] addComponentType(ComponentContents.Type<?>[] type) {
		return addComponentTypes(type, StandSkinTranslatableContents.TYPE);
	}
	
	@Unique
	private static ComponentContents.Type<?>[] addComponentTypes(ComponentContents.Type<?>[] original, ComponentContents.Type<?>... modded) {
		ComponentContents.Type<?>[] newArr = new ComponentContents.Type[original.length + modded.length];
		for (int i = 0; i < original.length; ++i) {
			newArr[i] = original[i];
		}
		for (int i = 0; i < modded.length; i++) {
			newArr[original.length + i] = modded[i];
		}
		return newArr;
	}

}
