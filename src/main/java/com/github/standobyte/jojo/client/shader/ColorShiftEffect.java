package com.github.standobyte.jojo.client.shader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.shader.core.ManualInitPostChain;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.RandomSource;

public class ColorShiftEffect extends ManualInitPostChain {
	
	public static class Parameters {
		public float hueShift = 0;
		public boolean flipRedGreen = false;
		public Float desaturateSector = null;
		public Float excludeHalf = null;
		public boolean split = false;

		static final float ALWAYS_FLIP_THRESHOLD = 0.1667f;
		static final int DESAT_OR_EXCLUDE_WEIGHT_EACH = 2;
		public static ColorShiftEffect.Parameters createRandom(RandomSource random) {
			ColorShiftEffect.Parameters effect = new ColorShiftEffect.Parameters();
			effect.hueShift = random.nextFloat();
			effect.flipRedGreen = 
					effect.hueShift < ALWAYS_FLIP_THRESHOLD || 
					effect.hueShift > (1 - ALWAYS_FLIP_THRESHOLD)
					? true : random.nextBoolean();
			effect.split = random.nextBoolean();
			
			final int effWeight = DESAT_OR_EXCLUDE_WEIGHT_EACH;
			int mode = random.nextInt(1 + effWeight * 2); // 0-4
			if (mode < effWeight) 			effect.desaturateSector = random.nextFloat();// 0-1
			else if (mode < effWeight * 2)	effect.excludeHalf = random.nextFloat(); // 2-3
			
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				JojoMod.getLogger().debug("{}", effect);
			}
			
			return effect;
		}

		protected static List<PostPass> usedPasses = new ArrayList<>(7);
		protected List<PostPass> preparePasses(ColorShiftEffect shaders) {
			usedPasses.clear();
			
			if (this.hueShift != 0) {
				shaders.hueShift.effect.getUniform("ShiftAmount").set(this.hueShift);
				usedPasses.add(shaders.hueShift);
			}
			if (this.flipRedGreen) {
				usedPasses.add(shaders.rgFlip);
			}
			usedPasses.add(shaders.colorConvolve);
			if (this.excludeHalf != null) {
				shaders.hueExcludeHalf.effect.getUniform("ExclusionCenter").set(this.excludeHalf);
				usedPasses.add(shaders.hueExcludeHalf);
			}
			if (this.desaturateSector != null) {
				shaders.hueDesaturateSector.effect.getUniform("SectorCenter").set(this.desaturateSector);
				usedPasses.add(shaders.hueDesaturateSector);
			}
			if (this.split) {
				usedPasses.add(shaders.hueSplit);
			}
			
			return usedPasses;
		}
		
		static StringBuilder str = new StringBuilder();
		@Override
		public String toString() {
			str.setLength(0);
											str.append("Hue shift = " + hueShift + "; ");
			if (flipRedGreen)				str.append("+ Flip R and G channels; ");
			if (desaturateSector != null)	str.append("Desaturate hue sector at " + desaturateSector + "; ");
			if (excludeHalf != null)		str.append("Exclude half of hues at at " + excludeHalf + "; ");
			if (split)						str.append("+ Split; ");
			return str.toString();
		}
	}



	protected RenderTarget swapBuffer;
	
	protected PostPass hueShift;
	protected PostPass rgFlip;
	protected PostPass colorConvolve;
	protected PostPass hueDesaturateSector;
	protected PostPass hueExcludeHalf;
	protected PostPass hueSplit;
	protected PostPass blitFromSwap;

	protected static final String dummy = "swap";
	public ColorShiftEffect(TextureManager textureManager, ResourceProvider resourceProvider, RenderTarget screenTarget, ResourceLocation resourceLocation) throws IOException {
		super(textureManager, resourceProvider, screenTarget, resourceLocation);
		swapBuffer = addTempTarget(this, new TempTargetDefinition("swap"));
		
		hueShift = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("hue_shift").toString(), 
				dummy, dummy, false, null, null), textureManager);
		
		rgFlip = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("rg_swap").toString(), 
				dummy, dummy, false, null, null), textureManager);
		
		colorConvolve = addPassNode(this, new PassDefinition(
				JojoMod.resLoc("color_convolve").toString(), 
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
				JojoMod.resLoc("blit_solid").toString(), 
				"swap", MAIN_RENDER_TARGET, false, null, null), textureManager);
	}

	public void process(ColorShiftEffect.Parameters colorShift, float partialTick) {
		List<PostPass> usedPasses = colorShift.preparePasses(this);
		
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
