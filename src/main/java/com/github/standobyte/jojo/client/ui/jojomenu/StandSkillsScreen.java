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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StandSkillsScreen extends Screen implements IJojoMenuScreen {
	public static final ResourceLocation WINDOW = JojoMod.resLoc("textures/gui/paper_style/stand_skills.png");
	public static final GuiIcon SCROLL_BAR = new GuiIcon(WINDOW, 243, 58, 5, 162, 256, 256);
	
	public static final Set<String> NOT_YET_IMPLEMENTED = Util.make(new HashSet<>(), set -> {
		Collections.addAll(set, 
				"finisher",
				"block_toss",
				"guard",
				"leap",
				"ledge_grab",
				
				"uppercut",
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
	
	protected TabCategory category;
	protected Tab tab;

	protected StandPower standPower;
	protected StandTypePersistentData levelingData;
	protected StandSkin standSkin;
	protected Scrolling skillListScrolling;
	protected Iterable<UnlockableSkill> skills;
	@Nullable protected UnlockableSkill selectedSkill;

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
		standPower = ClientPowerCache.getPower(PowerClass.STAND);
		levelingData = standPower.getCurTypeData();
		skills = standPower.getPowerType().getUnlockableSkills();
		skillListScrolling = new Scrolling(162, Iterables.size(skills) * 20 + 2);
		standSkin = StandSkinsLoader.getInstance().getSkin(standPower);
    }

	protected static final int SKILL_LIST_X = 22;
	protected static final int SKILL_LIST_Y = 57;
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		super.render(guiGraphics, mouseX, mouseY, p_283123_);

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
		
		int spriteX = skillListX + 4;
		int spriteY = skillListY + 4;
		AbilityIconSprites abilityIconSprites = StandSkinsLoader.getInstance().abilityIcons;
		UnlockableSkill hovered = getHoveredSkill(mouseX, mouseY);
		for (UnlockableSkill skill : skills) {
			TextureAtlasSprite icon = abilityIconSprites.getAbilityIcon(skill.skillName, standSkin);
			BlitFloat.blit(guiGraphics.pose(), minecraft, icon, 
					spriteX, spriteY, 16, 16, 0, BlitFloat.NO_TINT);
			
			if (!NOT_YET_IMPLEMENTED.contains(skill.skillName)) {
				boolean isUnlocked = true;
				if (isUnlocked) {
					guiGraphics.drawString(font, String.valueOf(IconSymbols.CHECKMARK), spriteX + 18, spriteY + 4, 0xFFFFFFFF);
				}
				else {
					int skillPoints = ((StandUnlockableSkill) skill).pointsToUnlock;
					guiGraphics.drawString(font, "(" + String.valueOf(skillPoints) + ")", spriteX + 19, spriteY + 4, textColor);
				}
			}
			spriteY += 20;
		}
		
		skillListScrolling.pop(guiGraphics);
		skillListScrolling.renderScrollBar(skillListX - 8, skillListY + 1, guiGraphics, SCROLL_BAR, 1);
		
		if (selectedSkill != null) {
			TextUtil.drawRightAlignedString(guiGraphics, font, selectedSkill.textName, 
					x + getWindowWidth() - 10, y + 24, textColor, false);
			
			var description = font.split(selectedSkill.textDesc, 111);
			for (int i = 0; i < description.size(); i++) {
				guiGraphics.drawString(this.minecraft.font, description.get(i), x + 90, y + 91 + 9 * i, textColor, false);
			}
			
			var controls = font.split(selectedSkill.textControls.copy(), 101);
			for (int i = 0; i < controls.size(); i++) {
				guiGraphics.drawString(this.minecraft.font, controls.get(i), x + 104, y + 52 + 9 * i, textColor, false);
			}
		}
		
		Component levels = Component.literal(String.valueOf(levelingData.getResolveReached())).withStyle(ChatFormatting.BOLD);
		guiGraphics.drawString(font, levels, 
				x + 41 - font.width(levels) / 2, y + 32, standSkin.getColor(), false);
		
		renderTabs(guiGraphics, this);
		
		if (hovered != null) {
			TooltipParams.set(TooltipParams.paperStyle());
			setTooltipForNextRenderPass(hovered.textName.copy().withStyle(ChatFormatting.BLACK));
		}
		else {
			renderTabTooltip(guiGraphics, this, mouseX, mouseY);
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
		this.selectedSkill = skill;
		if (skill != null) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    	skillListScrolling.scroll(scrollY);
    	return true;
    }

}
