package com.github.standobyte.jojo.util.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class AttributeUtil {

	public static double getValueOrDefault(LivingEntity entity, Holder<Attribute> attribute, double defaultValue) {
		if (entity == null) return defaultValue;
		AttributeMap attributes = entity.getAttributes();
		return attributes.hasAttribute(attribute) ? attributes.getValue(attribute) : defaultValue;
	}

	public static double getValueOrDefault(LivingEntity entity, Holder<Attribute> attribute) {
		if (entity == null) return attribute.value().getDefaultValue();
		AttributeMap attributes = entity.getAttributes();
		return attributes.hasAttribute(attribute) ? attributes.getValue(attribute) : attribute.value().getDefaultValue();
	}
	
	public static void setBaseValue(AttributeMap attributes, Holder<Attribute> attribute, double baseValue) {
		var instance = attributes.getInstance(attribute);
		if (instance != null) instance.setBaseValue(baseValue);
	}
	
	
	static Collection<AttributeModifier> add = new ArrayList<>();
	static Collection<AttributeModifier> multBase = new ArrayList<>();
	static Collection<AttributeModifier> multTotal = new ArrayList<>();
	static <T> void groupMultipliers(Collection<T> modifiers, Predicate<T> filter, Function<T, AttributeModifier> getModifier) {
		add.clear();
		multBase.clear();
		multTotal.clear();
		for (T modifierEntry : modifiers) {
			if (filter == null || filter.test(modifierEntry)) {
				AttributeModifier modifier = getModifier.apply(modifierEntry);
				switch (modifier.operation()) {
					case ADD_VALUE -> add.add(modifier);
					case ADD_MULTIPLIED_BASE -> multBase.add(modifier);
					case ADD_MULTIPLIED_TOTAL -> multTotal.add(modifier);
				}
			}
		}
	}
	
	static double calc(double baseValue, 
			Collection<AttributeModifier> add,
			Collection<AttributeModifier> multBase,
			Collection<AttributeModifier> multTotal,
			Predicate<AttributeModifier> filter, Attribute sanitize) {
		for (AttributeModifier modifier : add) {
			if (filter == null || filter.test(modifier)) 
				baseValue += modifier.amount();
		}

		double value = baseValue;

		for (AttributeModifier modifier : multBase) {
			if (filter == null || filter.test(modifier)) 
				value += baseValue * modifier.amount();
		}

		for (AttributeModifier modifier : multTotal) {
			if (filter == null || filter.test(modifier)) 
				value *= 1 + modifier.amount();
		}

		value = sanitize.sanitizeValue(value);
		return value;
	}
	
	public static double calculateValue(ItemAttributeModifiers itemModifiers, Holder<Attribute> attribute, EquipmentSlot equipmentSlot, double baseValue) {
		groupMultipliers(itemModifiers.modifiers(),
				modifierEntry -> modifierEntry.attribute().is(attribute) && modifierEntry.slot().test(equipmentSlot),
				ItemAttributeModifiers.Entry::modifier);
		return calc(baseValue, add, multBase, multTotal, modifier -> true, attribute.value());
	}


	public static double calcValueWithoutModifiers(AttributeInstance attribute, ResourceLocation... modifierIds) {
		return calcValueWithoutModifiers(attribute, Arrays.stream(modifierIds));
	}

	public static double calcValueWithoutModifiers(AttributeInstance attribute, Stream<ResourceLocation> modifierIds) {
		Collection<ResourceLocation> exclude = modifierIds.collect(Collectors.toCollection(HashSet::new));
		if (exclude.isEmpty()) return attribute.getValue();

		double baseValue = attribute.getBaseValue();
		return calc(baseValue, 
				attribute.getModifiers(AttributeModifier.Operation.ADD_VALUE).values(), 
				attribute.getModifiers(AttributeModifier.Operation.ADD_MULTIPLIED_BASE).values(), 
				attribute.getModifiers(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).values(), 
				modifier -> !exclude.contains(modifier.id()), attribute.getAttribute().value());
	}

}
