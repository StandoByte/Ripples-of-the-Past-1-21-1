package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.util.functions.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StoryCharacter {
	protected final List<ResourceLocation> stands;
	private Map<ResourceLocation, Holder<ClothesSet>> clothesInit = new TreeMap<>();
	protected List<Holder<ClothesSet>> clothesOrdered;

	protected Component nameFull;
	protected Component nameShortened;
	
	public StoryCharacter(Optional<List<ResourceLocation>> stands) {
		this(stands.orElseGet(Collections::emptyList));
	}
	
	private StoryCharacter(List<ResourceLocation> stands) {
		this.stands = stands;
	}
	
	public StoryCharacter merge(StoryCharacter other) {
		if (other.stands.isEmpty()) return this;
		if (this.stands.isEmpty())	return other;
		
		List<ResourceLocation> allStandIds = new ArrayList<>(this.stands.size() + other.stands.size());
		allStandIds.addAll(this.stands);
		allStandIds.addAll(other.stands);
		return new StoryCharacter(allStandIds);
	}
	
	public List<ResourceLocation> getStandIds() {
		return stands;
	}
	
	private Optional<List<ResourceLocation>> getStandIdsOptional() {
		return !stands.isEmpty() ? Optional.of(stands) : Optional.empty();
	}
	
	public static final Codec<StoryCharacter> DIRECT_CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					CodecUtil.listOrSingleCodec(ResourceLocation.CODEC).optionalFieldOf("stand").forGetter(StoryCharacter::getStandIdsOptional))
			.apply(builder, StoryCharacter::new));
	
	
	public void initName(ResourceKey<StoryCharacter> key) {
		ResourceLocation id = key.location();
		String tlKey = Util.makeDescriptionId("character", id);
		this.nameFull = Component.translatable(tlKey);
		this.nameShortened = Component.translatable(tlKey + ".short");
	}
	
	protected void addClothesSet(ResourceLocation key, Holder<ClothesSet> clothesSet) {
		clothesInit.put(key, clothesSet);
		clothesOrdered = null;
	}
	

	public Component getName(boolean shortened) {
		return shortened ? nameShortened : nameFull;
	}
	
	public List<Holder<ClothesSet>> getClothesSets() {
		if (clothesOrdered == null) {
			clothesOrdered = clothesInit.values().stream().toList();
		}
		return clothesOrdered;
	}
	
	
	public static final Codec<Holder<StoryCharacter>> REG_CODEC = RegistryFixedCodec.create(JojoRegistries.STORY_CHARACTERS_REG_KEY);
	
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<StoryCharacter>> STREAM_CODEC = 
			ByteBufCodecs.holderRegistry(JojoRegistries.STORY_CHARACTERS_REG_KEY);
}