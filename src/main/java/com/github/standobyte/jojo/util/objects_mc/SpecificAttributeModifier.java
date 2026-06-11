package com.github.standobyte.jojo.util.objects_mc;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class SpecificAttributeModifier {
	public final Holder<Attribute> attribute;
	public final ResourceLocation id;
	public final AttributeModifier.Operation operation;
	protected AttributeModifier modifier;
	protected double modifierAmount;
	
	public SpecificAttributeModifier(Holder<Attribute> attribute, AttributeModifier constantAmountModifier) {
		this(attribute, constantAmountModifier.id(), constantAmountModifier.operation(), constantAmountModifier);
	}
	
	public static SpecificAttributeModifier variableAmount(Holder<Attribute> attribute, ResourceLocation id, AttributeModifier.Operation operation) {
		return new SpecificAttributeModifier(attribute, id, operation, null);
	}
	
	protected SpecificAttributeModifier(Holder<Attribute> attribute, ResourceLocation id, AttributeModifier.Operation operation, AttributeModifier modifier) {
		this.attribute = attribute;
		this.id = id;
		this.operation = operation;
		this.modifier = modifier;
	}
	
	
	@Nullable
	public AttributeInstance getAttribute(LivingEntity entity) {
		return entity.getAttribute(attribute);
	}

	@Nullable
	public AttributeModifier getApplied(LivingEntity entity) {
		AttributeInstance attribute = getAttribute(entity);
		if (attribute != null) {
			return attribute.getModifier(id);
		}
		return null;
	}


	public SpecificAttributeModifier withAmount(double amount) {
		this.modifierAmount = amount;
		if (modifier == null || modifier.amount() != amount) {
			modifier = new AttributeModifier(id, amount, operation);
		}
		return this;
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
