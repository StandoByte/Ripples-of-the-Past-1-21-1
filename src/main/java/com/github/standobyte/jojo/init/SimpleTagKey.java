package com.github.standobyte.jojo.init;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

/** 
 * Similar to Minecraft's tag system, but unlike the vanilla code, 
 * doesn't have 100500 safety checks, doesn't require the values to be wrapped in a holder
 * or registered in a registry, and doesn't twist my fucking balls
 */
// TODO load SimpleTagKey from datapack
// see ReloadableServerResources.updateRegistryTags() and TagNetworkSerialization for reference
@SuppressWarnings("unchecked")
public class SimpleTagKey<V> {
	protected SimpleTagType<V> type;
	protected ResourceLocation tagName;
	protected Set<ResourceLocation> hardcodedValues = new HashSet<>();
	protected Set<ResourceLocation> values = new HashSet<>();
	
	protected SimpleTagKey(SimpleTagType<V> type, ResourceLocation tagName) {
		this.type = type;
		this.tagName = tagName;
	}
	
	public static <V> SimpleTagKey<V> create(Class<V> type, ResourceLocation tagName) {
		SimpleTagType<V> tagType = (SimpleTagType<V>) SimpleTagType.TYPES.computeIfAbsent(type, __ -> new SimpleTagType<>());
		if (tagType == null) {
			throw new IllegalStateException("No SimpleTagType for class " + type.getName() + " registered!");
		}
		return tagType.tagKeysCache.computeIfAbsent(tagName, __ -> new SimpleTagKey<>(tagType, tagName));
	}
	
	
	public V add(V value) {
		ResourceLocation id = type.getId.apply(value);
		hardcodedValues.add(id);
		values.add(id);
		return value;
	}
	
	public <H extends Holder<V>> H add(H holder) {
		ResourceLocation id = holder.getKey().location();
		hardcodedValues.add(id);
		values.add(id);
		return holder;
	}
	
	
	public boolean contains(V value) {
		return values.contains(type.getId.apply(value));
	}
	
	public boolean contains(Holder<V> value) {
		return values.contains(value.getKey().location());
	}
	
	public boolean contains(ResourceLocation id) {
		return values.contains(id);
	}
	
	
	
	public static class SimpleTagType<V> {
		protected static Map<Class<?>, SimpleTagType<?>> TYPES = new HashMap<>();
		
		public static <V> void registerType(Class<V> clazz, 
				@Nullable Function<V, ResourceLocation> getId, // No need for getId function if you use holders
				String directory) {
			registerType(clazz, getId, directory, false);
		}
		
		public static <V> void registerType(Class<V> clazz, @Nullable Function<V, ResourceLocation> getId, String directory, boolean overwrite) {
			SimpleTagType<V> existing = (SimpleTagType<V>) TYPES.get(clazz);
			if (existing != null && existing.typeInitialized) {
				JojoMod.getLogger().warn("SimpleTagType for class {} is already registered", clazz.getName());
				if (!overwrite) return;
			}
			
			if (existing == null) {
				existing = new SimpleTagType<>();
			}
			existing.init(getId, directory);
		}
		
		protected boolean typeInitialized = false;
		protected Map<ResourceLocation, SimpleTagKey<V>> tagKeysCache = new HashMap<>();
		
		protected Function<V, ResourceLocation> getId;
		protected String directory;
		
		protected void init(Function<V, ResourceLocation> getId, String directory) {
			this.getId = getId;
			this.directory = directory;
			this.typeInitialized = true;
		}
		
	}
	
}
