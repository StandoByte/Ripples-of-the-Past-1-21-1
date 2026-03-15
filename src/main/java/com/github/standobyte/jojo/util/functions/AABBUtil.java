package com.github.standobyte.jojo.util.functions;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AABBUtil {

	public static AABB scale(AABB aabb, double scale) {
		return scale(aabb, scale, scale, scale);
	}

	public static AABB scale(AABB aabb, double scaleX, double scaleY, double scaleZ) {
		Vec3 center = aabb.getCenter();
		double inflX = aabb.getXsize() * scaleX / 2;
		double inflY = aabb.getYsize() * scaleY / 2;
		double inflZ = aabb.getZsize() * scaleZ / 2;
		return new AABB(
				center.x - inflX, center.y - inflY, center.z - inflZ,
				center.x + inflX, center.y + inflY, center.z + inflZ);
	}

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

}
