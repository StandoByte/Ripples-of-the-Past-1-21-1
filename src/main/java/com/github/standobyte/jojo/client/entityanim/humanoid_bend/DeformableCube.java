package com.github.standobyte.jojo.client.entityanim.humanoid_bend;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;

public class DeformableCube extends ModelPart.Cube {
	public final Iterable<RememberingPos> distinctVertices;
	public final DeformableQuad[] dQuads;
	
	public DeformableCube(ModelPart.Polygon[] polygons, 
			float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		super(0, 0, minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ, 
				0, 0, 0, false, 1, 1, BlockbenchMeshDefinition.NO_DIRECTIONAL_FACES);
		this.polygons = polygons;
		this.dQuads = new DeformableQuad[polygons.length];
		Map<Vector3f, RememberingPos> distinctVertices = new HashMap<>();
		for (int i = 0; i < polygons.length; i++) {
			dQuads[i] = DeformableQuad.fromVanilla(polygons[i], distinctVertices);
		}
		this.distinctVertices = distinctVertices.values();
	}

	@Override
	public void compile(PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		Matrix4f matrix4f = pose.pose();
		Vector3f vector3f = new Vector3f();

		for (DeformableQuad modelpart$polygon : this.dQuads) {
			Vector3f vector3f1 = pose.transformNormal(modelpart$polygon.normal, vector3f);
			float f = vector3f1.x();
			float f1 = vector3f1.y();
			float f2 = vector3f1.z();

			for (DeformableVertex modelpart$vertex : modelpart$polygon.vertices) {
				Vector3f pos = modelpart$vertex.pos();
				float f3 = pos.x() / 16.0F;
				float f4 = pos.y() / 16.0F;
				float f5 = pos.z() / 16.0F;
				Vector3f vector3f2 = matrix4f.transformPosition(f3, f4, f5, vector3f);
				buffer.addVertex(
					vector3f2.x(), vector3f2.y(), vector3f2.z(), color, modelpart$vertex.u, modelpart$vertex.v, packedOverlay, packedLight, f, f1, f2
				);
			}
		}
	}
	
	public void reset() {
		for (RememberingPos vertex : distinctVertices) {
			vertex.reset();
		}
	}
	
}
