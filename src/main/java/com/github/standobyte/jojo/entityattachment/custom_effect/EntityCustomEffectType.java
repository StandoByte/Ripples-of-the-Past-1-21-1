package com.github.standobyte.jojo.entityattachment.custom_effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class EntityCustomEffectType<T extends EntityCustomEffect> {
	public final ResourceLocation registryKey;
	protected IFactory<T> factory;

	public EntityCustomEffectType(ResourceLocation registryKey, IFactory<T> factory) {
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



	public interface IFactory<T extends EntityCustomEffect> {
		T create(EntityCustomEffectType<T> effect);
	}
}
