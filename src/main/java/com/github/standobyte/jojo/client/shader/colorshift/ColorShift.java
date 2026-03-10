package com.github.standobyte.jojo.client.shader.colorshift;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.SharedConstants;
import net.minecraft.util.RandomSource;

public class ColorShift {
	public float hueShift = 0;
	public boolean flipRedGreen = false;
	public Float desaturateSector = null;
	public Float excludeHalf = null;
	public boolean split = false;

	static final float ALWAYS_FLIP_THRESHOLD = 0.1667f;
	static final int DESAT_OR_EXCLUDE_WEIGHT_EACH = 4;
	public static ColorShift createRandom(RandomSource random) {
		ColorShift effect = new ColorShift();
		effect.hueShift = random.nextFloat();
		effect.flipRedGreen = 
				effect.hueShift < ALWAYS_FLIP_THRESHOLD || 
				effect.hueShift > (1 - ALWAYS_FLIP_THRESHOLD)
				? true : random.nextBoolean();
		effect.split = random.nextBoolean();
		
		final int effWeight = DESAT_OR_EXCLUDE_WEIGHT_EACH;
		int mode = random.nextInt(1 + effWeight * 2); // 0-8
		if (mode < effWeight) 			effect.desaturateSector = random.nextFloat();// 0-3
		else if (mode < effWeight * 2)	effect.excludeHalf = random.nextFloat(); // 4-7
		
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			JojoMod.getLogger().debug("{}", effect);
		}
		
		return effect;
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
