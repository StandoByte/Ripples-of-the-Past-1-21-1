package com.github.standobyte.core_subsystems.entitydata;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class EntityAttachmentType<T extends TickingEntityAttachment> {
	public final ResourceLocation registryKey;
	protected IFactory<T> factory;

	public EntityAttachmentType(ResourceLocation registryKey, IFactory<T> factory) {
		this.registryKey = registryKey;
		this.factory = factory;
	}

	@Deprecated
	public T create() {
		return create(null);
	}

	public T create(Level level) {
		T effect = factory.create(this);
		effect.level = level;
		return effect;
	}



	public interface IFactory<T extends TickingEntityAttachment> {
		T create(EntityAttachmentType<T> effect);
	}
}
