package com.github.standobyte.jojo.mechanics.standdisc;

import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class StandWrittenOnDisc {
	protected final StandInstance.NetworkData standInstance;

	public StandWrittenOnDisc(StandInstance stand) {
		this.standInstance = StandInstance.NetworkData.wrap(stand);
	}

	public StandWrittenOnDisc(StandInstance.NetworkData standSynced) {
		this.standInstance = standSynced;
	}
	
	
	public StandInstance getInstance() {
		return standInstance.get();
	}
	
	public StandInstance copyStandInstance() {
		return getInstance().copy();
	}
	
	public boolean isValid() {
		return standInstance != null && getInstance() != null;
	}
	

	@Override
	public int hashCode() {
		return standInstance.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof StandWrittenOnDisc other
				&& this.standInstance.equals(other.standInstance);
	}
	
	public static final Codec<StandWrittenOnDisc> CODEC = StandInstance.CODEC.xmap(
			StandWrittenOnDisc::new, discData -> discData.getInstance());
	
	public static final StreamCodec<FriendlyByteBuf, StandWrittenOnDisc> STREAM_CODEC = StandInstance.NetworkData.NETWORK_CODEC.map(
			StandWrittenOnDisc::new, discData -> discData.standInstance);

}
