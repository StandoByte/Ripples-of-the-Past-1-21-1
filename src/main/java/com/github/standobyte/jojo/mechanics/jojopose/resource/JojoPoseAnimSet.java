package com.github.standobyte.jojo.mechanics.jojopose.resource;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.util.functions.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public record JojoPoseAnimSet(AnimationSet anims, JojoPoseAnimSetData data) {
	public static final JojoPoseAnimSetData EMPTY_DATA = new JojoPoseAnimSetData(null, null, null);

	public static record JojoPoseAnimSetData(
			@Nullable ResourceLocation character, 
			@Nullable List<ResourceLocation> storyPart, 
			@Nullable Map<String, JojoPoseAnimData> animSpecificData) {

		public static final Codec<JojoPoseAnimSetData> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						ResourceLocation.CODEC.optionalFieldOf("character", null).forGetter(set -> set.character),
						CodecUtil.listOrSingleCodec(ResourceLocation.CODEC).optionalFieldOf("story_part", null).forGetter(set -> set.storyPart),
						Codec.unboundedMap(Codec.STRING, JojoPoseAnimData.CODEC).optionalFieldOf("anim_specific", null).forGetter(set -> set.animSpecificData))
				.apply(builder, JojoPoseAnimSetData::new));
	}
	
	public static record JojoPoseAnimData(
			@Nullable String standSummonPose) {
		
		public static final Codec<JojoPoseAnimData> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						Codec.STRING.optionalFieldOf("stand_summon_pose", null).forGetter(pose -> pose.standSummonPose))
				.apply(builder, JojoPoseAnimData::new));
		
	}
	
}
