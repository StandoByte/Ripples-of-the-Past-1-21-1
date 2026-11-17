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
import com.github.standobyte.jojo.client.textsymbols.IconSymbols;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.PaperButton;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.Tab;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.TabCategory;
import com.github.standobyte.jojo.client.ui.screen_widgets.PaperCornerTurnOverButton;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillNodeUI;
import com.github.standobyte.jojo.client.ui.skill_tree.Sprites32x32;
import com.github.standobyte.jojo.client.ui.skill_tree.more_complex.StandSkillTreeUI;
import com.github.standobyte.jojo.client.ui.utils.Alignment;
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
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData.StandExpSummary;
import com.github.standobyte.jojo.powersystem.unlockableskill.ClLearnSkillPacket;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandSkillsScreen extends Screen implements IJojoMenuScreen {
	public static final ResourceLocation WINDOW = JojoMod.resLoc("textures/gui/paper_style/stand_skills.png");
	public static final ResourceLocation WINDOW_BLANK = JojoMod.resLoc("textures/gui/paper_style/stand_skills2.png");
	public static final GuiIcon SCROLL_BAR = new GuiIcon(WINDOW, 243, 58, 5, 162, 256, 256);
	public static final ResourceLocation PAGE_BACKWARD_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted");
	public static final ResourceLocation PAGE_BACKWARD_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_backward");
	
	public static final Sprites32x32 SKILL_SQUARE_UNLOCKABLE = new Sprites32x32(
			JojoMod.resLoc("stand_skill"), null);
	public static final Sprites32x32 SKILL_SQUARE_UNLOCKED = new Sprites32x32(
			JojoMod.resLoc("stand_skill_unlocked"), JojoMod.resLoc("stand_skill_unlocked2"));
	public static final Sprites32x32 SKILL_SQUARE_CLOSED = new Sprites32x32(
			JojoMod.resLoc("stand_skill_closed"), null);
	
	protected TabCategory category;
	protected Tab tab;

	protected StandPower standPower;
	protected StandTypePersistentData levelingData;
	protected StandSkin standSkin;
	protected Iterable<StandUnlockableSkill> skills;
	
	protected StandSkillScreenMode mode = StandSkillScreenMode.TREES;
	@Nullable protected StandUnlockableSkill hoveredSkill;
	@Nullable protected StandUnlockableSkill selectedSkill;
	
	protected Scrolling skillListScrolling;
	protected ScrollingText skillDescription;
	protected ScrollingText skillControls;
	
	protected Button learnSkillButton;
	protected Button resetSkillsButton;
	protected Button learnAllSkillsButton;
	protected MutableTooltipWrapper learnSkillTooltip;
	protected Button deselectSkillButton;
	protected Map<String, ConditionCheck> unlockSkillChecks = new HashMap<>();
	
	protected StandSkillTreeUI skillsLayout;
	

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
	
	protected enum StandSkillScreenMode {
		TREES,
		SKILL_DESC
	}

	@Override
    protected void init() {
		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		
		standPower = ClientPowerCache.getPower(PowerClass.STAND);
		levelingData = standPower.getCurTypeData();
		StandType standType = standPower.getPowerType();
		skills = standType.getUnlockableSkills().values();
		skillsLayout = StandSkillTreeUI.create(standType.getSkillTreeLayout(), getMinecraft(), this);
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
		
		resetSkillsButton = this.addRenderableWidget(new PaperButton(x + width + 4, y + height - 24, 70, 20, 
				Component.translatable("jojo_ripples.stand_skills.reset"), 
				button -> {
					if (standPower != null && standPower.hasPower()) {
						PacketDistributor.sendToServer(ClLearnSkillPacket.resetAll(
								PowerClass.STAND, standPower.getPowerType().getId()));
					}
				}));
		resetSkillsButton.setTooltip(Tooltip.create(Component.translatable("jojo_ripples.note.creative_only")));
		
		learnAllSkillsButton = this.addRenderableWidget(new PaperButton(x + width + 4, y + height - 48, 70, 20, 
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
		
		deselectSkillButton = addRenderableWidget(new PaperCornerTurnOverButton(
				x, y + getWindowHeight() - PaperCornerTurnOverButton.HEIGHT, CommonComponents.GUI_BACK, 
				button -> setSelectedSkill(null), HumanoidArm.LEFT, true));
	}
	
	protected static Sprites32x32 getSkillSquareSprite(UnlockableSkill skill, 
			StandTypePersistentData playerStandSkills, Map<String, ConditionCheck> unlockSkillChecks) {
		if (playerStandSkills.isSkillUnlocked(skill.skillName)) {
			return SKILL_SQUARE_UNLOCKED;
		}
		ConditionCheck canUnlock = unlockSkillChecks.get(skill.skillName);
		return canUnlock.positive() ? SKILL_SQUARE_UNLOCKABLE : SKILL_SQUARE_CLOSED;
	}

	protected static final int SKILL_LIST_X = 22;
	protected static final int SKILL_LIST_Y = 57;
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		if (!standPower.hasPower()) {
			onClose();
			return;
		}
		LivingEntity standUser = standPower.getUser();
		mode = selectedSkill != null ? StandSkillScreenMode.SKILL_DESC : StandSkillScreenMode.TREES;
		this.renderBackground(guiGraphics, mouseX, mouseY, p_283123_);
		
		StandInstance standInstance = standPower.getStandInstance().get();
		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		StandExpSummary expSummary = levelingData.expSummary(standPower);
		
		for (StandUnlockableSkill skill : skills) {
			unlockSkillChecks.put(skill.skillName, skill.canUnlockFromMenu(standPower, levelingData));
		}
		
		deselectSkillButton.visible = selectedSkill != null;
		learnSkillButton.visible = selectedSkill != null && !levelingData.isSkillUnlocked(selectedSkill.skillName);
		learnSkillButton.active = selectedSkill != null && unlockSkillChecks.get(selectedSkill.skillName).positive();
		
		resetSkillsButton.visible = selectedSkill == null && minecraft.player.isCreative();
		learnAllSkillsButton.visible = selectedSkill == null && minecraft.player.isCreative();
		
		switch (mode) {
			case TREES -> {
				BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), WINDOW_BLANK, 
						x, y, width, height, 0, 
						0, 0, width, height, 256, 256, 
						BlitFloat.NO_TINT);
				
				SkillNodeUI<UnlockableSkill> hovered = skillsLayout.getHovered(x, y, mouseX, mouseY);
				hoveredSkill = hovered != null ? (StandUnlockableSkill) hovered.getValue() : null;
				skillsLayout.draw(guiGraphics, 
						skill -> getSkillSquareSprite(skill, levelingData, unlockSkillChecks), 
						x, y, mouseX, mouseY);
				
				renderStandExp(guiGraphics, x + 4, y + height - 13, Alignment.LEFT, mouseX, mouseY, standUser, expSummary);
				
				if (hoveredSkill != null) {
					TooltipParams.set(TooltipParams.paperStyle(1));
					List<FormattedCharSequence> skillNameTooltip = new ArrayList<>();
					skillNameTooltip.add(hoveredSkill.getSkillName(standInstance)
							.withStyle(ChatFormatting.BLACK).getVisualOrderText());
					switch (hoveredSkill.implemented) {
						case WIP -> skillNameTooltip.add(Component.translatable("rotp_tag_wip")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC).getVisualOrderText());
						case NYI -> skillNameTooltip.add(Component.translatable("rotp_tag_nyi")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC).getVisualOrderText());
						case IMPLEMENTED -> {}
					}
					setTooltipForNextRenderPass(skillNameTooltip);
				}
			}
			case SKILL_DESC -> {
				BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), WINDOW, 
						x, y, width, height, 0, 
						0, 0, width, height, 256, 256, 
						BlitFloat.NO_TINT);
				//int textColor = standSkin.getColor();
				int textColor = 0xFF000000;
		
				int skillListX = x + SKILL_LIST_X;
				int skillListY = y + SKILL_LIST_Y;
				skillListScrolling.pushOffsetScissor(guiGraphics, skillListY + 1, skillListX + 1, skillListX + 60);
				
				int spriteX = skillListX + 2;
				int spriteY = skillListY + 4;
				AbilityIconSprites abilityIconSprites = StandSkinsLoader.getInstance().abilityIcons;
				
				hoveredSkill = null;
				if (mouseX >= x + SKILL_LIST_X && mouseX <= x + SKILL_LIST_X + 48) {
					int pos = skillListScrolling.getYHovered(y + SKILL_LIST_Y, (int) mouseY);
					if (pos >= 0) {
						int index = pos / 20;
						int pixel = pos % 20;
						if (pixel > 3 && index < Iterables.size(skills)) {
							hoveredSkill = Iterables.get(skills, index);
						}
					}
				}
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
					
					switch (skill.implemented) {
						case NYI -> guiGraphics.fill(spriteX - 1, spriteY - 1, spriteX + 37, spriteY + 17, 0x80FF0000);
						case WIP -> guiGraphics.fill(spriteX - 1, spriteY - 1, spriteX + 37, spriteY + 17, 0x80FFFF00);
						case IMPLEMENTED -> {}
					}
					spriteY += 20;
				}
		
				skillListScrolling.pop(guiGraphics);
				skillListScrolling.renderScrollBar(skillListX - 8, skillListY + 1, 0, 4, guiGraphics, SCROLL_BAR, 1);
		
				TextUtil.drawRightAlignedString(guiGraphics, font, 
						selectedSkill.getSkillName(standInstance),
						x + getWindowWidth() - 10, y + 24, textColor, false);
				
				skillDescription.draw(4, 3, guiGraphics, this.minecraft.font, textColor, false);
				skillDescription.drawSmallScrollBar(guiGraphics);
				skillControls.draw(4, 3, guiGraphics, this.minecraft.font, textColor, false);
				skillControls.drawSmallScrollBar(guiGraphics);
				
				renderStandExp(guiGraphics, x + 41, y + 33, Alignment.CENTER, mouseX, mouseY, standUser, expSummary);
				
				if (learnSkillButton.visible) {
					int expCostColor = getExpCostColor(selectedSkill);
					if (expCostColor == STAND_EXP_NUMBER_COLOR) {
						Component expCost = Component.literal(IconSymbols.STAND_EXP + " " + String.valueOf(selectedSkill.expToUnlock));
						guiGraphics.drawString(font, expCost, 
								learnSkillButton.getX() - 4 - font.width(expCost), learnSkillButton.getY() + 6, 
								expCostColor, false);
					}
				}
				
				if (hoveredSkill != null) {
					TooltipParams.set(TooltipParams.paperStyle(1));
					List<FormattedCharSequence> skillNameTooltip = new ArrayList<>();
					skillNameTooltip.add(hoveredSkill.getSkillName(standInstance)
							.withStyle(ChatFormatting.BLACK).getVisualOrderText());
					switch (hoveredSkill.implemented) {
						case WIP -> skillNameTooltip.add(Component.translatable("rotp_tag_wip")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC).getVisualOrderText());
						case NYI -> skillNameTooltip.add(Component.translatable("rotp_tag_nyi")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC).getVisualOrderText());
						case IMPLEMENTED -> {}
					}
					setTooltipForNextRenderPass(skillNameTooltip);
				}
			}
		}

		renderTabs(guiGraphics, this);
		renderTabTooltip(guiGraphics, this, mouseX, mouseY);
		for (Renderable renderable : this.renderables) {
			renderable.render(guiGraphics, mouseX, mouseY, p_283123_);
		}
	}
	
	protected void renderStandExp(GuiGraphics guiGraphics, int x, int y, Alignment alignment, int mouseX, int mouseY, LivingEntity standUser, StandExpSummary expSummary) {
		int maxPossibleExp = expSummary.total - expSummary.spent;
		boolean hasInfiniteExp = StandTypePersistentData.hasInfiniteExp(standUser);
		int canSpendExp = Math.min(levelingData.getExp(), maxPossibleExp);
		boolean isAtMaxExp = canSpendExp >= maxPossibleExp;
		int expAcquired = canSpendExp + expSummary.spent;
		int expTotalForStand = expSummary.total;
		
		Component expLine = 
				hasInfiniteExp 	? Component.literal("∞") :
				!isAtMaxExp 	? Component.literal(String.valueOf(canSpendExp)) : 
				Component.translatable("jojo_ripples.stand_exp.max");
		expLine = Component.literal(String.valueOf(IconSymbols.STAND_EXP)).append(expLine);
		x += alignment.getOffset(font, expLine);
		guiGraphics.drawString(font, expLine, x, y, STAND_EXP_NUMBER_COLOR, false);
		
		if (mouseX - x >= -5 && mouseX - x <= 32 && mouseY - y >= -3 && mouseY - y <= 10) {
			TooltipParams.set(TooltipParams.paperStyle(1));
			List<FormattedCharSequence> standExpTooltip = new ArrayList<>();
			standExpTooltip.add(Component.translatable("jojo_ripples.stand_exp")
					.withStyle(ChatFormatting.BLACK).getVisualOrderText());
			standExpTooltip.add(Component.translatable("jojo_ripples.stand_exp.total", expAcquired, expTotalForStand)
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
	
	public void setSelectedSkill(StandUnlockableSkill skill) {
		if (this.selectedSkill != skill) {
			skillDescription.scrolling.setScrollOffset(0);
			skillControls.scrolling.setScrollOffset(0);
		}
		this.selectedSkill = skill;
		if (skill != null && standPower.hasPower()) {
			StandInstance stand = standPower.getStandInstance().get();
			Component textDesc = selectedSkill.getSkillDescription(stand);
			Component textControls = selectedSkill.getSkillControlsHint(stand);
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (clickTab(mouseX, mouseY, button, this)) return true;
		if (hoveredSkill != null) {
			setSelectedSkill(hoveredSkill);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return switch (mode) {
			case SKILL_DESC -> {
				if (
						skillDescription.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || 
						skillControls.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
					yield true;
				}
		    	skillListScrolling.scroll(scrollY);
				yield true;
			}
			case TREES -> {
				yield false;
			}
		};
    }
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (closeOnKeybind(minecraft, keyCode, scanCode, modifiers)) return true;
		if (selectedSkill != null && keyCode == InputConstants.KEY_ESCAPE) {
			setSelectedSkill(null);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

}
