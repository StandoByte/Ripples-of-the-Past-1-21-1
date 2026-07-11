package com.github.standobyte.jojo.mechanics.voiceline;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record ClientVoiceLineDefinition(
		List<ResourceLocation> sounds,
		@Nullable Component subtitle) {}
