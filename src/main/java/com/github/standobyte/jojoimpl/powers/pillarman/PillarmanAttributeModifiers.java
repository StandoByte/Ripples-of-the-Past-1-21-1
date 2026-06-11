package com.github.standobyte.jojoimpl.powers.pillarman;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.util.objects_mc.SpecificAttributeModifier;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class PillarmanAttributeModifiers {
	public static final SpecificAttributeModifier[] SPECIES_MODIFIERS = new SpecificAttributeModifier[] {
			new SpecificAttributeModifier(Attributes.SCALE, 
					new AttributeModifier(JojoMod.resLoc("pillar_man_scale"), 
							0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),

			new SpecificAttributeModifier(ModEntityAttributes.SUN_BURN_DAMAGE, 
					new AttributeModifier(JojoMod.resLoc("pillar_man_sun_burn"), 
							4, AttributeModifier.Operation.ADD_VALUE)),

			new SpecificAttributeModifier(Attributes.MAX_HEALTH, 
					new AttributeModifier(JojoMod.resLoc("pillar_man_species_max_health"), 
							20, AttributeModifier.Operation.ADD_VALUE)),
	};
	
	
	public static final SpecificAttributeModifier[] SANTANA_MODIFIERS = new SpecificAttributeModifier[] {
			new SpecificAttributeModifier(Attributes.MAX_HEALTH, 
					new AttributeModifier(JojoMod.resLoc("pillar_man_stage_max_health"), 
							20, AttributeModifier.Operation.ADD_VALUE)),
	};
	
	public static final SpecificAttributeModifier[] MODE_USER_MODIFIERS = new SpecificAttributeModifier[] {
			new SpecificAttributeModifier(Attributes.MAX_HEALTH, 
					new AttributeModifier(JojoMod.resLoc("pillar_man_stage_max_health"), 
							40, AttributeModifier.Operation.ADD_VALUE)),
	};
	
	public static final SpecificAttributeModifier[] AJA_BUFF_MODIFIERS = new SpecificAttributeModifier[] {
			new SpecificAttributeModifier(Attributes.MAX_HEALTH, 
					new AttributeModifier(JojoMod.resLoc("pillar_man_stage_max_health"), 
							60, AttributeModifier.Operation.ADD_VALUE)),
	};
	
	public static final SpecificAttributeModifier[] ULTIMATE_THING_MODIFIERS = new SpecificAttributeModifier[] {
			new SpecificAttributeModifier(Attributes.MAX_HEALTH, 
					new AttributeModifier(JojoMod.resLoc("pillar_man_stage_max_health"), 
							80, AttributeModifier.Operation.ADD_VALUE)),
			
			new SpecificAttributeModifier(
					ModEntityAttributes.SUN_BURN_DAMAGE, new AttributeModifier(JojoMod.resLoc("ulf_no_sun_burn"), 
							-1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
	};
	
	public static SpecificAttributeModifier[] getModifiersForStage(PillarmanStage stage) {
		return switch (stage) {
			case SANTANA -> SANTANA_MODIFIERS;
			case MODE_USER -> MODE_USER_MODIFIERS;
			case AJA_BUFF -> AJA_BUFF_MODIFIERS;
			case ULTIMATE_THING -> ULTIMATE_THING_MODIFIERS;
		};
	}

}
