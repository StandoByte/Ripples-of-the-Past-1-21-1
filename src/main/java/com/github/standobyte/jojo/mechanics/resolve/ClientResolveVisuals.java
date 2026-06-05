package com.github.standobyte.jojo.mechanics.resolve;

import com.github.standobyte.jojo.client.shader.ColorShiftEffect;
import com.github.standobyte.jojo.client.shader.ColorShiftShader;
import com.github.standobyte.jojo.client.shader.ModShaders;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmEngine;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmInstance;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.OOPMoment;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientResolveVisuals {
	private static boolean prevTickResolveEffect = false;

	@SubscribeEvent
	public static void onTick(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		ColorShiftShader colorShift = ModShaders.getInstance().colorShift;
		boolean resolveEffect = mc.player != null
				&& mc.player.isAlive()
				&& ResolveModeEffect.getResolveEffectLvl(mc.player) >= 0;
		if (resolveEffect && !prevTickResolveEffect) {
			StandSkin standSkin = StandSkinsLoader.getCurSkin();
			if (standSkin != null) {
				BgmInstance resolveBgm = BgmInstance.standResolve(standSkin);
				if (resolveBgm != null) {
					resolveBgm.start();
				}
			}
			if (colorShift != null && JojoMod.config.getClient().resolveShaders.getAsBoolean()) {
				colorShift.parameters = ColorShiftEffect.Parameters.createRandom(OOPMoment.RANDOM);
			}
		}
		else if (!resolveEffect && prevTickResolveEffect) {
			BgmInstance curPlaying = BgmEngine.getCurTrackPlaying();
			if (curPlaying != null && curPlaying.bgmType == BgmInstance.BgmType.STAND_RESOLVE) {
				curPlaying.finishWithOutro();
			}
			if (colorShift != null) {
				colorShift.parameters = null;
			}
		}
		prevTickResolveEffect = resolveEffect;
	}
	
	public static void onColorShiftSettingUpdated(boolean value) {
		ColorShiftShader colorShiftShader = ModShaders.getInstance().colorShift;
		if (colorShiftShader != null) {
			if (!value) {
				colorShiftShader.parameters = null;
			}
			else if (prevTickResolveEffect) {
				colorShiftShader.parameters = ColorShiftEffect.Parameters.createRandom(OOPMoment.RANDOM);
			}
		}
	}
	
}
