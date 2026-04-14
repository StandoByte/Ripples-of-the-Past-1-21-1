package com.github.standobyte.jojo.client.entityrender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityanim.pose.AnimatedEntity;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.missingmethods._PartDefinition;
import com.github.standobyte.v1_21_4_stuff.missingmethods._PartPose;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MaterialDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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

	// LayerDefinition stuff
	
	public static LayerDefinition merge(LayerDefinition src, LayerDefinition dest) {
		MaterialDefinition destTex = dest.material;
		MaterialDefinition srcTex = src.material;
		if (destTex.xTexSize != srcTex.xTexSize || destTex.yTexSize != srcTex.yTexSize) {
			JojoMod.getLogger().warn("Trying to merge two model definitions with different texture sizes ({}x{} and {}x{}). You probably do not want that.", 
					destTex.xTexSize, destTex.yTexSize, srcTex.xTexSize, srcTex.yTexSize);
		}
		mergeModelParts(src.mesh.getRoot(), dest.mesh.getRoot());
		return dest;
	}
	
	public static void mergeModelParts(PartDefinition src, PartDefinition dest) {
		dest.cubes.addAll(src.cubes);
		for (var srcChildEntry : src.children.entrySet()) {
			String modelPartName = srcChildEntry.getKey();
			PartDefinition destChild = dest.getChild(modelPartName);
			PartDefinition srcChild = srcChildEntry.getValue();
			if (destChild != null) {
				mergeModelParts(srcChild, destChild);
			}
			else {
				_PartDefinition.addOrReplaceChild(dest, modelPartName, srcChild);
			}
		}
	}
	
	public static LayerDefinition copy(LayerDefinition src) {
		MeshDefinition newMesh = new MeshDefinition();
		PartDefinition oldRoot = src.mesh.getRoot();
		PartDefinition newRoot = newMesh.getRoot();
		copyChildren(oldRoot, newRoot);
		LayerDefinition newModel = LayerDefinition.create(newMesh, 
				src.material.xTexSize,
				src.material.yTexSize);
		return newModel;
	}
	
	static void copyChildren(PartDefinition src, PartDefinition dest) {
		for (var childEntry : src.children.entrySet()) {
			String childName = childEntry.getKey();
			PartDefinition oldChild = childEntry.getValue();
			PartDefinition newChild = new PartDefinition(new ArrayList<>(oldChild.cubes), oldChild.partPose);
			dest.children.put(childName, newChild);
			copyChildren(oldChild, newChild);
		}
	}
	
	public static LayerDefinition copyMaterial(LayerDefinition src, MeshDefinition newMesh) {
		return LayerDefinition.create(newMesh, src.material.xTexSize, src.material.yTexSize);
	}
	
	public static void forAllDescendants(LayerDefinition modelDefinition, BiConsumer<String, PartDefinition> action) {
		PartDefinition root = modelDefinition.mesh.getRoot();
		forPartAndChildren(root, "root", action);
	}
	
	static void forPartAndChildren(PartDefinition modelPart, String modelPartName, BiConsumer<String, PartDefinition> action) {
		action.accept(modelPartName, modelPart);
		for (var childEntry : modelPart.children.entrySet()) {
			forPartAndChildren(childEntry.getValue(), childEntry.getKey(), action);
		}
	}
	
	
	// common code friendly versions
	@Nullable
	public static Vec3 getModelPartPos(Entity entity, 
			String modelPartName, Vec3 modelPartOffset) {
		AnimFramePose modelPose = ((AnimatedEntity) entity).jojo_ripples$getModelPose(AnimatedEntity.PoseType.FINAL);
		return modelPose != null ? getModelPartPos(
				entity, modelPose, 
				modelPartName, modelPartOffset) : null;
	}

	@Nullable
	public static Vec3 getModelPartPos(Entity entity, AnimFramePose clientSavedPose, 
			String modelPartName, Vec3 modelPartOffset) {
		EntityModel<?> model = getEntityModel(entity);
		return model != null ? getModelPartPos(
				model, clientSavedPose, 
				modelPartName, modelPartOffset) : null;
	}
	
	// NOT-common code friendly method
	@Nullable
	// FIXME !!! grab punches rotate the stand weirdly
	// FIXME !!! IT'S NOT SAVING THE HEAD ROTATION WHAT THE FUCKKKKKKK
	// FIXME !!! PoseStandsOutsideFrustum isn't working correctly with grab
	// FIXME consider the entity scaling done in the renderer
	static Vector3f offset = new Vector3f();
	static Matrix4f pose = new Matrix4f();
	public static Vec3 getModelPartPos(Model model, AnimFramePose savedPose, 
			String finalModelPartName, Vec3 finalModelPartOffset) {
		ModelPartWithName[] inheritanceChain = ((ModelWithExtraFeatures) model).jojo_ripples$getPathToModelPart(finalModelPartName);
		if (inheritanceChain == null || inheritanceChain.length == 0) {
			return null;
		}
		
		offset.set(0);
		pose.identity();
		
		float x; float y; float z;
		float xRot; float yRot; float zRot;
		float xScale; float yScale; float zScale;
		
		for (ModelPartWithName modelPartEntry : inheritanceChain) {
			String modelPartName = modelPartEntry.partName();
			PartPose initialPose = modelPartEntry.part().getInitialPose();
			ModelPartFrame animPose = savedPose.getForModelPart(modelPartName);
			
			x = (initialPose.x + animPose.positionOffset.x) / 16;
			y = (initialPose.y + animPose.positionOffset.y) / 16;
			z = (initialPose.z + animPose.positionOffset.z) / 16;
			xRot = initialPose.xRot + animPose.rotationOffset.x;
			yRot = initialPose.yRot + animPose.rotationOffset.y;
			zRot = initialPose.zRot + animPose.rotationOffset.z;
			xScale = _PartPose.xScale(initialPose) + animPose.scaleOffset.x;
			yScale = _PartPose.yScale(initialPose) + animPose.scaleOffset.y;
			zScale = _PartPose.zScale(initialPose) + animPose.scaleOffset.z;

			pose.translate(x, y, z);
			if (xRot != 0.0F || yRot != 0.0F || zRot != 0.0F) {
				pose.rotate(new Quaternionf().rotationZYX(zRot, yRot, xRot));
			}
			if (xScale != 1.0F || yScale != 1.0F || zScale != 1.0F) {
				pose.scale(xScale, yScale, zScale);
			}
		}

		pose.transformPosition(
				(float) finalModelPartOffset.x, 
				(float) -finalModelPartOffset.y, 
				(float) -finalModelPartOffset.z, 
				offset);

		offset = offset.mul(1, -1, -1).add(0, 1.501f, 0);
		return new Vec3(offset.x, offset.y, offset.z);
	}
	
	
	public static EntityRenderer<?> getEntityRenderer(Entity entity) {
		return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
	}
	
	@SuppressWarnings("unchecked")
	public static EntityModel<?> getEntityModel(Entity entity) {
		EntityRenderer<?> renderer = getEntityRenderer(entity);
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
				|| entity instanceof AbstractClientPlayer player && player.getSkin().model() == PlayerSkin.Model.SLIM
				|| entity instanceof PowerUserMobEntity npc && npc.clientStuff.getModelType(entity) == PlayerSkin.Model.SLIM;
	}
	
}