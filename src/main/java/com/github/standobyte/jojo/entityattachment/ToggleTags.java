package com.github.standobyte.jojo.entityattachment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModEntityVariables;
import com.github.standobyte.jojo.network.s2c.EntityToggleTagsPacket;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

/** An object that stores String tags (like when you use {@link Entity#addTag(String)}), 
 *  but also automatically syncs it to clients. */
public class ToggleTags implements INBTSerializable<ListTag> {
	private final Entity entity;
	private final Set<String> tags = new HashSet<>();
	private final Set<String> tagsSaved = new HashSet<>();
	
	public ToggleTags(Entity entity) {
		this.entity = entity;
	}

	public void addTag(String tag, boolean syncToSelf, boolean saveInNBT) {
		tags.add(tag);
		if (!entity.level().isClientSide()) {
			if (saveInNBT) {
				tagsSaved.add(tag);
			}

			EntityToggleTagsPacket packet = new EntityToggleTagsPacket(entity.getId(), Collections.singletonList(tag));
			if (syncToSelf) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
			}
			else {
				PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
			}
		}
	}

	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}

	public boolean removeTag(String tag) {
		if (!entity.level().isClientSide()) {
			tagsSaved.remove(tag);
		}
		return tags.remove(tag);
	}
	
	public void toggleTag(String tag, boolean syncToSelf, boolean saveInNBT) {
		if (hasTag(tag)) {
			removeTag(tag);
		}
		else {
			addTag(tag, syncToSelf, saveInNBT);
		}
	}


	public static ToggleTags get(Entity entity) {
		return JojoModEntityVariables.get(entity).tags;
	}
	
	public static boolean hasTag(Entity entity, String tag) {
		ToggleTags tags = getIfPresent(entity);
		return tags != null && tags.hasTag(tag);
	}
	
	@Nullable
	public static ToggleTags getIfPresent(Entity entity) {
		JojoModEntityVariables<?> vars = JojoModEntityVariables.getIfPresent(entity);
		return vars != null ? vars.tags : null;
	}


	@Override
	public ListTag serializeNBT(HolderLookup.Provider provider) {
		ListTag tagsNBT = new ListTag();
		for (String tag : tags) {
			tagsNBT.add(StringTag.valueOf(tag));
		}
		return tagsNBT;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt) {
		tags.clear();
		tagsSaved.clear();
		for (Tag element : nbt) {
			tags.add(element.getAsString() /* not to be confused with Java's Object#toString() */);
			tagsSaved.add(element.getAsString());
		}
	}


	public void sync(ServerPlayer player) {
		if (!tags.isEmpty()) {
			PacketDistributor.sendToPlayer(player, new EntityToggleTagsPacket(entity.getId(), tags));
		}
	}

	public void cloneData(ToggleTags newData, boolean wasDeath) {
		if (!wasDeath) {
			newData.tags.addAll(this.tags);
			newData.tagsSaved.addAll(this.tagsSaved);
		}
	}
}
