package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mechanics.clothes.sewing.client.SewingMachineScreen;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class ClothesSet {
	protected final Holder<StoryCharacter> character;
	protected final Optional<Holder<StoryPart>> storyPart;
	protected final Map<ClothesSlotType, ClothesPiece> pieces;
	
	protected Component name;
	
	public ClothesSet(Holder<StoryCharacter> character, Optional<Holder<StoryPart>> storyPart, Map<ClothesSlotType, ClothesPiece> clothesPieces) {
		this.character = character;
		this.storyPart = storyPart;
		this.pieces = clothesPieces;
	}
	
	public void initName(ResourceKey<ClothesSet> key) {
		ResourceLocation id = key.location();
		String tlKey = Util.makeDescriptionId("clothes", id);
		this.name = Component.translatable(tlKey);
	}
	
	public Holder<StoryCharacter> getCharacter() {
		return character;
	}
	
	public Optional<Holder<StoryPart>> getStoryPart() {
		return storyPart;
	}
	
	@Nullable
	public ClothesPiece getPiece(ClothesSlotType slot) { 
		return pieces.get(slot);
	}

	public Component getName() {
		return name;
	}
	
	
	public static void onBake(Registry<ClothesSet> registry) {
		registry.holders().forEach(holder -> {
			ClothesSet clothes = holder.value();
			ResourceLocation key = holder.getKey().location();
			clothes.character.value().addClothesSet(key, holder);
		});
		if (FMLEnvironment.dist == Dist.CLIENT) {
			SewingMachineScreen.onReload();
		}
	}
	
	
	public static final Codec<ClothesSet> DIRECT_CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					StoryCharacter.REG_CODEC.fieldOf("character").forGetter(set -> set.character),
					StoryPart.REG_CODEC.optionalFieldOf("story_part").forGetter(set -> set.storyPart),
					Codec.unboundedMap(ClothesSlotType.CODEC, ClothesPiece.CODEC).fieldOf("pieces").forGetter(set -> set.pieces))
			.apply(builder, ClothesSet::new));

	public static final Codec<Holder<ClothesSet>> REG_CODEC = RegistryFixedCodec.create(JojoRegistries.CLOTHES_SETS_REG_KEY);
	
}