package com.github.standobyte.jojo.client.entityrender;

import java.util.function.Function;

import com.github.standobyte.jojo.util.objects_java.LazyNullable;
import com.github.standobyte.v1_21_4_stuff.missingmethods.PartPoseScale;
import com.github.standobyte.v1_21_4_stuff.missingmethods._PartPose;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class BabyModelVariant<M extends Model> {
	protected final LayerDefinition modelDefinition;
	protected final Function<LayerDefinition, M> createModel;
	protected LazyNullable<M> adultModel;
	protected LazyNullable<M> babyModel;
	
	
	public BabyModelVariant(LayerDefinition modelDefinition, Function<LayerDefinition, M> createModel) {
		this.modelDefinition = modelDefinition;
		this.createModel = createModel;
		this.adultModel = LazyNullable.of(() -> createModel.apply(modelDefinition));
		this.babyModel = LazyNullable.of(() -> {
			MeshDefinition babyMeshDefinition = ModelUtil.transform(modelDefinition.mesh, (String name, PartDefinition part) -> {
				return new PartDefinition(part.cubes, sethanifyHumanoidPartPose(name, part.partPose));
			});
			return createModel.apply(LayerDefinition.create(babyMeshDefinition, 
					modelDefinition.material.xTexSize, 
					modelDefinition.material.yTexSize));
		});
	}

	public M get(boolean baby) {
		babyModel.invalidate();
		return baby ? babyModel.get() : adultModel.get();
	}



	public float headScale = 0.75f;
	public float bodyScale = 0.5f;
	public PartPose sethanifyHumanoidPartPose(String partName, PartPose pose) {
		switch (partName) {
			case "body" -> {
				float x = pose.x;
				float y = pose.y + 18 * (1 - bodyScale);
				float z = pose.z;
				pose = PartPose.offset(x, y, z);
			}
			case "head" -> {
				PartPoseScale scale = _PartPose.getScale(pose);
				float x = pose.x;
				float y = pose.y * bodyScale;
				float z = pose.z;
				float xScale = scale.xScale * headScale;
				float yScale = scale.yScale * headScale;
				float zScale = scale.zScale * headScale;
				pose = PartPose.offset(x, y, z);
				_PartPose.setScale(pose, xScale, yScale, zScale);
			} 
			case "torso_no_arms" -> {
				PartPoseScale scale = _PartPose.getScale(pose);
				float x = pose.x;
				float y = pose.y;
				float z = pose.z;
				float xScale = scale.xScale * bodyScale;
				float yScale = scale.yScale * bodyScale;
				float zScale = scale.zScale * bodyScale;
				pose = PartPose.offset(x, y, z);
				_PartPose.setScale(pose, xScale, yScale, zScale);
			} 
			case "torso_lower" -> {
				PartPoseScale scale = _PartPose.getScale(pose);
				float x = pose.x;
				float y = pose.y;
				float z = pose.z;
				float xScale = scale.xScale * bodyScale;
				float yScale = scale.yScale * bodyScale;
				float zScale = scale.zScale * bodyScale;
				pose = PartPose.offset(x, y, z);
				_PartPose.setScale(pose, xScale, yScale, zScale);
			} 
			case "left_arm_xrot" -> {
				PartPoseScale scale = _PartPose.getScale(pose);
				float x = pose.x * bodyScale;
				float y = pose.y * bodyScale;
				float z = pose.z * bodyScale;
				float xScale = scale.xScale * bodyScale;
				float yScale = scale.yScale * bodyScale;
				float zScale = scale.zScale * bodyScale;
				pose = PartPose.offset(x, y, z);
				_PartPose.setScale(pose, xScale, yScale, zScale);
			} 
			case "right_arm_xrot" -> {
				PartPoseScale scale = _PartPose.getScale(pose);
				float x = pose.x * bodyScale;
				float y = pose.y * bodyScale;
				float z = pose.z * bodyScale;
				float xScale = scale.xScale * bodyScale;
				float yScale = scale.yScale * bodyScale;
				float zScale = scale.zScale * bodyScale;
				pose = PartPose.offset(x, y, z);
				_PartPose.setScale(pose, xScale, yScale, zScale);
			} 
			case "left_leg_xrot" -> {
				PartPoseScale scale = _PartPose.getScale(pose);
				float x = pose.x * bodyScale;
				float y = pose.y * bodyScale - 6 * (1 - bodyScale);
				float z = pose.z * bodyScale;
				float xScale = scale.xScale * bodyScale;
				float yScale = scale.yScale * bodyScale;
				float zScale = scale.zScale * bodyScale;
				pose = PartPose.offset(x, y, z);
				_PartPose.setScale(pose, xScale, yScale, zScale);
			} 
			case "right_leg_xrot" -> {
				PartPoseScale scale = _PartPose.getScale(pose);
				float x = pose.x * bodyScale;
				float y = pose.y * bodyScale - 6 * (1 - bodyScale);
				float z = pose.z * bodyScale;
				float xScale = scale.xScale * bodyScale;
				float yScale = scale.yScale * bodyScale;
				float zScale = scale.zScale * bodyScale;
				pose = PartPose.offset(x, y, z);
				_PartPose.setScale(pose, xScale, yScale, zScale);
			}
			default -> {}
		};
		return pose;
	}

}
