package com.github.standobyte.jojo.client.ui.skill_tree.more_complex;

import com.github.standobyte.jojo.client.ui.skill_tree.SkillNode;
import com.github.standobyte.jojo.client.ui.utils.Orientation;

public class NodeConnection<T> {
	public SkillNode<T> parent;
	public SkillNode<T> child;
	public Orientation direction;
	
	public NodeConnection(SkillNode<T> parent, SkillNode<T> child, Orientation direction) {
		this.parent = parent;
		this.child = child;
		this.direction = direction;
	}
	
}
