package com.github.standobyte.jojo.client.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ModelUtil {

	public static Map<String, ModelPart> mapNamedModelParts(ModelPart root) {
		return mapNamedModelParts(root, Function.identity());
	}

	public static <T> Map<String, T> mapNamedModelParts(ModelPart root, Function<ModelPart, T> wrap) {
		Map<String, T> map = new HashMap<>();
		putChildrenRecursive(root, "root", map, wrap);
		return map;
	}
	
	private static <T> void putChildrenRecursive(ModelPart modelPart, String partName, Map<String, T> dest, Function<ModelPart, T> wrap) {
		dest.put(partName, wrap.apply(modelPart));
		for (var childEntry : modelPart.children.entrySet()) {
			ModelPart childModelPart = childEntry.getValue();
			String childName = childEntry.getKey();
			putChildrenRecursive(childModelPart, childName, dest, wrap);
		}
	}
	
	
	@Nullable
	public static Map<String, ModelPart[]> modelPartInheritanceChains(String rootName, ModelPart root, String... endPartNames) {
		Stack<ModelPart> stack = new Stack<>();
		Set<String> toFind = new HashSet<>(Arrays.asList(endPartNames));
		Map<String, ModelPart[]> destination = new HashMap<>();
		recursionMyBeloved(rootName, root, stack, toFind, destination);
		return destination;
	}
	
	private static void recursionMyBeloved(String partName, ModelPart part, Stack<ModelPart> stack, Collection<String> toFind, Map<String, ModelPart[]> destination) {
		if (toFind.isEmpty()) return;
		
		stack.add(part);
		if (toFind.remove(partName)) {
			destination.put(partName, stack.toArray(ModelPart[]::new));
		}
		if (!toFind.isEmpty()) {
			for (var child : part.children.entrySet()) {
				recursionMyBeloved(child.getKey(), child.getValue(), stack, toFind, destination);
			}
		}
		stack.pop();
	}
	
	
	// common code friendly version
	// FIXME !!! make sure the animation is from the entity (if there are 2+ star platinums in render distance, this may not work correctly)
	@Nullable
	public static Vec3 getModelPartPos(Entity entity, String modelPartName, Vec3 finalOffset) {
		EntityModel<?> model = getEntityModel(entity);
		return model != null ? getModelPartPos(model, modelPartName, finalOffset) : null;
	}
	
	public static final Map<Model, Map<String, ModelPart[]>> __cache = new IdentityHashMap<>();
	@Nullable
	// FIXME ModelUtil.getModelPartPos (this shit is still incorrect)
	public static Vec3 getModelPartPos(Model model, String modelPartName, Vec3 finalOffset) {
		ModelPart modelRoot = ((Model_1_21_2plus) model).jojo_ripples$root();
		if (modelRoot == null) return null;
		String rootName = "root";
		
		Map<String, ModelPart[]> modelParts = __cache.computeIfAbsent(model, _model -> {
			return modelPartInheritanceChains(rootName, modelRoot, modelPartName);
		});

		ModelPart[] inheritanceChain;
		if (!modelParts.containsKey(modelPartName)) {
			recursionMyBeloved(rootName, modelRoot, new Stack<>(), 
					Util.make(new ArrayList<>(), list -> list.add(modelPartName)), modelParts);
			if (!modelParts.containsKey(modelPartName)) {
				modelParts.put(modelPartName, null);
			}
		}
		
		inheritanceChain = modelParts.get(modelPartName);
		if (inheritanceChain == null || inheritanceChain.length == 0) {
			return null;
		}
		
		Vec3 fullOffset = new Vec3(0, 0, 0);
		
		float xRotAccum = 0;
		float yRotAccum = 0;
		float zRotAccum = 0;
		double xScaleAccum = 1;
		double yScaleAccum = 1;
		double zScaleAccum = 1;
		
		for (ModelPart modelPart : inheritanceChain) {
			Vec3 offset = new Vec3(modelPart.x / 16, modelPart.y / 16, modelPart.z / 16);

			if (xRotAccum != 0) offset = offset.xRot(-xRotAccum);
			if (zRotAccum != 0) offset = offset.zRot(-zRotAccum);
			if (yRotAccum != 0) offset = offset.yRot(yRotAccum);
			if (xScaleAccum != 1.0F || yScaleAccum != 1.0F || zScaleAccum != 1.0F)
				offset = offset.multiply(xScaleAccum, yScaleAccum, zScaleAccum);
			fullOffset = fullOffset.add(offset);
			
			xRotAccum += modelPart.xRot;
			yRotAccum += modelPart.yRot;
			zRotAccum += modelPart.zRot;
			xScaleAccum *= modelPart.xScale;
			yScaleAccum *= modelPart.yScale;
			zScaleAccum *= modelPart.zScale;
		}

		if (xRotAccum != 0) finalOffset = finalOffset.xRot(-xRotAccum);
		if (zRotAccum != 0) finalOffset = finalOffset.zRot(-zRotAccum);
		if (yRotAccum != 0) finalOffset = finalOffset.yRot(yRotAccum);
		if (xScaleAccum != 1.0F || yScaleAccum != 1.0F || zScaleAccum != 1.0F)
			finalOffset = finalOffset.multiply(xScaleAccum, yScaleAccum, zScaleAccum);
		fullOffset = fullOffset.add(finalOffset);
		
		
		fullOffset = fullOffset.multiply(1, -1, -1);
		return fullOffset;
	}
	
	
	@SuppressWarnings("unchecked")
	public static EntityModel<?> getEntityModel(Entity entity) {
		EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
		if (renderer instanceof LivingEntityRenderer livingRenderer) {
			if (renderer instanceof StandEntityRenderer standRenderer) {
				return standRenderer.getEntityModel((StandEntity) entity);
			}
			return livingRenderer.getModel();
		}
		
		return null;
	}
	
	public static boolean isSlimModel(LivingEntity entity) {
		return entity instanceof MannequinEntity mannequin && mannequin.isSlim()
				|| entity instanceof AbstractClientPlayer player && player.getSkin().model() == PlayerSkin.Model.SLIM;
	}
	
}