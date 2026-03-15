package com.github.standobyte.jojo.util.objects_math;

import java.util.Random;

import net.minecraft.world.phys.Vec3;

public class PlaneRectangle {
	private static final Random RANDOM = new Random();
	public final Vec3 pLD;
	public final Vec3 pLU;
	public final Vec3 pRU;
	public final Vec3 pRD;
	public final Vec3 center;
	public final Vec3 normalVec;
	
	public static PlaneRectangle vertical(Vec3 pLD, Vec3 pRU) {
		return new PlaneRectangle(pLD, new Vec3(pLD.x, pRU.y, pLD.z), pRU, new Vec3(pRU.x, pLD.y, pRU.z));
	}
	
	public static PlaneRectangle clockwisePoints(Vec3 pLD, Vec3 pLU, Vec3 pRU) {
		return new PlaneRectangle(pLD, pLU, pRU, pRU.add(pLD.subtract(pLU)));
	}
	
	public PlaneRectangle(Vec3 pLD, Vec3 pLU, Vec3 pRU, Vec3 pRD) {
		this.pLD = pLD;
		this.pLU = pLU;
		this.pRU = pRU;
		this.pRD = pRD;
		this.center = pLD.add(pLU.subtract(pLD).scale(0.5)).add(pRD.subtract(pLD).scale(0.5));
		this.normalVec = pRU.subtract(pLD).cross(pLU.subtract(pRD)).normalize();
	}
	
	public PlaneRectangle scale(double scale) {
		return scale(scale, scale);
	}
	
	public PlaneRectangle scale(double scaleX, double scaleY) {
		Vec3 right = pRD.subtract(pLD).scale(scaleX * 0.5);
		Vec3 up = pLU.subtract(pLD).scale(scaleY * 0.5);
		return clockwisePoints(
				center.add(right.reverse()).add(up.reverse()),
				center.add(right.reverse()).add(up),
				center.add(right).add(up));
	}
	
	public Vec3 getUniformRandomPos() {
		return pLD
				.add(pRD.subtract(pLD).scale(RANDOM.nextDouble()))
				.add(pLU.subtract(pLD).scale(RANDOM.nextDouble()));
	}
}
