package com.github.standobyte.jojo.mechanics.clothes.client.layer;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.utils.ModelUtil;
import com.github.standobyte.jojo.core.utils.EnumUtil;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class HumanoidClothesRSExtension {
	public boolean hasClothesComponent;
	public final Map<ClothesSlotType, ItemStack> items = EnumUtil.makeEnumMap(ClothesSlotType.class, slot -> ItemStack.EMPTY);
	public boolean slimModel;
	
	public final boolean extract(LivingEntity entity) {
		EntityClothesInventory entityClothes = EntityClothesInventory.getExisting(entity);
		hasClothesComponent = entityClothes != null;
		if (!hasClothesComponent) return false;
		
		for (ClothesSlotType slot : ClothesSlotType.values()) {
			items.put(slot, entityClothes.getClothingPiece(slot));
		}
		slimModel = ModelUtil.isSlimModel(entity);
		return true;
	}
	
	public static final HumanoidClothesRSExtension reusedInstance = new HumanoidClothesRSExtension();
	
	@Nullable 
	public static HumanoidClothesRSExtension getCurRenderData() {
		return reusedInstance.hasClothesComponent ? reusedInstance : null;
	}
	
}