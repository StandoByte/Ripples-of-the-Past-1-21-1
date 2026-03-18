package com.github.standobyte.jojo.client.shader.standaura;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

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


	public static class ResolveAuraVars { public int color; public float inflate; static ResolveAuraVars instance = new ResolveAuraVars(); }
	@Nullable
	public static ResolveAuraVars getStandAuraColor(LivingEntity entity) {
		ResolveAuraVars vars = ResolveAuraVars.instance;
		
		MobEffectInstance resolve = entity.getEffect(ModStatusEffects.RESOLVE);
		if (resolve != null) {
			StandPower standPower = StandPower.get(entity);
			if (standPower != null) {
				StandSkin standSkin = StandSkinsLoader.getInstance().getSkin(standPower);
				if (standSkin != null) {
					vars.color = standSkin.getColor();
					vars.inflate = Math.min(resolve.amplifier * 0.5f, 2);
					return vars;
				}
			}
			vars.color = 0xFF00FF;
			vars.inflate = 0;
			return vars;
		}
		
		return null;
	}
}
