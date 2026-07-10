package com.github.standobyte.jojo.mechanics.jojopose.resource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public record JojoPoseAnimSet(AnimationSet anims, JojoPoseAnimSetData data) {
	public static final JojoPoseAnimSetData EMPTY_DATA = new JojoPoseAnimSetData(Optional.empty(), Optional.empty(), Optional.empty());

	public static record JojoPoseAnimSetData(
			Optional<ResourceLocation> character, 
			Optional<List<ResourceLocation>> storyPart, 
			Optional<Map<String, String>> standSummonPoses) {

		public static final Codec<JojoPoseAnimSetData> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						ResourceLocation.CODEC.optionalFieldOf("character").forGetter(set -> set.character),
						Codec.list(ResourceLocation.CODEC).optionalFieldOf("story_part").forGetter(set -> set.storyPart),
						Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("stand_summon_poses").forGetter(set -> set.standSummonPoses))
				.apply(builder, JojoPoseAnimSetData::new));
	}
}
