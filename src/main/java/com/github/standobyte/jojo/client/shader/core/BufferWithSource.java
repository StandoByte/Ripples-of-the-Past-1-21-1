package com.github.standobyte.jojo.client.shader.core;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;

public class BufferWithSource {
	public RenderTarget buffer;
	public MultiBufferSource bufferSource;
	
	public BufferWithSource(RenderTarget buffer) {
		this.buffer = buffer;
	}
	
	public RenderStateShard.OutputStateShard createTargetShard(String name) {
		return new RenderStateShard.OutputStateShard(
				name, 
				() -> this.getBuffer().bindWrite(false), 
//				() -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));
				() -> this.getBuffer().unbindWrite());
	}
	
	public RenderTarget getBuffer() {
		return buffer;
	}
	
	public void initSource(MultiBufferSource source) {
		this.bufferSource = source;
	}
	
	public void destroyBuffers() {
		buffer.destroyBuffers();
		buffer = null;
		bufferSource = null;
	}
	
	
	public void clearBuffer() {
		if (buffer != null) buffer.clear(Minecraft.ON_OSX);
	}
	
	public void resizeBuffer(int width, int height) {
		if (buffer != null) buffer.resize(width, height, Minecraft.ON_OSX);
	}
	
	public void copyDepthFrom(RenderTarget otherBuffer) {
		if (buffer != null) buffer.copyDepthFrom(otherBuffer);
	}
	
	
	public void sourceEndBatch() {
		((MultiBufferSource.BufferSource) this.bufferSource).endBatch();
	}
}
