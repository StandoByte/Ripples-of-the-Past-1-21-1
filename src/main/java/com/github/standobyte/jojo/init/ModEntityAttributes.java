package com.github.standobyte.jojo.init;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BooleanAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ModEntityAttributes {
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, JojoMod.MOD_ID);

	@ApiStatus.Internal
	public static class StaticInit {
		public static final RangedAttribute STAND_STRENGTH = (RangedAttribute) new RangedAttribute(
				"attribute.name.jojo_ripples.stand_strength", 0, 0, 1024)
				.setSyncable(true);
		
		public static final RangedAttribute STAND_SPEED = (RangedAttribute) new RangedAttribute(
				"attribute.name.jojo_ripples.stand_speed", 0, 0, 1024)
				.setSyncable(true);
		
		public static final RangedAttribute STAND_EFFECTIVE_RANGE = (RangedAttribute) new RangedAttribute(
				"attribute.name.jojo_ripples.stand_effective_range", 4, 0, Double.MAX_VALUE)
				.setSyncable(true);
		
		public static final RangedAttribute STAND_MAX_RANGE = (RangedAttribute) new RangedAttribute(
				"attribute.name.jojo_ripples.stand_max_range", 2, 0, Double.MAX_VALUE)
				.setSyncable(true);
		
		public static final RangedAttribute STAND_DURABILITY = (RangedAttribute) new RangedAttribute(
				"attribute.name.jojo_ripples.stand_durability", 0, 0, 1024)
				.setSyncable(true);
		
		public static final RangedAttribute STAND_PRECISION = (RangedAttribute) new RangedAttribute(
				"attribute.name.jojo_ripples.stand_precision", 0, 0, 160)
				.setSyncable(true);
	}
	
	/**
	 * Only intended for Stand users, Stands themselves use Attributes.ATTACK_DAMAGE
	 */
	public static final Holder<Attribute> STAND_STRENGTH = ATTRIBUTES.register("stand_strength", 
			() -> StaticInit.STAND_STRENGTH);

	/**
	 * Only intended for Stand users, Stands themselves use Attributes.ATTACK_SPEED
	 */
	public static final Holder<Attribute> STAND_SPEED = ATTRIBUTES.register("stand_speed", 
			() -> StaticInit.STAND_SPEED);
	
	public static final Holder<Attribute> STAND_EFFECTIVE_RANGE = ATTRIBUTES.register("stand_effective_range", 
			() -> StaticInit.STAND_EFFECTIVE_RANGE);
	
	public static final Holder<Attribute> STAND_MAX_RANGE = ATTRIBUTES.register("stand_max_range", 
			() -> StaticInit.STAND_MAX_RANGE);
	
	public static final Holder<Attribute> STAND_DURABILITY = ATTRIBUTES.register("stand_durability", 
			() -> StaticInit.STAND_DURABILITY);
	
	public static final Holder<Attribute> STAND_PRECISION = ATTRIBUTES.register("stand_precision", 
			() -> StaticInit.STAND_PRECISION);
	
	// TODO stamina regen attribute


	public static final Holder<Attribute> CAN_SEE_STANDS = ATTRIBUTES.register("can_see_stands", 
			() -> new BooleanAttribute("attribute.name.jojo_ripples.can_see_stands", false)
			.setSyncable(true));
	
	public static final Holder<Attribute> CAN_ATTACK_STANDS = ATTRIBUTES.register("can_attack_stands", 
			() -> new BooleanAttribute("attribute.name.jojo_ripples.can_attack_stands", false)
			.setSyncable(true));
	
	public static final Holder<Attribute> SUN_BURN_DAMAGE = ATTRIBUTES.register("sun_burn_damage", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.sun_burn_damage", 0, 0, 1024));
	
	
	@SubscribeEvent
	public static void addModdedAttributes(EntityAttributeModificationEvent event) {
		for (var entityType : event.getTypes()) {
			if (!event.has(entityType, STAND_STRENGTH)) event.add(entityType, STAND_STRENGTH);
			if (!event.has(entityType, STAND_SPEED)) event.add(entityType, STAND_SPEED);
			if (!event.has(entityType, STAND_EFFECTIVE_RANGE)) event.add(entityType, STAND_EFFECTIVE_RANGE);
			if (!event.has(entityType, STAND_MAX_RANGE)) event.add(entityType, STAND_MAX_RANGE);
			if (!event.has(entityType, STAND_DURABILITY)) event.add(entityType, STAND_DURABILITY);
			if (!event.has(entityType, STAND_PRECISION)) event.add(entityType, STAND_PRECISION);
			
			if (!event.has(entityType, CAN_SEE_STANDS)) event.add(entityType, CAN_SEE_STANDS);
			if (!event.has(entityType, CAN_ATTACK_STANDS)) event.add(entityType, CAN_ATTACK_STANDS);
			if (!event.has(entityType, SUN_BURN_DAMAGE)) event.add(entityType, SUN_BURN_DAMAGE);
		}
	}
}
