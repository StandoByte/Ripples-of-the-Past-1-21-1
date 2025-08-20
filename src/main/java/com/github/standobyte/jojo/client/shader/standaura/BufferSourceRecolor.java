package com.github.standobyte.jojo.client.shader.standaura;

import java.util.SequencedMap;

import com.github.standobyte.jojo.client.rendertype.CustomMultiBufferSource;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;

public class BufferSourceRecolor extends CustomMultiBufferSource {
	private int teamR = 255;
	private int teamG = 255;
	private int teamB = 255;
	private int teamA = 255;

	public BufferSourceRecolor(ByteBufferBuilder sharedBuffer, 
			SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers,
			RenderStateShard... onBatchDraw) {
		super(sharedBuffer, fixedBuffers, onBatchDraw);
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		VertexConsumer buffer = _getBuffer(renderType);
		OutlineGenerator entityOutlineGenerator = new OutlineGenerator(
				buffer, this.teamR, this.teamG, this.teamB, this.teamA);
		return entityOutlineGenerator;
	}
	
	protected VertexConsumer _getBuffer(RenderType renderType) {
		BufferBuilder bufferbuilder = this.startedBuilders.get(renderType);
		if (bufferbuilder != null && !renderType.canConsolidateConsecutiveGeometry()) {
			this.endBatch(renderType, bufferbuilder);
			bufferbuilder = null;
		}

		if (bufferbuilder != null) {
			return bufferbuilder;
		} else {
			VertexFormat format = DefaultVertexFormat.POSITION_TEX_COLOR;
			
			ByteBufferBuilder bytebufferbuilder = this.fixedBuffers.get(renderType);
			if (bytebufferbuilder != null) {
				bufferbuilder = new BufferBuilder(bytebufferbuilder, renderType.mode(), format);
			} else {
				if (this.lastSharedType != null) {
					this.endBatch(this.lastSharedType);
				}

				bufferbuilder = new BufferBuilder(this.sharedBuffer, renderType.mode(), format);
				this.lastSharedType = renderType;
			}

			this.startedBuilders.put(renderType, bufferbuilder);
			return bufferbuilder;
		}
	}

	public void setColor(int color) {
		this.teamR = FastColor.ARGB32.red(color);
		this.teamG = FastColor.ARGB32.green(color);
		this.teamB = FastColor.ARGB32.blue(color);
		this.teamA = FastColor.ARGB32.alpha(color);
	}

	public void setColor(int red, int green, int blue, int alpha) {
		this.teamR = red;
		this.teamG = green;
		this.teamB = blue;
		this.teamA = alpha;
	}

}
