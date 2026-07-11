package com.github.standobyte.jojo.client.entityanim.player.bend_crutches;

import org.joml.Vector3f;

import net.minecraft.client.model.geom.ModelPart;

public class DeformableVertex {
	public final float originalX;
	public final float originalY;
	public final float originalZ;
	public final Vector3f pos;
	public final float u;
	public final float v;
	
	public DeformableVertex(float x, float y, float z, float u, float v) {
		this.pos = new Vector3f(x, y, z);
		this.originalX = x;
		this.originalY = y;
		this.originalZ = z;
		this.u = u;
		this.v = v;
	}
	
	public void reset() {
		this.pos.set(originalX, originalY, originalZ);
	}
	
	public static DeformableVertex fromVanilla(ModelPart.Vertex vertex) {
		return new DeformableVertex(vertex.pos.x, vertex.pos.y, vertex.pos.z, vertex.u, vertex.v);
	}
	
}
