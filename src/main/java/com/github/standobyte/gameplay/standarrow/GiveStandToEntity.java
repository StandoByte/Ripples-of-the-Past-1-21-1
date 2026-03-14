package com.github.standobyte.gameplay.standarrow;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.github.standobyte.jojo.init.power.ModStands;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GiveStandToEntity {
    
    public static boolean onPiercedByArrow(Level level, LivingEntity entity, ItemStack arrowItem, 
    		@Nullable Entity directDamageEntity, @Nullable Entity standGivingCharacter) {
        if (!level.isClientSide()) {
        	boolean givePowerTypeStand = entity.getType() == EntityType.PLAYER;
        	
        	// TODO event
        	if (givePowerTypeStand) {
        		StandPower stand = StandPower.get(entity);
        		if (!stand.hasPower()) {
        			StandType standToGive = pickStandToGive(entity);
        			if (standToGive != null) {
        				stand.setStand(standToGive);
        				return true;
        			}
        		}
        	}
        }
        return false;
    }


    public static Stream<StandType> getStandsForPlayer() {
    	return StandType.getAllEnabledStands().filter(ModStands.PLAYER_CAN_GET_FROM_ARROW::contains);
    }
    
    @Nullable
    public static StandType pickStandToGive(LivingEntity entity) {
    	List<StandType> stands = GiveStandToEntity.getStandsForPlayer().toList();
    	if (!stands.isEmpty()) {
    		return stands.get(entity.getRandom().nextInt(stands.size()));
    	}
    	return null;
    }
}
