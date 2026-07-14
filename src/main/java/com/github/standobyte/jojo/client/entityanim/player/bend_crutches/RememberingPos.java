package com.github.standobyte.jojo.client.entityanim.player.bend_crutches;

import org.joml.Vector3f;

public record RememberingPos(Vector3f mutablePos, float originalX, float originalY, float originalZ) {
	
	public RememberingPos(Vector3f initializedPos) {
		this(initializedPos, initializedPos.x, initializedPos.y, initializedPos.z);
	}
	
	public void reset() {
		this.mutablePos.set(originalX, originalY, originalZ);
	}
	
}
