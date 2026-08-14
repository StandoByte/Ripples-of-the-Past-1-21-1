package com.github.standobyte.jojo.mechanics.jojopose.resource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

public class JojoPoseAnimSet2 {
	public final Map<String, JojoPose> anims;
	public final @Nullable ResourceLocation character;
	public final @Nullable List<ResourceLocation> storyPart;

	public JojoPoseAnimSet2(@Nullable ResourceLocation character, @Nullable List<ResourceLocation> storyPart) {
		this.anims = new LinkedHashMap<>();
		this.character = character;
		this.storyPart = storyPart;
	}
	
	@Nullable
	public JojoPose getPose(String animName) {
		return anims.get(animName);
	}
	
}
