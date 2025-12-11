package com.github.standobyte.jojo.client.rendertype;

import java.util.SequencedMap;

import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class CustomMultiBufferSource extends BufferSource {
	public RenderStateShard[] onBatchDraw;

	public CustomMultiBufferSource(ByteBufferBuilder sharedBuffer,
			SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers, 
			RenderStateShard... onBatchDraw) {
		super(sharedBuffer, fixedBuffers);
		this.onBatchDraw = onBatchDraw;
	}
	
	public static CustomMultiBufferSource create(Minecraft mc, RenderStateShard... onBatchDraw) {
		RenderBuffers renderBuffers = mc.renderBuffers();
		SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers;
		fixedBuffers = ClientReflection.getFixedBuffers(renderBuffers.bufferSource());
		CustomMultiBufferSource bufferSource = new CustomMultiBufferSource(
				new ByteBufferBuilder(786432), 
				fixedBuffers, onBatchDraw);
		return bufferSource;
	}

	@Override
	public void endBatch(RenderType renderType, BufferBuilder builder) {
		MeshData meshdata = builder.build();
		if (meshdata != null) {
			if (renderType.sortOnUpload()) {
				ByteBufferBuilder bytebufferbuilder = this.fixedBuffers.getOrDefault(renderType, this.sharedBuffer);
				meshdata.sortQuads(bytebufferbuilder, /*RenderSystem.getProjectionType().vertexSorting()*/RenderSystem.getVertexSorting());
			}

			renderType.setupRenderState();
			for (int i = 0; i < onBatchDraw.length; i++) {
				onBatchDraw[i].setupRenderState();
			}
			BufferUploader.drawWithShader(meshdata);
			for (int i = onBatchDraw.length - 1; i >= 0; i--) {
				onBatchDraw[i].clearRenderState();
			}
			renderType.clearRenderState();
		}

		if (renderType.equals(this.lastSharedType)) {
			this.lastSharedType = null;
		}
	}

}
