package com.github.standobyte.jojo.client.ui.skill_tree.more_complex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.github.standobyte.jojo.client.ui.skill_tree.SetIfNonNull;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillNode;
import com.github.standobyte.jojo.client.ui.skill_tree.Sprites32x32;
import com.github.standobyte.jojo.client.ui.skill_tree.advancement_copypaste.SkillNodeUI;
import com.github.standobyte.jojo.client.ui.skill_tree.advancement_copypaste.SkillTreeUI;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;

public class StandSkillsStructured {
	public final List<List<SkillNodeUI<UnlockableSkill>>> singularSkillsBase = new ArrayList<>();
	public final List<List<SkillNodeUI<UnlockableSkill>>> singularSkillsBelow = new ArrayList<>();
	public final List<SkillTreeUI<UnlockableSkill>> trees = new ArrayList<>();
	
	public static final Set<String> BASE_SKILLS = Util.make(new HashSet<>(), set -> {
		set.add("punch");
		set.add("barrage");
		set.add("heavy_punch");
		set.add("finisher");
		set.add("uppercut");
		set.add("heavy_charged");
		set.add("grab");
		set.add("grab_throw");
		set.add("block_toss");
		set.add("guard");
		set.add("manual_control");
		set.add("item_use");
		set.add("leap");
		set.add("ledge_grab");
	});
	
	public static StandSkillsStructured structure(List<SkillTreeUI<UnlockableSkill>> categorized) {
		StandSkillsStructured obj = new StandSkillsStructured();
		
		for (SkillTreeUI<UnlockableSkill> skillTree : categorized) {
			SkillNode<UnlockableSkill> root = skillTree.getRootNode();
			if (root.children().isEmpty()) {
				if (BASE_SKILLS.contains(root.value().skillName)) {
					addToRow(obj.singularSkillsBase, skillTree.root, 8);
				}
				else {
					addToRow(obj.singularSkillsBelow, skillTree.root, 8);
				}
			}
			else {
				obj.trees.add(skillTree);
			}
		}
		repositionElements(obj);
		
		return obj;
	}
	
	private static <T> void addToRow(List<List<T>> rows, T entry, int maxRowLength) {
		List<T> row;
		if (rows.isEmpty()) {
			row = new ArrayList<>();
			rows.add(row);
		}
		else {
			row = rows.get(rows.size() - 1);
			if (row.size() >= maxRowLength) {
				row = new ArrayList<>();
				rows.add(row);
			}
		}
		
		row.add(entry);
	}
	
	private static void repositionElements(StandSkillsStructured obj) {
		int minX = 1;
		int minY = 3;
		int maxX = -Integer.MAX_VALUE;
		int maxY = -Integer.MAX_VALUE;
		
		int x = minX;
		int y = minY;
		for (List<SkillNodeUI<UnlockableSkill>> row : obj.singularSkillsBase) {
			for (SkillNodeUI<UnlockableSkill> skill : row) {
				skill.x += x;
				skill.y += y;
				maxX = Math.max(skill.x, maxX);
				maxY = Math.max(skill.y, maxY);
				x += 28;
			}
			x = minX;
			y += 28;
		}
		
		y += 4;
		for (SkillTreeUI<UnlockableSkill> tree : obj.trees) {
			for (SkillNodeUI<UnlockableSkill> skill : tree.widgets.values()) {
				skill.x += x;
				skill.y += y;
				maxX = Math.max(skill.x, maxX);
				maxY = Math.max(skill.y, maxY);
			}
			x += tree.getWidth() + 6;
		}
		
		x = minX;
		y = maxY + 32;
		for (List<SkillNodeUI<UnlockableSkill>> row : obj.singularSkillsBelow) {
			for (SkillNodeUI<UnlockableSkill> skill : row) {
				skill.x += x;
				skill.y += y;
				maxX = Math.max(skill.x, maxX);
				maxY = Math.max(skill.y, maxY);
				x += 28;
			}
			x = minX;
			y += 28;
		}
	}
	
	
	public StandUnlockableSkill draw(GuiGraphics guiGraphics, 
			Function<UnlockableSkill, Sprites32x32> getSprite32x32, 
			int x, int y, int mouseX, int mouseY) {
		SetIfNonNull<UnlockableSkill> hoveredSkillWrapper = new SetIfNonNull<>();
		for (List<SkillNodeUI<UnlockableSkill>> row : singularSkillsBase) {
			for (SkillNodeUI<UnlockableSkill> skill : row) {
				hoveredSkillWrapper.set(skill.draw(guiGraphics, getSprite32x32, x, y, mouseX, mouseY));
			}
		}
		for (SkillTreeUI<UnlockableSkill> tree : trees) {
			hoveredSkillWrapper.set(tree.drawContents(guiGraphics, getSprite32x32, x, y, mouseX, mouseY));
		}
		for (List<SkillNodeUI<UnlockableSkill>> row : singularSkillsBelow) {
			for (SkillNodeUI<UnlockableSkill> skill : row) {
				hoveredSkillWrapper.set(skill.draw(guiGraphics, getSprite32x32, x, y, mouseX, mouseY));
			}
		}
		return (StandUnlockableSkill) hoveredSkillWrapper.get();
	}

	
	public static Optional<String> getParent(UnlockableSkill skill, Map<String, UnlockableSkill> allSkils) {
		for (String parentName : skill.prerequisiteSkills) {
			UnlockableSkill parent = allSkils.get(parentName);
			if (parent != null) {
				return Optional.of(parentName);
			}
		}
		return Optional.empty();
	}
}
