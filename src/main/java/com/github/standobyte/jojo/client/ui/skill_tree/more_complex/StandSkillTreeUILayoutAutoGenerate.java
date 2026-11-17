package com.github.standobyte.jojo.client.ui.skill_tree.more_complex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.standobyte.jojo.client.ui.skill_tree.SkillDisplayInfo;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillHolder;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillNode;
import com.github.standobyte.jojo.client.ui.skill_tree.advancement_copypaste.TreeNodePosition2;
import com.github.standobyte.jojo.client.ui.utils.Orientation;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;

import net.minecraft.Util;

public class StandSkillTreeUILayoutAutoGenerate {
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
	
	public static void generate(StandSkillTreeUILayout obj, Iterable<SkillNode<UnlockableSkill>> skillTreeRoots) {
		List<List<NodePosition<UnlockableSkill>>> singularSkillRowsBase = new ArrayList<>();
		List<List<NodePosition<UnlockableSkill>>> singularSkillRowsBelow = new ArrayList<>();
		List<VanillaSkillTreeLayout<UnlockableSkill>> trees = new ArrayList<>();
		for (SkillNode<UnlockableSkill> root : skillTreeRoots) {
			Optional<SkillDisplayInfo> rootDisplay = root.display();
			if (rootDisplay.isPresent()) {
				TreeNodePosition2.run(root);
				if (root.children().isEmpty()) {
					NodePosition<UnlockableSkill> node = new NodePosition<>(root, rootDisplay.get());
					obj.allNodes.put(root.value().skillName, node);
					if (BASE_SKILLS.contains(root.value().skillName)) {
						addToRow(singularSkillRowsBase, node, 8);
					}
					else {
						addToRow(singularSkillRowsBelow, node, 8);
					}
				}
				else {
					VanillaSkillTreeLayout<UnlockableSkill> tree = new VanillaSkillTreeLayout<>();
					addWithChildrenRecursive(obj, root, tree);
					trees.add(tree);
				}
			}
		}
		
		int minX = 1;
		int minY = 3;
		int maxX = -Integer.MAX_VALUE;
		int maxY = -Integer.MAX_VALUE;
		
		int x = minX;
		int y = minY;
		for (List<NodePosition<UnlockableSkill>> row : singularSkillRowsBase) {
			for (NodePosition<UnlockableSkill> skill : row) {
				skill.x += x;
				skill.y += y;
				x += 28;
				maxX = Math.max(skill.x, maxX);
				maxY = Math.max(skill.y, maxY);
			}
			x = minX;
			y += 28;
		}
		
		y += 4;
		for (VanillaSkillTreeLayout<UnlockableSkill> tree : trees) {
			for (NodePosition<UnlockableSkill> skill : tree.widgets.values()) {
				skill.x += x;
				skill.y += y;
				maxX = Math.max(skill.x, maxX);
				maxY = Math.max(skill.y, maxY);
			}
			x += tree.getWidth() + 6;
		}
		
		x = minX;
		y = maxY + 32;
		for (List<NodePosition<UnlockableSkill>> row : singularSkillRowsBelow) {
			for (NodePosition<UnlockableSkill> skill : row) {
				skill.x += x;
				skill.y += y;
				x += 28;
				maxX = Math.max(skill.x, maxX);
				maxY = Math.max(skill.y, maxY);
				x += 28;
			}
			x = minX;
			y += 28;
		}
	}
	
	public static void addWithChildrenRecursive(StandSkillTreeUILayout obj, 
			SkillNode<UnlockableSkill> _node, 
			VanillaSkillTreeLayout<UnlockableSkill> tree) {
		NodePosition.create(_node).ifPresent(nodePos -> {
			SkillNode<UnlockableSkill> node = nodePos.node;
			SkillHolder<UnlockableSkill> holder = node.holder();
			String skillName = node.value().skillName;
			tree.addWidget(nodePos, holder);
			obj.allNodes.put(skillName, nodePos);
			if (!node.children().isEmpty()) {
				List<NodeConnection<UnlockableSkill>> connToChildren = new ArrayList<>();
				obj.linksToChildren.put(skillName, connToChildren);
				for (SkillNode<UnlockableSkill> child : node.children()) {
					if (node != child) {
						connToChildren.add(new NodeConnection<>(node, child, Orientation.HORIZONTAL));
						addWithChildrenRecursive(obj, child, tree);
					}
				}
			}
		});
	}
	
	public static <T> void addToRow(List<List<T>> rows, T entry, int maxRowLength) {
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
	
	public static class VanillaSkillTreeLayout<T> {
		public final Map<SkillHolder<T>, NodePosition<T>> widgets = new LinkedHashMap<>();
		private int minX = Integer.MAX_VALUE;
		private int minY = Integer.MAX_VALUE;
		private int maxX = Integer.MIN_VALUE;
		private int maxY = Integer.MIN_VALUE;
		
		public VanillaSkillTreeLayout() {}
		
		public void addWidget(NodePosition<T> node, SkillHolder<T> value) {
			this.widgets.put(value, node);
			int minX = node.x;
			int maxX = minX + 28;
			int minY = node.y;
			int maxY = minY + 27;
			this.minX = Math.min(this.minX, minX);
			this.maxX = Math.max(this.maxX, maxX);
			this.minY = Math.min(this.minY, minY);
			this.maxY = Math.max(this.maxY, maxY);
		}
		
		public int getWidth() {
			return maxX - minX;
		}
		
		public int getHeight() {
			return maxY - minY;
		}
	}
	
}
