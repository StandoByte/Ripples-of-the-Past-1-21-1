package com.github.standobyte.jojo.mechanics.clothes;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesPiece;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesPiece.SubClothingPiece;
import com.github.standobyte.v1_21_4_stuff.itemmodel.__ItemModelComponent;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ItemStack;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class ClothesItem extends Item {
	
	public ClothesItem(Item.Properties properties) {
		super(properties.stacksTo(1));
	}
	
	@Nullable
	public static ClothesDataComponent makeItemComponent(Holder<ClothesSet> clothesSetHolder, ClothesSlotType slot) {
		ClothesSet clothesSet = clothesSetHolder.value();
		if (clothesSet != null) {
			ClothesPiece piece = clothesSet.getPiece(slot);
			if (piece != null) {
				ClothesDataComponent component = new ClothesDataComponent(clothesSetHolder, slot, ClothesPiece.SubClothingPiece.FULL);
				return component;
			}
		}
		return null;
	}
	
	public ItemStack makeClothesPieceStack(ClothesDataComponent clothesData) {
		if (clothesData == null) {
			return ItemStack.EMPTY;
		}
		
		ItemStack stack = new ItemStack(this);
		stack.set(ModItemDataComponents.CLOTHES_PIECE.get(), clothesData);
		
		ResourceLocation itemModel = clothesData.getPiece().itemModel;
		if (itemModel != null) {
			__ItemModelComponent.set(stack, itemModel);
		}
		
		Component itemName = clothesData.getPiece().itemName;
		if (itemName != null) {
			stack.set(DataComponents.ITEM_NAME, itemName);
		}
		
		return stack;
	}
	
	public ItemStack makeClothesPieceStack(Holder<ClothesSet> clothesSetHolder, ClothesSlotType slot) {
		return makeClothesPieceStack(makeItemComponent(clothesSetHolder, slot));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		ClothesDataComponent itemClothes = heldItem.get(ModItemDataComponents.CLOTHES_PIECE);
		if (itemClothes == null) {
			return InteractionResultHolder.pass(heldItem);
		}
		ClothesSlotType clothesSlot = itemClothes.getSlot();

		EntityClothesInventory playerClothes = player.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
		ItemStack wornItem = playerClothes.getClothingPiece(clothesSlot);

		if ((!EnchantmentHelper.has(wornItem, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || player.isCreative())
				&& !ItemStack.matches(heldItem, wornItem)) {
			if (!level.isClientSide()) {
				player.awardStat(Stats.ITEM_USED.get(this));
			}
			
			ItemStack setHeldItem;
			ItemStack putOnItem;
			ItemStack combined = combineIntoFullPiece(heldItem, wornItem);
			if (combined != null) {
				setHeldItem = player.isCreative() ? heldItem : ItemStack.EMPTY;
				putOnItem = combined;
			}
			else {
				setHeldItem = wornItem.isEmpty() ? heldItem : wornItem.copyAndClear();
				putOnItem = player.isCreative() ? heldItem.copy() : heldItem.copyAndClear();
			}

			playerClothes.setItemSlot(clothesSlot, putOnItem);
			
			return InteractionResultHolder.sidedSuccess(setHeldItem, level.isClientSide());
		} else {
			return InteractionResultHolder.fail(heldItem);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		ClothesDataComponent clothes = getPiece(stack);		if (clothes == null) return;
		var clothesSet = clothes.getClothesSet().value();	if (clothesSet == null) return;
		
		clothesSet.getStoryPart().ifPresent(storyPart -> {
			Component partName = storyPart.value().getPartName();
			if (partName != null) {
				tooltipComponents.add(partName);
			}
		});
		
		var splitPieces = clothes.splitInto(null);
		if (splitPieces != null) {
			tooltipComponents.add(Component.translatable("ripples_clothes_split", 
					_ItemStack.getStyledHoverName(splitPieces.getFirst())
							.copy().withStyle(style -> style.withColor(ChatFormatting.GRAY)), 
					_ItemStack.getStyledHoverName(splitPieces.getSecond())
							.copy().withStyle(style -> style.withColor(ChatFormatting.GRAY)))
					.withStyle(ChatFormatting.DARK_GRAY));
		}
		
		var canCombinePieces = clothes.combineWithOtherPieceToGetFull(null, null);
		if (canCombinePieces != null) {
			tooltipComponents.add(Component.translatable("ripples_clothes_combine", 
					canCombinePieces.getSecond().itemName
							.copy().withStyle(style -> style.withColor(ChatFormatting.GRAY)), 
					_ItemStack.getStyledHoverName(canCombinePieces.getFirst())
							.copy().withStyle(style -> style.withColor(ChatFormatting.GRAY)))
					.withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	public ClothesDataComponent getPiece(ItemStack itemStack) {
		return itemStack.get(ModItemDataComponents.CLOTHES_PIECE.get());
	}


//	@Override
//	public boolean overrideStackedOnOther(ItemStack carriedItem, Slot slot, ClickAction mouseButton, Player player) {
//		return false;
//	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack clickedItem, ItemStack carriedItem, Slot slot, ClickAction mouseButton, Player player, SlotAccess mousePick) {
		if (slot.allowModification(player)) {
			// Split a two-piece item by right-clicking
			if (mouseButton == ClickAction.SECONDARY && carriedItem.isEmpty()) {
				ClothesDataComponent clothesData = clickedItem.get(ModItemDataComponents.CLOTHES_PIECE.get());
				if (clothesData != null) {
					var splitInto = clothesData.splitInto(clickedItem);
					if (splitInto != null) {
						ItemStack top = splitInto.getFirst().copy();
						ItemStack bottom = splitInto.getSecond().copy();
						mousePick.set(top);
						slot.setByPlayer(bottom);
						broadcastChangesOnContainerMenu(player);
						return true;
					}
				}
			}
			
			// Combine two pieces by left-clicking
			if (mouseButton == ClickAction.PRIMARY) {
				ItemStack fullPiece = combineIntoFullPiece(clickedItem, carriedItem);
				if (fullPiece != null) {
					carriedItem.setCount(0);
					slot.setByPlayer(fullPiece.copy());
					broadcastChangesOnContainerMenu(player);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * This method has no side effects on the item stacks that are passed as arguments.
	 */
	@Nullable
	public static ItemStack combineIntoFullPiece(ItemStack item1, ItemStack item2) {
		ClothesDataComponent _clothesData1 = item1.get(ModItemDataComponents.CLOTHES_PIECE.get());		if (_clothesData1 == null) return null;
		ClothesDataComponent _clothesData2 = item2.get(ModItemDataComponents.CLOTHES_PIECE.get());		if (_clothesData2 == null) return null;
		
		ItemStack bottomItem = null;
		ItemStack topItem = null;
		ClothesDataComponent bottomClothes = null;
		ClothesDataComponent topClothes = null;
		if (_clothesData1.getSubType() == SubClothingPiece.TOP && _clothesData2.getSubType() == SubClothingPiece.BOTTOM) {
			topItem = item1;
			topClothes = _clothesData1;
			bottomItem = item2;
			bottomClothes = _clothesData2;
		}
		else if (_clothesData1.getSubType() == SubClothingPiece.BOTTOM && _clothesData2.getSubType() == SubClothingPiece.TOP) {
			bottomItem = item1;
			bottomClothes = _clothesData1;
			topItem = item2;
			topClothes = _clothesData2;
		}
		else {
			return null;
		}
		
		var fullItem_topPiece = bottomClothes.combineWithOtherPieceToGetFull(bottomItem, topItem);
		if (fullItem_topPiece.getSecond().equals(topClothes.getPiece())) {
			return fullItem_topPiece.getFirst();
		}
		
		return null;
	}

    public static void broadcastChangesOnContainerMenu(Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null) {
            menu.slotsChanged(player.getInventory());
        }
    }
	
}