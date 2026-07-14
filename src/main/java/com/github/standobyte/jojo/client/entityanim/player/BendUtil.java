package com.github.standobyte.jojo.client.entityanim.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.player.bend_crutches.DeformableCube;
import com.github.standobyte.jojo.client.entityanim.player.bend_crutches.RememberingPos;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ModelPart$Polygon;
import com.google.common.collect.Iterables;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;

public class BendUtil {

	public static record LimbHalf(List<ModelPart.Cube> cubes, Map<String, ModelPart> children) {
		public ModelPart makePart() {
			return new ModelPart(cubes, children);
		}
	}
	
	public static record LimbSplit(LimbHalf base, LimbHalf bend, Map<String, ModelPart> joint,
			float x, float y, float z, float yOffset, boolean bendIsAbove) {}

	static List<ModelPart.Polygon> baseQuads = new ArrayList<>(6);
	static List<ModelPart.Polygon> bendQuads = new ArrayList<>(6);
	static List<ModelPart.Vertex> yLess = new ArrayList<>(4);
	static List<ModelPart.Vertex> yMore = new ArrayList<>(4);
	static LimbSplit split(ModelPart modelPart, 
			float x, float y, float z, float yOffset, boolean bendIsAbove) {
		List<ModelPart.Cube> baseHalfCubes = new ArrayList<>(modelPart.cubes.size());
		List<ModelPart.Cube> bendHalfCubes = new ArrayList<>(modelPart.cubes.size());
		for (ModelPart.Cube cube : modelPart.cubes) {
			baseQuads.clear();
			bendQuads.clear();
			for (ModelPart.Polygon quad : cube.polygons) {
				yLess.clear();
				yMore.clear();
				for (ModelPart.Vertex vertex : quad.vertices) {
					if (vertex.pos.y < y) {
						yLess.add(vertex);
					}
					else if (vertex.pos.y > y) {
						yMore.add(vertex);
					}
				}
				
				if (yLess.size() == 2 && yMore.size() == 2) {
					ModelPart.Vertex vertLess = yLess.get(0);
					ModelPart.Vertex vertMore = yMore.get(0);
					float v0 = vertLess.v;
					float v2 = vertMore.v;
					float y0 = vertLess.pos.y;
					float y2 = vertMore.pos.y;
					float yRatio = Mth.inverseLerp(y, y0, y2);
					float vSplit = Mth.lerp(yRatio, v0, v2);
					
					ModelPart.Vertex[] vertices = new ModelPart.Vertex[4];
					ModelPart.Vertex vert1 = yLess.get(0);
					ModelPart.Vertex vert2 = yLess.get(1);
					if (bendIsAbove) {
						vert1 = new ModelPart.Vertex(vert1.pos.x + x, vert1.pos.y - y, vert1.pos.z + z, vert1.u, vert1.v);
						vert2 = new ModelPart.Vertex(vert2.pos.x + x, vert2.pos.y - y, vert2.pos.z + z, vert2.u, vert2.v);
						vertices[0] = vert1;
						vertices[1] = vert2;
						vertices[2] = new ModelPart.Vertex(vert2.pos.x, 0, vert2.pos.z, vert2.u, vSplit);
						vertices[3] = new ModelPart.Vertex(vert1.pos.x, 0, vert1.pos.z, vert1.u, vSplit);
					}
					else {
						vert1 = new ModelPart.Vertex(vert1.pos.x, vert1.pos.y + yOffset, vert1.pos.z, vert1.u, vert1.v);
						vert2 = new ModelPart.Vertex(vert2.pos.x, vert2.pos.y + yOffset, vert2.pos.z, vert2.u, vert2.v);
						vertices[0] = vert1;
						vertices[1] = vert2;
						vertices[2] = new ModelPart.Vertex(vert2.pos.x, y + yOffset, vert2.pos.z, vert2.u, vSplit);
						vertices[3] = new ModelPart.Vertex(vert1.pos.x, y + yOffset, vert1.pos.z, vert1.u, vSplit);
					}
					ModelPart.Polygon yLessQuad = _ModelPart$Polygon.create(vertices, quad.normal);

					vertices = new ModelPart.Vertex[4];
					vert1 = yMore.get(0);
					vert2 = yMore.get(1);
					if (bendIsAbove) {
						vert1 = new ModelPart.Vertex(vert1.pos.x, vert1.pos.y + yOffset, vert1.pos.z, vert1.u, vert1.v);
						vert2 = new ModelPart.Vertex(vert2.pos.x, vert2.pos.y + yOffset, vert2.pos.z, vert2.u, vert2.v);
						vertices[0] = vert1;
						vertices[1] = vert2;
						vertices[2] = new ModelPart.Vertex(vert2.pos.x, y + yOffset, vert2.pos.z, vert2.u, vSplit);
						vertices[3] = new ModelPart.Vertex(vert1.pos.x, y + yOffset, vert1.pos.z, vert1.u, vSplit);
					}
					else {
						vert1 = new ModelPart.Vertex(vert1.pos.x + x, vert1.pos.y - y, vert1.pos.z + z, vert1.u, vert1.v);
						vert2 = new ModelPart.Vertex(vert2.pos.x + x, vert2.pos.y - y, vert2.pos.z + z, vert2.u, vert2.v);
						vertices[0] = vert1;
						vertices[1] = vert2;
						vertices[2] = new ModelPart.Vertex(vert2.pos.x, 0, vert2.pos.z, vert2.u, vSplit);
						vertices[3] = new ModelPart.Vertex(vert1.pos.x, 0, vert1.pos.z, vert1.u, vSplit);
					}
					ModelPart.Polygon yMoreQuad = _ModelPart$Polygon.create(vertices, quad.normal);
					
					if (bendIsAbove) {
						baseQuads.add(yMoreQuad);
						bendQuads.add(yLessQuad);
					}
					else {
						baseQuads.add(yLessQuad);
						bendQuads.add(yMoreQuad);
					}
				}
				else if (yLess.isEmpty()) {
					if (bendIsAbove) {
						ModelPart.Vertex[] vertices = new ModelPart.Vertex[quad.vertices.length];
						for (int i = 0; i < quad.vertices.length; i++) {
							ModelPart.Vertex oldV = quad.vertices[i];
							vertices[i] = new ModelPart.Vertex(oldV.pos.x, oldV.pos.y + yOffset, oldV.pos.z, oldV.u, oldV.v);
						}
						baseQuads.add(_ModelPart$Polygon.create(vertices, quad.normal));
					}
					else {
						ModelPart.Vertex[] vertices = new ModelPart.Vertex[quad.vertices.length];
						for (int i = 0; i < quad.vertices.length; i++) {
							ModelPart.Vertex oldV = quad.vertices[i];
							vertices[i] = new ModelPart.Vertex(oldV.pos.x + x, oldV.pos.y - y, oldV.pos.z + z, oldV.u, oldV.v);
						}
						bendQuads.add(_ModelPart$Polygon.create(vertices, quad.normal));
					}
				}
				else {
					if (bendIsAbove) {
						ModelPart.Vertex[] vertices = new ModelPart.Vertex[quad.vertices.length];
						for (int i = 0; i < quad.vertices.length; i++) {
							ModelPart.Vertex oldV = quad.vertices[i];
							vertices[i] = new ModelPart.Vertex(oldV.pos.x + x, oldV.pos.y - y, oldV.pos.z + z, oldV.u, oldV.v);
						}
						bendQuads.add(_ModelPart$Polygon.create(vertices, quad.normal));
					}
					else {
						ModelPart.Vertex[] vertices = new ModelPart.Vertex[quad.vertices.length];
						for (int i = 0; i < quad.vertices.length; i++) {
							ModelPart.Vertex oldV = quad.vertices[i];
							vertices[i] = new ModelPart.Vertex(oldV.pos.x, oldV.pos.y + yOffset, oldV.pos.z, oldV.u, oldV.v);
						}
						baseQuads.add(_ModelPart$Polygon.create(vertices, quad.normal));
					}
				}
			}
			
			DeformableCube baseCube = fromPolygons(baseQuads);
			DeformableCube bendCube = fromPolygons(bendQuads);

			baseHalfCubes.add(baseCube);
			bendHalfCubes.add(bendCube);
		}
		
		LimbHalf baseHalf = new LimbHalf(baseHalfCubes, new HashMap<>());
		LimbHalf bendHalf = new LimbHalf(bendHalfCubes, new HashMap<>());
		Map<String, ModelPart> joint = new HashMap<>();
		
		if (!modelPart.children.isEmpty()) {
			Map<String, ModelPart> yMoreChildren = (bendIsAbove ? baseHalf : bendHalf).children;
			Map<String, ModelPart> yLessChildren = (bendIsAbove ? bendHalf : baseHalf).children;
			for (var childEntry : modelPart.children.entrySet()) {
				ModelPart child = childEntry.getValue();
				if (!BendUtil.isSamePivotAsParent(child)) {
					float childY = child.y;
					if (childY < yOffset) {
						yLessChildren.put(childEntry.getKey(), child);
					}
					else if (childY > yOffset) {
						yMoreChildren.put(childEntry.getKey(), child);
					}
					else {
						joint.put(childEntry.getKey(), child);
					}
				}
			}
		}
		
		return new LimbSplit(baseHalf, bendHalf, joint,
				x, y, z, yOffset, bendIsAbove);
	}
	
	public static DeformableCube fromPolygons(Iterable<ModelPart.Polygon> polygons) {
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		float maxZ = -Float.MAX_VALUE;
		for (ModelPart.Polygon polygon : polygons) {
			for (ModelPart.Vertex vertex : polygon.vertices) {
				minX = Math.min(minX, vertex.pos.x);
				minY = Math.min(minY, vertex.pos.y);
				minZ = Math.min(minZ, vertex.pos.z);
				maxX = Math.max(maxX, vertex.pos.x);
				maxY = Math.max(maxY, vertex.pos.y);
				maxZ = Math.max(maxZ, vertex.pos.z);
			}
		}
		DeformableCube cube = new DeformableCube(Iterables.toArray(polygons, ModelPart.Polygon.class),
				minX, minY, minZ, maxX, maxY, maxZ);
		return cube;
	}
	
	public static boolean isSamePivotAsParent(ModelPart modelPart) {
		PartPose initialPose = modelPart.getInitialPose();
		return 
				initialPose.xRot == 0 && initialPose.yRot == 0 && initialPose.zRot == 0 
				&& initialPose.x == 0 && initialPose.y == 0 && initialPose.z == 0;
	}
	
	
	// TODO smoother bends on high bend value
	public static void connectVertices(LimbHalf limbPart, float bend, 
			float bendX, float bendY, float bendZ, boolean isBendPart) {
		for (ModelPart.Cube _cube : limbPart.cubes) {
			DeformableCube cube = (DeformableCube) _cube;
			cube.reset();
			
			if (bend != 0) {
				float tan = MathUtil.tan(bend / 2);
				for (RememberingPos vertex : cube.distinctVertices) {
					Vector3f pos = vertex.mutablePos();
					
					float width = pos.z - bendZ;
					float yDiff = width * tan;
					
					if (pos.y == bendY) {
						if (isBendPart)	pos.y += yDiff;
						else			pos.y -= yDiff;
					}
				}
			}
		}
		
	}
	
}
