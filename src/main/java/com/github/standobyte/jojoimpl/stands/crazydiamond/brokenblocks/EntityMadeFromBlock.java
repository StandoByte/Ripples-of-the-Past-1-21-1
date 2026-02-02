package com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface EntityMadeFromBlock {
	/** @return true if the block can be restored by the terrain restoration ability */
	boolean crazyDRestore(BlockPos blockPos);
	default boolean isEntityAlive() {
		return ((Entity) this).isAlive();
	}
	
	public static abstract class VanillaEntityWrapper<E extends Entity> implements EntityMadeFromBlock {
		protected WeakReference<E> entity;
		
		public VanillaEntityWrapper(E vanillaEntity) {
			this.entity = new WeakReference<>(vanillaEntity);
		}
		
		@Override
		public boolean isEntityAlive() {
			E entity = this.entity.get();
			return entity != null && entity.isAlive();
		}
	}
	
	
	public static class EntityReference {
		protected WeakReference<EntityMadeFromBlock> directEntityRef;
		protected EntityMadeFromBlock wrapperClass;
		
		protected EntityReference(
				WeakReference<EntityMadeFromBlock> ripplesEntityDirectRef, 
				EntityMadeFromBlock orVanillaEntityWrapperClass) {
			this.directEntityRef = ripplesEntityDirectRef;
			this.wrapperClass = orVanillaEntityWrapperClass;
		}
		
		public static EntityReference makeStorage(EntityMadeFromBlock entity) {
			if (entity instanceof Entity) {
				return new EntityReference(new WeakReference<>(entity), null);
			}
			else {
				return new EntityReference(null, entity);
			}
		}
		
		@Nullable
		public EntityMadeFromBlock get() {
			if (wrapperClass != null) {
				return wrapperClass;
			}
			if (directEntityRef != null) {
				return directEntityRef.get();
			}
			return null;
		}
		
	}
}
