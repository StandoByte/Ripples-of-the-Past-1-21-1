package com.github.standobyte.jojo.client.ui.skill_tree.more_complex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.screen_jojomenu.stand.StandSkillsScreen;
import com.github.standobyte.jojo.client.ui.skill_tree.DrawStandSkillIcon;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillHolder;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillNode;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillNodeUI;
import com.github.standobyte.jojo.client.ui.skill_tree.Sprites32x32;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public abstract class StandSkillTreeUI {
	public Map<SkillHolder<UnlockableSkill>, SkillNodeUI<UnlockableSkill>> widgets = new LinkedHashMap<>();
	public Map<SkillHolder<UnlockableSkill>, List<NodeConnectivityLineUI<UnlockableSkill>>> connectivityLinesByChild = new HashMap<>();
	
	public static void fill(StandSkillTreeUI ui, StandSkillTreeUILayout layout, Minecraft mc, Screen screen) {
		StandSkillsScreen screen_ = (StandSkillsScreen) screen;
		for (NodePosition<UnlockableSkill> nodePos : layout.allNodes.values()) {
			SkillNode<UnlockableSkill> node = nodePos.node;
			UnlockableSkill skill = node.value();
			SkillNodeUI<UnlockableSkill> widget = new SkillNodeUI<>(null, 
					mc, node, node.display().get()) {

				@Override
				protected void renderIcon(UnlockableSkill value, GuiGraphics guiGraphics, int x, int y) {
					DrawStandSkillIcon.drawSkillIcon(skill, 
							screen_.abilityIconSprites, screen_.standSkin, 
							guiGraphics, x + 8, y + 5, 
							DrawStandSkillIcon.drawnAsSilhouette(skill, screen_.levelingData, screen_.unlockSkillChecks.get(skill.skillName)));
				}
			};
			widget.x = nodePos.x;
			widget.y = nodePos.y;
			ui.widgets.put(node.holder(), widget);
		}
		for (var parentEntry : layout.linksToChildren.entrySet()) {
			for (NodeConnection<UnlockableSkill> connection : parentEntry.getValue()) {
				SkillNodeUI<UnlockableSkill> parent = ui.widgets.get(connection.parent.holder());
				SkillNodeUI<UnlockableSkill> child = ui.widgets.get(connection.child.holder());
				if (parent != null && child != null && parent != child) {
					NodeConnectivityLineUI<UnlockableSkill> connectivity = new NodeConnectivityLineUI<>(parent, child, connection.direction);
					ui.connectivityLinesByChild.computeIfAbsent(connection.child.holder(), __ -> new ArrayList<>()).add(connectivity);
				}
			}
		}
	}
	
	@Nullable
	public SkillNodeUI<UnlockableSkill> getHovered(int x, int y, int mouseX, int mouseY) {
		for (SkillNodeUI<UnlockableSkill> skill : widgets.values()) {
			int nodeX = skill.x + x;
			int nodeY = skill.y + y;
			if (
					mouseX >= nodeX && mouseX <= nodeX + 26 && 
					mouseY >= nodeY && mouseY <= nodeY + 26) {
				return skill;
			}
		}
		return null;
	}

	public void draw(GuiGraphics guiGraphics, 
			int x, int y, int mouseX, int mouseY) {
		drawConnectivityLines(guiGraphics, x, y, mouseX, mouseY, true);
		drawConnectivityLines(guiGraphics, x, y, mouseX, mouseY, false);
		drawSkills(guiGraphics, x, y, mouseX, mouseY);
	}
	
	protected abstract Sprites32x32 getSkillSquareSprite(UnlockableSkill skill);
	
	protected void drawSkills(GuiGraphics guiGraphics, 
			int x, int y, int mouseX, int mouseY) {
		for (SkillNodeUI<UnlockableSkill> skill : widgets.values()) {
			drawSkillNode(skill, guiGraphics,x, y, mouseX, mouseY);
		}
	}
	
	protected void drawSkillNode(SkillNodeUI<UnlockableSkill> skillNode, GuiGraphics guiGraphics, 
			int x, int y, int mouseX, int mouseY) {
		skillNode.draw(guiGraphics, this::getSkillSquareSprite, x, y, mouseX, mouseY);
	}
	
	protected void drawConnectivityLines(GuiGraphics guiGraphics, 
			int x, int y, int mouseX, int mouseY, boolean shadow) {
		for (var lineEntryByChild : connectivityLinesByChild.entrySet()) {
			for (NodeConnectivityLineUI<UnlockableSkill> line : lineEntryByChild.getValue()) {
				int color = shadow ? 0xFF000000 : 0xFFFFFFFF;
				line.draw(guiGraphics, x, y, shadow, color);
			}
		}
	}
	
}
