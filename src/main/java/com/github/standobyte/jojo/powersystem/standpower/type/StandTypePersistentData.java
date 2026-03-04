package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.packet.StandExpPacket;
import com.github.standobyte.jojo.util.NBTUtil;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandTypePersistentData extends PowerData {
	public Set<String> unlockedSkills = new HashSet<>();
	protected float exp;
	
	
	public boolean isSkillUnlocked(String skillName) {
		return unlockedSkills.contains(skillName);
	}
	
	public boolean setSkillUnlocked(String skillName, boolean unlocked) {
		if (unlocked) {
			return unlockedSkills.add(skillName);
		}
		else {
			return unlockedSkills.remove(skillName);
		}
	}

	
	@Override
	public void onInit(PowerType powerType, Power<?> userPower) {
		LivingEntity user = userPower.getUser();
		if (!user.level().isClientSide()) {
			StandType standType = (StandType) powerType;
			for (var skillEntry : standType.getUnlockableSkills().entrySet()) {
				StandUnlockableSkill skill = skillEntry.getValue();
				if (skill.isStarting) {
					String skillName = skillEntry.getKey();
					unlockedSkills.add(skillName);
				}
			}
		}
	}
	
	@Override
	public boolean unlockSkill(Power<?> userPower, String skillName) {
		LivingEntity user = userPower.getUser();
		if (user.level().isClientSide()) return false;
		
		StandPower standPower = PowerClass.STAND.cast(userPower);
		StandType standType = standPower.getPowerType();
		if (standType != null && !isSkillUnlocked(skillName)) {
			StandUnlockableSkill skill = standType.getUnlockableSkills().get(skillName);
			if (skill != null && skill.canUnlockFromMenu(standPower, this).isPositive()) {
				setSkillUnlocked(skillName, true);
				this.exp -= skill.expToUnlock;
				syncOnUpdate(user);
				return true;
			}
		}
		return false;
	}
	
	
	public int getExp() {
		return (int) exp;
	}
	
	public int addExp(float exp, LivingEntity standUser) {
		int prevInt = (int) this.exp;
		this.exp += exp;
		int newInt = (int) this.exp;
		syncExp(standUser);
		return newInt - prevInt;
	}
	
	public void setExp(float exp, LivingEntity standUser) {
		this.exp = exp;
		syncExp(standUser);
	}
	
	protected void syncExp(LivingEntity standUser) {
		if (standUser instanceof ServerPlayer player) {
			PacketDistributor.sendToPlayer(player, new StandExpPacket(this.exp));
		}
	}
	

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		ListTag skillsNbt = new ListTag();
		unlockedSkills.forEach(skillName -> skillsNbt.add(StringTag.valueOf(skillName)));
		nbt.put("skills", skillsNbt);

		nbt.putFloat("exp", exp);
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
		
		this.exp = nbt.getFloat("exp");
	}
	
	@Override
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		if (!isSentToTracking) {
			buf.writeFloat(exp);
			NetworkUtil.writeCollection(buf, unlockedSkills, FriendlyByteBuf::writeUtf);
		}
	}

	@Override
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		if (!isSentToTracking) {
			exp = buf.readFloat();
			this.unlockedSkills.clear();
			this.unlockedSkills.addAll(NetworkUtil.readCollection(buf, FriendlyByteBuf::readUtf));
		}
	}
	
	
	@Override public PowerClass<?> getPowerClass() { return PowerClass.STAND; }
	
}
