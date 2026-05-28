package com.github.standobyte.jojo.client.entityanim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.SavedPose;

public class AnimVariantsList {
	public final List<RotpAnimDefinition> anims;
	@Nullable public Map<String, SavedPose> poses;

	public AnimVariantsList(List<RotpAnimDefinition> anims) {
		if (anims == null || anims.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.anims = anims;
		
		for (RotpAnimDefinition anim : anims) {
			if (anim.poses != null && !anim.poses.isEmpty()) {
				if (poses == null) {
					poses = new HashMap<>();
				}
				poses.putAll(anim.poses);
			}
		}
	}
	
	public RotpAnimDefinition get(int i) {
		return anims.get(i % anims.size());
	}
	
}
