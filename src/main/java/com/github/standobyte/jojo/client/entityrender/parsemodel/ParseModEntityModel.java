package com.github.standobyte.jojo.client.entityrender.parsemodel;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import com.github.standobyte.jojo.client.entityrender.parsemodel.gecko.GeckoModelFormat;
import com.github.standobyte.jojo.client.entityrender.parsemodel.generic.GenericModelFormat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.v1_21_4_stuff.missingmethods._PartDefinition;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MaterialDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ParseModEntityModel {
	
	public static enum ModelFormat {
		GECKO,
		GENERIC
	}
	
	public static LayerDefinition parse(JsonElement json, ModelFormat format) {
		return switch (format) {
			case GECKO -> GeckoModelFormat.parseGeckoModel(json);
			case GENERIC -> GenericModelFormat.parseGenericModel(json);
		};
	}

	
	public static LayerDefinition merge(LayerDefinition dest, LayerDefinition src) {
		MaterialDefinition destTex = dest.material;
		MaterialDefinition srcTex = src.material;
		if (destTex.xTexSize != srcTex.xTexSize || destTex.yTexSize != srcTex.yTexSize) {
			JojoMod.getLogger().warn("Trying to merge two model definitions with different texture sizes ({}x{} and {}x{}). You probably do not want that.", 
					destTex.xTexSize, destTex.yTexSize, srcTex.xTexSize, srcTex.yTexSize);
		}
		mergeModelParts(dest.mesh.getRoot(), src.mesh.getRoot());
		return dest;
	}
	
	protected static void mergeModelParts(PartDefinition dest, PartDefinition src) {
		dest.cubes.addAll(src.cubes);
		for (var srcChildEntry : src.children.entrySet()) {
			String modelPartName = srcChildEntry.getKey();
			PartDefinition destChild = dest.getChild(modelPartName);
			PartDefinition srcChild = srcChildEntry.getValue();
			if (destChild != null) {
				mergeModelParts(destChild, srcChild);
			}
			else {
				_PartDefinition.addOrReplaceChild(dest, modelPartName, srcChild);
			}
		}
	}
	
	
	public static class UnbakedModelGeometry {
		private final MeshDefinition vanillaGeomDefinition = new MeshDefinition();
		private final Map<String, PartDefinition> allModelParts = new HashMap<>();
		
		public UnbakedModelGeometry() {}
		
		private final Map<PartDefinition, String> orphanage = new HashMap<>();
		public void addModelPart(String name, PartDefinition modelPart, @Nullable String parentName) {
			allModelParts.put(name, modelPart);
			if (parentName == null) {
				_PartDefinition.addOrReplaceChild(vanillaGeomDefinition.getRoot(), name, modelPart);
			}
			else {
				if (parentName.equals(name)) throw new IllegalArgumentException();
				
				PartDefinition parent = allModelParts.get(parentName);
				if (parent != null) {
					_PartDefinition.addOrReplaceChild(parent, name, modelPart);
				}
				else {
					orphanage.put(modelPart, parentName);
				}
			}
			
			if (!orphanage.isEmpty()) {
				Iterator<Map.Entry<PartDefinition, String>> orphanIter = orphanage.entrySet().iterator();
				while (orphanIter.hasNext()) {
					Map.Entry<PartDefinition, String> orphan = orphanIter.next();
					if (orphan.getValue().equals(name)) {
						_PartDefinition.addOrReplaceChild(modelPart, orphan.getValue(), orphan.getKey());
						orphanIter.remove();
					}
				}
			}
		}
		
		public Map<String, PartDefinition> getNamedModelParts() {
			return allModelParts;
		}
		
		public MeshDefinition getGeometryDefinition() {
			return vanillaGeomDefinition;
		}
	}
	
	public static class Utils {
		
		/* 
		 * doesn't handle an edgecase where the model might already have model parts ending with "_r" + some number, 
		 * but why would it be the case in the first place, just rename the f-ing model parts then
		 */ 
		public static class RotatedCubeCounter {
			private final Object2IntMap<String> rotatedCubesConvertedCount = new Object2IntArrayMap<>();
			
			public String incMakeNewPartName(String modelPartName) {
				int number = rotatedCubesConvertedCount.computeInt(modelPartName, (__, prev) -> prev == null ? 1 : prev + 1);
				return modelPartName + "_r" + number;
			}
		}
		
		public static boolean isCubeRotated(Vector3f rotation) {
			return rotation != null && (rotation.x() != 0 || rotation.y() != 0 || rotation.z() != 0);
		}
		
		public static final JsonDeserializer<Vector3f> VEC_3F_DESERIALIZER = new JsonDeserializer<Vector3f> () {
			@Override
			public Vector3f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				JsonArray array = json.getAsJsonArray();
				return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
			}
		};
		
		public static final JsonDeserializer<Vector3i> VEC_3I_DESERIALIZER = new JsonDeserializer<Vector3i> () {
			@Override
			public Vector3i deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				JsonArray array = json.getAsJsonArray();
				return new Vector3i(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
			}
		};
		
		public static final JsonDeserializer<Vector2f> VEC_2F_DESERIALIZER = new JsonDeserializer<Vector2f> () {
			@Override
			public Vector2f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				JsonArray array = json.getAsJsonArray();
				return new Vector2f(array.get(0).getAsFloat(), array.get(1).getAsFloat());
			}
		};
		
	}
	
}
