package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.client.standskin.text.StandSkinComponent;
import com.github.standobyte.jojo.client.ui.hud_misc.BottomLeftNotifications;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.packet.StandExpPacket;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;
import com.github.standobyte.jojo.util.functions.NBTUtil;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandTypePersistentData extends PowerData {
	protected float exp;
	public Set<UUID> defeatedCharacters = new HashSet<>();
	public Set<ResourceLocation> defeatedStands = new HashSet<>();
	
	public StandTypePersistentData(StandType powerType) {
		super(powerType);
	}
	
	@Override
	public StandType getPowerType() {
		return (StandType) super.getPowerType();
	}
	
	//@Override
	//public void onInit(Power<?> userPower) {
	//	super.onInit(userPower);
	//}
	
	@Override
	public boolean _setSkillUnlocked(UnlockableSkill skill, boolean unlocked, boolean inGameplay) {
		boolean changed = super._setSkillUnlocked(skill, unlocked, inGameplay);
		if (changed && inGameplay) {
			if (unlocked) {
				this.exp -= ((StandUnlockableSkill) skill).expToUnlock;
			}
			else {
				this.exp += ((StandUnlockableSkill) skill).expToUnlock;
			}
		}
		return changed;
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
	
	public void setExp(float exp, StandPower userPower) {
		setExp(exp, userPower, false);
	}
	
	public void setExp(float exp, StandPower userPower, boolean clientSideNewSkillNotification) {
		if (clientSideNewSkillNotification && userPower.hasPower()) {
			Collection<? extends UnlockableSkill> couldUnlock = getAllSkills().values().stream()
					.filter(skill -> skill.canUnlockFromMenu(userPower, this).isPositive()).collect(Collectors.toSet());
			
			this.exp = exp;
			
			Collection<? extends UnlockableSkill> newSkillsToUnlock = getAllSkills().values().stream()
					.filter(skill -> skill.canUnlockFromMenu(userPower, this).isPositive() && !couldUnlock.contains(skill)).toList();
			if (!newSkillsToUnlock.isEmpty()) {
				StandInstance stand = userPower.getStandInstance().get();
				BottomLeftNotifications.add(Component.translatable("jojo_ripples.notification.stand_skill"));
				for (UnlockableSkill skill : newSkillsToUnlock) {
					Component skillName = StandSkinComponent.translatable(stand, skill.tlKeyName);
					BottomLeftNotifications.add(Component.translatable("jojo_ripples.list.entry.no_newline", skillName));
				}
			}
		}
		else {
			this.exp = exp;
			syncExp(userPower.getUser());
		}
	}
	
	protected void syncExp(LivingEntity standUser) {
		if (standUser instanceof ServerPlayer player) {
			PacketDistributor.sendToPlayer(player, new StandExpPacket(this.exp));
		}
	}
	
	
	public static class StandExpSummary {
		static StandExpSummary instance = new StandExpSummary();
		public int spent, total, devPotential, remainingSkills, remainingHiddenSkills;
		StandExpSummary clear() { spent = 0; total = 0; devPotential = 0; remainingSkills = 0; remainingHiddenSkills = 0; return this; }
	}
	public StandExpSummary expSummary(StandPower userPower) {
		StandExpSummary obj = StandExpSummary.instance.clear();
		for (var skillEntry : getAllSkills().entrySet()) {
			StandUnlockableSkill skill = (StandUnlockableSkill) skillEntry.getValue();
			boolean isUnlocked = isSkillUnlocked(skill.skillName);
			if (skill.expToUnlock > 0) {
				obj.total += skill.expToUnlock;
				if (isUnlocked) {
					obj.spent += skill.expToUnlock;
				}
			}
			if (!isUnlocked) {
				obj.remainingSkills++;
			}
			obj.devPotential += skill.getDevPotentialCosmeticPoints(userPower, this, isUnlocked);
		}
		return obj;
	}
	

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		nbt.putFloat("exp", exp);
		nbt.put("defeatedChars", NBTUtil.toList(defeatedCharacters, NbtUtils::createUUID));
		nbt.put("defeatedStands", NBTUtil.toList(defeatedStands, ResourceLocation.CODEC));
		return nbt;
	}
	
	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		this.exp = nbt.getFloat("exp");
		NBTUtil.fromList(nbt, "defeatedChars", defeatedCharacters::add, NbtUtils::loadUUID);
		NBTUtil.fromList(nbt, "defeatedStands", defeatedStands, ResourceLocation.CODEC);
	}
	
	@Override
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		super.toBuf(buf, isSentToTracking);
		if (!isSentToTracking) {
			buf.writeFloat(exp);
		}
	}

	@Override
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		super.fromBuf(buf, isSentToTracking);
		if (!isSentToTracking) {
			exp = buf.readFloat();
		}
	}
	
	
	@Override public PowerClass<?> getPowerClass() { return PowerClass.STAND; }
	
}
