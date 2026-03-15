package com.github.standobyte.jojo.client.entityrender.parsemodel.gecko;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel;
import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel.UnbakedModelGeometry;
import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel.Utils.RotatedCubeCounter;
import com.github.standobyte.jojo.client.entityrender.parsemodel.gecko.GeckoPerFaceCubeDefinition.FaceUV;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.BlockbenchMeshDefinition;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.Util;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

public class GeckoModelFormat {
	
	public static LayerDefinition parseGeckoModel(JsonElement json) {
		JsonElement modelJson = json.getAsJsonObject().get("minecraft:geometry").getAsJsonArray().get(0);
		GeometryParsed geomerty = GSON.fromJson(modelJson, GeometryParsed.class);
		convertRotatedCubes(geomerty);
		MeshDefinition modelUnbaked = createGeometry(geomerty);
		Description texSize = geomerty.description;
		return LayerDefinition.create(modelUnbaked, texSize.texture_width, texSize.texture_height);
	}
	
	
	
	static record GeometryParsed(
			Description description, 
			List<BoneParsed> bones) {}
	
	static record Description(
			int texture_width, 
			int texture_height) {}
	
	static record BoneParsed(
			String name, 
			@Nullable String parent,
			Vector3f pivot,
			@Nullable Vector3f rotation,
			List<CubeParsed> cubes,
			@Nullable Meshy poly_mesh) {}
	
	static record CubeParsed(
			Vector3f origin,
			Vector3f size,
			float inflate,
			Vector3f pivot,
			@Nullable Vector3f rotation,
			boolean mirror,
			CubeUV uv) {}
	
	static interface CubeUV {
		
		static record Box(
				int[] uv) implements CubeUV {}
		
		static record PerFace(
				Map<String, FaceUV> uv) implements CubeUV {}
		
	}
	
	
	static void convertRotatedCubes(GeometryParsed parsed) {
		RotatedCubeCounter rotatedCubeCounter = new RotatedCubeCounter();
		Collection<BoneParsed> addModelParts = new ArrayList<>();
		for (BoneParsed modelPart : parsed.bones) {
			if (modelPart.cubes == null) continue;
			
			Iterator<CubeParsed> cubeIter = modelPart.cubes.iterator();
			while (cubeIter.hasNext()) {
				CubeParsed cube = cubeIter.next();
				Vector3f rotation = cube.rotation;
				if (ParseModEntityModel.Utils.isCubeRotated(rotation)) {
					CubeParsed cubeNoRotation = new CubeParsed(cube.origin, cube.size, 
							cube.inflate, cube.pivot, null, cube.mirror, cube.uv);
					
					BoneParsed newModelPart = new BoneParsed(
							rotatedCubeCounter.incMakeNewPartName(modelPart.name),
							modelPart.name,
							cube.pivot != null ? cube.pivot : new Vector3f(),
							rotation,
							Util.make(new ArrayList<>(), list -> list.add(cubeNoRotation)),
							null);
					addModelParts.add(newModelPart);
					
					cubeIter.remove();
				}
			}
		}
		parsed.bones.addAll(addModelParts);
	}
	
	static MeshDefinition createGeometry(GeometryParsed geckoGeometry) {
		UnbakedModelGeometry geometry = new UnbakedModelGeometry();
		
		Map<String, BoneParsed> bonesNamed = geckoGeometry.bones.stream().collect(
				Collectors.toMap(BoneParsed::name, Function.identity()));
		
		for (BoneParsed bone : geckoGeometry.bones) {
			Vector3f parentPivot = bone.parent != null ? bonesNamed.get(bone.parent).pivot : null;
			PartDefinition modelPart = makeModelPart(bone, parentPivot, geckoGeometry.description);
			geometry.addModelPart(bone.name, modelPart, bone.parent);
		}
		return geometry.getGeometryDefinition();
	}
	
	static PartDefinition makeModelPart(BoneParsed bone, @Nullable Vector3f parentPivot, Description modelDesc) {
		float yOffset = 24;
		Vector3f bonePivot = bone.pivot != null ? bone.pivot : new Vector3f();
		
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

		float xRot = 0;
		float yRot = 0;
		float zRot = 0;
		if (bone.rotation != null) {
			xRot = bone.rotation.x() * MathUtil.DEG_TO_RAD;
			yRot = bone.rotation.y() * MathUtil.DEG_TO_RAD;
			zRot = bone.rotation.z() * MathUtil.DEG_TO_RAD;
		}
		
		PartPose partPose = PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot);
		
		List<CubeDefinition> cubes = new ArrayList<>();
		if (bone.cubes != null) {
			for (CubeParsed cubeParsed : bone.cubes) {
				cubes.add(makeModelBox(cubeParsed, bone));
			}
		}
		
		if (bone.poly_mesh != null) {
			BlockbenchMeshDefinition mesh = bone.poly_mesh.makeMesh(modelDesc, bonePivot);
			if (mesh != null) {
				cubes.add(mesh);
			}
		}
		
		PartDefinition modelPart = new PartDefinition(cubes, partPose);
		return modelPart;
	}
	
	static CubeDefinition makeModelBox(CubeParsed cube, BoneParsed parentBone) {
		Vector3f originJ = new Vector3f(
				  cube.origin.x() - parentBone.pivot.x(),
				-(cube.origin.y() - parentBone.pivot.y()) - cube.size.y(),
				  cube.origin.z() - parentBone.pivot.z()
		);
		
		return switch (cube.uv) {
			case CubeUV.Box boxUV -> new GeckoBoxCubeDefinition(
					boxUV.uv[0], boxUV.uv[1], 
					originJ.x(), originJ.y(), originJ.z(), 
					cube.size.x(), cube.size.y(), cube.size.z(), 
					cube.inflate, cube.mirror);
			case CubeUV.PerFace perFaceUV -> {
				Map<Direction, FaceUV> uvFaces = new EnumMap<>(Direction.class);
				for (Direction direction : Direction.values()) {
					if (perFaceUV.uv.containsKey(direction.getName())) {
						uvFaces.put(direction, perFaceUV.uv.get(direction.getName()));
					}
				}
				yield new GeckoPerFaceCubeDefinition(
						originJ.x(), originJ.y(), originJ.z(), 
						cube.size.x(), cube.size.y(), cube.size.z(), 
						cube.inflate, cube.mirror, uvFaces);
			}
			default -> throw new IllegalArgumentException();
		};
	}
	
	
	
	static final JsonDeserializer<CubeUV> UV_DESERIALIZER = new JsonDeserializer<CubeUV> () {
		@Override
		public CubeUV deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if (json.isJsonArray()) {
				int[] uv = context.deserialize(json, int[].class);
				return new CubeUV.Box(uv);
			}
			else {
				JsonObject wrappingJson = new JsonObject();
				wrappingJson.add("uv", json);
				CubeUV.PerFace uv = context.deserialize(wrappingJson, CubeUV.PerFace.class);
				return uv;
			}
		}
	};
	
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(CubeUV.class, UV_DESERIALIZER)
			.registerTypeAdapter(Vector3f.class, ParseModEntityModel.Utils.VEC_3F_DESERIALIZER)
			.registerTypeAdapter(Vector3i.class, ParseModEntityModel.Utils.VEC_3I_DESERIALIZER)
			.registerTypeAdapter(Vector2f.class, ParseModEntityModel.Utils.VEC_2F_DESERIALIZER)
			.create();
	
	
}
