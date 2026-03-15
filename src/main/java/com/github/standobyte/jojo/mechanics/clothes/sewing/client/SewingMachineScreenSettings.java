package com.github.standobyte.jojo.mechanics.clothes.sewing.client;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.mechanics.clothes.sewing.ClSetSewingMachineItemPacket;
import com.github.standobyte.jojo.subsystems.StoryPart;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.network.PacketDistributor;

public class SewingMachineScreenSettings {
	public Collection<Holder<StoryCharacter>> charactersOrder;
	public StoryPart[] presentParts;

	public LinkedHashMap<StoryCharacter, ClothesCharacterUIEntry> characters;

	public Holder<ClothesSet> selectedSet;
	public StoryCharacter selectedCharacter;

	public Set<StoryPart> partFilters;
	public String searchBar = "";
	public boolean searchByName = true;

//	public Map<Rarity, List<ClothesSet>> unlockedByRarity;

	public SewingMachineScreenSettings() {
		LevelReader registries = Minecraft.getInstance().level;
		
		this.partFilters = Util.make(new LinkedHashSet<>(), set -> {
			registries.holderLookup(JojoRegistries.STORY_PARTS_REG_KEY)
				.listElements()
				.map(Holder::value)
				.forEach(set::add);
		});
		
		this.charactersOrder = registries.holderLookup(JojoRegistries.STORY_CHARACTERS_REG_KEY)
				.listElements()
				.sorted(Comparator.comparing(Holder::value, sortBy()))
				.collect(Collectors.toList());
	}

	public void initNewScreen(SewingMachineScreen screen) {
//		Player player = screen.getMinecraft().player;
		Map<StoryCharacter, ClothesCharacterUIEntry> prevMap = characters;

//		Set<ResourceLocation> unlockedClothesIds = player.level().holderLookup(JojoRegistries.CLOTHES_SETS_REG_KEY)
//				.listElementIds().map(ResourceKey::location).collect(Collectors.toSet());
//
//		if (player.getAbilities().instabuild) {
			this.characters = Util.make(new LinkedHashMap<>(), map -> {
				charactersOrder.forEach(characterHolder -> {
					StoryCharacter character = characterHolder.value();
					List<Holder<ClothesSet>> clothesSets = character.getClothesSets();
					map.put(character, new ClothesCharacterUIEntry(CommonComponents.EMPTY, 
							characterHolder, clothesSets, 
							screen, prevMap != null ? prevMap.get(character) : null));
				});
			});
//		}
//		else {
//			if (selectedSet != null && !unlockedClothesIds.contains(selectedSet.getRegistryName())) {
//				selectedSet = null;
//			}
//
//			Map<StoryCharacter, List<ClothesSet>> setsGrouped = unlockedClothesIds.stream()
//					.filter(characters::containsKey)
//					.map(characters::getValue)
//					.collect(Collectors.groupingBy(ClothesSet::getCharacter));
//			this.characters = Util.make(new LinkedHashMap<>(), map -> {
//				charactersOrder.forEach(character -> {
//					if (setsGrouped.containsKey(character)) {
//						List<ClothesSet> unlockedSets = new ArrayList<>(setsGrouped.get(character));
//						unlockedSets.sort(Comparator.comparingInt(character.getAllClothesSets()::indexOf));
//						map.put(character, new ClothesCharacterUIEntry(CommonComponents.EMPTY, 
//								character, unlockedSets, 
//								screen, prevMap != null ? prevMap.get(character) : null));
//					}
//				});
//			});
//		}

//		unlockedByRarity = JojoRegistries.CLOTHES_SETS.getRegistry().getValues().stream()
//				.filter(clothes -> clothesIds.contains(clothes.getRegistryName()))
//				.collect(Collectors.groupingBy(ClothesSet::getRarity));

		presentParts = characters.values().stream()
				.flatMap(character -> character.getAllUnlockedSets().map(clothesHolder -> clothesHolder.value().getStoryPart()))
				.filter(Optional::isPresent).map(Optional::get)
				.distinct()
				.sorted(StoryPart.COMPARATOR)
				.map(Holder::value)
				.toArray(StoryPart[]::new);
		if (selectedSet != null) {
			PacketDistributor.sendToServer(new ClSetSewingMachineItemPacket(selectedSet));
		}
	}

	public void forEachCharacter(BiConsumer<StoryCharacter, ClothesCharacterUIEntry> action) {
		characters.forEach(action);
	}


	public void selectSet(SewingMachineScreen screen, Holder<ClothesSet> clothesHolder) {
		if (clothesHolder != null) {
			this.selectedSet = clothesHolder;
			PacketDistributor.sendToServer(new ClSetSewingMachineItemPacket(clothesHolder));

			ClothesSet clothes = clothesHolder.value();
			ClothesCharacterUIEntry character = characters.get(clothes.getCharacter().value());
			if (this.selectedCharacter != clothes.getCharacter()) {
				this.selectedCharacter = clothes.getCharacter().value();

				character.setupClothesSelectionUI();
			}
		}
	}

	public Holder<ClothesSet> getSelectedSet() {
		return selectedSet;
	}

	public StoryCharacter getSelectedCharacter() {
		return selectedCharacter;
	}

	public ClothesCharacterUIEntry getSelectedCharUI() {
		return selectedCharacter != null ? characters.get(selectedCharacter) : null;
	}



	public StoryPart[] getPresentParts() {
		return presentParts;
	}

	public boolean isPartIncluded(StoryPart part) {
		return partFilters.contains(part);
	}

	public void setFilter(SewingMachineScreen screen, StoryPart part, boolean included) {
		if (included) {
			partFilters.add(part);
		}
		else {
			partFilters.remove(part);
		}
		updateFilter(screen);
	}

	public void allParts(SewingMachineScreen screen) {
		Minecraft.getInstance().player.level()
				.holderLookup(JojoRegistries.STORY_PARTS_REG_KEY)
				.listElements()
				.map(Holder::value)
				.forEach(partFilters::add);
		updateFilter(screen);
	}

	public void clearParts(SewingMachineScreen screen) {
		partFilters.clear();
		updateFilter(screen);
	}



	public boolean doesSearchByName() {
		return searchByName;
	}

	public void setSearchByName(SewingMachineScreen screen, boolean doSearch) {
		this.searchByName = doSearch;
		updateFilter(screen);
	}

	public void updatedSearchBar(SewingMachineScreen screen, String input) {
		this.searchBar = input;
		updateFilter(screen);
	}

	public void updateFilter(SewingMachineScreen screen) {
		screen.charactersList.setFilter(button -> button.character.filter(partFilters, searchBar, searchByName));
	}


	public static final Comparator<StoryCharacter> BY_EARLIEST_APPEARRANCE_PART = Comparator.comparing(
			character -> character.getClothesSets().stream()
			.map(Holder::value)
			.map(ClothesSet::getStoryPart)
			.map(optional -> optional.orElse(null))
			.min(StoryPart.COMPARATOR).orElse(null), 
			StoryPart.COMPARATOR);
	
	public static final Comparator<StoryCharacter> BY_PRIORITY_IN_PART = Comparator.comparingInt(
			character -> 0);

	public Comparator<StoryCharacter> sortBy() {
		return BY_EARLIEST_APPEARRANCE_PART.thenComparing(BY_PRIORITY_IN_PART);
	}


//	public int getUnlockedSetsOfRarity(Rarity rarity) {
//		return unlockedByRarity.getOrDefault(rarity, Collections.emptyList()).size();
//	}
}
