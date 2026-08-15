package com.github.standobyte.jojo.event.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * This event triggers at the start of {@link ItemInHandRenderer#renderHandsWithItems(float, PoseStack, BufferSource, LocalPlayer, int)},
 * and can be used to completely override the hands rendering.
 * If {@link #setCancelsROTPRendering(boolean)} is set to true, the ROTP first person rendering methods
 * will not run, instead deferring to vanilla.
 * If this event is cancelled, both ROTP and vanilla rendering do not run.
 */
public class RipplesFirstPersonRenderEarlyEvent extends Event implements ICancellableEvent {
	public final Entity povEntity;
	public final float partialTick;
	public final PoseStack poseStack;
	public final BufferSource bufferSource;
	public final int light;
	private boolean cancelROTPRendering = false;

	public RipplesFirstPersonRenderEarlyEvent(Entity povEntity, float partialTick, PoseStack poseStack,
			BufferSource bufferSource, int light) {
		this.povEntity = povEntity;
		this.partialTick = partialTick;
		this.poseStack = poseStack;
		this.bufferSource = bufferSource;
		this.light = light;
	}

	public boolean getCancelsROTPRendering() {
		return cancelROTPRendering;
	}
	
	public void setCancelsROTPRendering(boolean cancel) {
		this.cancelROTPRendering = cancel;
	}
	
}
