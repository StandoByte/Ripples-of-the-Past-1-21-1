package com.github.standobyte.jojo.client.entityanim.player.bend_crutches;

import org.joml.Vector3f;

import net.minecraft.client.model.geom.ModelPart;

public class DeformableQuad {
	public final DeformableVertex[] vertices;
	public final Vector3f normal;
	
	public DeformableQuad(DeformableVertex[] vertices, Vector3f normal) {
		this.vertices = vertices;
		this.normal = normal;
	}
	
	public static DeformableQuad fromVanilla(ModelPart.Polygon quad) {
		DeformableVertex[] vertices = new DeformableVertex[quad.vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = DeformableVertex.fromVanilla(quad.vertices[i]);
		}
		return new DeformableQuad(vertices, quad.normal);
	}
	
}
