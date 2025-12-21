package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StoryCharacter {
	protected final Optional<ResourceLocation> stand;
	private Map<ResourceLocation, Holder<ClothesSet>> clothesInit = new TreeMap<>();
	protected List<Holder<ClothesSet>> clothesOrdered;

	protected Component nameFull;
	protected Component nameShortened;
	
	public StoryCharacter(Optional<ResourceLocation> stand) {
		this.stand = stand;
	}
	
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
	
	
	public static final Codec<StoryCharacter> DIRECT_CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					ResourceLocation.CODEC.optionalFieldOf("stand").forGetter(set -> set.stand))
			.apply(builder, StoryCharacter::new));
	
	public static final Codec<Holder<StoryCharacter>> REG_CODEC = RegistryFixedCodec.create(JojoRegistries.STORY_CHARACTERS_REG_KEY);
	
}