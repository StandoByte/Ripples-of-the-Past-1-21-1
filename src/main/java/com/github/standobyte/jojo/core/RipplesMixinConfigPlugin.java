package com.github.standobyte.jojo.core;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class RipplesMixinConfigPlugin implements IMixinConfigPlugin {
	public static final boolean ENABLE_DEBUG_MIXINS = true;

	private static final int PACKAGE_NAME_LENGTH = "com.github.standobyte.jojo.mixin.".length();
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		String mixinName = mixinClassName.substring(PACKAGE_NAME_LENGTH);
		if (!ENABLE_DEBUG_MIXINS && mixinName.startsWith("debughelper.")) {
			return false;
		}
		return true;
	}


	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}
