package com.github.standobyte.jojo.mixin.client.screen;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

// too annoying to add it to ATs right now, maybe do that later
@Mixin(Screen.class)
public interface ScreenAccessor {
	@Accessor("narratables") List<NarratableEntry> getNarratables();
}
