package com.github.standobyte.jojo.client.entityrender.parsemodel.generic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel;
import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel.Utils.RotatedCubeCounter;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchCubeDefinition.BoxFace;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition.MeshBuilder;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition.VertexDefinition;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition.MeshBuilder.MeshFaceBuilder;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods._PartDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import net.minecraft.Util;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

public class GenericModelFormat {
	
	public static LayerDefinition parseGenericModel(JsonElement json) {
		GeometryParsed geomertyParsed = GSON.fromJson(json, GeometryParsed.class);
		BlockbenchGeometry geomerty = organizeGeometry(geomertyParsed);
		MeshDefinition modelUnbaked = createGeometry(geomerty);
		Resolution texSize = geomerty.resolution;
		return LayerDefinition.create(modelUnbaked, texSize.width, texSize.height);
	}
	
	
	
	private static record GeometryParsed(
			Resolution resolution,
			List<BlockbenchElement> elements,
			List<BlockbenchObj> outliner) {}

	private static record Resolution(
			int width,
			int height) {}


	private static interface BlockbenchObj {}

	private static record GroupParsed(
			String name,
			Vector3f origin,
			@Nullable Vector3f rotation,
			UUID uuid,
			Boolean export,
			boolean mirror_uv,
			boolean visibility,
			int autoUv,
			List<BlockbenchObj> children) implements BlockbenchObj {

		boolean isExported() {
			return export() == null || export().booleanValue();
		}
	}

	private static record ElementUUID(
			UUID uuid) implements BlockbenchObj {}


	private static interface BlockbenchElement {
		Boolean export();
		String name();
		UUID uuid();
		boolean visibility();
		Vector3f origin();
		Vector3f rotation();
		String render_order();
		boolean allow_mirror_modeling();

		default boolean isExported() {
			return export() == null || export().booleanValue();
		}
		
		BlockbenchElement withZeroRotation();
	}

	private static record ElementMesh(
			Boolean export,
			String name,
			UUID uuid,
			boolean visibility,
			Vector3f origin,
			Vector3f rotation,
			String render_order,
			boolean allow_mirror_modeling,

			Map<String, Vector3f> vertices,
			Map<String, MeshFace> faces) implements BlockbenchElement {
		
		@Override public ElementMesh withZeroRotation() {
			return new ElementMesh(export, name, uuid, visibility, origin, null, render_order, allow_mirror_modeling, 
					vertices, faces);
		}
	}
	
	public record MeshFace(
			Map<String, float[]> uv,
			String[] vertices,
			int texture) {}

	public static record ElementCube(
			Boolean export,
			String name,
			UUID uuid,
			boolean visibility,
			Vector3f origin,
			Vector3f rotation,
			String render_order,
			boolean allow_mirror_modeling,

			boolean box_uv,
			boolean rescale,
			Vector3f from,
			Vector3f to,
			int autouv,
			float inflate,
			float[] uv_offset,
			Map<String, BoxFace> faces) implements BlockbenchElement {
		
		@Override public ElementCube withZeroRotation() {
			return new ElementCube(export, name, uuid, visibility, origin, null, render_order, allow_mirror_modeling, 
					box_uv, rescale, from, to, autouv, inflate, uv_offset, faces);
		}
	}


	private static record BlockbenchGeometry(
			Resolution resolution,
			Map<UUID, BlockbenchElement> elements,
			List<GroupParsed> topLevelModelParts) {}
	
	private static record ElementWrapper(
			BlockbenchElement element) implements BlockbenchObj {}
	
	static BlockbenchGeometry organizeGeometry(GeometryParsed parsed) {
		Map<UUID, BlockbenchElement> elements = parsed.elements.stream()
				.filter(BlockbenchElement::isExported)
				.collect(Collectors.toMap(BlockbenchElement::uuid, Function.identity()));
		
		List<GroupParsed> topLevelModelParts = parsed.outliner.stream()
				.filter(obj -> obj instanceof GroupParsed)
				.map(obj -> (GroupParsed) obj)
				.filter(GroupParsed::isExported)
				.collect(Collectors.toList());
		
		List<BlockbenchObj> topLevelCubes = parsed.outliner.stream()
				.filter(obj -> obj instanceof ElementUUID)
				.collect(Collectors.toList());
		if (!topLevelCubes.isEmpty()) {
			// technically there might already be a model part with the same name, why tho
			GroupParsed newTopLevel = new GroupParsed("bb_main",
					new Vector3f(), null, null,
					true, false, true, 0,
					topLevelCubes);
			topLevelModelParts.add(newTopLevel);
		}
		
		for (GroupParsed modelPart : topLevelModelParts) {
			recursiveVisitChildren(modelPart, elements, new RotatedCubeCounter());
		}
		
		return new BlockbenchGeometry(parsed.resolution, elements, topLevelModelParts);
	}
	
	static void recursiveVisitChildren(GroupParsed parentModelPart, Map<UUID, BlockbenchElement> elementsMap, RotatedCubeCounter rotatedCubeCounter) {
		Collection<BlockbenchObj> replacingObjects = new ArrayList<>();
		List<BlockbenchObj> children = parentModelPart.children;
		Iterator<BlockbenchObj> objIterator = children.iterator();
		while (objIterator.hasNext()) {
			BlockbenchObj child = objIterator.next();
			switch (child) {
				case ElementUUID elementId -> {
					objIterator.remove();
					BlockbenchElement element = elementsMap.get(elementId.uuid);
					if (element == null) continue;
					
					Vector3f rotation = element.rotation();
					if (ParseModEntityModel.Utils.isCubeRotated(rotation)) {
						BlockbenchElement elementNoRotation = element.withZeroRotation();
						BlockbenchObj newChildModelPart = new GroupParsed(
								rotatedCubeCounter.incMakeNewPartName(parentModelPart.name),
								element.origin() != null ? element.origin() : new Vector3f(),
								rotation,
								null /* unused */,
								element.export(),
								false,
								true,
								0,
								Util.make(new ArrayList<>(), list -> list.add(new ElementWrapper(elementNoRotation))));
						replacingObjects.add(newChildModelPart);
					}
					else {
						replacingObjects.add(new ElementWrapper(element));
					}
				}
				case GroupParsed childModelPart -> {
					if (childModelPart.isExported()) {
						recursiveVisitChildren(childModelPart, elementsMap, rotatedCubeCounter);
					}
				}
				default -> {}
			}
		}
		children.addAll(replacingObjects);
	}
	
	
	static MeshDefinition createGeometry(BlockbenchGeometry bbGeometry) {
		MeshDefinition geometry = new MeshDefinition();
		
		for (GroupParsed topLevelPart : bbGeometry.topLevelModelParts) {
			addModelPart(topLevelPart, geometry.getRoot(), null);
		}
		
		return geometry;
	}
	
	static void addModelPart(GroupParsed boneParsed, PartDefinition parent, @Nullable Vector3f parentPivot) {
		float yOffset = 24;
		Vector3f bonePivot = boneParsed.origin != null ? boneParsed.origin : new Vector3f();
		
		float x;
		float y;
		float z;
		if (parentPivot != null) {
			x =   bonePivot.x() - parentPivot.x();
			y = -(bonePivot.y() - parentPivot.y());
			z =   bonePivot.z() - parentPivot.z();
		}
		else {
			x =  bonePivot.x();
			y = -bonePivot.y() + yOffset;
			z =  bonePivot.z();
		}
		x = -x;
		
		float xRot = 0;
		float yRot = 0;
		float zRot = 0;
		if (boneParsed.rotation != null) {
			xRot = -boneParsed.rotation.x() * MathUtil.DEG_TO_RAD;
			yRot = -boneParsed.rotation.y() * MathUtil.DEG_TO_RAD;
			zRot =  boneParsed.rotation.z() * MathUtil.DEG_TO_RAD;
		}
		
		PartPose partPose = PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot);
		
		List<CubeDefinition> cubes = new ArrayList<>();
		
		PartDefinition modelPart = new PartDefinition(cubes, partPose);
		_PartDefinition.addOrReplaceChild(parent, boneParsed.name, modelPart);
		for (BlockbenchObj child : boneParsed.children) {
			switch (child) {
				case GroupParsed childModelPart -> {
					addModelPart(childModelPart, modelPart, bonePivot);
				}
				case ElementWrapper element -> {
					CubeDefinition cube = makeModelBox(element.element, bonePivot);
					if (cube != null) {
						cubes.add(cube);
					}
				}
				default -> {}
			}
		}
	}

	protected static final Set<String> visitedVerticesReused = new HashSet<>();
	static CubeDefinition makeModelBox(BlockbenchElement element, Vector3f parentPivot) {
		return switch (element) {
			case ElementMesh mesh -> {
				Vector3f origin = mesh.origin != null ? mesh.origin : new Vector3f();
				origin.sub(parentPivot);
				Map<String, Vector3f> verticesMap = mesh.vertices;
				Collection<MeshFace> faces = mesh.faces.values();
				
				MeshBuilder meshBuilder = new MeshBuilder(true);
				for (MeshFace face : faces) {
					if (face.vertices.length > 2) {
						visitedVerticesReused.clear();
						for (String vertex : face.vertices) {
							// using an ordered set in case a mesh face uses the same vertex twice for whatever f-ing reason
							visitedVerticesReused.add(vertex);
						}
						VertexDefinition[] vertices = new VertexDefinition[visitedVerticesReused.size()];
						int i = 0;
						for (String vertexId : visitedVerticesReused) {
							float[] uv = face.uv.get(vertexId);
							vertices[i++] = new VertexDefinition(verticesMap.get(vertexId), uv[0], uv[1]);
						}
						if (vertices.length > 3) {
							MeshVerticesHelper.sortVertices(vertices);
						}
						
						MeshFaceBuilder faceBuilder = meshBuilder.startFaceCalcNormal();
						for (VertexDefinition vertex : vertices) {
							faceBuilder.withVertex(
									vertex.pos().x() + origin.x(), 
									vertex.pos().y() + origin.y(), 
									vertex.pos().z() + origin.z(), 
									vertex.uPos(), vertex.vPos());
						}
						faceBuilder.createFace();
					}
				}
				
				yield meshBuilder.buildCube();
			}
			case ElementCube cube -> {
				Vector3f size = new Vector3f( 
						cube.to.x() - cube.from.x(), 
						cube.to.y() - cube.from.y(), 
						cube.to.z() - cube.from.z() 
				);
				
				Vector3f originJ = new Vector3f(
					  -(cube.from.x() - parentPivot.x()) - size.x(),
						-(cube.to.y() - parentPivot.y()),
						  cube.to.z() - parentPivot.z()  - size.z()
				);
				
				Map<Direction, BoxFace> perFaceUV = new EnumMap<>(Direction.class);
				for (Direction direction : Direction.values()) {
					if (cube.faces.containsKey(direction.getName())) {
						perFaceUV.put(direction, cube.faces.get(direction.getName()));
					}
				}
				
				yield new BlockbenchCubeDefinition(
						originJ.x(), originJ.y(), originJ.z(), 
						size.x(), size.y(), size.z(), 
						cube.inflate, perFaceUV);
			}
			default -> throw new IllegalArgumentException();
		};
	}
	
	
	
	static final JsonDeserializer<BlockbenchElement> ELEMENT_DESERIALIZER = new JsonDeserializer<BlockbenchElement>() {
		
		@Override
		public BlockbenchElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if (json.isJsonObject()) {
				JsonObject jsonObj = json.getAsJsonObject();
				if (jsonObj.has("type")) {
					JsonElement typeElem = jsonObj.get("type");
					if (typeElem.isJsonPrimitive()) {
						JsonPrimitive typePrim = typeElem.getAsJsonPrimitive();
						if (typePrim.isString()) {
							String type = typePrim.getAsString();
							try {
								switch (type) {
								case "cube":
									return context.deserialize(json, ElementCube.class);
								case "mesh":
									return context.deserialize(json, ElementMesh.class);
								default:
									throw new JsonParseException("Unknown element type: \"" + type + "\"");
								}
							}
							catch (Exception e) {
								JojoMod.getLogger().error("", e);
								throw e;
							}
						}
					}
				}
			}

			throw new JsonParseException("No model element type present!");
		}
	};
	
	static final JsonDeserializer<BlockbenchObj> BB_OBJ_DESERIALIZER = new JsonDeserializer<BlockbenchObj>() {

		@Override
		public BlockbenchObj deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			UUID uuid = null;
			try {
				uuid = context.deserialize(json, UUID.class);
			}
			catch (JsonParseException e) {}

			BlockbenchObj obj;
			if (uuid != null) {
				ElementUUID uuidChild = new ElementUUID(uuid);
				obj = uuidChild;
			}
			else {
				obj = context.deserialize(json, GroupParsed.class);
			}
			return obj;
		}
	};
	
	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(BlockbenchElement.class, ELEMENT_DESERIALIZER)
			.registerTypeAdapter(BlockbenchObj.class, BB_OBJ_DESERIALIZER)
			.registerTypeAdapter(Vector3f.class, ParseModEntityModel.Utils.VEC_3F_DESERIALIZER)
			.create();
	
}
