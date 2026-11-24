package com.github.standobyte.jojo.client.ui.skill_tree.advancement_copypaste;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.skill_tree.SkillHolder;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillNode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

// net.minecraft.advancements.AdvancementTree copypaste
public class SkillTree<T> {
	private final Map<String, SkillNode<T>> nodes = new Object2ObjectOpenHashMap<>();
	private final Set<SkillNode<T>> roots = new LinkedHashSet<>();
	private final Function<T, Optional<String>> getParent;
	
	public SkillTree(Function<T, Optional<String>> getParent) {
		this.getParent = getParent;
	}

	private void remove(SkillNode<T> node) {
		for (SkillNode<T> child : node.children()) {
			this.remove(child);
		}

		this.nodes.remove(node.holder().name());
		if (node.parent() == null) {
			this.roots.remove(node);
		}
	}

	public void remove(Set<String> skills) {
		for (String skillName : skills) {
			SkillNode<T> node = this.nodes.get(skillName);
			if (node != null) {
				this.remove(node);
			}
		}
	}

	public void addAll(Collection<SkillHolder<T>> advancements) {
		List<SkillHolder<T>> list = new ArrayList<>(advancements);

		while (!list.isEmpty()) {
			if (!list.removeIf(this::tryInsert)) {
				break;
			}
		}
	}

	private boolean tryInsert(SkillHolder<T> skill) {
		Optional<String> parentDeclared = getParent.apply(skill.value());
		if (parentDeclared.isEmpty()) {
			SkillNode<T> skillNode = new SkillNode<T>(skill, null);
			roots.add(skillNode);
			nodes.put(skill.name(), skillNode);
			return true;
		}
		else {
			SkillNode<T> parentNode = nodes.get(parentDeclared.get());
			if (parentNode == null) {
				return false;
			}
			SkillNode<T> skillNode = new SkillNode<T>(skill, parentNode);
			parentNode.addChild(skillNode);
			nodes.put(skill.name(), skillNode);
			return true;
		}
	}

	public void clear() {
		this.nodes.clear();
		this.roots.clear();
	}

	public Iterable<SkillNode<T>> roots() {
		return this.roots;
	}

	public Collection<SkillNode<T>> nodes() {
		return this.nodes.values();
	}

	@Nullable
	public SkillNode<T> get(String name) {
		return this.nodes.get(name);
	}

	@Nullable
	public SkillNode<T> get(SkillHolder<T> advancement) {
		return this.nodes.get(advancement.name());
	}

	
	public List<SkillTreeUI<T>> initUI(Minecraft minecraft, Screen screen) {
		List<SkillTreeUI<T>> forest = new ArrayList<>(roots.size());
		for (SkillNode<T> root : roots) {
			if (root.display().isPresent()) {
				TreeNodePosition2.run(root);
				SkillTreeUI<T> tree = SkillTreeUI.create(minecraft, screen, root);
				addChildNode(root, tree);
				forest.add(tree);
			}
		}
		return forest;
	}
	
	public static <T> void addChildNode(SkillNode<T> parent, SkillTreeUI<T> treeUI) {
		for (SkillNode<T> node : parent.children()) {
			if (parent != node) {
				treeUI.addNode(node);
			}
			addChildNode(node, treeUI);
		}
	}
}
