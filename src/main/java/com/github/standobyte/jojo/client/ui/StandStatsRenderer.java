package com.github.standobyte.jojo.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.screen_widgets.HeightScaledSlider;
import com.github.standobyte.jojo.client.ui.screen_widgets.IconButton;
import com.github.standobyte.jojo.client.ui.screen_widgets.ImageButton2;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MultiLineScreenTooltip;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MutableTooltipWrapper;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.client.util.functions.RGBUtil;
import com.github.standobyte.jojo.config.client.ClientModSettings;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class StandStatsRenderer {
	public static final ResourceLocation STAND_STATS_UI = JojoMod.resLoc("textures/gui/stand_stats.png");

	protected static final GuiIcon standStatsToggleIcon = new GuiIcon(StandStatsRenderer.STAND_STATS_UI, 492, 492, 20, 20, 512, 512);
	protected static final Tooltip standStatsHideTooltip = Tooltip.create(Component.translatable("jojo_ripples.stand_stat.button.hide"));
	protected static final Tooltip standStatsShowTooltip = Tooltip.create(Component.translatable("jojo_ripples.stand_stat.button.show"));
	protected static final GuiIcon bnwButton = new GuiIcon(StandStatsRenderer.STAND_STATS_UI, 464, 496, 8, 8, 512, 512);
	protected static final GuiIcon bnwInvertedButton = new GuiIcon(StandStatsRenderer.STAND_STATS_UI, 472, 496, 8, 8, 512, 512);
	protected static final GuiIcon bnwButtonHovered = new GuiIcon(StandStatsRenderer.STAND_STATS_UI, 464, 504, 8, 8, 512, 512);
	protected static final GuiIcon bnwInvertedButtonHovered = new GuiIcon(StandStatsRenderer.STAND_STATS_UI, 472, 504, 8, 8, 512, 512);
	
	protected static Boolean renderStandStatsToggle;
	protected static int standStatsTick;
	protected static boolean thisScreenRendersStandStats_StupidCrutch;
	public static StandPower standStatsPower;

	protected static boolean screenHasStandStats(Screen screen) {
		return screen instanceof PauseScreen pauseScreen && pauseScreen.showsPauseMenu();
	}
	
	@SubscribeEvent
	public static void onScreenOpened(ScreenEvent.Opening event) {
		Screen screen = event.getNewScreen();
		if (screenHasStandStats(screen)) {
			setStandToRender(ClientPowerCache.getPower(PowerClass.STAND), event.getCurrentScreen() == null);
		}
	}
	
	public static void setStandToRender(StandPower standPower, boolean newScreen) {
		/* it REALLY sucks that ScreenEvent.Closing is posted after ScreenEvent.Opening, 
		 * this would make so much more sense if it was before (you close the old screen, then open the new one)
		 */
		thisScreenRendersStandStats_StupidCrutch = true;
		standStatsPower = standPower;
		if (standStatsPower != null && standStatsPower.hasPower() && newScreen) {
			CosmeticStandStats handler = CosmeticStandStats.getHandler(standStatsPower);
			if (handler != null) {
				handler.onPauseScreenOpened();
			}
		}
	}

	@SubscribeEvent
	public static void onScreenClosed(ScreenEvent.Closing event) {
//		if (renderStandStatsToggle != null && renderStandStatsToggle) {
//			renderStandStatsToggle = null;
//		}
		if (thisScreenRendersStandStats_StupidCrutch = true) {
			thisScreenRendersStandStats_StupidCrutch = false;
		}
		else if (standStatsPower != null) {
			standStatsPower = null;
		}
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void addWidgets(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screenHasStandStats(screen)) {
			if (standStatsPower != null && standStatsPower.hasPower()) {
				AbstractSliderButton statsBgAlphaSlider = new HeightScaledSlider(
						screen.width - 160, screen.height - 6, 153, 6, CommonComponents.EMPTY, 0.0D) {
					{
						this.value = Mth.inverseLerp(
								ClientModSettings.getSettingsReadOnly().standStatsTranslucency, 
								0.1, 1.0);
						updateMessage();
					}

					@Override
					protected void updateMessage() {
						setMessage(CommonComponents.EMPTY);
					}

					@Override
					protected void applyValue() {
						ClientModSettings.getInstance().editSettings(settings -> {
							settings.standStatsTranslucency = (float) Mth.clampedLerp(0.1, 1.0, this.value);
						}, false);
					}
				};
				statsBgAlphaSlider.visible = doStandStatsRender(screen);
				event.addListener(statsBgAlphaSlider);
				
				ImageButton2 invertBnWButton = new ImageButton2(screen.width - 8, screen.height - 7, 8, 8, 
						ClientModSettings.getSettingsReadOnly().standStatsInvertBnW ? 
								bnwInvertedButton : bnwButton,
						null,
						ClientModSettings.getSettingsReadOnly().standStatsInvertBnW ? 
								bnwInvertedButtonHovered : bnwButtonHovered,
						null,
						_button -> {
							ClientModSettings.getInstance().editSettings(settings -> {
								settings.standStatsInvertBnW = !settings.standStatsInvertBnW;
								ImageButton2 button = (ImageButton2) _button;
								button.spriteEnabled = ClientModSettings.getSettingsReadOnly().standStatsInvertBnW ? 
										bnwInvertedButton : bnwButton;
								button.spriteEnabledFocused = ClientModSettings.getSettingsReadOnly().standStatsInvertBnW ? 
										bnwInvertedButtonHovered : bnwButtonHovered;
							}, false);
						});
				invertBnWButton.visible = doStandStatsRender(screen);
				event.addListener(invertBnWButton);

				Button standStatsToggleButton = new IconButton(screen.width - 28, screen.height - 28, 
						20, 20, standStatsToggleIcon, 
						button -> {
							renderStandStatsToggle = !doStandStatsRender(screen);
							statsBgAlphaSlider.visible = doStandStatsRender(screen);
							invertBnWButton.visible = doStandStatsRender(screen);
						}, 
						new MutableTooltipWrapper() {
							@Override
							public Tooltip updateToolip() {
								return doStandStatsRender(screen) ? standStatsHideTooltip : standStatsShowTooltip;
							}
						});
				event.addListener(standStatsToggleButton);
			}
		}
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		standStatsTick = screenHasStandStats(mc.screen) && doStandStatsRender(mc.screen) ? standStatsTick + 1 : 0;
	}

	@SubscribeEvent
	public static void afterScreenRender(ScreenEvent.Render.Post event) {
		Screen screen = event.getScreen();

		if (standStatsPower != null && standStatsPower.hasPower()
				&& screenHasStandStats(screen) && doStandStatsRender(screen)) {
			Minecraft mc = screen.getMinecraft();
			float partialTick = ClientUtil.partialTick(mc.getTimer(), true);
			float alpha = ClientModSettings.getSettingsReadOnly().standStatsTranslucency;
			boolean invertBnW = ClientModSettings.getSettingsReadOnly().standStatsInvertBnW;
			int xButtonsRightEdge = screen.width / 2 + 102;
			int windowWidth = screen.width;
			int windowHeight = screen.height;

			StandStatsRenderer.renderStandStats(event.getGuiGraphics(), mc, 
					windowWidth - StandStatsRenderer.statsWidth - 7, windowHeight - StandStatsRenderer.statsHeight - 7, 
					windowWidth, windowHeight,
					standStatsTick, partialTick, 
					alpha, invertBnW,
					event.getMouseX(), event.getMouseY(), windowWidth - xButtonsRightEdge - 14, 
					standStatsPower, standStatsPower.getUser(), true, true, true);
		}
	}

	protected static boolean doStandStatsRender(Screen screen) {
		if (renderStandStatsToggle != null) {
			return renderStandStatsToggle.booleanValue();
		}
		int xButtonsRightEdge = screen.width / 2 + 102;
		int windowWidth = screen.width;
		int windowHeight = screen.height;
		return windowWidth - xButtonsRightEdge >= 167 && windowHeight > 204;
	}





	/*
	 * "A" - 14+
	 * "B" - 11-14
	 * "C" - 8-11
	 * "D" - 5-8
	 * "E" - 0-5
	 * "∅" - 0
	 */
	public static List<String> STAT_LETTERS = Util.make(new ArrayList<>(), list -> {
		Collections.addAll(list, "∅", "E", "D", "C", "B", "A"/*, "A", "S"*/);
	});
	public static final String REFERENCE_MARK = "\u203B";

	public static String getRankFromConvertedValue(double value) {
		int rankIndex;
		if (value >= 2) rankIndex = Mth.floor(value);
		else if (value > 0)  rankIndex = 1;
		else                      rankIndex = 0;
		return STAT_LETTERS.get(Math.min(rankIndex, STAT_LETTERS.size() - 1));
	}

	protected static final double LN_2 = Math.log(2);
	public static enum HexagonStandStat {
		STRENGTH        ("jojo_ripples.stand_stat.strength",       0,  -72) {
			@Override
			float getValueConverted(StandPower standData, StandStats stats, float levelRatio) {
				float value = (float) stats.power();
				if (value > 0) value = (value + 1) / 3;
				return value;
			}
		},
		SPEED           ("jojo_ripples.stand_stat.speed",          58, -39) {
			@Override
			float getValueConverted(StandPower standData, StandStats stats, float levelRatio) {
				float value = (float) stats.speed();
				if (value > 0) value = (value + 1) / 3;
				return value;
			}
		},
		RANGE           ("jojo_ripples.stand_stat.range",          58,  32) {
			@Override
			float getValueConverted(StandPower standData, StandStats stats, float levelRatio) {
				float value = (float) (stats.rangeEffective() + (stats.rangeMax() - stats.rangeEffective()) * 0.5);
				if (value > 0) value = (float) (Math.log(value / 1.5) / LN_2 /* or log2(val / 1.5) */ + 1); 
				return value;
			}
		},
		DURABILITY      ("jojo_ripples.stand_stat.durability",     0,   65) {
			@Override
			float getValueConverted(StandPower standData, StandStats stats, float levelRatio) {
				float value = (float) stats.durability();
				if (value > 0) value = (value + 1) / 3;
				return value;
			}
		},
		PRECISION       ("jojo_ripples.stand_stat.precision",     -58,  32) {
			@Override
			float getValueConverted(StandPower standData, StandStats stats, float levelRatio) {
				float value = (float) stats.precision();
				if (value > 0) value = (value + 1) / 3;
				return value;
			}
		},
		DEV_POTENTIAL   ("jojo_ripples.stand_stat.dev_potential", -58, -39) {
			@Override
			float getValueConverted(StandPower standData, StandStats stats, float levelRatio) {
				float value = 0;
//				if (levelRatio < 1) {
//					value = 1 + (1 - levelRatio) * standData.getMaxResolveLevel();
//				}
//				else if (standData.hasUnlockedMatching(action -> action.isTrained() && standData.getLearningProgressRatio(action) < 1)) {
//					value = 1;
//				}
				return value;
			}
		};

		public final String tlKey;
		public final Component name;
		public final Component desc;
		public final int x;
		public final int y;

		private HexagonStandStat(String name, int x, int y) {
			this.tlKey = name;
			this.name = Component.translatable(name).withStyle(ChatFormatting.BLACK);
			this.desc = Component.translatable(name + ".desc").withStyle(ChatFormatting.DARK_GRAY);
			this.x = x;
			this.y = y;
		}

		abstract float getValueConverted(StandPower standData, StandStats stats, float levelRatio);
	}


	protected static final Map<ResourceLocation, CosmeticStandStats> OVERRIDE_STAT = new HashMap<>();
	public static void overrideCosmeticStats(ResourceLocation standId, CosmeticStandStats override) {
		OVERRIDE_STAT.put(standId, override);
	}

	public static class CosmeticStandStats {
		/*
		 *  These methods can be used in conjunction to render different stats for something like Sub-Stands or Stand Acts:
		 *  Create a few more instances of ICosmeticStandStats for each Sub-Stands/Act/etc. and keep them as final fields in the main instance.
		 *  Each time the pause screen is opened, choose one of them randomly (or on specific conditions, 
		 *  like having SHA summoned to render its stats) and set it to another (non-final) field.
		 *  Then return that randomly chosen instance in getSubStandStats().
		 */
		public void onPauseScreenOpened() {}

		@Nonnull
		public CosmeticStandStats getSubStandStats() {
			return this;
		}


		public void preStatsRenderFrame(StandPower standData, float partialTick) {}

		public float statConvertedValue(HexagonStandStat stat, StandPower standData, StandStats stats, float statLeveling) {
			return stat.getValueConverted(standData, stats, statLeveling);
		}

		public String statRankLetter(HexagonStandStat stat, StandPower standData, double statConvertedValue) {
			return getRankFromConvertedValue(statConvertedValue);
		}

		public Component standName(StandPower standData) {
			return standData.getName();
		}

		@Nullable
		public GuiIcon standIcon(StandPower standData) {
			StandSkin skin = StandSkinsLoader.getInstance().getSkin(standData);
			return skin != null ? skin.getStandIcon() : null;
		}

		protected Map<HexagonStandStat, MultiLineScreenTooltip> tooltip = new EnumMap<>(HexagonStandStat.class);
		public List<FormattedCharSequence> statTooltip(HexagonStandStat standStat, Minecraft mc, StandPower standData) {
			return tooltip.computeIfAbsent(standStat, stat -> {
				MultiLineScreenTooltip tooltip = new MultiLineScreenTooltip(stat.name, stat.desc);
				return tooltip;
			}).toCharSequence(mc);
		}


		public static final CosmeticStandStats DEFAULT = new CosmeticStandStats() {};
		public static CosmeticStandStats getHandler(StandPower standData) {
			CosmeticStandStats handler = standData.hasPower() ? OVERRIDE_STAT.getOrDefault(standData.getPowerType().getId(), DEFAULT) : DEFAULT;
			return handler;
		}
	}



	public static final int statsWidth = 163;
	public static final int statsHeight = 163;

	protected static final int BORDERS_FADE_IN_END = 20;
	protected static final int HEXAGON_TICK_START = 20;
	protected static final int HEXAGON_EXPAND_TICKS = 30;
	protected static final int LETTER_TICK_START = 50;
	protected static final float LETTER_FADE_IN = 1.5f;

	protected static final int BLACK = 0x000000;
	protected static final int WHITE = 0xFFFFFF;
	protected static int bnw(int defaultColor, boolean invert) {
		if (defaultColor == BLACK) 	return invert ? WHITE : BLACK;
		else 						return invert ? BLACK : WHITE;
	}
	
	protected static final Random random = new Random();

	public static void renderStandStats(GuiGraphics guiGraphics, Minecraft mc, 
			int x, int y, int screenWidth, int screenHeight, 
			int tick, float partialTick, 
			float bgAlpha, boolean invertBnW, 
			int mouseX, int mouseY, int maxTextWidth, 
			StandPower power, LivingEntity user, boolean knownStand, boolean knownUser, boolean knownStats) {
		if (power == null || !power.hasPower()) return;
		
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, 0, 1);

		StandStats stats = power.getPowerType().getStandStats();
//		float statLeveling = power.getStatsDevelopment();
		float statLeveling = 0;
		CosmeticStandStats override = CosmeticStandStats.getHandler(power).getSubStandStats();
		override.preStatsRenderFrame(power, partialTick);

		StandSkin skin = StandSkinsLoader.getInstance().getSkin(power);
		int standNameColor = skin != null && knownStand ? skin.getColor() : 0xFFFFFFFF;
		int statsHexagonColor = skin != null && knownStand ? FastColor.ARGB32.color(191, skin.getColor()) : FastColor.ARGB32.color(127, bnw(BLACK, invertBnW));

		float[] statVal = new float[6];
		String[] statRank = new String[6];

		for (int i = 0; i < statVal.length; i++) {
			HexagonStandStat stat = HexagonStandStat.values()[i];
			statVal[i] = override.statConvertedValue(stat, power, stats, statLeveling);
			statRank[i] = override.statRankLetter(stat, power, statVal[i]);
		}

		float xCenter = x + statsWidth / 2f;
		float yCenter = y + statsHeight / 2f;

		float scale = tick < HEXAGON_TICK_START ? 0.75f + 0.25f * ((tick + partialTick) / HEXAGON_TICK_START) : 1;
		float bordersAlpha = tick < BORDERS_FADE_IN_END ? (tick + partialTick) / BORDERS_FADE_IN_END : 1;
		if (scale < 1) {
			poseStack.translate(xCenter * (1 - scale), yCenter * (1 - scale), 0);
			poseStack.scale(scale, scale, 1);
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		// background
		int uiColor = ARGB.color(bgAlpha * bordersAlpha, bnw(WHITE, invertBnW));
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				256, 0, statsWidth, statsHeight, 512, 512, 
				uiColor);

		// circles
		uiColor = ARGB.color(bordersAlpha, bnw(BLACK, invertBnW));
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				0, 0, statsWidth, statsHeight, 512, 512, 
				uiColor);
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				0, 164, statsWidth, statsHeight, 512, 512, 
				uiColor);
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				256, 164, statsWidth, statsHeight, 512, 512, 
				uiColor);

		// rotating outer ring effect
		float outerRingRot = 0;
		float innerRingRot = 0;
		if (tick < LETTER_TICK_START) {
			outerRingRot = (tick + partialTick) * -7f;
			innerRingRot = (tick + partialTick) * 5f;
		}
		poseStack.pushPose();
		poseStack.translate(xCenter, yCenter, 0);

		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(outerRingRot));
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				-statsWidth / 2f, -statsHeight / 2f, statsWidth, statsHeight, 0,
				0, 328, statsWidth, statsHeight, 512, 512, 
				uiColor);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(innerRingRot));
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				-statsWidth / 2f, -statsHeight / 2f, statsWidth, statsHeight, 0,
				256, 328, statsWidth, statsHeight, 512, 512, 
				uiColor);
		poseStack.popPose();

		// scale letters
		uiColor = ARGB.color(bordersAlpha, bnw(BLACK, invertBnW));
		for (int i = 1; i < 6; i++) {
			if (STAT_LETTERS.size() <= i) break;
			String letter = STAT_LETTERS.get(i);
			guiGraphics.drawString(mc.font, letter, 
					3.5f, -18.5f - (i - 1) * 9f, 
					uiColor, false);
		}
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		poseStack.popPose();

		// stats hexagon
		if (knownStats && tick >= HEXAGON_TICK_START) {
			int tick_ = tick - HEXAGON_TICK_START;
			for (int i = 0; i < statVal.length; i++) {
				statVal[i] = statVal[i] <= 1 ? statVal[i] * 4 : 4 + (statVal[i] - 1) * 3;

				if (statVal[i] > 0) {
					statVal[i] = Math.min(statVal[i] + 1, 20);
				}

				if (tick_ < HEXAGON_EXPAND_TICKS) {
					statVal[i] *= ((double) tick_ + (double) partialTick) / (double) HEXAGON_EXPAND_TICKS;
				}
				statVal[i] *= 3;
			}

			int[] argb = RGBUtil.argbInt(statsHexagonColor);
			fillHexagon(guiGraphics, xCenter, yCenter, 
					statVal[0], statVal[1], statVal[2], 
					statVal[3], statVal[4], statVal[5], 
					argb[1], argb[2], argb[3], argb[0]);
		}

		// stand name and user
		if (tick >= HEXAGON_TICK_START) {
			var standName = mc.font.split(Component.translatable("jojo_ripples.stand_stat.stand_name", 
					knownStand ? override.standName(power) : Component.translatable("multiplayer.status.unknown")), maxTextWidth);
			var standUser = mc.font.split(Component.translatable("jojo_ripples.stand_stat.stand_user", 
					knownUser ? user.getDisplayName() : Component.translatable("multiplayer.status.unknown")), 
					maxTextWidth);
			int width = 0;
			if (standName.size() > 1 || standUser.size() > 1) {
				width = maxTextWidth;
			}
			else {
				if (!standName.isEmpty()) {
					width = mc.font.width(standName.get(0));
				}
				if (!standUser.isEmpty()) {
					width = Math.max(width, mc.font.width(standUser.get(0)));
				}
			}

			int standUserY = y - 5 - Math.max(standUser.size(), 1) * 9;
			int userIconY = standUserY;
			if (standUser.size() <= 1) {
				userIconY -= 5;
			}

			int standNameY = standUserY - 9 - Math.max(standName.size(), 1) * 9;
			int standIconY = standNameY;
			if (standName.size() <= 1) {
				standIconY -= 5;
			}

			if (knownStand) {
				GuiIcon standIcon = override.standIcon(power);
				if (standIcon != null) {
					standIcon.render(poseStack, x + statsWidth - 18 - width, standIconY);
				}
			}
			int lineY = standNameY;
			for (var line : standName) {
				guiGraphics.drawString(mc.font, line, 
						x + statsWidth - width, lineY, standNameColor, true);
				lineY += mc.font.lineHeight;
			}

			if (knownUser) {
				ClientUtil.renderEntityFace(poseStack, x + statsWidth - 18 - width, userIconY, user);
			}
			lineY = standUserY;
			for (var line : standUser) {
				guiGraphics.drawString(mc.font, line, 
						x + statsWidth - width, lineY, standNameColor, true);
				lineY += mc.font.lineHeight;
			}
		}

		// rank letters on the outer ring
		float tick_ = (tick + partialTick)/ LETTER_FADE_IN;
		for (int i = 0; i < statRank.length; i++) {
			int letterStartTick = (int) (LETTER_TICK_START / LETTER_FADE_IN) + i;
			int letterTicks = STAT_LETTERS.size() - 2;
			int letterFullTick = letterStartTick + letterTicks;
			if (tick_ < letterStartTick) continue;

			HexagonStandStat stat = HexagonStandStat.values()[i];
			float statX = xCenter + stat.x;
			float statY = yCenter + stat.y;
			String statRankLetter = knownStats ? statRank[i] : "?";

			if (tick_ < letterFullTick) {
				int letterToDraw = -1;
				if ("?".equals(statRankLetter)) {
					letterToDraw = random.nextInt(STAT_LETTERS.size());
				}
				else {
					int letterIndex = "∞".equals(statRankLetter) ? STAT_LETTERS.size() : STAT_LETTERS.indexOf(statRankLetter);
					if (letterIndex > 1) {
						letterToDraw = letterIndex - (int) ((letterFullTick - tick_) * letterIndex / letterTicks);
					}
				}
				if (letterToDraw >= 0 && letterToDraw < STAT_LETTERS.size()) {
					statRankLetter = STAT_LETTERS.get(Math.max(1, letterToDraw));
				}
			}
			float letterAlpha = tick_ >= letterFullTick ? 1 : 0.25f + 0.75f * 
					(float) (tick_ - letterStartTick) / (float) (letterFullTick - letterStartTick);


			Component rank = Component.literal(statRankLetter).withStyle(ChatFormatting.BOLD);
			int letterWidth = mc.font.width(rank);
			int letterColor = ARGB.color(letterAlpha, bnw(BLACK, invertBnW));

			float rankX = statX - letterWidth / 2;
			switch (statRankLetter) {
			case "∅":
				renderLetterFromTex(poseStack, letterColor, rankX, statY, 0, 504);
				break;
			case "∞":
				renderLetterFromTex(poseStack, letterColor, rankX, statY, 12, 504);
				break;
			case REFERENCE_MARK:
				renderLetterFromTex(poseStack, letterColor, rankX, statY, 24, 504);
				break;
			default:
				guiGraphics.drawString(mc.font, rank.getVisualOrderText(), 
						rankX, statY, 
						letterColor, false);
			}

			// stat name tooltip
			if (mouseX >= statX - letterWidth / 2 && mouseX <= statX + letterWidth / 2 && mouseY >= statY && mouseY <= statY + mc.font.lineHeight) {
				List<FormattedCharSequence> tooltip = override.statTooltip(stat, mc, power);
				mc.screen.setTooltipForNextRenderPass(tooltip);
				TooltipParams.set(TooltipParams.paperStyle());
			}
		}

		poseStack.popPose();
	}

	public static void renderLetterFromTex(PoseStack poseStack, int color, 
			float statX, float statY, int texX, int texY) {
		RenderSystem.enableBlend();
		BlitFloat.blit(poseStack, Minecraft.getInstance(), STAND_STATS_UI, 
				statX, statY, 8, 7, 0,
				texX, texY, 8, 7, 512, 512, 
				color);
	}


	/*
	 *             r1
	 *             |
	 *    r6 \     |      / r2   
	 *         \   |    /
	 *           center
	 *         /   |    \
	 *    r5 /     |      \ r3   
	 *             |
	 *             r4
	 */
	protected static final float COS_PI_BY_6 = Mth.sqrt(3.0f) / 2.0f;
	protected static final float SIN_PI_BY_6 = 0.5f;
	protected static void fillHexagon(GuiGraphics guiGraphics, float xCenter, float yCenter, 
			float r1, float r2, float r3, float r4, float r5, float r6, 
			int red, int green, int blue, int alpha) {
		RenderSystem.disableDepthTest();
		Matrix4f matrix4f = guiGraphics.pose().last().pose();
		VertexConsumer vertexConsumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());

		/*
		 *          {x1, y1}
		 * 
		 * {x6, y6}          {x2, y2}
		 * 
		 *          {x0, y0}
		 * 
		 * {x5, y5}          {x3, y3}
		 * 
		 *          {x4, y4}
		 */
		float x1 = xCenter;                        float y1 = yCenter - r1;
		float x2 = xCenter + r2 * COS_PI_BY_6;     float y2 = yCenter - r2 * SIN_PI_BY_6;
		float x3 = xCenter + r3 * COS_PI_BY_6;     float y3 = yCenter + r3 * SIN_PI_BY_6;
		float x4 = xCenter;                        float y4 = yCenter + r4;
		float x5 = xCenter - r5 * COS_PI_BY_6;     float y5 = yCenter + r5 * SIN_PI_BY_6;
		float x6 = xCenter - r6 * COS_PI_BY_6;     float y6 = yCenter - r6 * SIN_PI_BY_6;
		// this worked before btw
//		vertexConsumer.begin(6, DefaultVertexFormats.POSITION_COLOR);
//		vertexConsumer.addVertex(matrix4f, xCenter, yCenter, 0).setColor(red, green, blue, alpha);
//		vertexConsumer.addVertex(matrix4f, x1, y1, 0).setColor(red, green, blue, alpha);
//		vertexConsumer.addVertex(matrix4f, x6, y6, 0).setColor(red, green, blue, alpha);
//		vertexConsumer.addVertex(matrix4f, x5, y5, 0).setColor(red, green, blue, alpha);
//		vertexConsumer.addVertex(matrix4f, x4, y4, 0).setColor(red, green, blue, alpha);
//		vertexConsumer.addVertex(matrix4f, x3, y3, 0).setColor(red, green, blue, alpha);
//		vertexConsumer.addVertex(matrix4f, x2, y2, 0).setColor(red, green, blue, alpha);
//		vertexConsumer.addVertex(matrix4f, x1, y1, 0).setColor(red, green, blue, alpha);
//		Tessellator.getInstance().end();
		
		// You'll only draw quads and you'll be happy.

		vertexConsumer.addVertex(matrix4f, xCenter, yCenter, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x4, y4, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x3, y3, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x2, y2, 0).setColor(red, green, blue, alpha);

		vertexConsumer.addVertex(matrix4f, xCenter, yCenter, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x2, y2, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x1, y1, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x6, y6, 0).setColor(red, green, blue, alpha);

		vertexConsumer.addVertex(matrix4f, xCenter, yCenter, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x6, y6, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x5, y5, 0).setColor(red, green, blue, alpha);
		vertexConsumer.addVertex(matrix4f, x4, y4, 0).setColor(red, green, blue, alpha);
		

		if (r1 > 0 && r6 <= 0 && r2 <= 0) {
//			vertexConsumer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vertexConsumer.addVertex(matrix4f, x1 + 2,                    yCenter + 2,                0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x1 + 2,                    y1,                         0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, xCenter - 2,               y1,                         0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, xCenter - 2,               yCenter + 2,                0).setColor(red, green, blue, alpha);
//			Tessellator.getInstance().end();
		}
		if (r2 > 0 && r1 <= 0 && r3 <= 0) {
//			vertexConsumer.begin(9, DefaultVertexFormats.POSITION_COLOR);
			vertexConsumer.addVertex(matrix4f, xCenter + 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x2 + 2 * SIN_PI_BY_6,      y2 + 2 * COS_PI_BY_6,       0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x2 - 2 * SIN_PI_BY_6,      y2 - 2 * COS_PI_BY_6,       0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, xCenter - 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0).setColor(red, green, blue, alpha);
//			Tessellator.getInstance().end();
		}
		if (r3 > 0 && r2 <= 0 && r4 <= 0) {
//			vertexConsumer.begin(9, DefaultVertexFormats.POSITION_COLOR);
			vertexConsumer.addVertex(matrix4f, xCenter + 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, xCenter - 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x3 - 2 * SIN_PI_BY_6,      y3 + 2 * COS_PI_BY_6,       0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x3 + 2 * SIN_PI_BY_6,      y3 - 2 * COS_PI_BY_6,       0).setColor(red, green, blue, alpha);
//			Tessellator.getInstance().end();
		}
		if (r4 > 0 && r3 <= 0 && r5 <= 0) {
//			vertexConsumer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vertexConsumer.addVertex(matrix4f, xCenter - 2,               yCenter - 2,                0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, xCenter - 2,               y4,                         0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x4 + 2,                    y4,                         0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x4 + 2,                    yCenter - 2,                0).setColor(red, green, blue, alpha);
//			Tessellator.getInstance().end();
		}
		if (r5 > 0 && r4 <= 0 && r6 <= 0) {
//			vertexConsumer.begin(9, DefaultVertexFormats.POSITION_COLOR);
			vertexConsumer.addVertex(matrix4f, xCenter - 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x5 - 2 * SIN_PI_BY_6,      y5 - 2 * COS_PI_BY_6,       0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x5 + 2 * SIN_PI_BY_6,      y5 + 2 * COS_PI_BY_6,       0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, xCenter + 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0).setColor(red, green, blue, alpha);
//			Tessellator.getInstance().end();
		}
		if (r6 > 0 && r5 <= 0 && r1 <= 0) {
//			vertexConsumer.begin(9, DefaultVertexFormats.POSITION_COLOR);
			vertexConsumer.addVertex(matrix4f, xCenter + 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x6 + 2 * SIN_PI_BY_6,      y6 - 2 * COS_PI_BY_6,       0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, x6 - 2 * SIN_PI_BY_6,      y6 + 2 * COS_PI_BY_6,       0).setColor(red, green, blue, alpha);
			vertexConsumer.addVertex(matrix4f, xCenter - 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0).setColor(red, green, blue, alpha);
//			Tessellator.getInstance().end();
		}

		RenderSystem.enableDepthTest();
	}
}
