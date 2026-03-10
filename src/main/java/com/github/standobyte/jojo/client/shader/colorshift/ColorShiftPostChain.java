package com.github.standobyte.jojo.client.shader.colorshift;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

public class ColorShiftPostChain extends ManualInitPostChain {
	protected RenderTarget swapBuffer;
	
	protected PostPass hueShift;
	protected PostPass rgFlip;
	protected PostPass colorConvolve;
	protected PostPass hueDesaturateSector;
	protected PostPass hueExcludeHalf;
	protected PostPass hueSplit;
	protected PostPass blitFromSwap;

	protected static final String dummy = "swap";
	public ColorShiftPostChain(TextureManager textureManager, ResourceProvider resourceProvider, RenderTarget screenTarget, ResourceLocation resourceLocation) throws IOException {
		super(textureManager, resourceProvider, screenTarget, resourceLocation);
		swapBuffer = addTempTarget(this, new TempTargetDefinition("swap"));
		
		hueShift = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("hue_shift").toString(), 
				dummy, dummy, false, null, null), textureManager);
		
		rgFlip = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("rg_swap").toString(), 
				dummy, dummy, false, null, null), textureManager);
		
		colorConvolve = addPassNode(this, new PassDefinition(
				"color_convolve", 
				dummy, dummy, false, null, 
				new UniformDefinition[] { new UniformDefinition("Saturation", new float[] { 1.4f }) }), textureManager);
		
		hueDesaturateSector = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("hue_desaturate_sector").toString(), 
				dummy, dummy, false, null, null), textureManager);
		
		hueExcludeHalf = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("hue_exclude_half").toString(), 
				dummy, dummy, false, null, null), textureManager);
		
		hueSplit = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("hue_split").toString(), 
				dummy, dummy, false, null, null), textureManager);
		
		blitFromSwap = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("hue_shift").toString(), 
				"swap", MAIN_RENDER_TARGET, false, null, null), textureManager);
	}

	protected static List<PostPass> usedPasses = new ArrayList<>(7);
	public void process(ColorShift colorShift, float partialTick) {
		usedPasses.clear();
		if (colorShift.hueShift != 0) {
			hueShift.effect.getUniform("ShiftAmount").set(colorShift.hueShift);
			usedPasses.add(hueShift);
		}
		if (colorShift.flipRedGreen) {
			usedPasses.add(rgFlip);
		}
		usedPasses.add(colorConvolve);
		if (colorShift.excludeHalf != null) {
			hueExcludeHalf.effect.getUniform("ExclusionCenter").set(colorShift.excludeHalf);
			usedPasses.add(hueExcludeHalf);
		}
		if (colorShift.desaturateSector != null) {
			hueDesaturateSector.effect.getUniform("SectorCenter").set(colorShift.desaturateSector);
			usedPasses.add(hueDesaturateSector);
		}
		if (colorShift.split) {
			usedPasses.add(hueSplit);
		}
		
		for (int i = 0; i < usedPasses.size(); i++) {
			PostPass pass = usedPasses.get(i);
			if (i % 2 == 0) {
				pass.inTarget = screenTarget;
				pass.outTarget = swapBuffer;
			}
			else {
				pass.inTarget = swapBuffer;
				pass.outTarget = screenTarget;
			}
		}
		if (usedPasses.size() % 2 == 1) {
			usedPasses.add(blitFromSwap);
		}
		
		process(usedPasses, partialTick);
	}

}
