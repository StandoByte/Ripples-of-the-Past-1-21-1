package com.github.standobyte.jojo.client.entityanim.player;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ModelPart$Polygon;
import com.google.common.collect.Iterables;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidModelCubesBent {
	public List<ModelPart.Cube> head;
	public List<ModelPart.Cube> head2;
	public List<ModelPart.Cube> torso_lower;
	public List<ModelPart.Cube> torso_lower2;
	public List<ModelPart.Cube> torso_bend;
	public List<ModelPart.Cube> torso_bend2;
	public List<ModelPart.Cube> right_arm;
	public List<ModelPart.Cube> right_arm2;
	public List<ModelPart.Cube> right_arm_bend;
	public List<ModelPart.Cube> right_arm_bend2;
	public List<ModelPart.Cube> left_arm;
	public List<ModelPart.Cube> left_arm2;
	public List<ModelPart.Cube> left_arm_bend;
	public List<ModelPart.Cube> left_arm_bend2;
	public List<ModelPart.Cube> right_leg;
	public List<ModelPart.Cube> right_leg2;
	public List<ModelPart.Cube> right_leg_bend;
	public List<ModelPart.Cube> right_leg_bend2;
	public List<ModelPart.Cube> left_leg;
	public List<ModelPart.Cube> left_leg2;
	public List<ModelPart.Cube> left_leg_bend;
	public List<ModelPart.Cube> left_leg_bend2;
	
	public List<ModelPart.Cube> getCubes(String modelPartName, boolean outerLayer) {
		return switch (modelPartName) {
			case "left_arm" -> 			outerLayer ? left_arm2 : left_arm;
			case "right_arm" -> 		outerLayer ? right_arm2 : right_arm;
			case "left_leg" ->			outerLayer ? left_leg2 : left_leg;
			case "right_leg" -> 		outerLayer ? right_leg2 : right_leg;
			case "head" -> 				outerLayer ? head2 : head;
			case "torso_lower" -> 		outerLayer ? torso_lower2 : torso_lower;
			case "torso_bend" -> 		outerLayer ? torso_bend2 : torso_bend;
			case "left_arm_bend" -> 	outerLayer ? left_arm_bend2 : left_arm_bend;
			case "right_arm_bend" -> 	outerLayer ? right_arm_bend2 : right_arm_bend;
			case "left_leg_bend" -> 	outerLayer ? left_leg_bend2 : left_leg_bend;
			case "right_leg_bend" -> 	outerLayer ? right_leg_bend2 : right_leg_bend;
////			case "cape" -> 				playerModel.body.children.get("cape");
//			case "cape" -> 				((Model_1_21_2plus) playerModel).jojo_ripples$root().children.get("cloak");
//			case "cape_bend" -> 		((IPlayerBendModel) playerModel).jojo_ripples$animCapeBend();
			default -> null;
		};
	}
	
	

	public static <T extends LivingEntity> HumanoidModelCubesBent createFromBase(HumanoidModel<T> model) {
		HumanoidModelCubesBent obj = new HumanoidModelCubesBent();
		obj.head = model.head.cubes;
		obj.head2 = model.hat.cubes;
		
		var split = split(model.body, 6, true);
		obj.torso_lower = split.getLeft();
		obj.torso_bend = split.getRight();
		split = split(model.rightArm, 4, false);
		obj.right_arm = split.getLeft();
		obj.right_arm_bend = split.getRight();
		split = split(model.leftArm, 4, false);
		obj.left_arm = split.getLeft();
		obj.left_arm_bend = split.getRight();
		split = split(model.rightLeg, 6, false);
		obj.right_leg = split.getLeft();
		obj.right_leg_bend = split.getRight();
		split = split(model.leftLeg, 6, false);
		obj.left_leg = split.getLeft();
		obj.left_leg_bend = split.getRight();
		
		if (model instanceof PlayerModel playerModel) {
			split = split(playerModel.jacket, 6, true);
			obj.torso_lower2 = split.getLeft();
			obj.torso_bend2 = split.getRight();
			split = split(playerModel.rightSleeve, 4, false);
			obj.right_arm2 = split.getLeft();
			obj.right_arm_bend2 = split.getRight();
			split = split(playerModel.leftSleeve, 4, false);
			obj.left_arm2 = split.getLeft();
			obj.left_arm_bend2 = split.getRight();
			split = split(playerModel.rightPants, 6, false);
			obj.right_leg2 = split.getLeft();
			obj.right_leg_bend2 = split.getRight();
			split = split(playerModel.leftPants, 6, false);
			obj.left_leg2 = split.getLeft();
			obj.left_leg_bend2 = split.getRight();
		}
		
		return obj;
	}

	static List<ModelPart.Polygon> baseQuads = new ArrayList<>(6);
	static List<ModelPart.Polygon> bendQuads = new ArrayList<>(6);
	static List<ModelPart.Vertex> yLess = new ArrayList<>(4);
	static List<ModelPart.Vertex> yMore = new ArrayList<>(4);
	static Pair<List<ModelPart.Cube>, List<ModelPart.Cube>> split(ModelPart modelPart, float ySplitAt, boolean invert) {
		List<ModelPart.Cube> baseHalf = new ArrayList<>(modelPart.cubes.size());
		List<ModelPart.Cube> bentHalf = new ArrayList<>(modelPart.cubes.size());
		for (ModelPart.Cube cube : modelPart.cubes) {
			baseQuads.clear();
			bendQuads.clear();
			for (ModelPart.Polygon quad : cube.polygons) {
				yLess.clear();
				yMore.clear();
				for (ModelPart.Vertex vertex : quad.vertices) {
					if (vertex.pos.y < ySplitAt) {
						yLess.add(vertex);
					}
					else if (vertex.pos.y > ySplitAt) {
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
					float yRatio = Mth.inverseLerp(ySplitAt, y0, y2);
					float vSplit = Mth.lerp(yRatio, v0, v2);
					
					ModelPart.Vertex[] vertices = new ModelPart.Vertex[4];
					ModelPart.Vertex vert1 = yLess.get(0);
					ModelPart.Vertex vert2 = yLess.get(1);
					if (invert) {
						vert1 = new ModelPart.Vertex(vert1.pos.x, vert1.pos.y - ySplitAt, vert1.pos.z, vert1.u, vert1.v);
						vert2 = new ModelPart.Vertex(vert2.pos.x, vert2.pos.y - ySplitAt, vert2.pos.z, vert2.u, vert2.v);
						vertices[0] = vert1;
						vertices[1] = vert2;
						vertices[2] = new ModelPart.Vertex(vert2.pos.x, 0, vert2.pos.z, vert2.u, vSplit);
						vertices[3] = new ModelPart.Vertex(vert1.pos.x, 0, vert1.pos.z, vert1.u, vSplit);
					}
					else {
						vertices[0] = vert1;
						vertices[1] = vert2;
						vertices[2] = new ModelPart.Vertex(vert2.pos.x, ySplitAt, vert2.pos.z, vert2.u, vSplit);
						vertices[3] = new ModelPart.Vertex(vert1.pos.x, ySplitAt, vert1.pos.z, vert1.u, vSplit);
					}
					ModelPart.Polygon yLessQuad = _ModelPart$Polygon.create(vertices, quad.normal);

					vertices = new ModelPart.Vertex[4];
					vert1 = yMore.get(0);
					vert2 = yMore.get(1);
					if (invert) {
						vertices[0] = vert1;
						vertices[1] = vert2;
						vertices[2] = new ModelPart.Vertex(vert2.pos.x, ySplitAt, vert2.pos.z, vert2.u, vSplit);
						vertices[3] = new ModelPart.Vertex(vert1.pos.x, ySplitAt, vert1.pos.z, vert1.u, vSplit);
					}
					else {
						vert1 = new ModelPart.Vertex(vert1.pos.x, vert1.pos.y - ySplitAt, vert1.pos.z, vert1.u, vert1.v);
						vert2 = new ModelPart.Vertex(vert2.pos.x, vert2.pos.y - ySplitAt, vert2.pos.z, vert2.u, vert2.v);
						vertices[0] = vert1;
						vertices[1] = vert2;
						vertices[2] = new ModelPart.Vertex(vert2.pos.x, 0, vert2.pos.z, vert2.u, vSplit);
						vertices[3] = new ModelPart.Vertex(vert1.pos.x, 0, vert1.pos.z, vert1.u, vSplit);
					}
					ModelPart.Polygon yMoreQuad = _ModelPart$Polygon.create(vertices, quad.normal);
					
					if (invert) {
						baseQuads.add(yMoreQuad);
						bendQuads.add(yLessQuad);
					}
					else {
						baseQuads.add(yLessQuad);
						bendQuads.add(yMoreQuad);
					}

					// FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!! (bend) connect the split cubes
				}
				else if (yLess.isEmpty()) {
					if (invert) {
						baseQuads.add(quad);
					}
					else {
						ModelPart.Vertex[] vertices = new ModelPart.Vertex[quad.vertices.length];
						for (int i = 0; i < quad.vertices.length; i++) {
							ModelPart.Vertex oldV = quad.vertices[i];
							vertices[i] = new ModelPart.Vertex(oldV.pos.x, oldV.pos.y - ySplitAt, oldV.pos.z, oldV.u, oldV.v);
						}
						bendQuads.add(_ModelPart$Polygon.create(vertices, quad.normal));
					}
				}
				else {
					if (invert) {
						ModelPart.Vertex[] vertices = new ModelPart.Vertex[quad.vertices.length];
						for (int i = 0; i < quad.vertices.length; i++) {
							ModelPart.Vertex oldV = quad.vertices[i];
							vertices[i] = new ModelPart.Vertex(oldV.pos.x, oldV.pos.y - ySplitAt, oldV.pos.z, oldV.u, oldV.v);
						}
						bendQuads.add(_ModelPart$Polygon.create(vertices, quad.normal));
					}
					else {
						baseQuads.add(quad);
					}
				}
			}
			
			ModelPart.Cube baseCube = fromPolygons(baseQuads);
			ModelPart.Cube bendCube = fromPolygons(bendQuads);

			baseHalf.add(baseCube);
			bentHalf.add(bendCube);
		}
		
		return Pair.of(baseHalf, bentHalf);
	}
	
	public static ModelPart.Cube fromPolygons(Iterable<ModelPart.Polygon> polygons) {
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
		ModelPart.Cube cube = new ModelPart.Cube(0, 0, 
				minX, minY, minZ, 
				maxX - minX, maxY - minY, maxZ - minZ, 
				0, 0, 0, false, 1, 1, BlockbenchMeshDefinition.NO_DIRECTIONAL_FACES);
		cube.polygons = Iterables.toArray(polygons, ModelPart.Polygon.class);
		return cube;
	}
	
}
