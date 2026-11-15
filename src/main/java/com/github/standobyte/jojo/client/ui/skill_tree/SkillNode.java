package com.github.standobyte.jojo.client.ui.skill_tree;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

// net.minecraft.advancements.AdvancementNode copypaste
public class SkillNode<T> {
	private final SkillHolder<T> holder;
	private final Optional<SkillDisplayInfo> display;
	@Nullable
	private final SkillNode<T> parent;
	private final Set<SkillNode<T>> children = new LinkedHashSet<>();

	public SkillNode(SkillHolder<T> valueHolder, @Nullable SkillNode<T> parent) {
		this.holder = valueHolder;
		this.parent = parent;
		this.display = Optional.of(new SkillDisplayInfo());
	}

	public T value() {
		return holder.value();
	}

	public SkillHolder<T> holder() {
		return holder;
	}
	
	public Optional<SkillDisplayInfo> display() {
		return display;
	}

	@Nullable
	public SkillNode<T> parent() {
		return this.parent;
	}

	public SkillNode<T> root() {
		return getRoot(this);
	}

	public static <T> SkillNode<T> getRoot(SkillNode<T> node) {
		SkillNode<T> root = node;

		while (true) {
			SkillNode<T> parent = root.parent();
			if (parent == null) {
				return root;
			}

			root = parent;
		}
	}

	public Collection<SkillNode<T>> children() {
		return this.children;
	}

	public void addChild(SkillNode<T> child) {
		this.children.add(child);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else {
			if (other instanceof SkillNode otherNode && this.holder.equals(otherNode.holder)) {
				return true;
			}

			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.holder.hashCode();
	}

	@Override
	public String toString() {
		return this.holder.toString();
	}
}
