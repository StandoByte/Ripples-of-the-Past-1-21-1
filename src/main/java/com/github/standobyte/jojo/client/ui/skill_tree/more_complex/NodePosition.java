package com.github.standobyte.jojo.client.ui.skill_tree.more_complex;

import java.util.Optional;

import com.github.standobyte.jojo.client.ui.skill_tree.SkillDisplayInfo;
import com.github.standobyte.jojo.client.ui.skill_tree.SkillNode;

import net.minecraft.util.Mth;

public class NodePosition<T> {
	public SkillNode<T> node;
	public int x;
	public int y;
	
	public static <T> Optional<NodePosition<T>> create(SkillNode<T> node) {
		return node.display().map(display -> new NodePosition<>(node, display));
	}
	
	public NodePosition(SkillNode<T> node, SkillDisplayInfo display) {
		this(node, Mth.floor(display.getX() * 28.0F), Mth.floor(display.getY() * 27.0F));
	}
	
	public NodePosition(SkillNode<T> node, int x, int y) {
		this.node = node;
		this.x = x;
		this.y = y;
	}
	
}
