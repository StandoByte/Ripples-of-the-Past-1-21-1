package com.github.standobyte.jojo.client.shader.standaura;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.util.FastColor;

public record OutlineGenerator(VertexConsumer delegate, int color) implements VertexConsumer {

	public OutlineGenerator(VertexConsumer p_109943_, int r, int g, int b, int alpha) {
		this(p_109943_, FastColor.ARGB32.color(alpha, r, g, b));
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		this.delegate.addVertex(x, y, z).setColor(this.color);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		this.delegate.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		this.delegate.setUv1(0, 10);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		this.delegate.setUv2(0xFF, 0xFF);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		this.delegate.setNormal(normalX, normalY, normalZ);
		return this;
	}

}
