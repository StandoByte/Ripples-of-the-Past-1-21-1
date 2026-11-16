package com.github.standobyte.jojo.client.ui.skill_tree;

import com.github.standobyte.jojo.util.objects_java.ObjectWrapper;

@Deprecated
public class SetIfNonNull<T> extends ObjectWrapper<T> {

	@Deprecated
	public SetIfNonNull() {
		super(null);
	}

	@Override
	public void set(T object) {
		if (object != null) super.set(object);
	}
}
