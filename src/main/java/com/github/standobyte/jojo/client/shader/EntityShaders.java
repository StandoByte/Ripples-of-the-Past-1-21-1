package com.github.standobyte.jojo.client.shader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientTickHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.shader.standaura.StandAuraEntityShader;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.RegisterStageEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class EntityShaders {
	@ApiStatus.Internal
	public static List<SeparateBufferEntityShader> allShaders = new ArrayList<>();

	public static SeparateBufferEntityShader firstPersonStandTranslucency;
	public static StandAuraEntityShader standAura;
	
	public static ShaderInstance _outlineTranslucentShader;

//	@SubscribeEvent
	public static void bufferInit(/*ConfigureMainRenderTargetEvent event*/) {
		Minecraft mc = Minecraft.getInstance();
		allShaders.add(firstPersonStandTranslucency = new SeparateBufferEntityShader(mc, "stand_translucent", JojoMod.resLoc("fp_stand_translucent")));
		allShaders.add(standAura = new StandAuraEntityShader(mc, "stand_aura", JojoMod.resLoc("stand_aura")));
	}

//	static GraphicsResourceAllocator resourcePoolCache;
//	@SubscribeEvent
//	public static void mcInit2(RenderLevelStageEvent.RegisterStageEvent event) {
//		resourcePoolCache = ClientReflection.getResourcePool(Minecraft.getInstance().gameRenderer);
//	}
	
	// the problem is that this is fired before the vanilla buffer sources are created
//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void createRenderBuffer(RegisterRenderBuffersEvent event) {}
	
	@SubscribeEvent
	public static void createRenderBuffer(RegisterStageEvent event) {
		bufferInit();
		Minecraft mc = Minecraft.getInstance();
		RenderBuffers renderBuffers = mc.renderBuffers();
		for (SeparateBufferEntityShader shader : allShaders) {
			shader.createBufferSource(mc, renderBuffers);
		}
	}
	
	@ApiStatus.Internal
	public static void resourceReload(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(new ResourceManagerReloadListener() {
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				for (SeparateBufferEntityShader shader : allShaders) {
					shader.onResourceReload(resourceManager);
				}
			}
			
		});
	}

	@SubscribeEvent
	private static void loadCoreShaders(RegisterShadersEvent event) {
		ResourceProvider resourceProvider = event.getResourceProvider();
		List<ShaderInstance> addedShaders = new ArrayList<>();
		try {
			ShaderInstance shader = new ShaderInstance(resourceProvider, 
					JojoMod.resLoc("outline_translucent"), DefaultVertexFormat.POSITION_TEX_COLOR);
			addedShaders.add(shader);
			event.registerShader(shader, _shader -> _outlineTranslucentShader = _shader);
		}
		catch (IOException e) {
			for (ShaderInstance shader : addedShaders) shader.close();
			JojoMod.getLogger().error("Failed loading a core shader from the mod", e);
		}
	}

	
	@SubscribeEvent
	public static void frameRenderCallback(RenderLevelStageEvent event) {
		for (SeparateBufferEntityShader shader : allShaders) {
			shader.frameRenderCallback(event);
		}
	}
	
	public static void resize(int width, int height) {
		for (SeparateBufferEntityShader shader : allShaders) {
			shader.resize(width, height);
		}
	}
	
	
	@Nullable
	public static PostPass createCustomPass(ResourceProvider resourceProvider, String name, 
			RenderTarget inTarget, RenderTarget outTarget, boolean useLinearFilter) throws IOException {
		return null;
	}
	
	
	@Nullable
	public static void customizePass(PostPass pass, ResourceProvider resourceProvider, String name, 
			RenderTarget inTarget, RenderTarget outTarget, boolean useLinearFilter) throws IOException {
		switch (name) {
			case "jojo_ripples:aura_apply_noise_mask" -> {
				pass.effect.close();
				pass.effect = new EffectInstance(resourceProvider, name) {
					@Override
				    public void apply() {
				        this.setSampler("SilhouetteSampler", EntityShaders.standAura.silhouetteBuffer::getColorTextureId);
				        this.setSampler("NoiseSampler", EntityShaders.standAura.noiseBuffer::getColorTextureId);
						super.apply();
				    }
				};
			}
		}
	}
	
	public static float getTime() {
		return ClientTickHandler.tickCount + ClientUtil.partialTick(Minecraft.getInstance().getTimer(), true);
	}
}
