package com.github.standobyte.jojo.mechanics.standarrow;

import java.util.Collection;
import java.util.Optional;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;

public record StandArrowLore(
		boolean awakenedAStand, 
		Optional<Component> firstCharacterName,
		Optional<ResourceKey<Structure>> foundAtStructure) {
	public static final Codec<StandArrowLore> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					Codec.BOOL.optionalFieldOf("used", Boolean.valueOf(false)).forGetter(StandArrowLore::awakenedAStand),
					ComponentSerialization.CODEC.optionalFieldOf("character").forGetter(StandArrowLore::firstCharacterName),
					ResourceKey.codec(Registries.STRUCTURE).optionalFieldOf("structure").forGetter(StandArrowLore::foundAtStructure))
			.apply(builder, StandArrowLore::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, StandArrowLore> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, StandArrowLore::awakenedAStand,
			ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs::optional), StandArrowLore::firstCharacterName,
			ResourceKey.streamCodec(Registries.STRUCTURE).apply(ByteBufCodecs::optional), StandArrowLore::foundAtStructure,
			StandArrowLore::new);
	
	
	
	
	public static StandArrowLore empty() {
		return new StandArrowLore(false, Optional.empty(), Optional.empty());
	}
	
	public StandArrowLore withAwakenedAStand() {
		return awakenedAStand ? this : new StandArrowLore(true, firstCharacterName, foundAtStructure);
	}
	
	public StandArrowLore withFirstCharacterName(Optional<Component> firstCharacterName) {
		return new StandArrowLore(this.awakenedAStand, firstCharacterName, this.foundAtStructure);
	}
	
	public StandArrowLore withStructure(Optional<ResourceKey<Structure>> foundAtStructure) {
		return new StandArrowLore(this.awakenedAStand, this.firstCharacterName, foundAtStructure);
	}
	
	public static void onArrowFoundInSusBlock(ItemStack item, Entity player, ServerLevel level, BlockPos blockPos) {
		StandArrowLore lore = StandArrowLore.empty().withFirstCharacterName(Optional.of(player.getName()));
		
		StructureManager structureManager = level.structureManager();
		Collection<Structure> structures = structureManager.getAllStructuresAt(blockPos).keySet();
		if (!structures.isEmpty()) {
			Structure structure = structures.stream().filter(
					str -> structureManager.getStructureWithPieceAt(blockPos, str).isValid())
					.findAny().orElse(null);
			if (structure != null) {
				Registry<Structure> structuresRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
				Optional<ResourceKey<Structure>> structureKey = structuresRegistry.getResourceKey(structure);
				if (structureKey.isPresent()) {
					lore = lore.withStructure(structureKey);
				}
			}
		}
		
		item.set(ModItemDataComponents.ARROW_LORE, lore);
	}
	
	public static void onArrowCrafted(ItemStack item, Entity player) {
		if (player != null) {
			StandArrowLore lore = StandArrowLore.empty().withFirstCharacterName(Optional.of(player.getName()));
			item.set(ModItemDataComponents.ARROW_LORE, lore);
		}
	}

	public static void onStandGiven(ItemStack arrowItem) {
		StandArrowLore prevLore = arrowItem.get(ModItemDataComponents.ARROW_LORE);
		StandArrowLore newLore = null;
		if (prevLore == null) {
			newLore = empty();
		}
		else {
			newLore = prevLore.withAwakenedAStand();
		}
		
		if (newLore != prevLore) {
			arrowItem.set(ModItemDataComponents.ARROW_LORE, newLore);
		}
	}
	
}
