package com.github.standobyte.jojo.powersystem.ability;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.ui.powerhud.WindupIndicator;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.util.StringUtil;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Ability {
	public final AbilityType<?> abilityType;
	public final AbilityId abilityId;
	protected String spriteName;
	protected Component name;
	
	public AbilityUsageGroup usageGroup = AbilityUsageGroup.SPECIAL;
	public boolean isSubAbility = false;

	public Ability(AbilityType<?> abilityType, AbilityId abilityId) {
		this.abilityType = abilityType;
		this.abilityId = abilityId;
		this.spriteName = StringUtil.splitIntAtTheEnd(abilityId.nameInMoveset()).getFirst();
		this.name = abilityName(abilityId, "");
		String abilityName = abilityId.nameInMoveset();
		this.isSubAbility = !abilityName.isEmpty() && Character.isDigit(abilityName.charAt(abilityName.length() - 1));
		initVariationAssets();
	}
	
	protected static Component abilityName(AbilityId abilityId, String postfix) {
		return Component.translatable("jojo_ripples.ability." + abilityId.nameInMoveset() + postfix);
	}
	
	public AbilityId getAbilityId() {
		return abilityId;
	}
	
	public Power<?> getUserPower(LivingEntity user) {
		return this.abilityId.powerClass().get(user);
	}
	
	
	// Most of the methods below are called in AvailableAbilities#update(Power, Moveset)

	/**
	 * @deprecated Override {@link Ability#replaceWithSubAbility(Power, AvailableAbilities)} instead, this one is not used.
	 */
	@Deprecated
	public Ability replaceWithSubAbility(Power<?> context) {
		return replaceWithSubAbility(context, null);
	}
	
	/**
	 * @return A variation of this ability depending on the context
	 * (e.g. a specific punch in a combo string, a heavy punch finisher, etc.).
	 * You should also call {@link Ability#isAbilityAvailable(Power)} on each candidate yourself,
	 * to make sure the ability shows up when and only when it is unlocked.
	 */
	@ApiStatus.OverrideOnly
	@Nullable
	public Ability replaceWithSubAbility(Power<?> context, AvailableAbilities abilities) {
		return this;
	}
	
	/**
	 * Whether or not the ability should be visible in the HUD and available to the user.
	 * @return true if the ability is unlocked by the user, 
	 * and it makes sense for it to show up in the HUD in the current context 
	 * (e.g. only show the "Use item" ability if the Stand is holding an item).
	 */
	@ApiStatus.OverrideOnly
	public boolean isAbilityAvailable(Power<?> context) {
		return isAbilityUnlocked(context);
	}
	
	public boolean isAbilityUnlocked(Power<?> context) {
		return true;
	}
	
	/**
	 * A version of {@link Ability#checkSpecificConditions(Power)} with more control, allowing one ability to disable others dynamically
	 */
	// FIXME target parameter (or a simple enough getter)
	public void onConditionCheck(Power<?> context, AvailableAbilities abilities, AbilityConditionCheck thisAbility) {
		ConditionCheck check = checkConditions(context);
		thisAbility.conditionCheck = check;
		
		LivingEntity user = context.getUser();
		if (user != null && user.level().isClientSide() && user == ClientProxy.getClientPlayer()) {
			thisAbility.clientInputState = cl_abilityInputState(context)._value;
		}
	}
	
	public ConditionCheck checkConditions(Power<?> context) {
		ConditionCheck check = checkMainModLogicConditions(context);
		if (check.isPositive()) {
			check = checkSpecificConditions(context);
		}
		return check;
	}
	
	@ApiStatus.Internal
	public ConditionCheck checkMainModLogicConditions(Power<?> context) {
		return ConditionCheck.POSITIVE;
	}
	
	@ApiStatus.OverrideOnly
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		return ConditionCheck.POSITIVE;
	}
	
	/**
	 * Is only called on physical client side, can be used to override whether or not an ability
	 * shows up in the HUD and if the input is active.
	 * For example, abilities that are used on items in the inventory (Crazy D's Item repair or item marking abilities)
	 * don't show up in the HUD regularly, but are active when you're hovering over a fitting item.
	 * This method is only called on the client, so it can reference client-only classes and methods.
	 */
	public AbilityInputState cl_abilityInputState(Power<?> context) {
		AbilityInputState state = AbilityInputState.init();
		if (InputHandler.inputsDisabled || Minecraft.getInstance().screen != null) {
			state.setFlag(AbilityInputState.IS_ACTIVE, false);
			state.setFlag(AbilityInputState.VISIBLE_WHEN_INACTIVE, true);
		}
		return state;
	}
	
	@Nullable
	public WindupIndicator cl_windupIndicator(LivingEntity clientPlayer, WindupIndicator indicator, float partialTick) {
		return null;
	}
	
	public Component getName(Power<?> context) {
		// TODO ability names in stand skins
		return name;
	}
	
	public String getSpriteName(Power<?> context) {
		return spriteName;
	}
	
	// 
	
	
	public void writeExtraInput(FriendlyByteBuf serverboundBuf, LivingEntity user, boolean isClientPlayer) {}

	
	/**
	 * Is called in {@link com.github.standobyte.jojo.powersystem.ability.input.AbilityInput}
	 */
	@ApiStatus.OverrideOnly
	@Nullable
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime, BufferingState bufferingState) {
		bufferingState.isActionSuccess = true;
		onKeyPress(level, user, extraClientInput, inputMethod, clickHoldResolveTime);
		onClick(level, user, extraClientInput);
		return null;
	}
	
	/** @deprecated Add a {@link BufferingState} argument to the end of this method's signature when overriding it. */
	@Deprecated
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime) {
		return null;
	}

	/**
	 * A simplified version of {@link Ability#onKeyPress(Level, LivingEntity, FriendlyByteBuf, InputMethod, float)}, 
	 * when all you want is to just do something the moment user clicks the ability
	 */
	@ApiStatus.OverrideOnly
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {}
	
	
	public JsonObject toConfigJson() {
		JsonObject json = new JsonObject();
		return json;
	}
	
	public void applyConfig(JsonObject config) {
		// TODO (ability config) reflection to edit field values?
	}
	
	
	protected void initVariationAssets() {}
	
}
