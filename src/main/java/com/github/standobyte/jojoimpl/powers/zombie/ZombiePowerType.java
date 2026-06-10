package com.github.standobyte.jojoimpl.powers.zombie;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;
import static com.github.standobyte.jojo.init.power.ModPlayerPowers.PLAYER_POWERS;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.github.standobyte.jojo.util.objects_mc.SpecificAttributeModifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ZombiePowerType extends PlayerPowerType<ZombieData> {
	public static final SpecificAttributeModifier SUN_BURN_DAMAGE_MODIFIER = new SpecificAttributeModifier(
			ModEntityAttributes.SUN_BURN_DAMAGE, new AttributeModifier(JojoMod.resLoc("zombie_sun_burn"), 
					4, AttributeModifier.Operation.ADD_VALUE));

	public static final SpecificAttributeModifier MAX_HEALTH_MODIFIER = new SpecificAttributeModifier(
			Attributes.MAX_HEALTH, new AttributeModifier(JojoMod.resLoc("zombie_max_health"), 
					20, AttributeModifier.Operation.ADD_VALUE));


	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> ZOMBIE_DEVOUR = ABILITY_TYPES.register(
			"zombie_devour", key -> new AbilityType<>(key, Ability::new));

	public static final DeferredHolder<PlayerPowerType<?>, ZombiePowerType> ZOMBIE = PLAYER_POWERS.register(
			"zombie", key -> new ZombiePowerType(key, new MovesetBuilder()
					.addAbility("devour", ZOMBIE_DEVOUR)));

	
	protected ZombiePowerType(ResourceLocation registryKey, MovesetBuilder abilitySet) {
		super(registryKey, abilitySet);
	}
	
	@Override
	public ZombieData newDataInstance() {
		return new ZombieData();
	}

}
