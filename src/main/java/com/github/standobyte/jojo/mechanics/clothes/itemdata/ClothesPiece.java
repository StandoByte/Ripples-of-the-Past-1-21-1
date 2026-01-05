package com.github.standobyte.jojo.mechanics.clothes.itemdata;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.v1_21_4_stuff.missingmethods.EquipmentAsset;
import com.github.standobyte.v1_21_4_stuff.missingmethods.EquipmentAssets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public class ClothesPiece {
	public final ResourceKey<EquipmentAsset> modelId;
	public final ResourceKey<EquipmentAsset> textureId;
	public final ResourceLocation itemModel;
	public final Component itemName;
	public final Holder<SoundEvent> equipSound;

	public final ResourceLocation textureActualPath;
	
	private final Optional<Map<SubClothingPiece, ClothesPiece>> splitInto;
	private Optional<Map<SubClothingPiece, ClothesPiece>> allSplitPieces;
	private SubClothingPiece subPieceType;
	
	public ClothesPiece(ResourceKey<EquipmentAsset> assetId, Optional<ResourceKey<EquipmentAsset>> textureId, 
			ResourceLocation itemModel, Component itemName,
			Holder<SoundEvent> equipSound, Optional<Map<SubClothingPiece, ClothesPiece>> splitInto) {
		this.modelId = assetId;
		this.textureId = textureId.orElse(assetId);
		this.itemModel = itemModel;
		this.itemName = itemName;
		this.equipSound = equipSound;
		
		this.textureActualPath = this.textureId.location().withPath(p -> "textures/clothes/" + p + ".png");;
		
		this.subPieceType = SubClothingPiece.FULL;
		this.splitInto = splitInto;
		
		if (splitInto.isPresent()) {
			this.allSplitPieces = splitInto.map(subPiecesMap -> {
				Map<SubClothingPiece, ClothesPiece> mutableMap = Util.make(new EnumMap<>(SubClothingPiece.class), map -> {
					map.putAll(subPiecesMap);
					map.put(SubClothingPiece.FULL, this);
					
					ClothesPiece topPiece = map.get(SubClothingPiece.TOP);
					topPiece.subPieceType = SubClothingPiece.TOP;
					topPiece.allSplitPieces = Optional.of(new EnumMap<>(map));
					
					ClothesPiece bottomPiece = map.get(SubClothingPiece.BOTTOM);
					bottomPiece.subPieceType = SubClothingPiece.BOTTOM;
					bottomPiece.allSplitPieces = Optional.of(new EnumMap<>(map));
				});
				return mutableMap;
			});
		}
		else {
			this.allSplitPieces = Optional.empty();
		}
	}
	
	public boolean hasSubType() {
		return allSplitPieces.isPresent();
	}
	
	@Nullable
	public ClothesPiece getSubPiece(SubClothingPiece subPieceType) {
		return allSplitPieces.map(map -> map.get(subPieceType)).orElse(null);
	}
	
	
	public enum SubClothingPiece implements StringRepresentable {
		FULL("full"),
		TOP("top"),
		BOTTOM("bottom");
		
		private final String name;
		
		private SubClothingPiece(String name) {
			this.name = name;
		}

		public static final Codec<SubClothingPiece> CODEC = StringRepresentable.fromEnum(SubClothingPiece::values);
		public static final StreamCodec<FriendlyByteBuf, SubClothingPiece> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(SubClothingPiece.class);
		
		@Override
		public String getSerializedName() {
			return name;
		}
	}

	
	@ApiStatus.Internal
	public static final Codec<ClothesPiece> CODEC_NO_SPLIT_PARTS = RecordCodecBuilder.create(
			builder -> builder.group(
					ResourceKey.codec(EquipmentAssets.ROOT_ID).fieldOf("asset_id").forGetter(piece -> piece.modelId),
					ResourceKey.codec(EquipmentAssets.ROOT_ID).optionalFieldOf("texture_id").forGetter(piece -> piece.textureId.equals(piece.modelId) ? Optional.empty() : Optional.of(piece.textureId)),
					ResourceLocation.CODEC.fieldOf("item_model").forGetter(piece -> piece.itemModel),
					ComponentSerialization.CODEC.fieldOf("item_name").forGetter(piece -> piece.itemName),
					SoundEvent.CODEC.optionalFieldOf("equip_sound", SoundEvents.ARMOR_EQUIP_GENERIC).forGetter(piece -> piece.equipSound),
					RecordCodecBuilder.point(Optional.<Map<SubClothingPiece, ClothesPiece>>empty()))
			.apply(builder, ClothesPiece::new));
	
	public static final Codec<ClothesPiece> CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					ResourceKey.codec(EquipmentAssets.ROOT_ID).fieldOf("asset_id").forGetter(piece -> piece.modelId),
					ResourceKey.codec(EquipmentAssets.ROOT_ID).optionalFieldOf("texture_id").forGetter(piece -> piece.textureId.equals(piece.modelId) ? Optional.empty() : Optional.of(piece.textureId)),
					ResourceLocation.CODEC.fieldOf("item_model").forGetter(piece -> piece.itemModel),
					ComponentSerialization.CODEC.fieldOf("item_name").forGetter(piece -> piece.itemName),
					SoundEvent.CODEC.optionalFieldOf("equip_sound", SoundEvents.ARMOR_EQUIP_GENERIC).forGetter(piece -> piece.equipSound),
					Codec.unboundedMap(SubClothingPiece.CODEC, ClothesPiece.CODEC_NO_SPLIT_PARTS).optionalFieldOf("split_into").forGetter(set -> set.splitInto))
			.apply(builder, ClothesPiece::new));
	
}