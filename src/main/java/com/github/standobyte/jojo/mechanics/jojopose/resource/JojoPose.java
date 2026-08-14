package com.github.standobyte.jojo.mechanics.jojopose.resource;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.mechanics.voiceline.ClientVoiceLineDefinition;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.util.functions.JojoModUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class JojoPose {
	public final ResourceLocation animSet;
	public final String animName;
	
	public final @Nonnull RotpAnimDefinition anim;
	public final @Nullable WithStandSummonPose standSummonPose;
	public final @Nullable List<ClientVoiceLineDefinition> voiceLine;
	public final @Nullable Component authors;
	private AnimFramePose pose;
	
	public JojoPose(ResourceLocation animSet, String animName, 
			@Nonnull RotpAnimDefinition anim, 
			@Nullable WithStandSummonPose standSummonPose, 
			@Nullable List<ClientVoiceLineDefinition> voiceLine,
			Optional<String> authors) {
		this.animSet = animSet;
		this.animName = animName;
		this.anim = anim;
		this.standSummonPose = standSummonPose;
		this.voiceLine = voiceLine;
		this.authors = authors.map(authorName -> JojoModUtil.makeAssetCredits(authorName)
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
				.orElse(null);
	}
	
	public AnimFramePose getPose() {
		if (this.pose == null) {
			if (anim.poses != null && !anim.poses.isEmpty()) {
				this.pose = anim.poses.values().iterator().next().pose();
			}
			else {
				AnimFramePose frame = new AnimFramePose();
				anim.calcAnimPose(frame, anim.lengthInSeconds, 1, null, null);
				this.pose = frame;
			}
		}
		return this.pose;
	}
	
	
	public static record WithStandSummonPose(ResourceLocation matchStandId, ActionAnimIdentifier standSummonPose) {
		public static final Codec<WithStandSummonPose> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
						ResourceLocation.CODEC.fieldOf("stand_id").forGetter(WithStandSummonPose::matchStandId),
						ActionAnimIdentifier.NAME_CODEC.fieldOf("anim_name").forGetter(WithStandSummonPose::standSummonPose))
				.apply(builder, WithStandSummonPose::new));
	}
	
}
