package com.github.standobyte.jojo.mechanics.jojopose.resource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.mechanics.voiceline.ClientVoiceLineDefinition;
import com.github.standobyte.jojo.util.functions.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public record JojoPoseAnimSet(AnimationSet anims, JojoPoseAnimSetData data) {
	public static final JojoPoseAnimSetData EMPTY_DATA = new JojoPoseAnimSetData(null, null, null);
	
	public static record JojoPoseAnimData(
			@Nullable String standSummonPose,
			@Nullable ClientVoiceLineDefinition voiceLine) {
		// Note to self: when using optionalFieldOf(String name, A defaultValue), defaultValue can't be null
		public static JojoPoseAnimData absoluteDogshit(Optional<String> standSummonPose, Optional<ClientVoiceLineDefinition> voiceLine) { return new JojoPoseAnimData(standSummonPose.orElse(null), voiceLine.orElse(null)); }
		
		public static final Codec<JojoPoseAnimData> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						Codec.STRING.optionalFieldOf("stand_summon_pose").forGetter(data -> Optional.ofNullable(data.standSummonPose)),
						ClientVoiceLineDefinition.CODEC.optionalFieldOf("voice_line").forGetter(data -> Optional.ofNullable(data.voiceLine)))
				.apply(builder, JojoPoseAnimData::absoluteDogshit));
		
	}

	public static record JojoPoseAnimSetData(
			@Nullable ResourceLocation character, 
			@Nullable List<ResourceLocation> storyPart, 
			@Nullable Map<String, JojoPoseAnimData> animSpecificData) {
		public static JojoPoseAnimSetData absoluteDogshit(Optional<ResourceLocation> character, Optional<List<ResourceLocation>> storyPart, Optional<Map<String, JojoPoseAnimData>> animSpecificData) { return new JojoPoseAnimSetData(character.orElse(null), storyPart.orElse(null), animSpecificData.orElse(null)); }

		public static final Codec<JojoPoseAnimSetData> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						ResourceLocation.CODEC.optionalFieldOf("character").forGetter(set -> Optional.ofNullable(set.character)),
						CodecUtil.listOrSingleCodec(ResourceLocation.CODEC).optionalFieldOf("story_part").forGetter(set -> Optional.ofNullable(set.storyPart)),
						Codec.unboundedMap(Codec.STRING, JojoPoseAnimData.CODEC).optionalFieldOf("anim_specific").forGetter(set -> Optional.ofNullable(set.animSpecificData)))
				.apply(builder, JojoPoseAnimSetData::absoluteDogshit));
		
		@Nullable
		public JojoPoseAnimData getAnimSpecificData(String animName) {
			return animSpecificData != null ? animSpecificData.get(animName) : null;
		}
	}
	
}
