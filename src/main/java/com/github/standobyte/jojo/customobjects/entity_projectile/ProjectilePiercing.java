package com.github.standobyte.jojo.customobjects.entity_projectile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

public class ProjectilePiercing {
	private Collection<UUID> piercingIgnoreEntityIds;

	public ProjectilePiercing(int maxExpected) {
		piercingIgnoreEntityIds = new ArrayList<>(maxExpected);
	}

	public void resetPiercedEntities() {
		if (this.piercingIgnoreEntityIds != null) {
			this.piercingIgnoreEntityIds.clear();
		}
	}

	public boolean pierceEntity(EntityHitResult result, int pierceLevel) {
		if (pierceLevel > 0) {
			if (this.piercingIgnoreEntityIds.size() >= pierceLevel - 1) {
				return false;
			}

			Entity entity = result.getEntity();
			this.piercingIgnoreEntityIds.add(entity.getUUID());
			return true;
		}
		return false;
	}

	public boolean alreadyHit(Entity entity) {
		return this.piercingIgnoreEntityIds.contains(entity.getUUID());
	}


	public Tag toNBT() {
		ListTag list = new ListTag();
		for (UUID id : piercingIgnoreEntityIds) {
			list.add(NbtUtils.createUUID(id));
		}
		return list;
	}

	public static ProjectilePiercing fromNBT(Tag tag) {
		if (tag instanceof ListTag list) {
			ProjectilePiercing piercing = new ProjectilePiercing(list.size());
			for (Tag element : list) {
				UUID uuid = NbtUtils.loadUUID(element);
				if (uuid != null) {
					piercing.piercingIgnoreEntityIds.add(null);
				}
			}
			return piercing;
		}
		return null;
	}

}
