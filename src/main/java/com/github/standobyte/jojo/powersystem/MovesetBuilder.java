package com.github.standobyte.jojo.powersystem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.config.ConfigAbilityFactory;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.InputBindTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;

import net.minecraft.resources.ResourceLocation;

@ApiStatus.NonExtendable
public class MovesetBuilder {
	public final Map<String, ConfigAbilityFactory<?>> abilities = new LinkedHashMap<>();
	public final Map<String, UnlockableSkill> unlockableSkills = new LinkedHashMap<>();
	
	@ApiStatus.Internal public Map<String, ControlSchemeTemplate> controlSchemes = new LinkedHashMap<>();
	@ApiStatus.Internal public ControlSchemeTemplate curControlScheme = new ControlSchemeTemplate();
//	protected final Set<String> disable = new HashSet<>();
	
	
	public MovesetBuilder deepCopy() {
		MovesetBuilder copy = new MovesetBuilder();
		for (var abilityEntry : abilities.entrySet()) {
			copy.abilities.put(abilityEntry.getKey(), abilityEntry.getValue().copy());
		}
		copy.unlockableSkills.putAll(this.unlockableSkills);
		this.controlSchemes.forEach((name, scheme) -> copy.controlSchemes.put(name, scheme.deepCopy()));
		copy.curControlScheme = this.curControlScheme.deepCopy();
		return copy;
	}
	
	public Moveset build(PowerClass<?> powerClass, ResourceLocation powerTypeId) {
		if (this.controlSchemes.isEmpty()) {
			this.controlSchemes.put("default", curControlScheme);
		}
		postInitControls();
		
		Map<String, Ability> abilities = new LinkedHashMap<>();
		for (var abilityEntry : this.abilities.entrySet()) {
			String abilityName = abilityEntry.getKey();
			var abilityFactory = abilityEntry.getValue();
//			if (!disable.contains(key)) {
				var ability = abilityFactory.makeAbility(new AbilityId(powerClass, powerTypeId, abilityName));
				abilities.put(abilityName, ability);
//			}
		}
		Moveset moveset = new Moveset(abilities, powerClass, powerTypeId);
		moveset.controlScheme = this.curControlScheme;
		return moveset;
	}
	
	
	public <A extends Ability> MovesetBuilder addAbility(String abilityName, AbilityType<A> abilityType) {
		return addAbility(abilityName, abilityType, null);
	}

	public final <A extends Ability> MovesetBuilder addAbility(String abilityName, AbilityType<A> abilityType, 
			@Nullable Consumer<A> init) {
		abilities.put(abilityName, new ConfigAbilityFactory<>(abilityType, init));
		lastAbility = abilityName;
		return this;
	}
	
	public <A extends Ability> MovesetBuilder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType) {
		return addAbility(abilityName, abilityType.get());
	}
	
	public final <A extends Ability> MovesetBuilder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType, 
			Consumer<A> init) {
		return addAbility(abilityName, abilityType.get(), init);
	}
	
	
//	public MovesetBuilder disableAbility(String abilityName) {
//		disable.add(abilityName);
//		return this;
//	}
	
	
	// Control scheme stuff
	
	@ApiStatus.Internal public String lastAbility;
	
	public ControlSchemeTemplate makeControlScheme(String name) {
		curControlScheme = controlSchemes.computeIfAbsent(name, __ -> new ControlSchemeTemplate());
		curControlScheme.curMovesetBuilder = this;
		return curControlScheme;
	}
	
	@Deprecated
	public MovesetBuilder makeMovesetGroup(String name, InputBindTemplate toggleHudKey) {
		curControlScheme.makeMovesetGroup(name, toggleHudKey);
		return this;
	}

	@Deprecated
	public MovesetBuilder withBind(InputMethod inputMethod, InputBindTemplate key) {
		return withBind(null, inputMethod, key);
	}

	@Deprecated
	public MovesetBuilder withBind(String movesetGroupName, InputMethod inputMethod, InputBindTemplate key) {
		curControlScheme.setMovesetGroup(movesetGroupName);
		curControlScheme.bind(lastAbility, inputMethod, key);
		return this;
	}

	@Deprecated
	public MovesetBuilder makeHotbar(int hotbarId, InputBindTemplate useAbilityKey, InputBindTemplate switchAbilityKey) {
		return makeHotbar(null, hotbarId, useAbilityKey, switchAbilityKey);
	}

	@Deprecated
	public MovesetBuilder makeHotbar(String movesetGroupName, int hotbarId, InputBindTemplate useAbilityKey, InputBindTemplate switchAbilityKey) {
		curControlScheme.setMovesetGroup(movesetGroupName);
		curControlScheme.makeHotbar(hotbarId, useAbilityKey, switchAbilityKey);
		return this;
	}

	@Deprecated
	public MovesetBuilder inHotbar(int hotbarId, InputMethod inputMethod) {
		curControlScheme.addToHotbar(lastAbility, hotbarId, inputMethod);
		return this;
	}

	@Deprecated
	public MovesetBuilder inHotbarSlotVariation(String baseAbility, @Nullable InputKey.Modifier modifier, InputMethod inputMethod) {
		curControlScheme.addHotbarSlotVariation(lastAbility, baseAbility, modifier, inputMethod);
		return this;
	}
	
	// Unlockable skill stuff
	
	public MovesetBuilder addSkill(UnlockableSkill skill) {
		unlockableSkills.put(skill.skillName, skill);
		return this;
	}

	
	// shortcuts for some common abilities
	
	public MovesetBuilder addHumanoidStandStuff() {
		addManualControl();
		return this;
	}
	
	public MovesetBuilder addManualControl() {
		addAbility("manual_control", ModStandAbilities.MANUAL_CONTROL);
		return this;
	}
	
	@Deprecated
	public MovesetBuilder addItemUsage() {
		return this;
	}
	
	@Deprecated
	public MovesetBuilder addHumanoidStandSkills() {
		return this
		.addSkill(StandUnlockableSkill.startingAbility("guard").setNotYetImplemented())
		.addSkill(StandUnlockableSkill.startingAbility("manual_control"))
		.addSkill(new StandUnlockableSkill("item_use").setIsStartingSkill())
		.addSkill(StandUnlockableSkill.startingAbility("ledge_grab").setNotYetImplemented());
	}
	
	protected void postInitControls() {
		if (abilities.containsKey("grab")) {
			if (!abilities.containsKey("grab_release")) {
				addAbility("grab_release", ModStandAbilities.GRAB_RELEASE);
			}
			addBindToAllCtrlSchemesIfMissing("grab_release", InputMethod.CLICK, InputKey.Q);
		}
		addBindToAllCtrlSchemesIfMissing("manual_control", InputMethod.CLICK, InputKey.O);
		addBindToAllCtrlSchemesIfMissing("items_swap_w_user", InputMethod.CLICK, InputKey.F.withModifier(InputKey.Modifier.CONTROL));
		addBindToAllCtrlSchemesIfMissing("items_swap_hands", InputMethod.CLICK, new InputUseVanillaMapping("key.swapOffhand"));
		addBindToAllCtrlSchemesIfMissing("item_toss", InputMethod.CLICK, new InputUseVanillaMapping("key.drop"));
	}
	
	protected void addBindToAllCtrlSchemesIfMissing(String abilityName, InputMethod inputMethod, InputBindTemplate key) {
		if (abilities.containsKey(abilityName)) {
			for (ControlSchemeTemplate ctrlScheme : controlSchemes.values()) {
				ctrlScheme.setMovesetGroup(null);
				if (!ctrlScheme._curGroup.separateBinds.containsKey(abilityName)) {
					ctrlScheme.bind(abilityName, inputMethod, key);
				}
			}
		}
	}
	
	
	
	/**
	 * @deprecated Use {@link #withBind(InputMethod, InputBindTemplate)} (just swap the parameters)
	 */
	@Deprecated
	public MovesetBuilder withBind(InputBindTemplate key, InputMethod inputMethod) {
		return withBind(inputMethod, key);
	}

	/**
	 * @deprecated. Use {@link #withBind(String, InputMethod, InputBindTemplate)} (just swap the 2nd and 3rd parameters)
	 */
	@Deprecated
	public MovesetBuilder withBind(String movesetGroupName, InputBindTemplate key, InputMethod inputMethod) {
		return withBind(movesetGroupName, inputMethod, key);
	}
	
}
