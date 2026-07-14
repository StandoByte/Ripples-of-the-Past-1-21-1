package com.github.standobyte.jojo.client.entityanim.player.bend_crutches;

import java.util.Map;

import org.joml.Vector3f;

import net.minecraft.client.model.geom.ModelPart;

public class DeformableVertex {
	public final RememberingPos pos;
	public final float u;
	public final float v;
	
	public DeformableVertex(RememberingPos pos, float u, float v) {
		this.pos = pos;
		this.u = u;
		this.v = v;
	}
	
	public Vector3f pos() {
		return pos.mutablePos();
	}
	
	public static DeformableVertex fromVanilla(ModelPart.Vertex vertex, 
			Map<Vector3f, RememberingPos> cubeVertices) {
		Vector3f posHash = vertex.pos;
		RememberingPos cubeVertex = cubeVertices.computeIfAbsent(posHash, RememberingPos::new);
		return new DeformableVertex(cubeVertex, vertex.u, vertex.v);
	}
	
}
