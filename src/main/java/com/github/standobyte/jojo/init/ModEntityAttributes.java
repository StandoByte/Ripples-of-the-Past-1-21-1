package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ModEntityAttributes {
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, JojoMod.MOD_ID);

	
	/**
	 * Only intended for Stand users, Stands themselves use Attributes.ATTACK_DAMAGE
	 */
	public static final Holder<Attribute> STAND_STRENGTH = ATTRIBUTES.register("stand_strength", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_strength", 0, 0, 1024).setSyncable(true));

	/**
	 * Only intended for Stand users, Stands themselves use Attributes.ATTACK_SPEED
	 */
	public static final Holder<Attribute> STAND_SPEED = ATTRIBUTES.register("stand_speed", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_speed", 0, 0, 1024).setSyncable(true));
	
	public static final Holder<Attribute> STAND_EFFECTIVE_RANGE = ATTRIBUTES.register("stand_effective_range", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_effective_range", 4, 0, Double.MAX_VALUE).setSyncable(true));
	
	public static final Holder<Attribute> STAND_MAX_RANGE = ATTRIBUTES.register("stand_max_range", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_max_range", 2, 0, Double.MAX_VALUE).setSyncable(true));
	
	public static final Holder<Attribute> STAND_DURABILITY = ATTRIBUTES.register("stand_durability", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_durability", 0, 0, 1024).setSyncable(true));
	
	public static final Holder<Attribute> STAND_PRECISION = ATTRIBUTES.register("stand_precision", 
			() -> new RangedAttribute("attribute.name.jojo_ripples.stand_precision", 0, 0, 160).setSyncable(true));
	
	// TODO stamina regen attribute
	
	
	@SubscribeEvent
	public static void addModdedAttributes(EntityAttributeModificationEvent event) {
		for (var entityType : event.getTypes()) {
			if (!event.has(entityType, STAND_STRENGTH)) event.add(entityType, STAND_STRENGTH);
			if (!event.has(entityType, STAND_SPEED)) event.add(entityType, STAND_SPEED);
			if (!event.has(entityType, STAND_EFFECTIVE_RANGE)) event.add(entityType, STAND_EFFECTIVE_RANGE);
			if (!event.has(entityType, STAND_MAX_RANGE)) event.add(entityType, STAND_MAX_RANGE);
			if (!event.has(entityType, STAND_DURABILITY)) event.add(entityType, STAND_DURABILITY);
			if (!event.has(entityType, STAND_PRECISION)) event.add(entityType, STAND_PRECISION);
		}
	}
}
