package com.github.standobyte.jojo.client.shader.standaura;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;

/* another way could be to apply a morphological dilation, 
 * but then the aura would look too large from the distance
 */
public class AuraUtil {
	@Nullable public static Float inflateEachCube = null;
	
	public static PoseStack.Pose inflateCube(PoseStack.Pose pose, ModelPart.Cube cube) {
		if (AuraUtil.inflateEachCube != null) {
			PoseStack.Pose inflated = pose.copy();
			float inflate = AuraUtil.inflateEachCube;
			float sizeX = cube.maxX - cube.minX;
			float sizeY = cube.maxY - cube.minY;
			float sizeZ = cube.maxZ - cube.minZ;
			float offsetX = -(cube.minX + cube.maxX) / 32;
			float offsetY = -(cube.minY + cube.maxY) / 32;
			float offsetZ = -(cube.minZ + cube.maxZ) / 32;
			Matrix4f poseMatrix = inflated.pose();
			poseMatrix.translate(-offsetX, -offsetY, -offsetZ);
			poseMatrix.scale(1 + inflate * 2 / sizeX, 1 + inflate * 2 / sizeY, 1 + inflate * 2 / sizeZ);
			poseMatrix.translate(offsetX, offsetY, offsetZ);
			return inflated;
		}
		return pose;
	}
	
	public static void inflateCube(PoseStack poseStack, ModelPart.Cube cube) {
		if (AuraUtil.inflateEachCube != null) {
			float inflate = AuraUtil.inflateEachCube;
			float sizeX = cube.maxX - cube.minX;
			float sizeY = cube.maxY - cube.minY;
			float sizeZ = cube.maxZ - cube.minZ;
			float offsetX = -(cube.minX + cube.maxX) / 32;
			float offsetY = -(cube.minY + cube.maxY) / 32;
			float offsetZ = -(cube.minZ + cube.maxZ) / 32;
			poseStack.translate(-offsetX, -offsetY, -offsetZ);
			poseStack.scale(1 + inflate * 2 / sizeX, 1 + inflate * 2 / sizeY, 1 + inflate * 2 / sizeZ);
			poseStack.translate(offsetX, offsetY, offsetZ);
		}
	}


	public static int getStandAuraColor(LivingEntity entity) {
		if (entity instanceof Player || entity instanceof StandEntity) {
			return 0xFFFFD000;
		}
		if (entity instanceof Skeleton) {
			return 0xFFCB00FF;
		}
		if (entity instanceof Creeper) {
			return 0xFF00CE00;
		}
		if (entity instanceof Sheep) {
			return 0xFFFF00F6;
		}
		if (entity instanceof Husk) {
			return 0xFF80B000;
		}
		if (entity instanceof Spider) {
			return 0xFFFF0000;
		}
		if (entity instanceof EnderMan) {
			return 0xFFFF00FF;
		}
		return -1;
	}
}
