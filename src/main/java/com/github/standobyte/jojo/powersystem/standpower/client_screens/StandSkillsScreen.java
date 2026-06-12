package com.github.standobyte.jojo.powersystem.standpower.client_screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.standskin.text.StandSkinComponent;
import com.github.standobyte.jojo.client.textsymbols.IconSymbols;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.PaperButton;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.Tab;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.TabCategory;
import com.github.standobyte.jojo.client.ui.screen_widgets.ImageButton2;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.Scrolling;
import com.github.standobyte.jojo.client.ui.utils.ScrollingText;
import com.github.standobyte.jojo.client.ui.utils.TextUtil;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MutableTooltipWrapper;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData.StandExpSummary;
import com.github.standobyte.jojo.powersystem.unlockableskill.ClLearnSkillPacket;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill.DevStatus;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandSkillsScreen extends Screen implements IJojoMenuScreen {
	public static final ResourceLocation WINDOW = JojoMod.resLoc("textures/gui/paper_style/stand_skills.png");
	public static final GuiIcon SCROLL_BAR = new GuiIcon(WINDOW, 243, 58, 5, 162, 256, 256);
	public static final GuiIcon CROSS = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/cross8.png"), 8, 8);
	public static final GuiIcon CROSS_HIGHLIGHTED = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/cross8_highlighted.png"), 8, 8);
	
	protected TabCategory category;
	protected Tab tab;

	protected StandPower standPower;
	protected StandTypePersistentData levelingData;
	protected StandSkin standSkin;
	protected Iterable<StandUnlockableSkill> skills;
	@Nullable protected StandUnlockableSkill selectedSkill;
	
	protected Scrolling skillListScrolling;
	protected ScrollingText skillDescription;
	protected ScrollingText skillControls;
	
	protected Button learnSkillButton;
	protected Button resetSkillsButton;
	protected Button learnAllSkillsButton;
	protected MutableTooltipWrapper learnSkillTooltip;
	protected Map<String, ConditionCheck> unlockSkillChecks = new HashMap<>();
	
	protected Button deselectSkillButton;

	public StandSkillsScreen(Component title, TabCategory category, Tab tab) {
		super(title);
		this.category = category;
		this.tab = tab;
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
    protected void init() {
		int x = getWindowX(this);
		int y = getWindowY(this);
		
		standPower = ClientPowerCache.getPower(PowerClass.STAND);
		levelingData = standPower.getCurTypeData();
		skills = standPower.getPowerType().getUnlockableSkills().values();
		skillListScrolling = new Scrolling(162, Iterables.size(skills) * 20 + 2);
		standSkin = StandSkinsLoader.getInstance().getSkin(standPower);

		learnSkillButton = this.addRenderableWidget(new PaperButton(x + 144, y + 201, 80, 20, 
				Component.translatable("jojo_ripples.stand_skills.learn"), 
				button -> {
					if (standPower != null && standPower.hasPower() && selectedSkill != null) {
						PacketDistributor.sendToServer(ClLearnSkillPacket.learnSkill(
								PowerClass.STAND, standPower.getPowerType().getId(), selectedSkill.skillName));
					}
				}));
		learnSkillButton.setTooltip(learnSkillTooltip = new MutableTooltipWrapper() {
			@Override
			public Tooltip updateToolip() {
				if (selectedSkill != null) {
					ConditionCheck canLearn = unlockSkillChecks.get(selectedSkill.skillName);
					if (!canLearn.positive()) {
						Component message = canLearn.warning();
						if (message != null) {
							return Tooltip.create(message.plainCopy().withStyle(ChatFormatting.RED));
						}
					}
				}
				return null;
			}
			
		});
		
		resetSkillsButton = this.addRenderableWidget(new PaperButton(x + 154, y + 201, 70, 20, 
				Component.translatable("jojo_ripples.stand_skills.reset"), 
				button -> {
					if (standPower != null && standPower.hasPower()) {
						PacketDistributor.sendToServer(ClLearnSkillPacket.resetAll(
								PowerClass.STAND, standPower.getPowerType().getId()));
					}
				}));
		resetSkillsButton.setTooltip(Tooltip.create(Component.translatable("jojo_ripples.note.creative_only")));
		
		learnAllSkillsButton = this.addRenderableWidget(new PaperButton(x + 80, y + 201, 70, 20, 
				Component.translatable("jojo_ripples.stand_skills.learn_all"), 
				button -> {
					if (standPower != null && standPower.hasPower()) {
						PacketDistributor.sendToServer(ClLearnSkillPacket.learnAll(
								PowerClass.STAND, standPower.getPowerType().getId()));
					}
				}));
		learnAllSkillsButton.setTooltip(Tooltip.create(Component.translatable("jojo_ripples.note.creative_only")));
		
		skillDescription = new ScrollingText(x + 86, y + 87, 124, 105);
		skillControls = new ScrollingText(x + 100, y + 49, 117, 31);
		setSelectedSkill(this.selectedSkill);
		
		deselectSkillButton = addRenderableWidget(new ImageButton2(x + 68, y + 23, 8, 8, 
				CROSS, CROSS, CROSS_HIGHLIGHTED, CROSS_HIGHLIGHTED, 
				button -> setSelectedSkill(null)) {
			@Override public void playDownSound(SoundManager handler) {}
		});
	}

	protected static final int SKILL_LIST_X = 22;
	protected static final int SKILL_LIST_Y = 57;
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		if (!standPower.hasPower()) {
			onClose();
			return;
		}
		StandInstance standInstance = standPower.getStandInstance().get();
		
		deselectSkillButton.visible = selectedSkill != null;
		StandExpSummary expSummary = levelingData.expSummary(standPower);
		
		this.renderBackground(guiGraphics, mouseX, mouseY, p_283123_);
		
		for (StandUnlockableSkill skill : skills) {
			unlockSkillChecks.put(skill.skillName, skill.canUnlockFromMenu(standPower, levelingData));
		}
		
		learnSkillButton.visible = selectedSkill != null && !levelingData.isSkillUnlocked(selectedSkill.skillName);
		learnSkillButton.active = selectedSkill != null && unlockSkillChecks.get(selectedSkill.skillName).positive();
		
		resetSkillsButton.visible = selectedSkill == null && minecraft.player.isCreative();
		learnAllSkillsButton.visible = selectedSkill == null && minecraft.player.isCreative();
		
		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), WINDOW, 
				x, y, width, height, 0, 
				0, 0, width, height, 256, 256, 
				BlitFloat.NO_TINT);
//		int textColor = standSkin.getColor();
		int textColor = 0xFF000000;

		int skillListX = x + SKILL_LIST_X;
		int skillListY = y + SKILL_LIST_Y;
		skillListScrolling.pushOffsetScissor(guiGraphics, skillListY + 1, skillListX + 1, skillListX + 60);
		
		int spriteX = skillListX + 2;
		int spriteY = skillListY + 4;
		AbilityIconSprites abilityIconSprites = StandSkinsLoader.getInstance().abilityIcons;
		UnlockableSkill hovered = getHoveredSkill(mouseX, mouseY);
		
		for (StandUnlockableSkill skill : skills) {
			boolean isUnlocked = levelingData.isSkillUnlocked(skill.skillName);
			int expCostColor = !isUnlocked ? getExpCostColor(skill) : STAND_EXP_NUMBER_COLOR;
			boolean unlockedOrOnlyMissingStandExp = expCostColor == STAND_EXP_NUMBER_COLOR;
			int skillSpriteColor = unlockedOrOnlyMissingStandExp ? BlitFloat.NO_TINT : 0x40404040;
			
			TextureAtlasSprite icon = abilityIconSprites.getAbilityIcon(skill.skillName, standSkin);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			BlitFloat.blit(guiGraphics.pose(), minecraft, icon, 
					spriteX, spriteY, 16, 16, 0, skillSpriteColor);
			
			if (isUnlocked) {
				guiGraphics.drawString(font, String.valueOf(IconSymbols.CHECKMARK), spriteX + 18, spriteY + 4, 0xFFFFFFFF);
			}
			else {
				int expToUnlock = skill.expToUnlock;
				String expCostLine = expToUnlock > 0 ? String.valueOf(expToUnlock) : "-";
				guiGraphics.drawString(font, expCostLine, spriteX + 18, spriteY + 4, expCostColor, false);
			}
			if (skill.implemented == DevStatus.NYI) {
				guiGraphics.fill(spriteX - 1, spriteY - 1, spriteX + 37, spriteY + 17, 0x80FF0000);
			}
			spriteY += 20;
		}

		skillListScrolling.pop(guiGraphics);
		skillListScrolling.renderScrollBar(skillListX - 8, skillListY + 1, 0, 4, guiGraphics, SCROLL_BAR, 1);

		if (selectedSkill != null) {
			TextUtil.drawRightAlignedString(guiGraphics, font, 
					StandSkinComponent.translatable(standInstance, selectedSkill.tlKeyName), 
					x + getWindowWidth() - 10, y + 24, textColor, false);
			
			skillDescription.draw(4, 3, guiGraphics, this.minecraft.font, textColor, false);
			skillDescription.drawSmallScrollBar(guiGraphics);
			skillControls.draw(4, 3, guiGraphics, this.minecraft.font, textColor, false);
			skillControls.drawSmallScrollBar(guiGraphics);
		}
		
		int maxExp = expSummary.total - expSummary.spent;
		int exp = Math.min(levelingData.getExp(), maxExp);
		Component expLine = exp < maxExp ? Component.literal(String.valueOf(exp)) : Component.translatable("jojo_ripples.stand_exp.max");
		expLine = Component.literal(String.valueOf(IconSymbols.STAND_EXP)).append(expLine);
		guiGraphics.drawString(font, expLine, x + 41 - font.width(expLine) / 2, y + 33, STAND_EXP_NUMBER_COLOR, false);
		
		if (learnSkillButton.visible && selectedSkill != null) {
			int expCostColor = getExpCostColor(selectedSkill);
			if (expCostColor == STAND_EXP_NUMBER_COLOR) {
				Component expCost = Component.literal(IconSymbols.STAND_EXP + " " + String.valueOf(selectedSkill.expToUnlock));
				guiGraphics.drawString(font, expCost, 
						learnSkillButton.getX() - 4 - font.width(expCost), learnSkillButton.getY() + 6, 
						expCostColor, false);
			}
		}
		
		renderTabs(guiGraphics, this);
		
		if (hovered != null) {
			TooltipParams.set(TooltipParams.paperStyle(1));
			List<FormattedCharSequence> skillNameTooltip = new ArrayList<>();
			skillNameTooltip.add(StandSkinComponent.translatable(standInstance, hovered.tlKeyName)
					.withStyle(ChatFormatting.BLACK).getVisualOrderText());
			switch (hovered.implemented) {
				case WIP -> skillNameTooltip.add(Component.translatable("rotp_tag_wip")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC).getVisualOrderText());
				case NYI -> skillNameTooltip.add(Component.translatable("rotp_tag_nyi")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC).getVisualOrderText());
				default -> {}
			}
			setTooltipForNextRenderPass(skillNameTooltip);
		}
		else if (mouseX - x >= 22 && mouseX - x <= 59 && mouseY - y >= 30 && mouseY - y <= 43) {
			TooltipParams.set(TooltipParams.paperStyle(1));
			List<FormattedCharSequence> standExpTooltip = new ArrayList<>();
			standExpTooltip.add(Component.translatable("jojo_ripples.stand_exp")
					.withStyle(ChatFormatting.BLACK).getVisualOrderText());
			standExpTooltip.add(Component.translatable("jojo_ripples.stand_exp.total", expSummary.spent + exp, expSummary.total)
					.withStyle(ChatFormatting.BLACK).getVisualOrderText());
			if (expSummary.remainingHiddenSkills > 0) {
				standExpTooltip.add(Component.translatable("jojo_ripples.stand_exp.skills_left.hidden", expSummary.remainingSkills, expSummary.remainingHiddenSkills)
						.withStyle(ChatFormatting.BLACK).getVisualOrderText());
			}
			else {
				standExpTooltip.add(Component.translatable("jojo_ripples.stand_exp.skills_left", expSummary.remainingSkills)
						.withStyle(ChatFormatting.BLACK).getVisualOrderText());
			}
			standExpTooltip.addAll(Tooltip.splitTooltip(minecraft, Component.translatable("jojo_ripples.stand_exp.desc")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
			setTooltipForNextRenderPass(standExpTooltip);
		}
		else {
			renderTabTooltip(guiGraphics, this, mouseX, mouseY);
		}

		for (Renderable renderable : this.renderables) {
			renderable.render(guiGraphics, mouseX, mouseY, p_283123_);
		}
	}
	
	public static final int STAND_EXP_NUMBER_COLOR = 0x00A000;
	protected int getExpCostColor(StandUnlockableSkill skill) {
		ConditionCheck check = unlockSkillChecks.get(skill.skillName);
		if (check != null && (check.positive() || check == StandUnlockableSkill.NOT_ENOUGH_EXP)) {
			return STAND_EXP_NUMBER_COLOR;
		}
		else {
			return 0xB0B0B0;
		}
	}
	
	@Nullable
	protected StandUnlockableSkill getHoveredSkill(double mouseX, double mouseY) {
		int x = getWindowX(this) + SKILL_LIST_X;
		int y = getWindowY(this) + SKILL_LIST_Y;
		if (mouseX >= x && mouseX <= x + 48) {
			int pos = skillListScrolling.getYHovered(y, (int) mouseY);
			if (pos >= 0) {
				int index = pos / 20;
				int pixel = pos % 20;
				if (pixel > 3 && index < Iterables.size(skills)) {
					return Iterables.get(skills, index);
				}
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (clickTab(mouseX, mouseY, button, this)) return true;
		StandUnlockableSkill skill = getHoveredSkill(mouseX, mouseY);
		if (skill != null) {
			setSelectedSkill(skill);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	public void setSelectedSkill(StandUnlockableSkill skill) {
		if (this.selectedSkill != skill) {
			skillDescription.scrolling.setScrollOffset(0);
			skillControls.scrolling.setScrollOffset(0);
		}
		this.selectedSkill = skill;
		if (skill != null && standPower.hasPower()) {
			StandInstance stand = standPower.getStandInstance().get();
			Component textDesc = StandSkinComponent.translatable(stand, selectedSkill.tlKeyDesc);
			Component textControls = StandSkinComponent.translatable(stand, selectedSkill.tlKeyControls);
			skillDescription.setText(font.split(textDesc, 111));
			skillControls.setText(font.split(textControls, 101));
		}
		else {
			skillDescription.setText(null);
			skillControls.setText(null);
		}
		learnSkillButton.visible = skill != null;
	}

	@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (
				skillDescription.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || 
				skillControls.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
			return true;
		}
    	skillListScrolling.scroll(scrollY);
    	return true;
    }

}
