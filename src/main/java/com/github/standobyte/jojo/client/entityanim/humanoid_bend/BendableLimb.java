package com.github.standobyte.jojo.client.entityanim.humanoid_bend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import com.github.standobyte.v1_21_4_stuff.missingmethods._ModelPart$Polygon;
import com.google.common.collect.Iterables;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;

public record BendableLimb(LimbHalf base, LimbHalf bend, Map<String, ModelPart> joint,
		float x, float y, float z, boolean bendIsAbove) {

	public static record LimbHalf(List<ModelPart.Cube> cubes, Map<String, ModelPart> children) {
		public ModelPart makePart() {
			return new ModelPart(cubes, children);
		}
	}
	


	static List<ModelPart.Polygon> baseQuads = new ArrayList<>(6);
	static List<ModelPart.Polygon> bendQuads = new ArrayList<>(6);
	static List<ModelPart.Vertex> yLess = new ArrayList<>(4);
	static List<ModelPart.Vertex> yMore = new ArrayList<>(4);
	public static BendableLimb create(ModelPart modelPart, 
			float x, float y, float z, float yOffset, boolean bendIsAbove) {
		List<ModelPart.Cube> baseHalfCubes = new ArrayList<>(modelPart.cubes.size());
		List<ModelPart.Cube> bendHalfCubes = new ArrayList<>(modelPart.cubes.size());
		for (ModelPart.Cube cube : modelPart.cubes) {
			baseQuads.clear();
			bendQuads.clear();
			float z0 = Float.MAX_VALUE;
			float z1 = -Float.MAX_VALUE;
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
					
					z0 = Math.min(z0, vertex.pos.z);
					z1 = Math.max(z1, vertex.pos.z);
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
			
			int deformTexels = 2;
			Box baseSize;
			Box bendSize;
			if (bendIsAbove) {
				baseSize = cutUpPolygons(baseQuads, y + yOffset, deformTexels);
				bendSize = cutUpPolygons(bendQuads, 0, -deformTexels);
			}
			else {
				baseSize = cutUpPolygons(baseQuads, y + yOffset, -deformTexels);
				bendSize = cutUpPolygons(bendQuads, 0, deformTexels);
			}
			DeformableCube baseCube = fromPolygons(baseQuads, baseSize);
			DeformableCube bendCube = fromPolygons(bendQuads, bendSize);

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
					if (childY < y) {
						offsetAndPut(yLessChildren, childEntry.getKey(), child, x, y + yOffset, z, bendIsAbove);
					}
					else if (childY > y) {
						offsetAndPut(yMoreChildren, childEntry.getKey(), child, x, y + yOffset, z, !bendIsAbove);
					}
					else {
						joint.put(childEntry.getKey(), child);
					}
				}
			}
		}
		
		return new BendableLimb(baseHalf, bendHalf, joint,
				x, y + yOffset, z, bendIsAbove);
	}
	
	static void offsetAndPut(Map<String, ModelPart> children, String childName, ModelPart child, 
			float x, float y, float z, boolean isBendHalf) {
		Vector3f offset = new Vector3f(0, 0, 0);
		if (isBendHalf) {
			offset.sub(x, y, z);
		}
		ModelPart deepCopy = new ModelPart(child.cubes, child.children);
		PartPose initialPose = child.getInitialPose();
		deepCopy.setInitialPose(PartPose.offsetAndRotation(
				initialPose.x -/*???*/ offset.x, 
				initialPose.y + offset.y, 
				initialPose.z + offset.z, 
				initialPose.xRot, initialPose.yRot, initialPose.zRot));
		deepCopy.resetPose();
		children.put(childName, deepCopy);
	}

	static List<ModelPart.Polygon> buffer = new ArrayList<>(13);
	public static Box cutUpPolygons(Collection<ModelPart.Polygon> polygons, 
			float bendY, float distortYOffset) {
		if (polygons.isEmpty()) return null;
		
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		float maxZ = -Float.MAX_VALUE;
		
		for (ModelPart.Polygon quad : polygons) {
//			boolean keepQuadIntact = true;
			
			float x0 = Float.MAX_VALUE;
			float y0 = Float.MAX_VALUE;
			float z0 = Float.MAX_VALUE;
			float x1 = -Float.MAX_VALUE;
			float y1 = -Float.MAX_VALUE;
			float z1 = -Float.MAX_VALUE;
			
//			float v0 = Float.MAX_VALUE;
//			float v1 = -Float.MAX_VALUE;
//			
//			ModelPart.Vertex y0_0 = null;
//			ModelPart.Vertex y0_1 = null;
//			ModelPart.Vertex y1_0 = null;
//			ModelPart.Vertex y1_1 = null;
			
			for (ModelPart.Vertex vertex : quad.vertices) {
				x0 = Math.min(x0, vertex.pos.x);
				y0 = Math.min(y0, vertex.pos.y);
				z0 = Math.min(z0, vertex.pos.z);
				x1 = Math.max(x1, vertex.pos.x);
				y1 = Math.max(y1, vertex.pos.y);
				z1 = Math.max(z1, vertex.pos.z);
				
//				v0 = Math.min(v0, vertex.v);
//				v1 = Math.max(v1, vertex.v);
			}
			
			minX = Math.min(minX, x0);
			minY = Math.min(minY, y0);
			minZ = Math.min(minZ, z0);
			maxX = Math.max(maxX, x1);
			maxY = Math.max(maxY, y1);
			maxZ = Math.max(maxZ, z1);
			
//			if (distortYOffset != 0 && y0 != y1) {
//				for (ModelPart.Vertex vertex : quad.vertices) {
//					if (vertex.pos.y == y0) {
//						if (x0 != x1) {
//							if (vertex.pos.x == x0)		y0_0 = vertex;
//							else						y0_1 = vertex;
//						}
//						else {
//							if (vertex.pos.z == z0)		y0_0 = vertex;
//							else						y0_1 = vertex;
//						}
//					}
//					else {
//						if (x0 != x1) {
//							if (vertex.pos.x == x0)		y1_0 = vertex;
//							else						y1_1 = vertex;
//						}
//						else {
//							if (vertex.pos.z == z0)		y1_0 = vertex;
//							else						y1_1 = vertex;
//						}
//					}
//				}
//				
//				if (y0_0 != null && y0_1 != null && y1_0 != null && y1_1 != null) {
//					float maxOffsetAbs = Math.abs(distortYOffset);
//					
//					for (float offsetAbs = 0; offsetAbs <= maxOffsetAbs; /*offsetAbs++*/ offsetAbs += maxOffsetAbs) {
//						float y0_2;
//						float y1_2;
//						if (distortYOffset > 0) {
//							float offset = offsetAbs;
//							y0_2 = bendY + offset;
//							y1_2 = offsetAbs < maxOffsetAbs ? y0_2 + /*1*/maxOffsetAbs : y1;
//						}
//						else {
//							float offset = -offsetAbs;
//							y1_2 = bendY + offset;
//							y0_2 = offsetAbs < maxOffsetAbs ? y1_2 - /*1*/maxOffsetAbs : y0;
//						}
//						
//						if (y0_2 > y0 || y1_2 < y1) {
//							float y0_border = y0;
//							float y1_border = y1;
//							
//							if (y0_2 > y0) {
//								y0_border = y0_2;
//							}
//							else if (y1_2 < y1) {
//								y1_border = y1_2;
//							}
//							
//							float vRatio = (v1 - v0) / (y1 - y0);
//							float v0_border = v0 + (y0_border - y0) * vRatio;
//							float v1_border = v0 + (y1_border - y0) * vRatio;
//							ModelPart.Vertex[] newVertices = new ModelPart.Vertex[4];
//							newVertices[0] = new ModelPart.Vertex(y0_0.pos.x, y0_border, y0_0.pos.z, y0_0.u, v0_border);
//							newVertices[1] = new ModelPart.Vertex(y0_1.pos.x, y0_border, y0_1.pos.z, y0_1.u, v0_border);
//							newVertices[2] = new ModelPart.Vertex(y1_1.pos.x, y1_border, y1_1.pos.z, y1_1.u, v1_border);
//							newVertices[3] = new ModelPart.Vertex(y1_0.pos.x, y1_border, y1_0.pos.z, y1_0.u, v1_border);
//							buffer.add(_ModelPart$Polygon.create(newVertices, quad.normal));
//							
//							keepQuadIntact = false;
//						}
//					}
//				}
//			}
//			
//			if (keepQuadIntact) {
//				buffer.add(quad);
//			}
		}
		
//		polygons.clear();
//		polygons.addAll(buffer);
//		buffer.clear();
		
		return new Box(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	static record Box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}
	
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
		return fromPolygons(polygons, minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public static DeformableCube fromPolygons(Iterable<ModelPart.Polygon> polygons, Box size) {
		if (size != null) {
			return fromPolygons(polygons, 
					size.minX, size.minY, size.minZ, 
					size.maxX, size.maxY, size.maxZ);
		}
		else {
			return fromPolygons(polygons);
		}
	}
	
	public static DeformableCube fromPolygons(Iterable<ModelPart.Polygon> polygons, 
			float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		DeformableCube cube = new DeformableCube(Iterables.toArray(polygons, ModelPart.Polygon.class),
				minX, minY, minZ, maxX, maxY, maxZ);
		return cube;
	}
}
