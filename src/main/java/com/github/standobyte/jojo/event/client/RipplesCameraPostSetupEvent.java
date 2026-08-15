package com.github.standobyte.jojo.event.client;

import javax.annotation.Nonnull;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;

/**
 * RipplesCameraPostSetupEvent is fired at the end of {@link Camera#setup(BlockGetter, Entity, boolean, boolean, float)}.
 * This event can be used to move the camera position offset.
 * During a player animation in 1st person, the offset corresponds to the player model's head position.
 */
public class RipplesCameraPostSetupEvent extends Event {
	public final Camera camera;
	public final boolean thirdPerson;
	public final boolean thirdPersonReverse;
	private Vec3 cameraOffset;

	public RipplesCameraPostSetupEvent(Camera camera, boolean thirdPerson, boolean thirdPersonReverse, Vec3 cameraOffset) {
		this.camera = camera;
		this.thirdPerson = thirdPerson;
		this.thirdPersonReverse = thirdPersonReverse;
		this.cameraOffset = cameraOffset;
	}

	@Nonnull
	public Vec3 getCameraOffset() {
		return cameraOffset;
	}

	public void setCameraOffset(Vec3 offset) {
		this.cameraOffset = offset != null ? offset : Vec3.ZERO;
	}

}
