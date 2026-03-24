package com.github.standobyte.jojo.util.functions;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.objects_java.OptionalFloat;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Strictness;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class JSONUtil {
	public static final Gson PLAIN_GSON = new Gson();
	public static final Gson GSON = new GsonBuilder().create();

	public static JsonObject parse(String pJson, Strictness strictness) {
		return parse(new StringReader(pJson), strictness);
	}

	public static JsonObject parse(Reader pReader, Strictness strictness) {
		return fromJson(GSON, pReader, JsonObject.class, strictness);
	}

	public static JsonObject parse(String pJson) {
		return parse(pJson, Strictness.STRICT);
	}

	public static JsonObject parse(Reader pReader) {
		return parse(pReader, Strictness.STRICT);
	}

	@Nullable
	public static <T> T fromJson(Gson pGson, Reader pReader, Class<T> pAdapter, Strictness strictness) {
		try {
			JsonReader jsonreader = new JsonReader(pReader);
			if (strictness == Strictness.LENIENT) {
				jsonreader.setLenient(true);
			}
//			jsonreader.setStrictness(strictness);
			return pGson.getAdapter(pAdapter).read(jsonreader);
		} catch (IOException ioexception) {
			throw new JsonParseException(ioexception);
		}
	}
	
	
	public static void mergeWithObjMember(JsonObject json, String objMemberKey, JsonElement overwriting) {
		if (overwriting.isJsonObject()) {
			JsonObject member = json.getAsJsonObject(objMemberKey);
			if (member != null) {
				merge(member, overwriting.getAsJsonObject());
				return;
			}
		}
		json.add(objMemberKey, overwriting.deepCopy());
	}
	
	public static void merge(JsonObject existing, JsonObject overwriting) {
		for (var entry : overwriting.entrySet()) {
			mergeWithObjMember(existing, entry.getKey(), entry.getValue());
		}
	}
	
	
	public static <J extends JsonElement, T> List<T> parseArrayOrSingleElement(JsonElement element, Function<J, T> parse) {
		if (element == null) return null;
		
		List<T> list;
		if (element.isJsonArray()) {
			JsonArray jsonArray = element.getAsJsonArray();
			list = new ArrayList<>(jsonArray.size());
			for (JsonElement arrayElement : jsonArray) {
				list.add(parse.apply((J) arrayElement));
			}
		}
		else {
			T object = parse.apply((J) element);
			list = Collections.singletonList(object);
		}
		return list;
	}
	
	
	public static final TypeAdapter<ResourceLocation> RES_LOC_ADAPTER =
			new TypeAdapter<ResourceLocation>() {
		@Override
		public ResourceLocation read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}
			String nextString = in.nextString();
			return nextString.equals("null") ? null : ResourceLocation.parse(nextString);
		}

		@Override
		public void write(JsonWriter out, ResourceLocation value) throws IOException {
			out.value(value == null ? null : value.toString());
		}
	};
	
	
	public static interface JsonAdapter<V> extends JsonSerializer<V>, JsonDeserializer<V> {}

	// TODO log the exception too (unregistered ability types when reading a moveset)
	public static <V> JsonAdapter<V> codecToGsonAdapter(Codec<V> codec, boolean partial) {
		return new JsonAdapter<>() {

			@Override
			public JsonElement serialize(V src, Type typeOfSrc, JsonSerializationContext context) {
				DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, src);
				return partial ? result.getPartialOrThrow() : result.getOrThrow();
			}

			@Override
			public V deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				DataResult<Pair<V, JsonElement>> result = codec.decode(JsonOps.INSTANCE, json);
				return (partial ? result.getPartialOrThrow(JsonParseException::new) : result.getOrThrow(JsonParseException::new)).getFirst();
			}
			
		};
	}
	
	
	public static <V> JsonAdapter<V> registryValueAdapter(Supplier<Registry<V>> registrySupplier, String typeName) {
		return new JsonAdapter<>() {

			@Override
			public JsonElement serialize(V src, Type typeOfSrc, JsonSerializationContext context) {
				ResourceLocation id = registrySupplier.get().getKey(src);
				return context.serialize(id);
			}

			@Override
			public V deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				ResourceLocation id = context.deserialize(json, ResourceLocation.class);
				Registry<V> registry = registrySupplier.get();
				if (!registry.containsKey(id)) throw new JsonParseException("Unknown " + typeName + " : " + id.toString());
				V value = registry.get(id);
				return value;
			}
			
		};
	}
	
	public static <V> JsonAdapter<V> registryValueAdapter(Registry<V> registry, String typeName) {
		return registryValueAdapter(() -> registry, typeName);
	}
	
	public static <V> JsonAdapter<V> registryValueAdapter(RegistryAccess registries, ResourceKey<Registry<V>> registryKey, String typeName) {
		return registryValueAdapter(() -> registries.holderOrThrow(registryKey).value(), typeName);
	}


	public static float getFloatOr(String key, JsonObject json, float defaultValue) {
		JsonElement jsonelement = json.get(key);
		if (jsonelement != null) {
			return jsonelement.isJsonNull() ? defaultValue : jsonelement.getAsFloat();
		} else {
			return defaultValue;
		}
	}

	public static OptionalFloat getFloatOptional(String key, JsonObject json) {
		JsonElement jsonelement = json.get(key);
		if (jsonelement != null) {
			return jsonelement.isJsonNull() ? OptionalFloat.empty() : OptionalFloat.of(jsonelement.getAsFloat());
		} else {
			return OptionalFloat.empty();
		}
	}


	public static int parseColor(JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive()) {
			JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
			if (primitive.isString()) {
				String str = primitive.getAsString();
				try {
					int numeric = Integer.decode(str);
					return numeric & 0xFFFFFF;
				}
				catch (NumberFormatException e) {}

				return TextColor.parseColor(str).getOrThrow().getValue();
			}
		}
		
		else if (jsonElement.isJsonArray()) {
			JsonArray array = jsonElement.getAsJsonArray();
			if (array.size() == 3) {
				int r = array.get(0).getAsInt() & 0xFF;
				int g = array.get(1).getAsInt() & 0xFF;
				int b = array.get(2).getAsInt() & 0xFF;
				return (r << 16) | (g << 8) | b;
			}
		}
		
		throw new JsonParseException("Couldn't parse color");
	}
}
