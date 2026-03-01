package com.github.standobyte.jojoimpl.powers.hamon;

import net.minecraft.resources.ResourceLocation;

public class HamonSkill {
	private final ResourceLocation registryKey;

	public HamonSkill(ResourceLocation registryKey) {
		this.registryKey = registryKey;
	}
	
	public ResourceLocation getRegistryKey() {
		return registryKey;
	}
	
}
