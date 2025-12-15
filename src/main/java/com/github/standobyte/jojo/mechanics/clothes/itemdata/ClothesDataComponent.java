package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesPiece.SubClothingPiece;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

// if you delete a clothes data pack and reinstall it, it breaks the existing items with that clothes piece :skull:
public class ClothesDataComponent {
	private final Holder<ClothesSet> set;
	private final ClothesSlotType slot;
	private final Optional<ClothesPiece.SubClothingPiece> subPieceType;

	private final Holder<StoryCharacter> character;
	private final ClothesPiece piece;
	
	protected ClothesDataComponent(
			Holder<ClothesSet> set,
			ClothesSlotType pieceSlot, 
			Optional<ClothesPiece.SubClothingPiece> subPieceType) {
		this(set, pieceSlot, subPieceType.orElse(ClothesPiece.SubClothingPiece.FULL));
	}
	
	public ClothesDataComponent(
			Holder<ClothesSet> set,
			ClothesSlotType pieceSlot, 
			ClothesPiece.SubClothingPiece subPieceType) {
		this.set = set;
		this.slot = pieceSlot;
		
		this.character = set.value().getCharacter();
		ClothesPiece piece = set.value().getPiece(pieceSlot);
		ClothesPiece subPiece = subPieceType != null ? piece.getSubPiece(subPieceType) : null;
		if (subPiece != null) {
			this.piece = subPiece;
			this.subPieceType = Optional.of(subPieceType);
		}
		else {
			this.piece = piece;
			this.subPieceType = Optional.empty();
		}
	}
	
	public Holder<StoryCharacter> getJojoCharacter() {
		return character;
	}
	
	public ClothesPiece getPiece() {
		return piece;
	}
	
	
	public Holder<ClothesSet> getClothesSet() {
		return set;
	}
	
	public ClothesSlotType getSlot() {
		return slot;
	}
	
	@Nullable
	public SubClothingPiece getSubType() {
		return subPieceType.orElse(null);
	}


	@Nullable
	public Pair<ItemStack, ItemStack> splitInto(@Nullable ItemStack fullItem) {
		if (getSubType() == SubClothingPiece.FULL) {
			ClothesPiece top = piece.getSubPiece(SubClothingPiece.TOP);
			if (top == null) return null;
			ClothesPiece bottom = piece.getSubPiece(SubClothingPiece.BOTTOM);
			if (bottom == null) return null;
			
			ItemStack topItem = ModItems.CLOTHES_BASE_ITEM.get().makeClothesPieceStack(
					new ClothesDataComponent(this.set, this.slot, SubClothingPiece.TOP));
			ItemStack bottomItem = ModItems.CLOTHES_BASE_ITEM.get().makeClothesPieceStack(
					new ClothesDataComponent(this.set, this.slot, SubClothingPiece.BOTTOM));
			return Pair.of(topItem, bottomItem);
		}
		return null;
	}
	
	@Nullable
	public Pair<ItemStack, ClothesPiece> combineWithOtherPieceToGetFull(@Nullable ItemStack bottomPieceItem, @Nullable ItemStack topPieceItem) {
		SubClothingPiece subPiece = this.getSubType();
		if (subPiece == null) return null;
		
		SubClothingPiece otherPiece = switch (subPiece) {
			case TOP -> SubClothingPiece.BOTTOM;
			case BOTTOM -> SubClothingPiece.TOP;
			default -> null;
		};
		if (otherPiece == null) return null;
		
		ClothesPiece other = piece.getSubPiece(otherPiece);
		if (other == null) return null;
		ClothesPiece full = piece.getSubPiece(SubClothingPiece.FULL);
		if (full == null) return null;
		
		ItemStack fullItem = ModItems.CLOTHES_BASE_ITEM.get().makeClothesPieceStack(
				new ClothesDataComponent(this.set, this.slot, SubClothingPiece.FULL));
		return Pair.of(fullItem, other);
	}
	
	public static boolean areDifferentSubpiecesOfTheSamePiece(ItemStack item1, ItemStack item2) {
		ClothesDataComponent clothesData1 = item1.get(ModItemDataComponents.CLOTHES_PIECE.get());		if (clothesData1 == null) return false;
		ClothesDataComponent clothesData2 = item2.get(ModItemDataComponents.CLOTHES_PIECE.get());		if (clothesData2 == null) return false;

		return (clothesData1.getSubType() == SubClothingPiece.TOP && clothesData2.getSubType() == SubClothingPiece.BOTTOM
				|| clothesData1.getSubType() == SubClothingPiece.BOTTOM && clothesData2.getSubType() == SubClothingPiece.TOP)
				&& clothesData1.slot == clothesData2.slot
				&& clothesData1.getClothesSet().is(clothesData2.getClothesSet());
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(set, slot, subPieceType);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else {
			return obj instanceof ClothesDataComponent other
				&& this.set.equals(other.set)
				&& this.slot == other.slot
				&& this.subPieceType.equals(other.subPieceType);
		}
	}
	

	public static final Codec<ClothesDataComponent> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					ClothesSet.REG_CODEC.fieldOf("set").forGetter(ClothesDataComponent::getClothesSet),
					ClothesSlotType.CODEC.fieldOf("slot").forGetter(ClothesDataComponent::getSlot),
					ClothesPiece.SubClothingPiece.CODEC.optionalFieldOf("sub_piece").forGetter(component -> component.subPieceType))
			.apply(builder, ClothesDataComponent::new));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, ClothesDataComponent> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(JojoRegistries.CLOTHES_SETS_REG_KEY), ClothesDataComponent::getClothesSet,
			ClothesSlotType.STREAM_CODEC, ClothesDataComponent::getSlot,
			SubClothingPiece.STREAM_CODEC.apply(ByteBufCodecs::optional), component -> component.subPieceType,
			ClothesDataComponent::new);
	
}