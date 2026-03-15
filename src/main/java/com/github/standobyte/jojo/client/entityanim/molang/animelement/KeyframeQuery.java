package com.github.standobyte.jojo.client.entityanim.molang.animelement;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.molang.KeyframesMolangEngine;
import com.github.standobyte.jojo.config.MolangValue;
import com.google.gson.JsonArray;

import net.minecraft.client.animation.AnimationChannel.Interpolation;
import net.minecraft.client.animation.Keyframe;

public class KeyframeQuery {
	private Keyframe keyframe;
	private final Vector3f keyframeTarget;
	@Nullable private final MolangValue[] query;
	
	public static KeyframeQuery constant(Vector3f vec, float timestamp, Interpolation interpolation) {
		KeyframeQuery keyframe = new KeyframeQuery(vec, null);
		keyframe.setKeyframe(timestamp, interpolation);
		return keyframe;
	}
	
	protected KeyframeQuery(Vector3f keyframeTarget, @Nullable MolangValue[] query) {
		this.keyframeTarget = keyframeTarget;
		this.query = query;
	}
	
	public KeyframeQuery copy() {
		KeyframeQuery rotpKeyframe = new KeyframeQuery(new Vector3f(this.keyframeTarget), this.query);
		if (this.keyframe != null) {
			rotpKeyframe.keyframe = new Keyframe(keyframe.timestamp(), rotpKeyframe.keyframeTarget, keyframe.interpolation());
		}
		return rotpKeyframe;
	}
	
	protected KeyframeQuery setKeyframe(float timestamp, Interpolation interpolation) {
		keyframe = new Keyframe(timestamp, keyframeTarget, interpolation);
		return this;
	}
	
	
	public Keyframe getKeyframe() {
		return keyframe;
	}
	
	public boolean isNumericLiteral() {
		return query == null;
	}
	
	public void evaluate() {
		if (query != null) {
			keyframeTarget.set(query[0].getAsFloat(), query[1].getAsFloat(), query[2].getAsFloat());
		}
	}
	
	
	public static KeyframeQuery parseJsonVec(JsonArray vecJson, float timestamp, Interpolation interpolation) {
		boolean isNumericLiteral = true;
		MolangValue[] elements = new MolangValue[3];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = MolangValue.fromJson(vecJson.get(i), KeyframesMolangEngine.get());
			isNumericLiteral &= elements[i].isNumericLiteral();
		}
		KeyframeQuery keyframe;
		if (isNumericLiteral) {
			keyframe = new KeyframeQuery(new Vector3f(elements[0].getAsFloat(), elements[1].getAsFloat(), elements[2].getAsFloat()), null);
		}
		else {
			keyframe = new KeyframeQuery(new Vector3f(0, 0, 0), new MolangValue[] { elements[0], elements[1], elements[2] });
		}
		keyframe.setKeyframe(timestamp, interpolation);
		return keyframe;
	}
	
}
