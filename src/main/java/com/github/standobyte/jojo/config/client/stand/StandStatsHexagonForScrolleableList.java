package com.github.standobyte.jojo.config.client.stand;

import static com.github.standobyte.jojo.client.ui.StandStatsRenderer.BLACK;
import static com.github.standobyte.jojo.client.ui.StandStatsRenderer.OVERRIDE_STAT;
import static com.github.standobyte.jojo.client.ui.StandStatsRenderer.REFERENCE_MARK;
import static com.github.standobyte.jojo.client.ui.StandStatsRenderer.STAND_STATS_UI;
import static com.github.standobyte.jojo.client.ui.StandStatsRenderer.STAT_LETTERS;
import static com.github.standobyte.jojo.client.ui.StandStatsRenderer.WHITE;
import static com.github.standobyte.jojo.client.ui.StandStatsRenderer.statsHeight;
import static com.github.standobyte.jojo.client.ui.StandStatsRenderer.statsWidth;

import java.util.List;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.StandStatsRenderer;
import com.github.standobyte.jojo.client.ui.StandStatsRenderer.CosmeticStandStats;
import com.github.standobyte.jojo.client.ui.StandStatsRenderer.HexagonStandStat;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.client.util.functions.RGBUtil;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData.StandExpSummary;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;

public class StandStatsHexagonForScrolleableList extends ScrolleableButtonList.Renderable2 {
	public StandType standType;
	public ResourceLocation standTypeId;
	public StandStats standStats;

	public StandStatsHexagonForScrolleableList(StandType standType, StandStats standStats) {
		super(statsWidth, statsHeight);
		this.standType = standType;
		this.standTypeId = standType != null ? standType.getId() : null;
		this.standStats = standStats;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		
		float statLeveling = 0;
		CosmeticStandStats override = standTypeId != null ? 
				OVERRIDE_STAT.getOrDefault(standTypeId, CosmeticStandStats.DEFAULT)
				: CosmeticStandStats.DEFAULT.getSubStandStats();
		
		StandSkin skin = standTypeId != null ? StandSkinsLoader.getInstance().getDefaultSkin(standTypeId) : null;
		int statsHexagonColor = skin != null ? FastColor.ARGB32.color(191, skin.getColors().stats()) : FastColor.ARGB32.color(127, BLACK);

		float[] statVal = new float[6];
		String[] statRank = new String[6];

		for (int i = 0; i < statVal.length; i++) {
			HexagonStandStat stat = HexagonStandStat.values()[i];
			statVal[i] = switch (stat) {
				case DEV_POTENTIAL -> {
					StandExpSummary allSkills = StandExpSummary.expSummary(standType.getUnlockableSkills().values(), null, null);
					yield StandStatsRenderer.devPotentialValueConverted(allSkills);
				}
				default -> override.statConvertedValue(stat, null, standStats, statLeveling);
			};
			statRank[i] = override.statRankLetter(stat, null, statVal[i]);
		}
		
		float xCenter = x + statsWidth / 2f;
		float yCenter = y + statsHeight / 2f;
		
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		
		// circles
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				0, 0, statsWidth, statsHeight, 512, 512, 
				WHITE);
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				0, 164, statsWidth, statsHeight, 512, 512, 
				WHITE);
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				256, 164, statsWidth, statsHeight, 512, 512, 
				WHITE);
		
		// rotating outer ring effect
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				0, 328, statsWidth, statsHeight, 512, 512, 
				WHITE);
		BlitFloat.blit(poseStack, mc, STAND_STATS_UI, 
				x, y, statsWidth, statsHeight, 0,
				256, 328, statsWidth, statsHeight, 512, 512, 
				WHITE);

		poseStack.pushPose();
		poseStack.translate(xCenter, yCenter, 0);
		
		// scale letters
		for (int i = 1; i < 6; i++) {
			if (STAT_LETTERS.size() <= i) break;
			String letter = STAT_LETTERS.get(i);
			guiGraphics.drawString(mc.font, letter, 
					3.5f, -18.5f - (i - 1) * 9f, 
					WHITE, false);
		}
		poseStack.popPose();
		
		// stats hexagon
		if (standStats != null) {
			for (int i = 0; i < statVal.length; i++) {
				statVal[i] = statVal[i] <= 1 ? statVal[i] * 4 : 4 + (statVal[i] - 1) * 3;

				if (statVal[i] > 0) {
					statVal[i] = Math.min(statVal[i] + 1, 20);
				}

				statVal[i] *= 3;
			}

			int[] argb = RGBUtil.argbInt(statsHexagonColor);
			StandStatsRenderer.fillHexagon(guiGraphics, xCenter, yCenter, 
					statVal[0], statVal[1], statVal[2], 
					statVal[3], statVal[4], statVal[5], 
					argb[1], argb[2], argb[3], argb[0]);
		}
		
		// rank letters on the outer ring
		for (int i = 0; i < statRank.length; i++) {
			HexagonStandStat stat = HexagonStandStat.values()[i];
			float statX = xCenter + stat.x;
			float statY = yCenter + stat.y;
			String statRankLetter = statRank[i];

			Component rank = Component.literal(statRankLetter).withStyle(ChatFormatting.BOLD);
			int letterWidth = mc.font.width(rank);
			int letterColor = WHITE;

			float rankX = statX - letterWidth / 2;
			switch (statRankLetter) {
			case "∅":
				StandStatsRenderer.renderLetterFromTex(poseStack, letterColor, rankX, statY, 0, 504);
				break;
			case "∞":
				StandStatsRenderer.renderLetterFromTex(poseStack, letterColor, rankX, statY, 12, 504);
				break;
			case REFERENCE_MARK:
				StandStatsRenderer.renderLetterFromTex(poseStack, letterColor, rankX, statY, 24, 504);
				break;
			default:
				guiGraphics.drawString(mc.font, rank.getVisualOrderText(), 
						rankX, statY, 
						letterColor, false);
			}

			// stat name tooltip
			if (mouseX >= statX - letterWidth / 2 && mouseX <= statX + letterWidth / 2 && mouseY >= statY && mouseY <= statY + mc.font.lineHeight) {
				List<FormattedCharSequence> tooltip = override.statTooltip(stat, mc, null);
				mc.screen.setTooltipForNextRenderPass(tooltip);
				TooltipParams.set(TooltipParams.paperStyle(1));
			}
		}
		
		poseStack.popPose();
	}

}
