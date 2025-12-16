package com.github.standobyte.jojo.powersystem.ability.controls;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.Util;

public class ControlSchemeTemplate {
	public Map<String, GroupTemplate> groups = new LinkedHashMap<>();
	public GroupTemplate defaultGroup = new GroupTemplate("moveset_default_group", null);
	private transient Int2ObjectMap<AbilitiesHotbar> hotbarsById = new Int2ObjectArrayMap<>();

	public ControlSchemeTemplate deepCopy() {
		// TODO control scheme deep copy
		return this;
	}

	public static class GroupTemplate {
		public final String name;
		@Nullable public final InputBindTemplate toggleHudKey;

		public Map<String, Pair<InputMethod, InputBindTemplate>> separateBinds = new LinkedHashMap<>();
		public List<AbilitiesHotbar> hotbars = new ArrayList<>();

		public GroupTemplate(String name, InputBindTemplate toggleHudKey) {
			this.name = name;
			this.toggleHudKey = toggleHudKey;
		}

		public boolean isEmpty() {
			return separateBinds.isEmpty() && hotbars.isEmpty();
		}
	}

	public static class AbilitiesHotbar {
		public List<Map<InputKey.Modifier, Map<InputMethod, String>>> slots = new ArrayList<>();
		public InputBindTemplate useAbilityKey;
		public InputBindTemplate switchAbilityKey;

		public AbilitiesHotbar(InputBindTemplate useAbilityKey, InputBindTemplate switchAbilityKey) {
			this.useAbilityKey = useAbilityKey;
			this.switchAbilityKey = switchAbilityKey;
		}
	}

	public ControlSchemeTemplate() {
		groups.put(defaultGroup.name, defaultGroup);
	}


	@ApiStatus.Internal
	public MovesetBuilder curMovesetBuilder;

	@ApiStatus.Internal
	public MovesetBuilder finalizeControlScheme() {
		MovesetBuilder movesetBuilder = curMovesetBuilder;
		this.curMovesetBuilder = null;
		return movesetBuilder;
	}


	public ControlSchemeTemplate bind(String ability, InputMethod inputMethod, InputBindTemplate key) {
		_curGroup.separateBinds.put(ability, Pair.of(inputMethod, key));
		return this;
	}

	public ControlSchemeTemplate makeHotbar(int hotbarId, InputBindTemplate useAbilityKey, InputBindTemplate switchAbilityKey) {
		if (_curGroup != null) {
			AbilitiesHotbar hotbar = new AbilitiesHotbar(useAbilityKey, switchAbilityKey);
			hotbarsById.put(hotbarId, hotbar);
			_curGroup.hotbars.add(hotbar);
		}
		return this;
	}

	public ControlSchemeTemplate addToHotbar(String ability, int hotbarId, InputMethod inputMethod) {
		Map<InputKey.Modifier, Map<InputMethod, String>> slot = new HashMap<>();
		slot.put(null, Util.make(new EnumMap<>(InputMethod.class), map -> map.put(inputMethod, ability)));
		hotbarsById.get(hotbarId).slots.add(slot);
		return this;
	}

	public ControlSchemeTemplate addHotbarSlotVariation(String ability, String baseAbility, @Nullable InputKey.Modifier modifier, InputMethod inputMethod) {
		for (AbilitiesHotbar hotbar : hotbarsById.values()) {
			for (Map<InputKey.Modifier, Map<InputMethod, String>> slot : hotbar.slots) {
				Map<InputMethod, String> baseVariation = slot.get(null);
				if (baseVariation != null && baseVariation.values().contains(baseAbility)) {
					Map<InputMethod, String> byInputMethod = slot.computeIfAbsent(modifier, 
							__ -> new EnumMap<>(InputMethod.class));
					byInputMethod.put(inputMethod, ability);
				}
			}
		}
		return this;
	}
	
	
	
	@ApiStatus.Internal
	public GroupTemplate _curGroup = defaultGroup;
	public ControlSchemeTemplate makeMovesetGroup(String name, InputBindTemplate toggleHudKey) {
		GroupTemplate group = groups.computeIfAbsent(name, _name -> new GroupTemplate(_name, toggleHudKey));
		this._curGroup = group;
		return this;
	}
	
	public ControlSchemeTemplate setMovesetGroup(@Nullable String name) {
		this._curGroup = getMovesetGroup(name);
		return this;
	}

	public GroupTemplate getMovesetGroup(@Nullable String name) {
		return name == null ? defaultGroup : groups.get(name);
	}

}
