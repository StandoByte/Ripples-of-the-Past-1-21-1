package com.github.standobyte.jojo.client.shader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ModClientResources;
import com.github.standobyte.jojo.client.shader.core.RotpShader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopShader;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModShaders implements ResourceManagerReloadListener, AutoCloseable {
	private static ModShaders instance;
	private ModShaders() {}

	public static ModShaders getInstance() {
		return instance;
	}

	public ColorShiftShader colorShift;
	public StandTranslucencyShader firstPersonStandTranslucency;
	public StandAuraShader standAura;
	public TimeStopShader timeStop;
	@ApiStatus.Internal public List<RotpShader> _allShaders = new ArrayList<>();
	
	private void init() {
		Minecraft mc = Minecraft.getInstance();
		SequencedMap<RenderType, ByteBufferBuilder> fixedRenderBuffers = ClientReflection.getFixedBuffers(mc.renderBuffers().bufferSource());

		_allShaders.add(firstPersonStandTranslucency = new StandTranslucencyShader(mc, fixedRenderBuffers));
		_allShaders.add(colorShift = new ColorShiftShader());
		_allShaders.add(standAura = new StandAuraShader(mc, fixedRenderBuffers));
		_allShaders.add(timeStop = new TimeStopShader());
	}
	
	
	
	public static void init(RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new ModShaders();
			instance.init();
			ModClientResources.closeables.add(instance);
			event.registerReloadListener(instance);
			
			NeoForge.EVENT_BUS.addListener(instance::_frameRenderCallback);
		}
	}
	
	// (this was written on 1.21.4 at first)
	// static GraphicsResourceAllocator resourcePoolCache;
	// @SubscribeEvent
	// public static void mcInit2(RenderLevelStageEvent.RegisterStageEvent event) {
	// 	resourcePoolCache = ClientReflection.getResourcePool(Minecraft.getInstance().gameRenderer);
	// }



	@Override
	public void close() {
		for (RotpShader shader : _allShaders) {
			shader.close();
		}
		_allShaders.clear();
	}

	@Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
		for (RotpShader shader : _allShaders) {
			shader.loadPostShader(resourceManager);
		}
	}
	
	@SubscribeEvent
	public static void loadCoreShaders(RegisterShadersEvent event) {
		for (RotpShader shader : instance._allShaders) {
			shader.loadCoreShaders(event);
		}
	}

	public void resize(int width, int height) {
		for (RotpShader shader : _allShaders) {
			shader.resize(width, height);
		}
	}
	
	private void _frameRenderCallback(RenderLevelStageEvent event) {
		frameRenderCallback(event.getStage());
	}
	
	public void frameRenderCallback(RenderLevelStageEvent.Stage stage) {
		if (stage == null) {
			JojoMod.getLogger().warn("Custom level render stage is null!");
			return;
		}
		
		for (RotpShader shader : _allShaders) {
			shader.frameRenderCallback(stage);
		}
	}


	
	public static PostChain loadPostShaderChain(ResourceLocation path, RenderTarget targetBuffer) {
		ResourceLocation actualPath = path.withPath(p -> "shaders/post/" + p + ".json");
		Minecraft mc = Minecraft.getInstance();
		PostChain effect;
		try {
			effect = new PostChain(mc.getTextureManager(), mc.getResourceManager(), targetBuffer, actualPath);
			effect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		} catch (IOException e) {
			JojoMod.getLogger().error("Failed to load shader: {}", actualPath, e);
			effect = null;
		} catch (JsonSyntaxException e) {
			JojoMod.getLogger().error("Failed to parse shader: {}", actualPath, e);
			effect = null;
		}
		return effect;
	}
	
	public static void loadCoreShader(RegisterShadersEvent event, 
			ResourceLocation path, VertexFormat vertexFormat, 
			Consumer<ShaderInstance> init) {
		ResourceProvider resourceProvider = event.getResourceProvider();
		ShaderInstance shader;
		try {
			shader = new ShaderInstance(resourceProvider, path, vertexFormat);
			event.registerShader(shader, init);
		}
		catch (IOException e) {
			JojoMod.getLogger().error("Failed loading a core shader from the mod", e);
			init.accept(null);
		}
	}

}
