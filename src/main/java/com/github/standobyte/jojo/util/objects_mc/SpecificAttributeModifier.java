package com.github.standobyte.jojo.util.objects_mc;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record SpecificAttributeModifier(Holder<Attribute> attribute, AttributeModifier modifier) {

	@Nullable
	public AttributeInstance getAttribute(LivingEntity entity) {
		return entity.getAttribute(attribute);
	}

	@Nullable
	public AttributeModifier getApplied(LivingEntity entity) {
		AttributeInstance attribute = getAttribute(entity);
		if (attribute != null) {
			return attribute.getModifier(modifier.id());
		}
		return null;
	}

	public void addOrUpdateTransient(LivingEntity entity) {
		AttributeInstance attribute = getAttribute(entity);
		if (attribute != null) {
			attribute.addOrUpdateTransientModifier(modifier);
		}
	}

	public void addTransient(LivingEntity entity) {
		AttributeInstance attribute = getAttribute(entity);
		if (attribute != null) {
			attribute.addTransientModifier(modifier);
		}
	}

	public void addOrReplacePermanent(LivingEntity entity) {
		AttributeInstance attribute = getAttribute(entity);
		if (attribute != null) {
			attribute.addOrReplacePermanentModifier(modifier);
		}
	}

	public void addPermanent(LivingEntity entity) {
		AttributeInstance attribute = getAttribute(entity);
		if (attribute != null) {
			attribute.addPermanentModifier(modifier);
		}
	}

	public void remove(LivingEntity entity) {
		AttributeInstance attribute = getAttribute(entity);
		if (attribute != null) {
			attribute.removeModifier(modifier);
		}
	}

}
