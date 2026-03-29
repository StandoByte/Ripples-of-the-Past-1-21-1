package com.github.standobyte.jojo.util.functions;

import java.util.Random;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class MathUtil {
	public static final float DEG_TO_RAD = (float) (Math.PI / 180D);
	public static final float RAD_TO_DEG = (float) (180D / Math.PI);
	public static final float PI = (float) Math.PI;
	public static final float DOUBLE_PI = PI * 2F;
	
	private static final int[] TAN = new int[0x8000];
	
	public static void initTanLUT(float[] SIN) {
		for (int i = 0; i < TAN.length; i++) {
			float sin = SIN[i];
			float cos = SIN[(i + 0x4000) & 0xFFFF];
			TAN[i] = Float.floatToRawIntBits(sin / cos);
		}
	}
	
	public static float tan(float angle) {
		int index = (int)(angle * 10430.378F) & 0x7FFF /* tan(pi + a) = tan(a) */;
		return Float.intBitsToFloat(TAN[index]);
	}


	public static float wrapRadians(float angle) {
		angle %= DOUBLE_PI;
		if (angle >= PI) {
			angle -= DOUBLE_PI;
		}
		if (angle < -PI) {
			angle += DOUBLE_PI;
		}
		return angle;
	}

	
	public static Vec2 lookAngles(Vec3 lookVec) {
		double xzProjLen = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
		float xRot = Mth.wrapDegrees((float)(-(Mth.atan2(lookVec.y, xzProjLen) * RAD_TO_DEG)));
		float yRot = Mth.wrapDegrees((float)(Mth.atan2(lookVec.z, lookVec.x) * RAD_TO_DEG) - 90);
		return new Vec2(xRot, yRot);
	}
	
	public static Vec2 lookAnglesTowards(Vec3 targetPos, Entity lookingEntity, EntityAnchorArgument.Anchor lookingAnchor) {
		Vec3 entityPos = lookingAnchor.apply(lookingEntity);
		Vec3 vecToTarget = targetPos.subtract(entityPos);
		double xzProjLen = Math.sqrt(vecToTarget.x * vecToTarget.x + vecToTarget.z * vecToTarget.z);
		float xRot = Mth.wrapDegrees((float)(-(Mth.atan2(vecToTarget.y, xzProjLen) * RAD_TO_DEG)));
		float yRot = Mth.wrapDegrees((float)(Mth.atan2(vecToTarget.z, vecToTarget.x) * RAD_TO_DEG) - 90);
		return new Vec2(xRot, yRot);
	}

	public static float yRotDegFromVec(Vec3 vec) {
		return (float) -Mth.atan2(vec.x, vec.z) * RAD_TO_DEG;
	}

	public static float xRotDegFromVec(Vec3 vec) {
		return (float) -Mth.atan2(vec.y, Math.sqrt(vec.x * vec.x + vec.z * vec.z)) * RAD_TO_DEG;
	}

	public static float inverseLerp(float x, float a, float b) {
		return (x - a) / (b - a);
	}

	public static Vec3 lerpVector(float partial, Vec3 vec1, Vec3 vec2) {
		return lerpVector(partial, vec1.x, vec1.y, vec1.z, vec2.x, vec2.y, vec2.z);
	}

	public static Vec3 lerpVector(float partial, double x1, double y1, double z1, double x2, double y2, double z2) {
		double x = Mth.lerp(partial, x1, x2);
		double y = Mth.lerp(partial, y1, y2);
		double z = Mth.lerp(partial, z1, z2);
		return new Vec3(x, y, z);
	}

	public static Vec3 vecFromAngles(float xRotRad, float yRotRad) {
		yRotRad = -yRotRad;
		float f2 = Mth.cos(yRotRad);
		float f3 = Mth.sin(yRotRad);
		float f4 = Mth.cos(xRotRad);
		float f5 = Mth.sin(xRotRad);
		return new Vec3((double)(f3 * f4), (double)(-f5), (double)(f2 * f4));
	}


	public static float inverseArmorProtectionDamage(float damageAfterAbsorb, float armor, float toughness) {
		float f = armor / 25 - 1;
		float f2 = 25 * (1 + toughness / 8);
		return Mth.clamp(
				f2 * (f + (float) Math.sqrt(f * f + 2 * damageAfterAbsorb / f2)), 
				damageAfterAbsorb / (1 - armor / 125), 
				5 * damageAfterAbsorb);
	}

	
	public static double getAABBDistance(AABB aabb1, AABB aabb2) {
		double x1 = Math.min(aabb1.maxX, aabb2.maxX);
		double x2 = Math.max(aabb1.minX, aabb2.minX);
		double xDiff = Math.max(x2 - x1, 0);
		double y1 = Math.min(aabb1.maxY, aabb2.maxY);
		double y2 = Math.max(aabb1.minY, aabb2.minY);
		double yDiff = Math.max(y2 - y1, 0);
		double z1 = Math.min(aabb1.maxZ, aabb2.maxZ);
		double z2 = Math.max(aabb1.minZ, aabb2.minZ);
		double zDiff = Math.max(z2 - z1, 0);
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
	}
	
	public static AABBDist getAABBDistanceDetailed(AABB aabb1, AABB aabb2) {
		double x1 = Math.min(aabb1.maxX, aabb2.maxX);
		double x2 = Math.max(aabb1.minX, aabb2.minX);
		double xBB1;
		double xBB2;
		double xDiff;
		if (x1 >= x2) {
			xDiff = 0;
			double x = (x1 + x2) / 2;
			xBB1 = x;
			xBB2 = x;
		}
		else {
			xDiff = x2 - x1;
			if (x1 == aabb1.maxX) {
				xBB1 = x1;
				xBB2 = x2;
			}
			else {
				xBB1 = x2;
				xBB2 = x1;
			}
		}

		double y1 = Math.min(aabb1.maxY, aabb2.maxY);
		double y2 = Math.max(aabb1.minY, aabb2.minY);
		double yBB1;
		double yBB2;
		double yDiff;
		if (y1 >= y2) {
			yDiff = 0;
			double y = (y1 + y2) / 2;
			yBB1 = y;
			yBB2 = y;
		}
		else {
			yDiff = y2 - y1;
			if (y1 == aabb1.maxY) {
				yBB1 = y1;
				yBB2 = y2;
			}
			else {
				yBB1 = y2;
				yBB2 = y1;
			}
		}

		double z1 = Math.min(aabb1.maxZ, aabb2.maxZ);
		double z2 = Math.max(aabb1.minZ, aabb2.minZ);
		double zBB1;
		double zBB2;
		double zDiff;
		if (z1 >= z2) {
			zDiff = 0;
			double z = (z1 + z2) / 2;
			zBB1 = z;
			zBB2 = z;
		}
		else {
			zDiff = z2 - z1;
			if (z1 == aabb1.maxZ) {
				zBB1 = z1;
				zBB2 = z2;
			}
			else {
				zBB1 = z2;
				zBB2 = z1;
			}
		}
		
		double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
		return new AABBDist(new Vec3(xBB1, yBB1, zBB1), new Vec3(xBB2, yBB2, zBB2), distance);
	}
	
	public static record AABBDist(Vec3 posBB1, Vec3 posBB2, double distance) {}
	
	
	public static double getManhattanDist(AABB aabb1, AABB aabb2) {
		double xDist = 0;
		double yDist = 0;
		double zDist = 0;

		if      (aabb1.maxX < aabb2.minX) xDist = aabb2.minX - aabb1.maxX;
		else if (aabb2.maxX < aabb1.minX) xDist = aabb1.minX - aabb2.maxX;

		if      (aabb1.maxY < aabb2.minY) yDist = aabb2.minY - aabb1.maxY;
		else if (aabb2.maxY < aabb1.minY) yDist = aabb1.minY - aabb2.maxY;

		if      (aabb1.maxZ < aabb2.minZ) zDist = aabb2.minZ - aabb1.maxZ;
		else if (aabb2.maxZ < aabb1.minZ) zDist = aabb1.minZ - aabb2.maxZ;

		return xDist + yDist + zDist;
	}
	
	
	public static int min(int num1, int num2, int... nums) {
		int min = (num1 <= num2) ? num1 : num2;
		for (int num : nums) {
			if (num < min) {
				min = num;
			}
		}
		return min;
	}
	
	public static float min(float num1, float num2, float... nums) {
		float min = (num1 <= num2) ? num1 : num2;
		for (float num : nums) {
			if (num < min) {
				min = num;
			}
		}
		return min;
	}
	
	public static double min(double num1, double num2, double... nums) {
		double min = (num1 <= num2) ? num1 : num2;
		for (double num : nums) {
			if (num < min) {
				min = num;
			}
		}
		return min;
	}
	
	public static int max(int num1, int num2, int... nums) {
		int max = (num1 >= num2) ? num1 : num2;
		for (int num : nums) {
			if (num > max) {
				max = num;
			}
		}
		return max;
	}
	
	public static float max(float num1, float num2, float... nums) {
		float max = (num1 >= num2) ? num1 : num2;
		for (float num : nums) {
			if (num > max) {
				max = num;
			}
		}
		return max;
	}
	
	public static double max(double num1, double num2, double... nums) {
		double max = (num1 >= num2) ? num1 : num2;
		for (double num : nums) {
			if (num > max) {
				max = num;
			}
		}
		return max;
	}


	public static int fractionRandomInc(double num) {
		int numInt = Mth.floor(num);
		if (Math.random() < num - (double) numInt) {
			numInt++;
		}
		return numInt;
	}

	private static final Random RANDOM = new Random();
	public static int fractionRandomInc(float num) {
		int numInt = Mth.floor(num);
		if (RANDOM.nextFloat() < num - (float) numInt) {
			numInt++;
		}
		return numInt;
	}

	public static int round(double value) {
		int i = (int) value;
		double frac = value > i ? value - i : i - value;
		if (frac < 0.5) {
			return i;
		}
		else {
			return value > i ? i + 1 : i - 1;
		}
	}
	
	public static float ratioSafe(float value, float maxValue) {
		return value >= maxValue ? 1 : maxValue > 0 ? value / maxValue : 0;
	}

}
