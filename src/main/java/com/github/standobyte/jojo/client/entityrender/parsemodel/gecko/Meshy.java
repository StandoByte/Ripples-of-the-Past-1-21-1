package com.github.standobyte.jojo.client.entityrender.parsemodel.gecko;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition.MeshBuilder;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition.VertexDefinition;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition.MeshBuilder.MeshFaceBuilder;

// Mesh shapes can be exported in a Bedrock Entity format using Meshy plugin for Blockbench
// https://github.com/Shadowkitten47/Meshy
public record Meshy(boolean normalized_uvs, Vector3f[] positions, Vector3f[] normals, Vector2f[] uvs, Vector3i[][] polys) {

	public BlockbenchMeshDefinition makeMesh(GeckoModelFormat.Description textureSize, Vector3f bonePivot) {
		if (normalized_uvs) {
			float mulTexU = textureSize.texture_width();
			float mulTexV = textureSize.texture_height();
			for (Vector2f uv : uvs) {
				uv.mul(mulTexU, mulTexV);
			}
		}
		for (Vector2f uv : uvs) {
			uv.set(uv.x, textureSize.texture_height() - uv.y);
		}
		
		MeshBuilder meshBuilder = new MeshBuilder(true);
		for (Vector3i[] face : polys) {
			if (face.length > 2) {
				VertexDefinition[] vertices = new VertexDefinition[face.length];
				int i = 0;
				for (Vector3i vertexDefinition : face) {
					Vector3f pos = positions[vertexDefinition.x];
					Vector2f uv = uvs[vertexDefinition.z];
					vertices[i++] = new VertexDefinition(pos, uv.x, uv.y); 
				}
				
				MeshFaceBuilder faceBuilder = meshBuilder.startFaceCalcNormal();
				for (VertexDefinition vertex : vertices) {
					faceBuilder.withVertex(
							vertex.pos().x() - bonePivot.x(), 
							vertex.pos().y() - bonePivot.y(), 
							vertex.pos().z() - bonePivot.z(), 
							vertex.uPos(), vertex.vPos());
				}
				faceBuilder.createFace();
			}
		}
		
		return meshBuilder.buildCube();
	}
}
