package com.github.standobyte.jojo.event;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * RipplesAbilityUseEvent is fired on both sides when an Entity uses an ability from their Power moveset.<br>
 * <br>
 * This event is {@link ICancellableEvent}.<br>
 * If this event is canceled, the ability is not used.<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 **/
public class RipplesAbilityKeyPressEvent extends LivingEvent implements ICancellableEvent {
	public Ability ability;
	public final InputMethod inputMethod;
	public float clickHoldResolveTime;
	public HeldInput newHeldInput;
	
	public RipplesAbilityKeyPressEvent(LivingEntity user, Ability ability, 
			InputMethod inputMethod, float clickHoldResolveTime) {
		super(user);
		this.ability = ability;
		this.inputMethod = inputMethod;
		this.clickHoldResolveTime = clickHoldResolveTime;
		this.newHeldInput = null;
	}
	
	public Power<?> getPower() {
		return ability.getUserPower(getEntity());
	}
	
	public Ability getAbility() {
		return ability;
	}
	
	public void replaceAbility(Ability newAbility) {
		this.ability = newAbility;
	}
	
	public void cancelReplaceEntityAction(HeldInput newEntityAction) {
		this.setCanceled(true);
		this.newHeldInput = newEntityAction;
	}
	
}
