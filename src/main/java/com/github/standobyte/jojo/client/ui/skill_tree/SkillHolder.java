package com.github.standobyte.jojo.client.ui.skill_tree;

// net.minecraft.advancements.AdvancementHolder copypaste
public record SkillHolder<T>(String name, T value) {

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else {
			if (other instanceof SkillHolder otherHolder && this.name.equals(otherHolder.name)) {
				return true;
			}

			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name.toString();
	}
}
