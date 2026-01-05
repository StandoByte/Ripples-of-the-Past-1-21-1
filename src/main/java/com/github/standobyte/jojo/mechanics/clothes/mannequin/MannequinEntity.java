package com.github.standobyte.jojo.mechanics.clothes.mannequin;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.mechanics.clothes.ClothesItem;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;

import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class MannequinEntity extends ArmorStand {
	private static final Rotations ZERO_ROTATIONS = new Rotations(0, 0, 0);
	private EntityClothesInventory clothes;

	public MannequinEntity(EntityType<? extends MannequinEntity> type, Level level) {
		super(type, level);
		entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 4, true)); // setShowArms();
		setHeadPose(ZERO_ROTATIONS);
		setBodyPose(ZERO_ROTATIONS);
		setLeftArmPose(ZERO_ROTATIONS);
		setRightArmPose(ZERO_ROTATIONS);
		setLeftLegPose(ZERO_ROTATIONS);
		setRightLegPose(ZERO_ROTATIONS);
	}

	public MannequinEntity(Level level) {
		this(ModEntityTypes.MANNEQUIN.get(), level);
	}

	public MannequinEntity(Level level, double x, double y, double z) {
		this(level);
		this.setPos(x, y, z);
	}	

	public void setSlim(boolean slim) {
		this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 64, slim));
	}

	public boolean isSlim() {
		return (this.entityData.get(DATA_CLIENT_FLAGS) & 64) != 0;
	}
	
	
	protected EntityClothesInventory clothesLazyInit() {
		if (clothes == null) {
			clothes = this.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
		}
		return clothes;
	}

	@Override
	public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (!this.isMarker() && heldItem.getItem() != Items.NAME_TAG && !player.isSpectator() && !player.level().isClientSide()) {
			clothesLazyInit();
			// Take off a hovered item from the mannequin
			if (heldItem.isEmpty()) {
				ClothesSlotType clickedSlot = getClothesSlotAt(vec);
				if (clickedSlot != null) {
					ItemStack wornClothes = clothes.getClothingPiece(clickedSlot);
					if (!wornClothes.isEmpty()) {
						clothes.setItemSlot(clickedSlot, ItemStack.EMPTY);
						player.setItemInHand(hand, wornClothes);
						player.swing(hand, true);
						return InteractionResult.SUCCESS;
					}
				}
			}

			else {
				ClothesDataComponent heldClothesPiece = heldItem.get(ModItemDataComponents.CLOTHES_PIECE.get());
				if (heldClothesPiece != null) {
					ClothesSlotType clothesSlot = heldClothesPiece.getSlot();
					if (clothesSlot == null) {
						return InteractionResult.FAIL;
					}
					ItemStack wornClothes = clothes.getClothingPiece(clothesSlot);

					// Combine the item in hand and the item worn on the mannequin
					if (!heldItem.isEmpty() && !wornClothes.isEmpty()) {
						ItemStack fullItem = ClothesItem.combineIntoFullPiece(heldItem, wornClothes);
						if (fullItem != null) {
							clothes.setItemSlot(clothesSlot, fullItem);
							if (!player.isCreative()) {
								player.setItemInHand(hand, ItemStack.EMPTY);
							}
							player.swing(hand, true);
							return InteractionResult.SUCCESS;
						}
					}
					
					// Swap the worn item and the item in hand
					clothes.setItemSlot(clothesSlot, heldItem);
					if (!(player.isCreative() && wornClothes.isEmpty())) {
						player.setItemInHand(hand, wornClothes);
					}
					return InteractionResult.SUCCESS;
				}
			}
		}

		return super.interactAt(player, vec, hand);
	}

	@Nullable
	private ClothesSlotType getClothesSlotAt(Vec3 clickedPos) {
		boolean isSmall = isSmall();
		double height = isSmall ? clickedPos.y * 2.0 : clickedPos.y;
		if (height >= 0.1 &&								height < 0.1 + (isSmall ? 0.8 : 0.45)) {
			return ClothesSlotType.FEET;
		} else if (height >= 0.9 + (isSmall ? 0.3 : 0.0) && height < 0.9 + (isSmall ? 1.0 : 0.7)) {
			return ClothesSlotType.CHEST;
		} else if (height >= 0.4						 && height < 0.4 + (isSmall ? 1.0 : 0.8)) {
			return ClothesSlotType.LEGS;
		} else if (height >= 1.6) {
			return ClothesSlotType.HEAD;
		}
		return null;
	}


	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putBoolean("Slim", isSlim());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		setSlim(nbt.getBoolean("Slim"));
	}

	protected byte setBit(byte pOldBit, int pOffset, boolean pValue) {
	   if (pValue) {
		  pOldBit = (byte)(pOldBit | pOffset);
	   } else {
		  pOldBit = (byte)(pOldBit & ~pOffset);
	   }

	   return pOldBit;
	}

	@Override
	public void tick() {
		super.tick();
	}


	@Override
	public ItemStack getPickResult() {
		return new ItemStack(getItem());
	}

	protected Item getItem() {
		return isSlim() ? ModItems.MANNEQUIN_SLIM.get() : ModItems.MANNEQUIN.get();
	}

	@Override
	public void brokenByPlayer(ServerLevel level, DamageSource pDamageSource) {
		ItemStack itemstack = new ItemStack(getItem());
		itemstack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
		Block.popResource(this.level(), this.blockPosition(), itemstack);
		this.brokenByAnything(level, pDamageSource);
	}

	@Override
	public void brokenByAnything(ServerLevel level, DamageSource pDamageSource) {
		super.brokenByAnything(level, pDamageSource);

		var clothes = clothesLazyInit();
		for (ClothesSlotType slot : ClothesSlotType.values()) {
			ItemStack item = clothes.getClothingPiece(slot);
			if (!item.isEmpty()) {
				Block.popResource(this.level(), this.blockPosition().above(), item);
				clothes.setItemSlot(slot, ItemStack.EMPTY);
			}
		}
	}

}