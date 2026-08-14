package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.include.com.google.common.base.Objects;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.util.functions.StringUtil;
import com.github.standobyte.jojo.util.functions.StringUtil.StringWithNumber;
import com.mojang.serialization.Codec;

import net.minecraft.world.entity.HumanoidArm;

public class ActionAnimIdentifier {
	public final String name;
	public final int index;
	public boolean isIdle;
	
	public ActionAnimIdentifier(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public ActionAnimIdentifier(String name) {
		this(name, 0);
	}
	
	public static final Codec<ActionAnimIdentifier> NAME_CODEC = StringWithNumber.CODEC.xmap(
			ActionAnimIdentifier::getOrCreate, 
			animId -> StringUtil.StringWithNumber.splitIntAtTheEnd(animId.getOriginalAnimName()));

	private static final Map<ActionAnimIdentifier, ActionAnimIdentifier> ANIM_IDS = new HashMap<>();
	/**
	 * Automatically splits the number at the end of the animation name.
	 */
	public static ActionAnimIdentifier getOrCreate(String animName) {
		StringWithNumber enumeratedName = StringUtil.StringWithNumber.splitIntAtTheEnd(animName);
		return getOrCreate(enumeratedName);
	}
	
	public static ActionAnimIdentifier getOrCreate(StringWithNumber enumeratedName) {
		return getOrCreate(
				enumeratedName.str(), 
				enumeratedName.number().orElse(1) - 1 /* 1-based indexing in anims */);
	}
	
	public static ActionAnimIdentifier getOrCreate(String animName, int index) {
		ActionAnimIdentifier anim = new ActionAnimIdentifier(animName, index);
		ActionAnimIdentifier present = ANIM_IDS.get(anim);
		if (present != null) {
			return present;
		}
		else {
			ANIM_IDS.put(anim, anim);
			return anim;
		}
	}
	
	public static ActionAnimIdentifier getOrCreate(AbilityId abilityId) {
		return getOrCreate(abilityId.nameInMoveset());
	}
	
	public ActionAnimIdentifier setIdle() {
		this.isIdle = true;
		return this;
	}
	
	public ActionAnimIdentifier copyFrom(ActionAnimIdentifier source) {
		this.isIdle = source.isIdle;
		return this;
	}
	
	@Override
	public String toString() {
		String name = getOriginalAnimName();
		if (isIdle) {
			name += " (idle)";
		}
		return name;
	}
	
	public String getOriginalAnimName() {
		return index > 0 ? this.name + index : this.name;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ActionAnimIdentifier other
				&& this.name.equals(other.name)
				&& this.index == other.index;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name, index);
	}
	
	
	public static record ActionAnimIdHandsided(ActionAnimIdentifier left, ActionAnimIdentifier right) {
		
		public ActionAnimIdHandsided(ActionAnimIdentifier animId) {
			this(
					ActionAnimIdentifier.getOrCreate(animId.name + "_left", animId.index).copyFrom(animId),
					ActionAnimIdentifier.getOrCreate(animId.name + "_right", animId.index).copyFrom(animId));
		}
		
		public ActionAnimIdentifier get(HumanoidArm side) {
			return switch (side) {
				case LEFT -> left;
				case RIGHT -> right;
			};
		}
	}
	
	
	
	@Deprecated
	public ActionAnimIdentifier(String name, int index, boolean isIdle) {
		this(name, index);
		this.isIdle = isIdle;
	}

	@Deprecated
	public ActionAnimIdentifier(String name, boolean isIdle) {
		this(name);
		this.isIdle = isIdle;
	}
	
	@Deprecated public String name() { return name; }
	@Deprecated public int index() { return index; }
	@Deprecated public boolean isIdle() { return isIdle; }
	
	@Deprecated 
	public static ActionAnimIdentifier getOrCreate(String animName, boolean setIdle) {
		ActionAnimIdentifier anim = getOrCreate(animName);
		anim.isIdle = setIdle;
		return anim;
	}
	
	@Deprecated
	public static ActionAnimIdentifier getOrCreate(String animName, int index, boolean setIdle) {
		ActionAnimIdentifier anim = getOrCreate(animName, index);
		anim.isIdle = setIdle;
		return anim;
	}
	
}
