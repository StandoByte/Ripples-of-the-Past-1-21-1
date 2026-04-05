package com.github.standobyte.jojo.event.client;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.LivingAnimState;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

@ApiStatus.Internal
public abstract class ModClientEventHooks {
	
	public static ReplacePlayerModelEvent preRenderReplacePlayerModel(LivingEntity entity, 
			LivingEntityRenderer<?, ? extends PlayerModel<?>> renderer, 
			float partialTick, LivingAnimState animVariables) {
		return NeoForge.EVENT_BUS.post(new ReplacePlayerModelEvent(entity, renderer, partialTick, animVariables));
	}

	public static boolean onKeyboardInputPre(int key, int scanCode, int action, int modifiers) {
		return NeoForge.EVENT_BUS.post(new PreKeyInputEvent(key, scanCode, action, modifiers)).isCanceled();
	}
}
