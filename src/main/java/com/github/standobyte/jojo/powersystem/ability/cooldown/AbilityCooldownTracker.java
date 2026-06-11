package com.github.standobyte.jojo.powersystem.ability.cooldown;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.entityattachment.SynchronizablePlayerData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public class AbilityCooldownTracker implements INBTSerializable<CompoundTag>, TickingEntityData, SynchronizablePlayerData {

	static record Cooldown(int startTime, int endTime) {}

	private final Map<AbilityId, AbilityCooldownTracker.Cooldown> cooldowns = new HashMap<>();
	private int tickCount;
	@Nullable private final ServerPlayer playerEntity;

	public AbilityCooldownTracker(LivingEntity entity) {
		addTicking(entity);
		addSynchronization(entity);
		playerEntity = entity instanceof ServerPlayer __ ? __ : null;
	}

	public boolean isOnCooldown(Ability ability) {
		return getCooldownTimer(ability.abilityId) > 0;
	}

	public int getCooldownTimer(AbilityId abilityId) {
		AbilityCooldownTracker.Cooldown cooldown = cooldowns.get(abilityId);
		if (cooldown != null) {
			int cooldownValue = cooldown.endTime - tickCount;
			return cooldownValue;
		}
		return 0;
	}

	public float getCooldownRatio(AbilityId abilityId, float partialTick) {
		AbilityCooldownTracker.Cooldown cooldown = cooldowns.get(abilityId);
		if (cooldown != null) {
			float cooldownTotal = (float) (cooldown.endTime - cooldown.startTime);
			float cooldownValue = (float) cooldown.endTime - ((float) tickCount + partialTick);
			return Mth.clamp(cooldownValue / cooldownTotal, 0.0F, 1.0F);
		}
		return 0.0F;
	}

	@Override
	public void tick() {
		++tickCount;
		if (!cooldowns.isEmpty()) {
			var iterator = cooldowns.entrySet().iterator();
			while (iterator.hasNext()) {
				var entry = iterator.next();
				if (entry.getValue().endTime <= tickCount) {
					iterator.remove();
				}
			}
		}
	}


	public void addAndSendCooldown(AbilityId abilityId, int ticks) {
		setCooldown(abilityId, ticks, ticks);
		if (playerEntity != null) {
			PacketDistributor.sendToPlayer(playerEntity, AbilityCooldownPacket.singleCooldown(playerEntity.getId(), abilityId, ticks));
		}
	}

	public void setCooldown(AbilityId abilityId, int ticks, int totalTicks) {
		ticks = Math.max(ticks, 0);
		totalTicks = Math.max(totalTicks, 0);
		cooldowns.put(abilityId, new AbilityCooldownTracker.Cooldown(tickCount + ticks - totalTicks, tickCount + ticks));
	}

	public boolean removeCooldown(AbilityId abilityId) {
		if (cooldowns.remove(abilityId) != null) {
			if (playerEntity != null) {
				PacketDistributor.sendToPlayer(playerEntity, AbilityCooldownPacket.removeCooldown(playerEntity.getId(), abilityId));
			}
			return true;
		}
		return false;
	}

	public void resetCooldowns() {
		cooldowns.clear();
		if (playerEntity != null) {
			PacketDistributor.sendToPlayer(playerEntity, AbilityCooldownPacket.resetAll(playerEntity.getId()));
		}
	}


	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		if (!wasDeath && !cooldowns.isEmpty()) {
			AbilityCooldownTracker newTracker = getOrCreate(newPlayer);
			newTracker.cooldowns.putAll(this.cooldowns);
			newTracker.tickCount = this.tickCount;
		}
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		for (var entry : cooldowns.entrySet()) {
			nbt.putIntArray(
					entry.getKey().toString(), 
					new int[]{ 
							entry.getValue().startTime - tickCount, 
							entry.getValue().endTime - tickCount 
					}
					);
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		tickCount = 0;
		cooldowns.clear();
		for (String key : nbt.getAllKeys()) {
			int[] array = nbt.getIntArray(key);
			if (array.length == 2) {
				AbilityId abilityId = AbilityId.parse(key);
				if (abilityId != null) {
					cooldowns.put(abilityId, new AbilityCooldownTracker.Cooldown(array[0], array[1]));
				}
			}
		}
	}

	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {}

	@Override
	public void syncToPlayer(ServerPlayer entityAsPlayer) {
		AbilityCooldownPacket packet = AbilityCooldownPacket.createForMultipleSync(entityAsPlayer.getId());
		for (var entry : cooldowns.entrySet()) {
			AbilityId abilityId = entry.getKey();
			Cooldown cooldown = entry.getValue();
			packet.addCooldown(abilityId, cooldown.endTime - tickCount, cooldown.endTime - cooldown.startTime);
		}
		PacketDistributor.sendToPlayer(entityAsPlayer, packet);
	}


	@Nullable
	public static AbilityCooldownTracker get(LivingEntity user) {
		return ComponentUtil.getExistingDataOrNull(user, ModDataAttachmentTypes.ABILITY_COOLDOWNS);
	}

	public static AbilityCooldownTracker getOrCreate(LivingEntity user) {
		return user.getData(ModDataAttachmentTypes.ABILITY_COOLDOWNS);
	}

}
