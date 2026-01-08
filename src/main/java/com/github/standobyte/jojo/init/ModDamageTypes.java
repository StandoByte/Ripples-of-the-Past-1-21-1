package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class ModDamageTypes {

	public static final TagHelper<DamageType> DAMAGE_TYPES = new TagHelper<>(JojoMod.MOD_ID, Registries.DAMAGE_TYPE);


	public static final TagKey<DamageType> CAN_HURT_STANDS = TagKey.create(Registries.DAMAGE_TYPE, 
			ResourceLocation.fromNamespaceAndPath("jojo", "can_hurt_stands"));
	public static final TagKey<DamageType> ARMOR_BREAK_COOLDOWN = TagKey.create(Registries.DAMAGE_TYPE, 
			ResourceLocation.fromNamespaceAndPath("jojo_ripples", "armor_break_cooldown"));
	public static final TagKey<DamageType> ADDS_RESOLVE = TagKey.create(Registries.DAMAGE_TYPE, 
			ResourceLocation.fromNamespaceAndPath("jojo_ripples", "adds_resolve"));
	
	// To future Stando: when you add/change damage types, don't forger to run Data_ROTP.
	// It *is* annoying but not *as* annoying as filling out each tag manually.

	public static final ResourceKey<DamageType> STAND_ATTACK = DAMAGE_TYPES.withTags(JojoMod.resLoc("stand_attack"),
			CAN_HURT_STANDS,
			ARMOR_BREAK_COOLDOWN,
			ADDS_RESOLVE,
			DamageTypeTags.BYPASSES_COOLDOWN,
			DamageTypeTags.BYPASSES_ENCHANTMENTS,
			DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS,
			DamageTypeTags.PANIC_CAUSES,
			Tags.DamageTypes.IS_PHYSICAL);

	public static final ResourceKey<DamageType> STAND_PROJECTILE = DAMAGE_TYPES.withTags(JojoMod.resLoc("stand_projectile"),
			CAN_HURT_STANDS,
			ARMOR_BREAK_COOLDOWN,
			ADDS_RESOLVE,
			DamageTypeTags.IS_PROJECTILE,
			DamageTypeTags.BYPASSES_COOLDOWN,
			DamageTypeTags.BYPASSES_ENCHANTMENTS,
			DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS,
			DamageTypeTags.PANIC_CAUSES,
			Tags.DamageTypes.IS_PHYSICAL);

	public static final ResourceKey<DamageType> MOD_PROJECTILE = DAMAGE_TYPES.withTags(JojoMod.resLoc("projectile"),
			ARMOR_BREAK_COOLDOWN,
			DamageTypeTags.IS_PROJECTILE,
			DamageTypeTags.BYPASSES_COOLDOWN,
			DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS,
			DamageTypeTags.PANIC_CAUSES,
			Tags.DamageTypes.IS_PHYSICAL);

	public static final ResourceKey<DamageType> STAND_HEALTH_LINK = DAMAGE_TYPES.withTags(JojoMod.resLoc("stand_health_link"),
			DamageTypeTags.BYPASSES_ARMOR,
			DamageTypeTags.BYPASSES_WOLF_ARMOR,
			DamageTypeTags.BYPASSES_SHIELD,
			DamageTypeTags.BYPASSES_COOLDOWN,
			DamageTypeTags.BYPASSES_EFFECTS,
			DamageTypeTags.NO_KNOCKBACK,
			Tags.DamageTypes.IS_TECHNICAL);

	public static final ResourceKey<DamageType> SUFFOCATION = DAMAGE_TYPES.withTags(JojoMod.resLoc("suffocation"),
			DamageTypeTags.BYPASSES_ARMOR,
			DamageTypeTags.BYPASSES_WOLF_ARMOR,
			DamageTypeTags.PANIC_CAUSES,
			DamageTypeTags.NO_IMPACT,
			DamageTypeTags.NO_KNOCKBACK,
			DamageTypeTags.WITHER_IMMUNE_TO,
			Tags.DamageTypes.IS_ENVIRONMENT);
	
	static {
		DAMAGE_TYPES.addToTag(CAN_HURT_STANDS, ResourceLocation.fromNamespaceAndPath("jojowor", "stand"), false);
	}


	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		event.getGenerator().addProvider(event.includeServer(), DAMAGE_TYPES.makeTagsDatagenProvider(event));
	}

}
