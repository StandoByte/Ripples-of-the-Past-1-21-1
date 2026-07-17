package com.github.standobyte.jojo.mechanics.voiceline;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

public record ClientVoiceLineWithFilters(
		ClientVoiceLineDefinition sounds, 
		ResourceLocation storyCharacter, 
		@Nullable List<ResourceLocation> storyPartsFilter, 
		@Nullable List<ResourceLocation> standTypesFilter) {}
