package com.github.standobyte.jojo.client.ui.skill_tree;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

// net.minecraft.advancements.TreeNodePosition copypaste
public class TreeNodePosition2<T> {
	private final SkillNode<T> node;
	@Nullable
	private final TreeNodePosition2<T> parent;
	@Nullable
	private final TreeNodePosition2<T> previousSibling;
	private final int childIndex;
	private final List<TreeNodePosition2<T>> children = Lists.newArrayList();
	private TreeNodePosition2<T> ancestor;
	@Nullable
	private TreeNodePosition2<T> thread;
	private int x;
	private float y;
	private float mod;
	private float change;
	private float shift;

	public TreeNodePosition2(SkillNode<T> node, @Nullable TreeNodePosition2<T> parent, @Nullable TreeNodePosition2<T> previousSibling, int childIndex, int x) {
		if (node.display().isEmpty()) {
			throw new IllegalArgumentException("Can't position an invisible advancement!");
		} else {
			this.node = node;
			this.parent = parent;
			this.previousSibling = previousSibling;
			this.childIndex = childIndex;
			this.ancestor = this;
			this.x = x;
			this.y = -1.0F;
			TreeNodePosition2<T> treenodeposition = null;

			for (SkillNode<T> advancementnode : node.children()) {
				treenodeposition = this.addChild(advancementnode, treenodeposition);
			}
		}
	}

	@Nullable
	private TreeNodePosition2<T> addChild(SkillNode<T> child, @Nullable TreeNodePosition2<T> previousSibling) {
		if (child.display().isPresent()) {
			previousSibling = new TreeNodePosition2<>(child, this, previousSibling, this.children.size() + 1, this.x + 1);
			this.children.add(previousSibling);
		} else {
			for (SkillNode<T> advancementnode : child.children()) {
				previousSibling = this.addChild(advancementnode, previousSibling);
			}
		}

		return previousSibling;
	}

	private void firstWalk() {
		if (this.children.isEmpty()) {
			if (this.previousSibling != null) {
				this.y = this.previousSibling.y + 1.0F;
			} else {
				this.y = 0.0F;
			}
		} else {
			TreeNodePosition2<T> treenodeposition = null;

			for (TreeNodePosition2<T> treenodeposition1 : this.children) {
				treenodeposition1.firstWalk();
				treenodeposition = treenodeposition1.apportion(treenodeposition == null ? treenodeposition1 : treenodeposition);
			}

			this.executeShifts();
			float f = (this.children.get(0).y + this.children.get(this.children.size() - 1).y) / 2.0F;
			if (this.previousSibling != null) {
				this.y = this.previousSibling.y + 1.0F;
				this.mod = this.y - f;
			} else {
				this.y = f;
			}
		}
	}

	private float secondWalk(float offsetY, int columnX, float subtreeTopY) {
		this.y += offsetY;
		this.x = columnX;
		if (this.y < subtreeTopY) {
			subtreeTopY = this.y;
		}

		for (TreeNodePosition2<T> treenodeposition : this.children) {
			subtreeTopY = treenodeposition.secondWalk(offsetY + this.mod, columnX + 1, subtreeTopY);
		}

		return subtreeTopY;
	}

	private void thirdWalk(float y) {
		this.y += y;

		for (TreeNodePosition2<T> treenodeposition : this.children) {
			treenodeposition.thirdWalk(y);
		}
	}

	private void executeShifts() {
		float f = 0.0F;
		float f1 = 0.0F;

		for (int i = this.children.size() - 1; i >= 0; i--) {
			TreeNodePosition2<T> treenodeposition = this.children.get(i);
			treenodeposition.y += f;
			treenodeposition.mod += f;
			f1 += treenodeposition.change;
			f += treenodeposition.shift + f1;
		}
	}

	@Nullable
	private TreeNodePosition2<T> previousOrThread() {
		if (this.thread != null) {
			return this.thread;
		} else {
			return !this.children.isEmpty() ? this.children.get(0) : null;
		}
	}

	@Nullable
	private TreeNodePosition2<T> nextOrThread() {
		if (this.thread != null) {
			return this.thread;
		} else {
			return !this.children.isEmpty() ? this.children.get(this.children.size() - 1) : null;
		}
	}

	private TreeNodePosition2<T> apportion(TreeNodePosition2<T> node) {
		if (this.previousSibling == null) {
			return node;
		} else {
			TreeNodePosition2<T> treenodeposition = this;
			TreeNodePosition2<T> treenodeposition1 = this;
			TreeNodePosition2<T> treenodeposition2 = this.previousSibling;
			TreeNodePosition2<T> treenodeposition3 = this.parent.children.get(0);
			float f = this.mod;
			float f1 = this.mod;
			float f2 = treenodeposition2.mod;

			float f3;
			for (f3 = treenodeposition3.mod;
					treenodeposition2.nextOrThread() != null && treenodeposition.previousOrThread() != null;
					f1 += treenodeposition1.mod
					) {
				treenodeposition2 = treenodeposition2.nextOrThread();
				treenodeposition = treenodeposition.previousOrThread();
				treenodeposition3 = treenodeposition3.previousOrThread();
				treenodeposition1 = treenodeposition1.nextOrThread();
				treenodeposition1.ancestor = this;
				float f4 = treenodeposition2.y + f2 - (treenodeposition.y + f) + 1.0F;
				if (f4 > 0.0F) {
					treenodeposition2.getAncestor(this, node).moveSubtree(this, f4);
					f += f4;
					f1 += f4;
				}

				f2 += treenodeposition2.mod;
				f += treenodeposition.mod;
				f3 += treenodeposition3.mod;
			}

			if (treenodeposition2.nextOrThread() != null && treenodeposition1.nextOrThread() == null) {
				treenodeposition1.thread = treenodeposition2.nextOrThread();
				treenodeposition1.mod += f2 - f1;
			} else {
				if (treenodeposition.previousOrThread() != null && treenodeposition3.previousOrThread() == null) {
					treenodeposition3.thread = treenodeposition.previousOrThread();
					treenodeposition3.mod += f - f3;
				}

				node = this;
			}

			return node;
		}
	}

	private void moveSubtree(TreeNodePosition2<T> node, float shift) {
		float f = (float)(node.childIndex - this.childIndex);
		if (f != 0.0F) {
			node.change -= shift / f;
			this.change += shift / f;
		}

		node.shift += shift;
		node.y += shift;
		node.mod += shift;
	}

	private TreeNodePosition2<T> getAncestor(TreeNodePosition2<T> self, TreeNodePosition2<T> other) {
		return this.ancestor != null && self.parent.children.contains(this.ancestor) ? this.ancestor : other;
	}

	private void finalizePosition() {
		this.node.display().ifPresent(display -> display.setLocation((float)this.x, this.y));
		if (!this.children.isEmpty()) {
			for (TreeNodePosition2<T> treenodeposition : this.children) {
				treenodeposition.finalizePosition();
			}
		}
	}

	public static <T> void run(SkillNode<T> rootNode) {
		if (rootNode.display().isEmpty()) {
			throw new IllegalArgumentException("Can't position children of an invisible root!");
		} else {
			TreeNodePosition2<T> treenodeposition = new TreeNodePosition2<>(rootNode, null, null, 1, 0);
			treenodeposition.firstWalk();
			float f = treenodeposition.secondWalk(0.0F, 0, treenodeposition.y);
			if (f < 0.0F) {
				treenodeposition.thirdWalk(-f);
			}

			treenodeposition.finalizePosition();
		}
	}
}
