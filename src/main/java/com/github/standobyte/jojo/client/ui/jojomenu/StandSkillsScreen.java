package com.github.standobyte.jojo.client.ui.jojomenu;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.text.IconSymbols;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.Scrolling;
import com.github.standobyte.jojo.client.ui.utils.ScrollingText;
import com.github.standobyte.jojo.client.ui.utils.TextUtil;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.skill.UnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;
import com.google.common.collect.Iterables;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StandSkillsScreen extends Screen implements IJojoMenuScreen {
	public static final ResourceLocation WINDOW = JojoMod.resLoc("textures/gui/paper_style/stand_skills.png");
	public static final GuiIcon SCROLL_BAR = new GuiIcon(WINDOW, 243, 58, 5, 162, 256, 256);
	
	public static final Set<String> NOT_YET_IMPLEMENTED = Util.make(new HashSet<>(), set -> {
		Collections.addAll(set, 
				"block_toss",
				"guard",
				"leap",
				"ledge_grab",
				
				"ground_slam",
				"grab_terrain",
				"uppercut_ground_throw",
				"enhanced_eyesight",
				"time_stop",

				"hit_armor_fix",
				"disfiguring_punch",
				"leave_object",
				"revert_state",
				"create_wall",
				"fuse_with_rock"
				);
	});
	
	public static final int STAND_EXP_NUMBER_COLOR = 0x00A000;
	
	protected TabCategory category;
	protected Tab tab;

	protected StandPower standPower;
	protected StandTypePersistentData levelingData;
	protected StandSkin standSkin;
	protected Iterable<UnlockableSkill> skills;
	@Nullable protected UnlockableSkill selectedSkill;
	
	protected Scrolling skillListScrolling;
	protected ScrollingText skillDescription;
	protected ScrollingText skillControls;
	
	protected Button learnSkillButton;

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
		skills = standPower.getPowerType().getUnlockableSkills();
		skillListScrolling = new Scrolling(162, Iterables.size(skills) * 20 + 2);
		standSkin = StandSkinsLoader.getInstance().getSkin(standPower);

		this.learnSkillButton = this.addRenderableWidget(new PaperButton(x + 144, y + 201, 80, 20, 
				Component.translatable("jojo_ripples.stand_skills.learn"), 
				button -> {
					
				}));
		skillDescription = new ScrollingText(x + 86, y + 87, 124, 105);
		skillControls = new ScrollingText(x + 100, y + 49, 117, 31);
		setSelectedSkill(this.selectedSkill);
	}

	protected static final int SKILL_LIST_X = 22;
	protected static final int SKILL_LIST_Y = 57;
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		this.renderBackground(guiGraphics, mouseX, mouseY, p_283123_);
		
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
		for (UnlockableSkill skill : skills) {
			TextureAtlasSprite icon = abilityIconSprites.getAbilityIcon(skill.skillName, standSkin);
			BlitFloat.blit(guiGraphics.pose(), minecraft, icon, 
					spriteX, spriteY, 16, 16, 0, BlitFloat.NO_TINT);
			
			if (!NOT_YET_IMPLEMENTED.contains(skill.skillName)) {
				boolean isUnlocked = levelingData.isSkillUnlocked(skill.skillName);
				if (isUnlocked) {
					guiGraphics.drawString(font, String.valueOf(IconSymbols.CHECKMARK), spriteX + 18, spriteY + 4, 0xFFFFFFFF);
				}
				else {
					int expToUnlock = ((StandUnlockableSkill) skill).expToUnlock;
					guiGraphics.drawString(font, String.valueOf(expToUnlock), spriteX + 18, spriteY + 4, STAND_EXP_NUMBER_COLOR, false);
				}
			}
			spriteY += 20;
		}

		skillListScrolling.pop(guiGraphics);
		skillListScrolling.renderScrollBar(skillListX - 8, skillListY + 1, 0, 4, guiGraphics, SCROLL_BAR, 1);

		if (selectedSkill != null) {
			TextUtil.drawRightAlignedString(guiGraphics, font, selectedSkill.textName, 
					x + getWindowWidth() - 10, y + 24, textColor, false);
			
			skillDescription.draw(4, 3, guiGraphics, this.minecraft.font, textColor, false);
			skillDescription.drawSmallScrollBar(guiGraphics);
			skillControls.draw(4, 3, guiGraphics, this.minecraft.font, textColor, false);
			skillControls.drawSmallScrollBar(guiGraphics);
		}
		
		Component exp = Component.literal(IconSymbols.STAND_EXP + " " + String.valueOf(levelingData.getExp()));
		guiGraphics.drawString(font, exp, x + 41 - font.width(exp) / 2, y + 32, STAND_EXP_NUMBER_COLOR, false);
		
		renderTabs(guiGraphics, this);
		
		if (hovered != null) {
			TooltipParams.set(TooltipParams.paperStyle());
			setTooltipForNextRenderPass(hovered.textName.copy().withStyle(ChatFormatting.BLACK));
		}
		else {
			renderTabTooltip(guiGraphics, this, mouseX, mouseY);
		}

		for (Renderable renderable : this.renderables) {
			renderable.render(guiGraphics, mouseX, mouseY, p_283123_);
		}
	}
	
	@Nullable
	protected UnlockableSkill getHoveredSkill(double mouseX, double mouseY) {
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
		UnlockableSkill skill = getHoveredSkill(mouseX, mouseY);
		if (skill != null) {
			setSelectedSkill(skill);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	public void setSelectedSkill(UnlockableSkill skill) {
		if (this.selectedSkill != skill) {
			skillDescription.scrolling.setScrollOffset(0);
			skillControls.scrolling.setScrollOffset(0);
		}
		this.selectedSkill = skill;
		if (skill != null) {
			skillDescription.setText(font.split(selectedSkill.textDesc, 111));
			skillControls.setText(font.split(selectedSkill.textControls.copy(), 101));
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
