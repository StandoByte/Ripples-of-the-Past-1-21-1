package com.github.standobyte.jojo.powersystem.entityaction;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.LivingEntity;

@ApiStatus.Internal
public class EntityActionInputState implements TickingEntityData {
	public final LivingEntity user;
	public final ActionInputBuffer inputBuffer = new ActionInputBuffer();

	public EntityActionInputState(LivingEntity entity) {
		this.user = entity;
		addTicking(entity);
	}

	@Override
	public void tick() {
		inputBuffer.tickInputBuffer(this);
	}

	// TODO (entity action 2) if the player logs out and the action gets saved in NBT, after relog they won't be able to stop the action - fix that
	@ApiStatus.Internal
	public final Int2ObjectMap<HeldInputEntry> heldKeys = new Int2ObjectArrayMap<>();

	public static class HeldInputEntry {
		public final short keyId;
		@Nullable public HeldInput action;

		public HeldInputEntry(short keyId, HeldInput action) {
			this.keyId = keyId;
			this.action = action;
		}
	}

}
