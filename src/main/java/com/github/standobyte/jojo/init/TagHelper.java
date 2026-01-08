package com.github.standobyte.jojo.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class TagHelper<T> {
	public final String modId;
	protected ResourceKey<Registry<T>> registry;
	protected Map<TagKey<T>, TagDefinition<T>> mapByTag = new HashMap<>();

	public TagHelper(String modId, ResourceKey<Registry<T>> registry) {
		this.modId = modId;
		this.registry = registry;
	}

	@SafeVarargs
	public final ResourceKey<T> withTags(ResourceLocation damageTypeId, TagKey<T>... tags) {
		ResourceKey<T> resourceKey = ResourceKey.create(registry, damageTypeId);
		for (TagKey<T> tagKey : tags) {
			TagDefinition<T> tag = mapByTag.computeIfAbsent(tagKey, __ -> new TagDefinition<>());
			tag.values.add(resourceKey);
		}
		return resourceKey;
	}

	public void addToTag(TagKey<T> tagKey, ResourceLocation externalDamageType, boolean required) {
		TagDefinition<T> tag = mapByTag.computeIfAbsent(tagKey, __ -> new TagDefinition<>());
		if (required)	tag.values.add(ResourceKey.create(registry, externalDamageType));
		else			tag.optionalValues.add(externalDamageType);
	}

	public TagsProvider<T> makeTagsDatagenProvider(GatherDataEvent event) {
		TagsProvider<T> tagsProvider = new TagsProvider<T>(
				event.getGenerator().getPackOutput(), 
				registry,
				event.getLookupProvider(), 
				this.modId, 
				event.getExistingFileHelper()) {

			@Override
			protected void addTags(HolderLookup.Provider lookupProvider) {
				for (Map.Entry<TagKey<T>, TagDefinition<T>> tagEntry : mapByTag.entrySet()) {
					TagKey<T> tagKey = tagEntry.getKey();
					TagDefinition<T> tag = tagEntry.getValue();

					TagsProvider.TagAppender<T> builder = tag(tagKey).replace(false);
					builder.addAll(tag.values);
					for (ResourceLocation value : tag.optionalValues) {
						builder.addOptional(value);
					}
				}
			}
		};
		return tagsProvider;
	}

	public static class TagDefinition<T> {
		public List<ResourceKey<T>> values = new ArrayList<>();
		public List<ResourceLocation> optionalValues = new ArrayList<>();
	}
}
