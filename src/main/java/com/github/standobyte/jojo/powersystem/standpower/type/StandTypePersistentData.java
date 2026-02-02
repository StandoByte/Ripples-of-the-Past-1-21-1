package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.util.NBTUtil;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class StandTypePersistentData extends PowerData {
	public Set<String> unlockedSkills = new HashSet<>();
	protected int resolveReached;
	
	
	public boolean isSkillUnlocked(String skillName) {
		return unlockedSkills.contains(skillName);
	}
	
	public boolean setSkillUnlocked(ResourceLocation powerType, String skillName, boolean unlocked) {
		if (unlocked) {
			return unlockedSkills.add(skillName);
		}
		else {
			return unlockedSkills.remove(skillName);
		}
	}
	
	
	public void incResolveReached(LivingEntity standUser) {
		++resolveReached;
		syncOnUpdate(standUser);
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		ListTag skillsNbt = new ListTag();
		unlockedSkills.forEach(skillName -> skillsNbt.add(StringTag.valueOf(skillName)));
		nbt.put("skills", skillsNbt);

		nbt.putInt("resolveReached", resolveReached);
		return nbt;
	}
	
	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		unlockedSkills.clear();
		NBTUtil.getElementOptional(nbt, "skills", ListTag.class).ifPresent(skillsNbt -> {
			if (skillsNbt.getElementType() == Tag.TAG_STRING) {
				for (Tag element : skillsNbt) {
					unlockedSkills.add(element.getAsString());
				}
			}
		});
		
		this.resolveReached = nbt.getInt("resolveReached");
	}
	
	@Override
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		if (!isSentToTracking) {
			buf.writeVarInt(resolveReached);
			NetworkUtil.writeCollection(buf, unlockedSkills, FriendlyByteBuf::writeUtf);
		}
	}

	@Override
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		if (!isSentToTracking) {
			resolveReached = buf.readVarInt();
			this.unlockedSkills.clear();
			this.unlockedSkills.addAll(NetworkUtil.readCollection(buf, FriendlyByteBuf::readUtf));
		}
	}
	
	
	@Override public PowerClass<?> getPowerClass() { return PowerClass.STAND; }
	
}
