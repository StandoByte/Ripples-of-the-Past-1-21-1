package com.github.standobyte.jojo.mixin.client.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.client.entityrender.ModelUtil;
import com.github.standobyte.jojo.client.entityrender.NamedModelParts;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(Model.class)
public abstract class ModelAnimOptimization implements Model_1_21_2plus, NamedModelParts {
	private Map<String, Optional<ModelPart>> jojo_ripples$allModelParts;

//	@Inject(method = "<init>("
//			+ "Lnet/minecraft/client/model/geom/ModelPart;"
//			+ "Ljava/util/function/Function;)V", at = @At("RETURN"))
//	public void jojo_ripples$onInit(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
//		jojo_ripples$initModelPartsCache(root);
//	}
	
	public void jojo_ripples$initModelPartsCache(ModelPart root) {
		jojo_ripples$allModelParts = ModelUtil.mapNamedModelParts(root, Optional::of);
	}
	
//	@Inject(method = "getAnyDescendantWithName", at = @At("HEAD"), cancellable = true)
	@Override
	public /*void*/Optional<ModelPart> jojo_ripples$getAnyDescendantWithName(String name/*, CallbackInfoReturnable<Optional<ModelPart>> ci*/) {
		if (jojo_ripples$allModelParts != null) {
			Optional<ModelPart> part = jojo_ripples$allModelParts.get(name);
			return part != null ? part : Optional.empty();
		}
		return Optional.empty();
	}
	
	@Override
	public Iterator<Map.Entry<String, Optional<ModelPart>>> jojo_ripples$getAllNamedParts() {
		return jojo_ripples$allModelParts != null ? jojo_ripples$allModelParts.entrySet().iterator() : null;
	}

}
