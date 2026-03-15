package com.github.standobyte.jojo.event;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

@ApiStatus.Internal
public abstract class ModEventHooks {

	public static RipplesAbilityKeyPressEvent onAbilityKeyPress(LivingEntity user, Ability ability, 
			InputMethod inputMethod, float clickHoldResolveTime) {
		RipplesAbilityKeyPressEvent event = new RipplesAbilityKeyPressEvent(user, ability, inputMethod, clickHoldResolveTime);
		event = NeoForge.EVENT_BUS.post(event);
		return event;
	}
}
