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
import com.github.standobyte.jojo.powersystem.ability.controls.InputBindTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate.AbilitiesHotbar;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class ClientControlScheme {
	public PowerClass<?> powerClassCosmetic;
	@ApiStatus.Internal public final Map<String, MoveGroup> moveGroups = new LinkedHashMap<>();
	@ApiStatus.Internal protected MoveGroup curGroup;
	protected static final MoveGroup EMPTY = new MoveGroup("", Component.empty(), null);
	
	public static class MoveGroup {
		public String internalName;
		@ApiStatus.Internal public Component name;
		@ApiStatus.Internal public ClientInputBind toggleHudKey;
		
		@ApiStatus.Internal public List<Bind> binds = new ArrayList<>();
		@ApiStatus.Internal public List<Hotbar> hotbars = new ArrayList<>();
		
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
			for (Bind bind : binds) {
				if (bind.input != null) {
					ClientKey key = bind.input.getKey();
					if (key != null) {
						KeyModifier keyModifier = bind.input.getKeyModifier();
						String abilityName = bind.ability.abilityName;
						InputMethod inputMethod = bind.inputMethod;
						PowerClass<?> powerClass = bind.ability.powerClass;

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
	
	public static record AbilityControlsEntry(PowerClass<?> powerClass, String abilityName) {}
	
	public static class Bind {
		public ClientInputBind input;
		public InputMethod inputMethod;
		public AbilityControlsEntry ability;
		
		public Bind(ClientInputBind input, InputMethod inputMethod, AbilityControlsEntry ability) {
			this.input = input;
			this.inputMethod = inputMethod;
			this.ability = ability;
		}
	}

	public static class Hotbar {
		public ClientInputBind useAbilityKey;
		@Nullable public ClientInputBind switchAbilityKey;
		public List<HotbarSlot> slots = new ArrayList<>();
		public int slotIndex = 0;
		
		public Hotbar(ClientInputBind useAbilityKey, @Nullable ClientInputBind switchAbilityKey) {
			this.useAbilityKey = useAbilityKey;
			this.switchAbilityKey = switchAbilityKey;
		}
		
		@Nullable
		public HotbarSlot getSelected() {
			return this.slotIndex >= 0 && this.slotIndex < this.slots.size() ? this.slots.get(this.slotIndex) : null;
		}
	}
	
	public static class HotbarSlot {
		public final InputsByKeyModifier binds = new InputsByKeyModifier();
		
		public InputsByKeyModifier getBinds() {
			return binds;
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
			return !list.isEmpty() ? list.get(0) : null;
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
		ClientControlScheme.MoveGroup controls = getCurGroup();
		InputsByKeyModifier allBindsInKey = controls.getBinds().get(key);
		if (allBindsInKey != null) {
			return allBindsInKey.getAll(currentModifier, keyInputMethod);
		}
		
		for (Hotbar hotbar : controls.hotbars) {
			ClientInputBind hotbarKey = hotbar.useAbilityKey;
			if (hotbarKey.getKey() == key) {
				HotbarSlot slot = hotbar.getSelected();
				if (slot != null) {
					return slot.getBinds().getAll(currentModifier, keyInputMethod);
				}
			}
		}
		
		return Collections.emptyList();
	}
	
	public static void setPrioritizedAbility(BaseAndActiveAbility dest, 
			List<AbilityControlsEntry> abilityNames, @Nullable Predicate<AbilityInputState> filter) {
		dest.reset();
		// FIXME shit code
		Stream<Pair<Ability, AbilityConditionCheck>> stream = abilityNames.stream()
				.map(abilityName -> {
					AvailableAbilities allAbilities = ClientPowerCache.getAvailableAbilities(abilityName.powerClass);
					Ability baseAbility = ClientPowerCache.getPower(abilityName.powerClass).getMoveset().getAbility(abilityName.abilityName);
					AbilityConditionCheck resolvedAbility = allAbilities._inMoveset.get(abilityName.abilityName);
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
	}

	static BaseAndActiveAbility target = new BaseAndActiveAbility();
	@Nullable
	public static AbilityConditionCheck prioritizedAbility(
			List<AbilityControlsEntry> abilityNames, @Nullable Predicate<AbilityInputState> filter) {
		setPrioritizedAbility(target, abilityNames, filter);
		return target.curActiveAbility;
	}
	
	protected static int abilityPriority(AbilityConditionCheck ability, Power<?> abilityCtx) {
		if (!ability.conditionCheck.isPositive()) {
			return 2;
		}
		return AbilityInputState.withValue(ability.clientInputState).getFlag(AbilityInputState.HIGH_PRIORITY) ? 0 : 1;
	}
	
	
	public static ClientControlScheme create(ControlSchemeTemplate template, PowerType powerType) {
		ClientControlScheme controls = new ClientControlScheme();
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
			
			ClientControlScheme.MoveGroup group = new ClientControlScheme.MoveGroup(groupTemplate.name, 
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
					group.binds.add(new Bind(inputBind, inputMethod, ability));
				}
			}
			
			// ability hotbars
			for (AbilitiesHotbar hotbarTemplate : groupTemplate.hotbars) {
				Hotbar clientHotbar = new Hotbar(
						ClientInputBind.toClientInput(hotbarTemplate.useAbilityKey), 
						ClientInputBind.toClientInput(hotbarTemplate.switchAbilityKey));
				for (Map<InputKey.Modifier, Map<InputMethod, String>> slotTemplate : hotbarTemplate.slots) {
					HotbarSlot slot = new HotbarSlot();
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
