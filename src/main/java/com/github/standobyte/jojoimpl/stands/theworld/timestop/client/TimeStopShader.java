package com.github.standobyte.jojoimpl.stands.theworld.timestop.client;

import com.github.standobyte.jojo.client.shader.CustomLevelRenderStages;
import com.github.standobyte.jojo.client.shader.core.ManualInitPostChain;
import com.github.standobyte.jojo.client.shader.core.RotpShader;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public class TimeStopShader extends RotpShader {
	public static final ResourceLocation SHADER_PATH = JojoMod.resLoc("time_stop_old");
	protected ManualInitPostChain glslShaderChain;

	public TimeStopShader() {
		NeoForge.EVENT_BUS.register(this);
	}
	
	public void set(ManualInitPostChain postChain) {
		this.glslShaderChain = postChain;
		if (postChain != null) {
			Minecraft mc = Minecraft.getInstance();
			postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		}
	}
	
	public void startFadeOut() {
		
	}

	@Override
	public void loadPostShader(ResourceManager resourceManager) {
		// loaded in StandSkinsLoader
	}

	@Override
	public void resize(int width, int height) {
		if (glslShaderChain != null) {
			glslShaderChain.resize(width, height);
		}
	}

	@Override
	public void close() {
		// closed in StandSkinsLoader
		glslShaderChain = null;
	}



	@Override
	public void frameRenderCallback(RenderLevelStageEvent.Stage stage) {
		if (glslShaderChain != null && stage == CustomLevelRenderStages.BEFORE_SPECTATOR_SHADER) {
			Minecraft mc = Minecraft.getInstance();

			RenderSystem.disableBlend();
			RenderSystem.disableDepthTest();
			RenderSystem.resetTextureMatrix();
			glslShaderChain.process(mc.getTimer().getGameTimeDeltaTicks());
			mc.getMainRenderTarget().bindWrite(true);
		}
	}
	
	@SubscribeEvent
	public void clientTick(ClientTickEvent.Pre event) {
		if (glslShaderChain != null && !TimeStopClientState.isTimeStopped) {
			glslShaderChain = null;
		}
	}
}
