package com.github.standobyte.jojo.entityattachment;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * For entity data attachments, {@link INBTSerializable#deserializeNBT(HolderLookup.Provider, Tag)}
 * is called before {@link Entity#readAdditionalSaveData(CompoundTag)}.<br>
 * But for Stand stats, I need to update the base LivingEntity attributes values from the user's Stand stats,
 * which I can only do after {@link LivingEntity#readAdditionalSaveData(CompoundTag)} 
 * (or else they'll get overwritten with the data from NBT).<br>
 * {@link PostNbtReadEntityData#afterNbtRead()} is called after the Entity is done deserializing.
 */
public interface PostNbtReadEntityData {
	void afterNbtRead();
	
	default void addPostNbtReadCallback(Entity entity) {
		entity.getData(ModDataAttachmentTypes.DATA_EVENT_HELPER.get()).addPostNbtReadCallback(this);
	}

}
