package com.github.standobyte.jojo.client.ui.skill_tree.more_complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.client.ui.skill_tree.SkillHolder;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillNode;
import com.github.standobyte.jojo.client.ui.utils.Orientation;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class StandSkillTreeUILayout {
	public Map<String, NodePosition<UnlockableSkill>> allNodes = new HashMap<>();
	public Map<String, List<NodeConnection<UnlockableSkill>>> linksToChildren = new HashMap<>();
	public Consumer<StandSkillTreeUILayout> onScreenReopen;
	
	public void generate(Map<String, UnlockableSkill> allSkills) {
		Map<String, SkillNode<UnlockableSkill>> nodes = new Object2ObjectOpenHashMap<>();
		Set<SkillNode<UnlockableSkill>> roots = new LinkedHashSet<>();
		
		Collection<SkillHolder<UnlockableSkill>> allHolders = allSkills.entrySet().stream()
				.map(entry -> new SkillHolder<>(entry.getKey(), entry.getValue()))
				.collect(Collectors.toCollection(ArrayList::new));
		
		while (!allHolders.isEmpty()) {
			if (!allHolders.removeIf(skill -> tryInsert(skill, nodes, roots, allSkills))) {
				break;
			}
		}
		
		StandSkillTreeUILayoutAutoGenerate.generate(this, roots);
	}
	
	private static boolean tryInsert(SkillHolder<UnlockableSkill> skill,
			Map<String, SkillNode<UnlockableSkill>> nodes,
			Set<SkillNode<UnlockableSkill>> roots,
			Map<String, UnlockableSkill> allSkillsStorage) {
		List<String> parents = skill.value().prerequisiteSkills;
		if (parents.isEmpty()) {
			SkillNode<UnlockableSkill> skillNode = new SkillNode<UnlockableSkill>(skill, null);
			roots.add(skillNode);
			nodes.put(skill.name(), skillNode);
			return true;
		}
		else {
			List<SkillNode<UnlockableSkill>> allParentNodes = new ArrayList<>(parents.size());
			for (String parent : parents) {
				SkillNode<UnlockableSkill> parentNode = nodes.get(parent);
				if (parentNode == null) {
					return false;
				}
				allParentNodes.add(parentNode);
			}
			
			SkillNode<UnlockableSkill> skillNode = SkillNode.withMultipleParents(skill, allParentNodes);
			for (SkillNode<UnlockableSkill> parentNode : allParentNodes) {
				parentNode.addChild(skillNode);
			}
				
			nodes.put(skill.name(), skillNode);
			return true;
		}
	}

	public void setPosition(String skillName, int x, int y) {
		NodePosition<UnlockableSkill> node = allNodes.get(skillName);
		if (node != null) {
			node.x = x;
			node.y = y;
		}
	}

	public void setConnectivityDirection(String parentSkill, String childSkill, Orientation direction) {
		List<NodeConnection<UnlockableSkill>> connections = linksToChildren.get(parentSkill);
		if (connections != null) {
			for (NodeConnection<UnlockableSkill> connection : connections) {
				String childName = connection.child.value().skillName;
				if (childName.equals(childSkill)) {
					connection.direction = direction;
					return;
				}
			}
		}
	}

}
