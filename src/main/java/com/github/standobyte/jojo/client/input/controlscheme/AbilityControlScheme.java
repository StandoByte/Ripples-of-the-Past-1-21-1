package com.github.standobyte.jojo.client.input.controlscheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.InputHandler.BaseAndActiveAbility;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate.AbilitiesHotbar;
import com.github.standobyte.jojo.powersystem.ability.controls.InputBindTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class AbilityControlScheme {
	public PowerClass<?> powerClassCosmetic;
	@ApiStatus.Internal public final Map<String, MoveGroup> moveGroups = new LinkedHashMap<>();
	@ApiStatus.Internal protected MoveGroup curGroup;
	protected static final MoveGroup EMPTY = new MoveGroup("", Component.empty(), null);
	
	public static class MoveGroup {
		public String internalName;
		@ApiStatus.Internal public Component name;
		@ApiStatus.Internal public ClientInputBind toggleHudKey;
		
		@ApiStatus.Internal public List<AbilityBind> binds = new ArrayList<>();
		@ApiStatus.Internal public List<AbilityHotbar> hotbars = new ArrayList<>();
		
		protected Map<ClientKey, InputsByKeyModifier> bindsMap = new TreeMap<>(Comparator.comparingInt(ClientKey::keyOrder));
		
		public MoveGroup(String internalName, Component name, ClientInputBind toggleHudKey) {
			this.name = name;
			this.toggleHudKey = toggleHudKey;
		}
		
		/* TODO cache the binds map
		 *   only clear the cache when any key changes
		 *   (*including* the vanilla keybinds, on KepMapping#setKey(InputConstants.Key))
		 */
		public Map<ClientKey, InputsByKeyModifier> getBinds() {
			bindsMap.clear();
			for (AbilityBind bind : binds) {
				if (bind.input != null) {
					ClientKey key = bind.input.getKey();
					if (key != null) {
						KeyModifier keyModifier = bind.input.getKeyModifier();
						String abilityName = bind.ability.abilityName();
						InputMethod inputMethod = bind.inputMethod;
						PowerClass<?> powerClass = bind.ability.powerClass();

						InputsByKeyModifier keyAllBinds = bindsMap.computeIfAbsent(key, 
								__ -> new InputsByKeyModifier());
						Map<InputMethod, List<AbilityControlsEntry>> modifierKeyBinds = keyAllBinds.movesByModifier.computeIfAbsent(keyModifier, 
								__ -> new EnumMap<>(InputMethod.class));
						List<AbilityControlsEntry> byInputMethod = modifierKeyBinds.computeIfAbsent(inputMethod, 
								__ -> new ArrayList<>());
						byInputMethod.add(new AbilityControlsEntry(powerClass, abilityName));
					}
				}
			}

			return bindsMap;
		}
	}
	
	public static class InputsByKeyModifier {
		public final Map<KeyModifier, Map<InputMethod, List<AbilityControlsEntry>>> movesByModifier = new EnumMap<>(KeyModifier.class);
		
		public List<AbilityControlsEntry> getAll(@Nonnull KeyModifier curModifier, InputMethod inputMethod) {
			List<AbilityControlsEntry> list = null;
			if (movesByModifier.containsKey(curModifier)) {
				list = movesByModifier.get(curModifier).get(inputMethod);
			}
			else if (movesByModifier.containsKey(KeyModifier.NONE)) {
				list = movesByModifier.get(KeyModifier.NONE).get(inputMethod);
			}
			return list != null ? list : Collections.emptyList();
		}
		
		@Nullable
		public AbilityControlsEntry getFirst(@Nonnull KeyModifier curModifier, InputMethod inputMethod) {
			List<AbilityControlsEntry> list = getAll(curModifier, inputMethod);
			if (!list.isEmpty()) {
				return list.get(0);
			}
			
			if (curModifier != KeyModifier.NONE) {
				return getFirst(KeyModifier.NONE, inputMethod);
			}
			else {
				return null;
			}
		}
	}


	public boolean hasAbility(Predicate<AbilityControlsEntry> condition) {
		MoveGroup moves = this.getCurGroup();
		for (var bind : moves.binds) {
			if (condition.test(bind.ability)) {
				return true;
			}
		}
		
		for (var hotbar : moves.hotbars) {
			for (var hotbarSlot : hotbar.slots) {
				for (var byModifier : hotbarSlot.binds.movesByModifier.entrySet()) {
					for (var byInputMethod : byModifier.getValue().entrySet()) {
						for (var ability : byInputMethod.getValue()) {
							if (condition.test(ability)) { // looks cursed, I know
								return true;
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
	
	@Nonnull
	public MoveGroup getCurGroup() {
		if (curGroup == null) {
			setCurGroup(moveGroups.values().stream().findFirst().orElse(EMPTY));
		}
		return curGroup;
	}
	
	protected void setCurGroup(MoveGroup moveGroup) {
		if (this.curGroup != moveGroup) {
			this.curGroup = moveGroup;
			InputHandler.getInstance().onUpdatedControls(moveGroup);
		}
	}
	
	public List<AbilityControlsEntry> getBindsWithModifier(InputMethod keyInputMethod, ClientKey key, KeyModifier currentModifier) {
		AbilityControlScheme.MoveGroup controls = getCurGroup();
		InputsByKeyModifier allBindsInKey = controls.getBinds().get(key);
		
		List<AbilityControlsEntry> fromBinds = allBindsInKey != null ? allBindsInKey.getAll(currentModifier, keyInputMethod) : Collections.emptyList();
		List<AbilityControlsEntry> fromHotbars = Collections.emptyList();
		boolean mergedHotbars = false;
		
		for (AbilityHotbar hotbar : controls.hotbars) {
			ClientInputBind hotbarKey = hotbar.useAbilityKey;
			if (hotbarKey.getKey() == key) {
				AbilityHotbarSlot slot = hotbar.getSelected();
				if (slot != null) {
					List<AbilityControlsEntry> inThisHotbar = slot.getBinds().getAll(currentModifier, keyInputMethod);
					if (fromHotbars.isEmpty()) {
						fromHotbars = inThisHotbar;
					}
					else if (!mergedHotbars) {
						fromHotbars = new ArrayList<>(fromHotbars.size() + inThisHotbar.size());
						fromHotbars.addAll(fromHotbars);
						fromHotbars.addAll(inThisHotbar);
						mergedHotbars = true;
					}
					else {
						fromHotbars.addAll(inThisHotbar);
					}
				}
			}
		}
		
		if (!fromBinds.isEmpty() && !fromHotbars.isEmpty()) {
			List<AbilityControlsEntry> merged = new ArrayList<>(fromBinds.size() + fromHotbars.size());
			merged.addAll(fromBinds);
			merged.addAll(fromHotbars);
			return merged;
		}
		else if (!fromBinds.isEmpty()) {
			return fromBinds;
		}
		else {
			return fromHotbars;
		}
	}
	
	public static void setPrioritizedAbility(BaseAndActiveAbility dest, KeyModifier curModifier, 
			Function<KeyModifier, List<AbilityControlsEntry>> getAbilities, 
			@Nullable Predicate<AbilityInputState> filter) {
		dest.reset();
		List<AbilityControlsEntry> abilityNames = getAbilities.apply(curModifier);
		// still shit code
		Stream<Pair<Ability, AbilityConditionCheck>> stream = abilityNames.stream()
				.map(_abilityName -> {
					PowerClass<?> powerClass = _abilityName.powerClass();
					String abilityName = _abilityName.abilityName();
					AvailableAbilities allAbilities = ClientPowerCache.getAvailableAbilities(powerClass);
					Ability baseAbility = ClientPowerCache.getPower(powerClass).getMoveset().getAbility(abilityName);
					AbilityConditionCheck resolvedAbility = allAbilities._inMoveset.get(abilityName);
					return baseAbility != null && resolvedAbility != null ? Pair.of(baseAbility, resolvedAbility) : null;
				})
				.filter(Objects::nonNull);
		if (filter != null) {
			stream = stream.filter(a -> filter.test(AbilityInputState.withValue(a.getSecond().clientInputState)));
		}
		
		Pair<Ability, AbilityConditionCheck> ability = stream
				.sorted(Comparator.comparingInt(a -> abilityPriority(a.getSecond(), ClientPowerCache.getPower(a.getFirst().abilityId.powerClass()))))
				.findFirst().orElse(null);
		if (ability != null) {
			dest.set(ability.getFirst(), ability.getSecond());
		}
		else if (curModifier != null && curModifier != KeyModifier.NONE) {
			setPrioritizedAbility(dest, KeyModifier.NONE, getAbilities, filter);
		}
	}

	static BaseAndActiveAbility target = new BaseAndActiveAbility();
	@Nullable
	public static AbilityConditionCheck prioritizedAbility(KeyModifier curModifier, 
			Function<KeyModifier, List<AbilityControlsEntry>> getAbilities, 
			@Nullable Predicate<AbilityInputState> filter) {
		setPrioritizedAbility(target, curModifier, getAbilities, filter);
		return target.curActiveAbility;
	}
	
	protected static int abilityPriority(AbilityConditionCheck ability, Power<?> abilityCtx) {
		if (!ability.conditionCheck.positive()) {
			return 2;
		}
		return AbilityInputState.withValue(ability.clientInputState).getFlag(AbilityInputState.HIGH_PRIORITY) ? 0 : 1;
	}
	
	
	public static AbilityControlScheme create(ControlSchemeTemplate template, PowerType powerType) {
		AbilityControlScheme controls = new AbilityControlScheme();
		PowerClass<?> powerClass = powerType.getPowerClass();
		controls.powerClassCosmetic = powerClass;
		
		for (ControlSchemeTemplate.GroupTemplate groupTemplate : template.groups.values()) {
			if (groupTemplate.isEmpty()) continue;
			
			InputBindTemplate toggleHudKey = groupTemplate.toggleHudKey;
			if (toggleHudKey == null) {
				if (powerClass == PowerClass.STAND) {
					toggleHudKey = new InputUseVanillaMapping(InputHandler.getInstance().vanillaKeybinds.standArmsOnlyHUD);
				}
				else {
					toggleHudKey = new InputUseVanillaMapping(InputHandler.getInstance().vanillaKeybinds.playerPowerHUD);
				}
			}
			ClientInputBind toggleHudKeybind = ClientInputBind.toClientInput(toggleHudKey);
			
			AbilityControlScheme.MoveGroup group = new AbilityControlScheme.MoveGroup(groupTemplate.name, 
					Component.translatable(groupTemplate.name), toggleHudKeybind);
			controls.moveGroups.put(groupTemplate.name, group);
			
			// separate binds
			for (Map.Entry<String, Pair<InputMethod, InputBindTemplate>> bind : groupTemplate.separateBinds.entrySet()) {
				var input = bind.getValue();
				InputBindTemplate inputBindTemplate = input.getSecond();
				ClientInputBind inputBind = ClientInputBind.toClientInput(inputBindTemplate);
				if (inputBind != null) {
					InputMethod inputMethod = input.getFirst();
					String abilityName = bind.getKey();
					AbilityControlsEntry ability = new AbilityControlsEntry(powerClass, abilityName);
					group.binds.add(new AbilityBind(inputBind, inputMethod, ability));
				}
			}
			
			// ability hotbars
			int i = 0;
			for (AbilitiesHotbar hotbarTemplate : groupTemplate.hotbars) {
				AbilityHotbar clientHotbar = new AbilityHotbar(
						ClientInputBind.toClientInput(hotbarTemplate.useAbilityKey), 
						ClientInputBind.toClientInput(hotbarTemplate.switchAbilityKey));
				for (Map<InputKey.Modifier, Map<InputMethod, String>> slotTemplate : hotbarTemplate.slots) {
					AbilityHotbarSlot slot = new AbilityHotbarSlot(i++);
					for (var slotVariation : slotTemplate.entrySet()) {
						InputKey.Modifier modifier = slotVariation.getKey();
						for (var abilityEntry : slotVariation.getValue().entrySet()) {
							InputMethod inputMethod = abilityEntry.getKey();
							String ability = abilityEntry.getValue();
							Map<InputMethod, List<AbilityControlsEntry>> byModifier = slot.binds.movesByModifier.computeIfAbsent(ClientInputBind.toClientModifier(modifier), 
									__ -> new EnumMap<>(InputMethod.class));
							byModifier.put(inputMethod, 
									Collections.singletonList(new AbilityControlsEntry(powerClass, ability)));
						}
					}
					clientHotbar.slots.add(slot);
				}
				group.hotbars.add(clientHotbar);
			}
		}
		
		return controls;
	}
	
}
