package com.github.standobyte.jojo.powersystem.standpower.resolve;

import com.github.standobyte.jojo.client.config.ClientModSettings;
import com.github.standobyte.jojo.client.shader.ColorShiftEffect;
import com.github.standobyte.jojo.client.shader.ModShaders;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmPlayer;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackLoader;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.OOPMoment;
import com.github.standobyte.jojo.util.java.WeightsList;

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
			if (ClientModSettings.getSettingsReadOnly().resolveShaders) {
				ModShaders.getInstance().colorShift.colorShift = ColorShiftEffect.Parameters.createRandom(OOPMoment.RANDOM);
			}
		}
		else if (!resolveEffect && prevTickResolveEffect) {
			BgmPlayer curPlaying = BgmTrackLoader.getInstance().bgmPlaying;
			if (curPlaying != null) {
				curPlaying.finishWithOutro();
			}
			ModShaders.getInstance().colorShift.colorShift = null;
		}
		prevTickResolveEffect = resolveEffect;
	}
	
	public static void onColorShiftSettingUpdated(boolean value) {
		if (!value) {
			ModShaders.getInstance().colorShift.colorShift = null;
		}
		else if (prevTickResolveEffect) {
			ModShaders.getInstance().colorShift.colorShift = ColorShiftEffect.Parameters.createRandom(OOPMoment.RANDOM);
		}
	}
	
}
