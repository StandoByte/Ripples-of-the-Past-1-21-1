package com.github.standobyte.jojo.client.ui.jojomenu;

import java.util.List;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.StandSkinsScreen;
import com.github.standobyte.jojo.client.text.ShortenText;
import com.github.standobyte.jojo.client.ui.StandStatsRenderer;
import com.github.standobyte.jojo.client.ui.StandStatsRenderer.CosmeticStandStats;
import com.github.standobyte.jojo.client.ui.StandStatsRenderer.HexagonStandStat;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModStands;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;

public class StandInfoScreen extends Screen implements IJojoMenuScreen {
	public static final ResourceLocation WINDOW = JojoMod.resLoc("textures/gui/paper_style/empty3.png");
	
	protected ResourceLocation texture;
	protected TabCategory category;
	protected Tab tab;
	
	protected StandPower standData;
	protected StandSkin standSkin;
	protected StandSkinsScreen.SkinView standRender;
	protected int tickCount = 0;

	public StandInfoScreen(TabCategory category, Tab tab) {
		super(Component.empty());
		this.category = category;
		this.tab = tab;
		this.texture = JojoMod.resLoc("textures/gui/paper_style/stand_stats.png");
	}
	
	@Override
	public void init() {
		super.init();
		standData = ClientPowerCache.getPower(PowerClass.STAND);
		if (standData.hasPower()) {
			standSkin = StandSkinsLoader.getInstance().getSkin(standData);
			standRender = standData.getPowerType().makeSkinUIElement(standSkin, null, 0, 0, 0, 0, 0, true);
		}
	}

	@Override
	public TabCategory getTabCategory() {
		return category;
	}

	@Override
	public Tab getTab() {
		return tab;
	}
	
	@Override
	public void tick() { this.tickCount++; }

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		super.render(guiGraphics, mouseX, mouseY, p_283123_);

		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), texture, 
				x, y, width, height, 0, 
				0, 0, width, height, 256, 256, 
				BlitFloat.NO_TINT);
		
		if (standData != null && standData.hasPower()) {
			renderContents(standData, guiGraphics, mouseX, mouseY,
					minecraft.player, true, true, true);
		}
		
		renderTabs(guiGraphics, this);
		renderTabTooltip(guiGraphics, this, mouseX, mouseY);
	}
	
	protected void renderContents(StandPower power, GuiGraphics guiGraphics, int mouseX, int mouseY,
			LivingEntity user, boolean knownStand, boolean knownUser, boolean knownStats) {
		PoseStack poseStack = guiGraphics.pose();
		
		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		
		StandType standType = power.getPowerType();
		StandStats stats = standType.getStandStats();
//		float statLeveling = power.getStatsDevelopment();
		float statLeveling = 0;
		CosmeticStandStats override = CosmeticStandStats.getHandler(power);

		float[] statVal = new float[6];
		String[] statRank = new String[6];

		for (int i = 0; i < statVal.length; i++) {
			HexagonStandStat stat = HexagonStandStat.values()[i];
			statVal[i] = override.statConvertedValue(stat, power, stats, statLeveling);
			statRank[i] = override.statRankLetter(stat, power, statVal[i]);
		}

		// stand name and user
		Component standName = Component.translatable("jojo_ripples.stand_stat.stand_name", 
				knownStand ? override.standName(power) : Component.translatable("multiplayer.status.unknown"));
		Component standUser = Component.translatable("jojo_ripples.stand_stat.stand_user", 
				knownUser ? user.getDisplayName() : Component.translatable("multiplayer.status.unknown"));
		int standNameX = x + (width - minecraft.font.width(standName)) / 2;
		int standUserX = x + (width - minecraft.font.width(standUser)) / 2;
		guiGraphics.drawString(minecraft.font, standName, standNameX, y + 9, 0xFF000000, false);
		guiGraphics.drawString(minecraft.font, standUser, standUserX, y + 20, 0xFF000000, false);

//		if (knownStand) {
//			GuiIcon standIcon = override.standIcon(power);
//			if (standIcon != null) {
//				standIcon.render(poseStack, x + statsWidth - 18 - width, standIconY);
//			}
//		}
//
//		if (knownUser) {
//			ClientUtil.renderEntityFace(poseStack, x + statsWidth - 18 - width, userIconY, user);
//		}

		// stand stats
		for (int i = 0; i < statRank.length; i++) {
			HexagonStandStat stat = HexagonStandStat.values()[i];
			String statRankLetter = knownStats ? statRank[i] : "?";

			Component statName = ((MutableComponent) ShortenText.shortenIfAble(stat.name));
			Component rank = Component.literal(statRankLetter).withStyle(ChatFormatting.BOLD);
			int statNameWidth = minecraft.font.width(statName);
			int letterWidth = minecraft.font.width(rank);
			int letterColor = 0xFF000000;
			
			int statBoxWidth = 74;
			int statBoxHeight = 14;
			
			float statCenterX = x + 41 + i % 3 * statBoxWidth;
			float statX = statCenterX - (statNameWidth + letterWidth) / 2;
			float statY = y + 35 + (i >= 3 ? 1 : 0) * statBoxHeight;
			float rankX = statX + statNameWidth;

			guiGraphics.drawString(minecraft.font, statName, 
					(int) statX, (int) statY, letterColor, false);
			
			switch (statRankLetter) {
			case "∅":
				letterWidth = 9;
				StandStatsRenderer.renderLetterFromTex(poseStack, letterColor, rankX, statY, 0, 504);
				break;
			case "∞":
				letterWidth = 9;
				StandStatsRenderer.renderLetterFromTex(poseStack, letterColor, rankX, statY, 12, 504);
				break;
			case StandStatsRenderer.REFERENCE_MARK:
				letterWidth = 9;
				StandStatsRenderer.renderLetterFromTex(poseStack, letterColor, rankX + 1f, statY, 24, 504);
				break;
			default:
				guiGraphics.drawString(minecraft.font, rank.getVisualOrderText(), 
						rankX, statY, 
						letterColor, false);
			}

			// stat name tooltip
			if (mouseX >= statCenterX - statBoxWidth / 2 && mouseX <= statCenterX + statBoxWidth / 2
					&& mouseY >= statY - (statBoxHeight - minecraft.font.lineHeight) / 2 && mouseY <= statY + (statBoxHeight + minecraft.font.lineHeight) / 2) {
				List<FormattedCharSequence> tooltip = override.statTooltip(stat, minecraft, power);
				minecraft.screen.setTooltipForNextRenderPass(tooltip);
				TooltipParams.set(TooltipParams.paperStyle(1));
			}
		}
		
		
		// stand model
		if (standRender != null) {
			float partialTick = ClientUtil.partialTick(Minecraft.getInstance().getTimer(), true);
			standRender.renderInStandInfo(guiGraphics, mouseX, mouseY, tickCount + partialTick, x, y, 35);
		}
		
		
		// stand description
		var description = minecraft.font.split(Component.translatable(standData.getPowerType().getId().toLanguageKey("stand", "desc")), 200);
		int lineX = x + 10;
		int lineHeight = 9;
		int lineY = y + 220 - description.size() * lineHeight;
		for (var line : description) {
			guiGraphics.drawString(minecraft.font, line, lineX, lineY, 0xFF000000, false);
			lineY += lineHeight;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (clickTab(mouseX, mouseY, button, this)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	// XXX scrollable text box with the description
	@Deprecated
	public static float spHairTmpCrutch(StandType standType) {
		return standType == ModStands.STAR_PLATINUM.get() ? 0 : -10;
	}

}
