package com.github.standobyte.jojo.mechanics.resolve;

import com.github.standobyte.jojo.client.shader.ColorShiftEffect;
import com.github.standobyte.jojo.client.shader.ColorShiftShader;
import com.github.standobyte.jojo.client.shader.ModShaders;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmPlayer;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackLoader;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.config.client.ClientModSettings;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.OOPMoment;
import com.github.standobyte.jojo.util.objects_mc.WeightsList;

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
				WeightsList<BgmTrackInfo> tracks = standSkin.getResolveBGM();
				if (tracks != null) {
					BgmPlayer player = new BgmPlayer(tracks);
					player.start();
				}
			}
			if (colorShift != null && ClientModSettings.getSettingsReadOnly().resolveShaders) {
				colorShift.parameters = ColorShiftEffect.Parameters.createRandom(OOPMoment.RANDOM);
			}
		}
		else if (!resolveEffect && prevTickResolveEffect) {
			// TODO make sure the BGM is from resolve and not smth else
			BgmPlayer curPlaying = BgmTrackLoader.getInstance().bgmPlaying;
			if (curPlaying != null) {
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
