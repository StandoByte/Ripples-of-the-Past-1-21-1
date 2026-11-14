package com.github.standobyte.jojo.config.stand;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

public class StandTypeClass<T extends StandType> {
	@ApiStatus.Internal
	public final Class<T> _standTypeJavaClass;
	protected final String alias;
	protected final StandTypeFactory<T> constructor;

	@ApiStatus.Internal
	protected StandTypeClass(Class<T> standTypeJavaClass, String alias, StandTypeFactory<T> constructor) {
		this._standTypeJavaClass = standTypeJavaClass;
		this.alias = alias;
		this.constructor = constructor;
	}
	
	public static <T extends StandType> StandTypeClass<T> registerStandClass(Class<T> standTypeJavaClass, String alias, StandTypeFactory<T> constructor) {
		StandTypeClass<T> standClass = new StandTypeClass<>(standTypeJavaClass, alias, constructor);
		BY_NAME.put(alias, standClass);
		BY_CLASS.put(standTypeJavaClass, standClass);
		return standClass;
	}
	
	protected static final Map<String, StandTypeClass<?>> BY_NAME = new HashMap<>();
	protected static final Map<Class<?>, StandTypeClass<?>> BY_CLASS = new HashMap<>();
	
	@ApiStatus.Internal
	public static final StandTypeClass<StandType> _DEFAULT_CLASS = registerStandClass(StandType.class, "", StandType::new);
	
	public static void addClassToJson(JsonObject json, Class<? extends StandType> standTypeJavaClass) {
		if (standTypeJavaClass != StandType.class) {
			StandTypeClass<?> standClass = _byClass(standTypeJavaClass);
			json.addProperty("standClassUnconfigurable", standClass.alias);
		}
	}
	
	@ApiStatus.Internal
	public static String _getStandClassAlias(JsonObject json) {
		JsonElement jsonElement = json.get("standClassUnconfigurable");
		return jsonElement != null ? jsonElement.getAsString() : null;
	}

	@ApiStatus.Internal
	public static <T extends StandType> StandTypeClass<?> _byName(String name) {
		if (name == null) {
			return _DEFAULT_CLASS;
		}
		StandTypeClass<?> standClass = BY_NAME.get(name);
		if (standClass == null) {
			try {
				@SuppressWarnings("unchecked")
				Class<T> standTypeJavaClass = (Class<T>) Class.forName(name);
				Constructor<T> c = standTypeJavaClass.getConstructor(StandStats.class, MovesetBuilder.class, ResourceLocation.class);
				StandTypeFactory<T> constructor = new StandTypeFactory.FromConstructor<>(c);
				standClass = registerStandClass(standTypeJavaClass, name, constructor);
			}
			catch (Exception e) {
				return null;
			}
		}
		return standClass;
	}

	@ApiStatus.Internal
	public static <T extends StandType> StandTypeClass<?> _byClass(Class<T> standTypeClass) {
		if (standTypeClass == null || standTypeClass == StandType.class) {
			return _DEFAULT_CLASS;
		}
		StandTypeClass<?> standClass = BY_CLASS.get(standTypeClass);
		if (standClass == null) {
			try {
				String className = standTypeClass.getName();
				Constructor<T> c = standTypeClass.getConstructor(StandStats.class, MovesetBuilder.class, ResourceLocation.class);
				StandTypeFactory<T> constructor = new StandTypeFactory.FromConstructor<>(c);
				standClass = registerStandClass(standTypeClass, className, constructor);
			}
			catch (Exception e) {
				return null;
			}
		}
		return standClass;
	}
	
	public T createStand(StandStats stats, MovesetBuilder moveset, ResourceLocation id) {
		return constructor.create(stats, moveset, id);
	}
	
	@FunctionalInterface
	public static interface StandTypeFactory<T extends StandType> {
		T create(StandStats stats, MovesetBuilder moveset, ResourceLocation id);
		
		@ApiStatus.Internal
		public static class FromConstructor<T extends StandType> implements StandTypeFactory<T> {
			public final Constructor<T> c;

			public FromConstructor(Constructor<T> c) {
				this.c = c;
			}
			
			@Override
			public T create(StandStats stats, MovesetBuilder moveset, ResourceLocation id) {
				try {
					return c.newInstance(stats, moveset, id);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					JojoMod.getLogger().error("Invalid " + c.getDeclaringClass().getName() + " constructor", e);
					throw new RuntimeException(e);
				}
			}
		}
	}

}
