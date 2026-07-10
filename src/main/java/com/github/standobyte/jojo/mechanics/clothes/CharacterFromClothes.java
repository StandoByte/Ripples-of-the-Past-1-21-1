package com.github.standobyte.jojo.mechanics.clothes;

import java.util.Optional;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.subsystems.StoryPart;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;

public record CharacterFromClothes(Optional<Holder<StoryCharacter>> character, Optional<Holder<StoryPart>> storyPart) {

	public static CharacterFromClothes fromClothes(EntityClothesInventory clothes) {
		Optional<Holder<StoryCharacter>> character = null;
		Optional<Holder<StoryPart>> storyPart = null;
		
		for (int i = 0; i < clothes.getContainerSize(); i++) {
			ItemStack clothesItem = clothes.getItem(i);
			if (!clothesItem.isEmpty()) {
				ClothesDataComponent clothesPiece = clothesItem.get(ModItemDataComponents.CLOTHES_PIECE);
				if (clothesPiece != null) {
					Holder<ClothesSet> clothesSet = clothesPiece.getClothesSet();
					
					Holder<StoryCharacter> pieceCharacter = clothesPiece.getJojoCharacter();
					Optional<Holder<StoryPart>> pieceStoryPart = Optional.ofNullable(clothesSet.value()).flatMap(ClothesSet::getStoryPart);
					
					if (character == null) {
						character = Optional.of(pieceCharacter);
					}
					else if (character.isPresent() && !character.get().is(pieceCharacter)) {
						character = Optional.empty();
					}
					
					if (storyPart == null) {
						storyPart = pieceStoryPart;
					}
					else if (storyPart.isPresent() && (pieceStoryPart.isEmpty() || !storyPart.get().is(pieceStoryPart.get()))) {
						storyPart = Optional.empty();
					}
				}
			}
		}
		
		if (character == null) character = Optional.empty();
		if (storyPart == null) storyPart = Optional.empty();
		return new CharacterFromClothes(character, storyPart);
	}
}
