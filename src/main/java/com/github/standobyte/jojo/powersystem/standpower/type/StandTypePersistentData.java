package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.skill.UnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.packet.StandExpPacket;
import com.github.standobyte.jojo.util.NBTUtil;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
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
	
	public void setExp(float exp, LivingEntity standUser) {
		this.exp = exp;
		syncExp(standUser);
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
	

	// I guess I'll keep this just as a stat
	protected int resolveReached;
	public void incResolveReached(LivingEntity standUser) {
		++resolveReached;
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		nbt.putFloat("exp", exp);
		nbt.putInt("resolveReached", resolveReached);
		nbt.put("defeatedChars", NBTUtil.toList(defeatedCharacters, NbtUtils::createUUID));
		nbt.put("defeatedStands", NBTUtil.toList(defeatedStands, ResourceLocation.CODEC));
		return nbt;
	}
	
	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		this.exp = nbt.getFloat("exp");
		this.resolveReached = nbt.getInt("resolveReached");
		NBTUtil.fromList(nbt, "defeatedChars", defeatedCharacters::add, NbtUtils::loadUUID);
		NBTUtil.fromList(nbt, "defeatedStands", defeatedStands, ResourceLocation.CODEC);
	}
	
	@Override
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		super.toBuf(buf, isSentToTracking);
		if (!isSentToTracking) {
			buf.writeFloat(exp);
			buf.writeVarInt(resolveReached);
		}
	}

	@Override
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		super.fromBuf(buf, isSentToTracking);
		if (!isSentToTracking) {
			exp = buf.readFloat();
			resolveReached = buf.readVarInt();
		}
	}
	
	
	@Override public PowerClass<?> getPowerClass() { return PowerClass.STAND; }
	
}
