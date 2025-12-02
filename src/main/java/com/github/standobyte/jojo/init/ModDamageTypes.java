package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

// TODO datagen for damage types and tags
public class ModDamageTypes {
	/** Tags: 
	 * jojo:can_hurt_stands, 
	 * jojo_ripples:armor_break_cooldown, 
	 * jojo_ripples:adds_resolve, 
	 * minecraft:bypasses_cooldown, 
	 * minecraft:bypasses_enchantments, 
	 * minecraft:always_hurts_ender_dragons, 
	 * minecraft:panic_causes, 
	 * neoforge:is_physical
	 */
	public static final ResourceKey<DamageType> STAND_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, JojoMod.resLoc("stand_attack"));
	
	/** Tags: 
	 * jojo:can_hurt_stands, 
	 * jojo_ripples:armor_break_cooldown, 
	 * jojo_ripples:adds_resolve, 
	 * minecraft:is_projectile, 
	 * minecraft:bypasses_cooldown, 
	 * minecraft:bypasses_enchantments, 
	 * minecraft:always_hurts_ender_dragons, 
	 * minecraft:panic_causes, 
	 * neoforge:is_physical
	 */
	public static final ResourceKey<DamageType> STAND_PROJECTILE = ResourceKey.create(Registries.DAMAGE_TYPE, JojoMod.resLoc("stand_projectile"));
	
	/** Tags: 
	 * jojo_ripples:armor_break_cooldown, 
	 * minecraft:is_projectile, 
	 * minecraft:bypasses_cooldown, 
	 * minecraft:always_hurts_ender_dragons, 
	 * minecraft:panic_causes, 
	 * neoforge:is_physical
	 */
	public static final ResourceKey<DamageType> MOD_PROJECTILE = ResourceKey.create(Registries.DAMAGE_TYPE, JojoMod.resLoc("projectile"));

	/** Tags: 
	 * minecraft:bypasses_armor, 
	 * minecraft:bypasses_wolf_armor, 
	 * minecraft:bypasses_shield, 
	 * minecraft:bypasses_cooldown, 
	 * minecraft:bypasses_effects,  
	 * minecraft:no_knockback, 
	 * neoforge:is_technical
	 */
	public static final ResourceKey<DamageType> STAND_HEALTH_LINK = ResourceKey.create(Registries.DAMAGE_TYPE, JojoMod.resLoc("stand_health_link"));

    /** Tags:
     * minecraft:bypasses_armor,
	 * minecraft:bypasses_wolf_armor,
     * minecraft:panic_causes,
     * minecraft:no_impact,
     * minecraft:no_knockback,
     * minecraft:wither_immune_to
     */
    public static final ResourceKey<DamageType> SUFFOCATION = ResourceKey.create(Registries.DAMAGE_TYPE, JojoMod.resLoc("suffocation"));
	
	
	public static final TagKey<DamageType> CAN_HURT_STANDS = TagKey.create(Registries.DAMAGE_TYPE, 
			ResourceLocation.fromNamespaceAndPath("jojo", "can_hurt_stands"));
	public static final TagKey<DamageType> ARMOR_BREAK_COOLDOWN = TagKey.create(Registries.DAMAGE_TYPE, 
			ResourceLocation.fromNamespaceAndPath("jojo_ripples", "armor_break_cooldown"));
	public static final TagKey<DamageType> ADDS_RESOLVE = TagKey.create(Registries.DAMAGE_TYPE, 
			ResourceLocation.fromNamespaceAndPath("jojo_ripples", "adds_resolve"));
}
