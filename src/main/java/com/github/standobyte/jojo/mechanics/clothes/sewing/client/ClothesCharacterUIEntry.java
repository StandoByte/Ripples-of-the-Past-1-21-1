package com.github.standobyte.jojo.mechanics.clothes.sewing.client;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.subsystems.StoryPart;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

public class ClothesCharacterUIEntry {
	public final SewingMachineScreen screen;
	public final Holder<StoryCharacter> character;
	public final List<Holder<ClothesSet>> allClothesSets;
	public List<Holder<ClothesSet>> filteredClothesSets;
	public Holder<ClothesSet> selectedSet;

	public ClothesCharacterUIEntry(Component message, Holder<StoryCharacter> character, List<Holder<ClothesSet>> clothesSets, 
			SewingMachineScreen screen, @Nullable ClothesCharacterUIEntry prev) {
		this.screen = screen;
		this.allClothesSets = clothesSets;
		this.character = character;
		setFilteredClothesSets(clothesSets);
		if (prev != null && allClothesSets.contains(prev.selectedSet)) {
			this.selectedSet = prev.selectedSet;
		}
		else {
			this.selectedSet = filteredClothesSets.isEmpty() ? null : filteredClothesSets.get(0);
		}
	}

	public Holder<StoryCharacter> getCharacter() {
		return character;
	}

	public Stream<Holder<ClothesSet>> getAllUnlockedSets() {
		return allClothesSets.stream();
	}

	public boolean filter(Set<StoryPart> partFilters, String searchBar, boolean searchByName) {
		setFilteredClothesSets(allClothesSets.stream()
				.filter(set -> 
				set.value().getStoryPart().map(Holder::value).filter(partFilters::contains).isPresent() && 
				("".equals(searchBar)
				|| searchByName && false
				)).collect(Collectors.toList()));
//		if (!filteredClothesSets.isEmpty() && !filteredClothesSets.contains(selectedSet)) {
//			setSelectedSet(filteredClothesSets.get(0));
//		}
		return !filteredClothesSets.isEmpty();
	}

	public Holder<ClothesSet> getSelectedSet() {
		return selectedSet;
	}

	public void setSelectedSet(Holder<ClothesSet> set) {
		this.selectedSet = set;
		ClothesSet currentlyRendered = screen.getSettings().getSelectedSet().value();
		if (currentlyRendered != null && currentlyRendered.getCharacter() == this.character && currentlyRendered != this.selectedSet) {
			screen.getSettings().selectSet(screen, selectedSet);
		}
	}

	protected void setFilteredClothesSets(List<Holder<ClothesSet>> list) {
		this.filteredClothesSets = list;
		if (screen.getSettings().getSelectedCharacter() == this.character) {
			setupClothesSelectionUI();
		}
	}

	public void setupClothesSelectionUI() {
		screen.setClothesSelection(this, filteredClothesSets);
	}
}
