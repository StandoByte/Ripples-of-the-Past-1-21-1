package com.github.standobyte.jojo.powersystem;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.packet.fromserver.TrPowerDataPacket;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.skill.UnlockableSkill;
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
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class PowerData implements INBTSerializable<CompoundTag> {
	public final PowerType powerType;
	protected final Set<String> _allLockedAbilities = new HashSet<>();
	
	protected Set<String> unlockedSkills = new HashSet<>();
	public Set<String> _lockedAbilities = new HashSet<>();
	
	public PowerData(PowerType powerType) {
		this.powerType = powerType;
		for (var skillEntry : getPowerType().getUnlockableSkills().entrySet()) {
			UnlockableSkill skill = skillEntry.getValue();
			_allLockedAbilities.addAll(skill.unlocksAbilities);
		}
	}
	
	public PowerType getPowerType() {
		return powerType;
	}
	
	
	public void onInit(Power<?> userPower) {
		for (var skillEntry : getPowerType().getUnlockableSkills().entrySet()) {
			UnlockableSkill skill = skillEntry.getValue();
			if (skill.isStarting) {
				String skillName = skillEntry.getKey();
				_setSkillUnlocked(skillName, true, false);
			}
		}
	}
	
	public boolean isSkillUnlocked(String skillName) {
		return unlockedSkills.contains(skillName);
	}
	
	public boolean unlockSkill(Power<?> userPower, String skillName) {
		LivingEntity user = userPower.getUser();
		if (user.level().isClientSide()) return false;
		
		PowerType powerType = userPower.getPowerType();
		if (powerType != null && !isSkillUnlocked(skillName)) {
			UnlockableSkill skill = powerType.getUnlockableSkills().get(skillName);
			if (skill != null) {
				ConditionCheck canUnlock = skill.canUnlockFromMenu(userPower, this);
				if (canUnlock.isPositive()) {
					_setSkillUnlocked(skillName, true, true);
					syncOnUpdate(user);
					return true;
				}
			}
		}
		return false;
	}

	public void resetUnlockedSkills(Power<?> userPower) {
		LivingEntity user = userPower.getUser();
		if (user.level().isClientSide()) return;
		
		for (var skillEntry : getPowerType().getUnlockableSkills().entrySet()) {
			UnlockableSkill skill = skillEntry.getValue();
			if (!skill.isStarting) {
				String skillName = skillEntry.getKey();
				_setSkillUnlocked(skillName, false, true);
			}
		}
		
		syncOnUpdate(user);
	}
	
	@ApiStatus.NonExtendable
	public boolean _setSkillUnlocked(String skillName, boolean unlocked, boolean inGameplay) {
		UnlockableSkill skill = getPowerType().getUnlockableSkills().get(skillName);
		return skill != null ? _setSkillUnlocked(skill, unlocked, inGameplay) : false;
	}
	
	@ApiStatus.Internal
	public boolean _setSkillUnlocked(UnlockableSkill skill, boolean unlocked, boolean inGameplay) {
		if (unlocked) {
			if (unlockedSkills.add(skill.skillName)) {
				_lockedAbilities.removeAll(skill.unlocksAbilities);
				return true;
			}
		}
		else {
			if (unlockedSkills.remove(skill.skillName)) {
				_lockedAbilities.addAll(skill.unlocksAbilities);
				return true;
			}
		}
		
		return false;
	}

	@ApiStatus.Internal
	public void _clearUnlockedSkills() {
		unlockedSkills.clear();
		_lockedAbilities.clear();
		_lockedAbilities.addAll(_allLockedAbilities);
	}
	

	public abstract PowerClass<?> getPowerClass();
	

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		ListTag skillsNbt = new ListTag();
		unlockedSkills.forEach(skillName -> skillsNbt.add(StringTag.valueOf(skillName)));
		nbt.put("skills", skillsNbt);

		return nbt;
	}
	
	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		_clearUnlockedSkills();
		NBTUtil.getElementOptional(nbt, "skills", ListTag.class).ifPresent(skillsNbt -> {
			if (skillsNbt.getElementType() == Tag.TAG_STRING) {
				for (Tag element : skillsNbt) {
					_setSkillUnlocked(element.getAsString(), true, false);
				}
			}
		});

	}
	
	
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		if (!isSentToTracking) {
			NetworkUtil.writeCollection(buf, unlockedSkills, FriendlyByteBuf::writeUtf);
		}
	}
	
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		if (!isSentToTracking) {
			_clearUnlockedSkills();
			for (String skillName : NetworkUtil.readCollection(buf, FriendlyByteBuf::readUtf)) {
				_setSkillUnlocked(skillName, true, false);
			}
		}
	}
	
	@ApiStatus.NonExtendable
	public void syncToPlayer(ServerPlayer user) {
		PacketDistributor.sendToPlayer(user, new TrPowerDataPacket(user.getId(), getPowerClass(), this, false));
	}

	@ApiStatus.NonExtendable
	public void syncToTracking(LivingEntity user, ServerPlayer tracking) {
		PacketDistributor.sendToPlayer(tracking, new TrPowerDataPacket(user.getId(), getPowerClass(), this, true));
	}

	@ApiStatus.NonExtendable
	public void syncToAllTracking(LivingEntity user) {
		PacketDistributor.sendToPlayersTrackingEntity(user, new TrPowerDataPacket(user.getId(), getPowerClass(), this, true));
	}
	
	public void syncOnUpdate(LivingEntity user) {
		if (!user.level().isClientSide()) {
			syncToAllTracking(user);
			if (user instanceof ServerPlayer player) {
				syncToPlayer(player);
			}
		}
	}
	
}
