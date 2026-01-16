package com.github.standobyte.jojoimpl.powers.pillarman;

import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.github.standobyte.jojo.powersystem.playerpower.PowerData;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PillarmanData extends PowerData {
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		
	}

	@Override
	public void syncToPlayer(ServerPlayer user) {
		
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
		
	}

	@Override
	public PlayerPowerType<?> getType() {
		return PillarmanPowerType.PILLAR_MAN.get();
	}
}
