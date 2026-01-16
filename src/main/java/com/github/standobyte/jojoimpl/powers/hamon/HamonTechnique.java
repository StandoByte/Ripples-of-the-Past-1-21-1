package com.github.standobyte.jojoimpl.powers.hamon;

import net.minecraft.resources.ResourceLocation;

public class HamonTechnique {
	private final ResourceLocation registryKey;

	public HamonTechnique(ResourceLocation registryKey) {
		this.registryKey = registryKey;
	}
	
	public ResourceLocation getRegistryKey() {
		return registryKey;
	}
	
}
