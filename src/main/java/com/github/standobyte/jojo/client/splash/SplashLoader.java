package com.github.standobyte.jojo.client.splash;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.util.OOPMoment;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Neo lets you add "splashes.txt" under different namespaces now 
 *  thanks to {@link net.neoforged.neoforge.client.resources.NeoForgeSplashHooks} apparently, 
 *  but some of our splashes do custom stuff like filling in the username 
 *  (like the &ltPLAYERNAME&gt IS YOU one from vanilla),
 *  so I'll stick with the custom loader like before.
 */
public class SplashLoader extends SimplePreparableReloadListener<List<String>> {
	// we can actually make *completely* custom splashes now, that could be fun
	//SplashLoader.CUSTOM_SPLASH_RENDERERS.put("I reject my humanity, @p!", line -> new SplashRendererExtendable(line) {
	//	@Override
	//	protected void doDrawString(GuiGraphics guiGraphics, int screenWidth, Font font, int color) {
	//		guiGraphics.drawCenteredString(font, splash, 0, -8, 0xFF0000 | color);
	//	}
	//});
	
	private static SplashLoader instance;

	@ApiStatus.Internal
	public static void init(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new SplashLoader();
		}
		//event.addListener(JojoMod.resLoc("splash"), instance);
		event.registerReloadListener(instance);
		
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, instance::preScreenInit);
	}

	public static SplashLoader getInstance() {
		return instance;
	}


	protected List<String> splashes = new ArrayList<>();

	public void preScreenInit(ScreenEvent.Init.Pre event) {
		if (event.getScreen() instanceof TitleScreen titleScreen && ClientReflection.getSplash(titleScreen) == null) {
			SplashRenderer splash = this.getJojoSplash(titleScreen.getMinecraft());
			if (splash != null) {
				ClientReflection.setSplash(titleScreen, splash);
			}
		}
	}

	@Nullable
	public SplashRenderer getJojoSplash(Minecraft mc) {
		// don't replace special occasion splashes hardcoded by Mojang
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		if (   calendar.get(2) + 1 == 12 && calendar.get(5) == 24	// Christmas
			|| calendar.get(2) + 1 == 1 && calendar.get(5) == 1		// New Year
			|| calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {// Halloween
			return null;
		}

		int modSplashesCount = this.splashes.size();
		int vanillaSplashesCount = ClientReflection.getSplashes(mc.getSplashManager()).size();
		int i = OOPMoment.RANDOM.nextInt(modSplashesCount + vanillaSplashesCount);
		if (i < modSplashesCount) {
			String line = this.splashes.get(i);
			Function<String, ? extends SplashRenderer> createRenderer = CUSTOM_SPLASH_RENDERERS.getOrDefault(line, SplashRenderer::new);
			line = line.replace("@p", mc.getUser().getName());
			return createRenderer.apply(line);
		}
		
		return null;
	}

	@Override
	protected List<String> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		List<String> list = new ArrayList<>();
		for (Resource resource : resourceManager.listResources("rotp/splashes.txt", location -> true).values()) {
			try (BufferedReader reader = resource.openAsReader()) {
				list.addAll(reader.lines().map(String::trim).toList());
			}
			catch (IOException e) {
				LogUtils.getLogger().warn("Invalid rotp/splashes.txt in resourcepack: '{}'", resource.sourcePackId(), e);
			}
		}
		return list;
	}

	@Override
	protected void apply(List<String> object, ResourceManager resourceManager, ProfilerFiller profiler) {
		splashes.clear();
		for (String line : object) {
			if (!line.isBlank() && !line.startsWith("//")) {
				splashes.add(line);
			}
		}
	}
	
	
	public static final Map<String, Function<String, ? extends SplashRenderer>> CUSTOM_SPLASH_RENDERERS = new HashMap<>();
	
	public static class SplashRendererExtendable extends SplashRenderer {
		protected /*INSTEAD OF GOSH DANG PRIVATE*/ String splash;
		
		public SplashRendererExtendable(String splash) {
			super(splash);
			this.splash = splash;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int screenWidth, Font font, int color) {
			PoseStack poseStack = guiGraphics.pose();
			poseStack.pushPose();
			poseStack.translate((float) screenWidth / 2.0F + 123.0F, 69.0F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
			float scale = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float) (Math.PI * 2)) * 0.1F);
			scale = scale * 100.0F / (float)(font.width(splash) + 32);
			poseStack.scale(scale, scale, scale);
			doDrawString(guiGraphics, screenWidth, font, color);
			poseStack.popPose();
		}
		
		protected void doDrawString(GuiGraphics guiGraphics, int screenWidth, Font font, int color) {
			guiGraphics.drawCenteredString(font, splash, 0, -8, 0xFFFF00 | color);
		}
	}

}
