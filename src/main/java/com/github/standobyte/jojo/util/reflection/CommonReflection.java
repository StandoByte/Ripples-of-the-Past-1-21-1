package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.resources.RegistryDataLoader;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;

public final class CommonReflection {
	
	private static final Field NEO_DataPackRegistriesHooks_NETWORKABLE_REGISTRIES = ObfuscationReflectionHelper.findField(DataPackRegistriesHooks.class, "NETWORKABLE_REGISTRIES");
	public static List<RegistryDataLoader.RegistryData<?>> getDataPackNetworkableRegistries() {
		return ReflectionUtil.getFieldValue(NEO_DataPackRegistriesHooks_NETWORKABLE_REGISTRIES, null);
	}
}
