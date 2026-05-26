package com.github.standobyte.jojo.client.entityanim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;

public class AnimVariantsList {
	public final List<RotpAnimDefinition> anims;
	@Nullable public Map<String, AnimFramePose> poses;

	public AnimVariantsList(List<RotpAnimDefinition> anims) {
		if (anims == null || anims.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.anims = anims;
		
		for (RotpAnimDefinition anim : anims) {
			if (anim.coolPoses != null && !anim.coolPoses.isEmpty()) {
				if (poses == null) {
					poses = new HashMap<>();
				}
				poses.putAll(anim.coolPoses);
			}
		}
	}
	
	public RotpAnimDefinition get(int i) {
		return anims.get(i % anims.size());
	}
	
}
