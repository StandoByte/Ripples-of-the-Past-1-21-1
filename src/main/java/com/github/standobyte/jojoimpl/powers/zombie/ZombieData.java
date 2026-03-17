package com.github.standobyte.jojoimpl.powers.zombie;

import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerData;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class ZombieData extends PlayerPowerData {
	
	public ZombieData() {
		super(ZombiePowerType.ZOMBIE.get());
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		
	}

	@Override
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		
	}

	@Override
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		
	}

}
