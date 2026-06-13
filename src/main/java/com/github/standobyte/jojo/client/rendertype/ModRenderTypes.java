package com.github.standobyte.jojo.client.rendertype;

import java.util.function.BiFunction;

import com.github.standobyte.jojo.client.shader.ModShaders;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModRenderTypes extends RenderType {

	public static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_DITHER_SHADER = new RenderStateShard.ShaderStateShard(
			() -> ModShaders.getInstance().coreEntityDither);

	@Deprecated
	private ModRenderTypes(String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling,
			boolean sortOnUpload, Runnable setupState, Runnable clearState) {
		super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
	}

	public static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_DITHER = Util.memoize(
			(texture, outline) -> {
				RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
						.setShaderState(RENDERTYPE_ENTITY_DITHER_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setCullState(NO_CULL)
						.setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY)
						.createCompositeState(outline);
				return create(JojoMod.MOD_ID + ":entity_dither", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, rendertype$compositestate);
			});

	public static RenderType entityDither(ResourceLocation texture, boolean outline) {
		return ENTITY_DITHER.apply(texture, outline);
	}

	public static RenderType entityDither(ResourceLocation texture) {
		return entityDither(texture, true);
	}
}
