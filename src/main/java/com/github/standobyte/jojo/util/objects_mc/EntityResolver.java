package com.github.standobyte.jojo.util.objects_mc;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityResolver {
	protected Entity entity;
	protected LivingEntity entityLiving;
	protected UUID entityUUID;
	protected int entityNetworkId;

	public void setEntity(@Nullable Entity owner) {
		this.entityUUID = owner != null ? owner.getUUID() : null;
		this.entityNetworkId = owner != null ? owner.getId() : 0;
		_setNewEntity(owner);
	}

	public void setEntityUUID(UUID ownerUuid) {
		this.entityUUID = ownerUuid;
	}

	public Entity getEntity(Level level) {
		updateEntity(level);
		return entity;
	}

	public LivingEntity getEntityLiving(Level level) {
		updateEntity(level);
		return entityLiving;
	}

	public void updateEntity(Level level) {
		if (entity != null && entity.isRemoved()) {
			_setNewEntity(null);
		}
		if (entity == null) {
			if (entityUUID != null && level instanceof ServerLevel) {
				_setNewEntity(((ServerLevel) level).getEntity(entityUUID));
			} else if (entityNetworkId != 0) {
				_setNewEntity(level.getEntity(entityNetworkId));
			}
		}
	}

	public boolean hasEntityId() {
		return entityNetworkId > 0;
	}

	protected void _setNewEntity(Entity entity) {
		this.entity = entity;
		this.entityLiving = entity instanceof LivingEntity ? (LivingEntity) entity : null;
		this.entityNetworkId = entity != null ? entity.getId() : 0;
	}



	public void saveNbt(CompoundTag nbt, String key) {
		if (entityUUID != null) {
			nbt.putUUID(key, entityUUID);
		}
	}

	public void loadNbt(CompoundTag nbt, String key) {
		setEntityUUID(nbt.hasUUID(key) ? nbt.getUUID(key) : null);
	}

	public void writeNetwork(FriendlyByteBuf buf) {
		buf.writeInt(entityNetworkId);
	}

	public void readNetwork(FriendlyByteBuf buf) {
		entityNetworkId = buf.readInt();
	}

	public int getNetworkId() {
		return entityNetworkId;
	}
}
