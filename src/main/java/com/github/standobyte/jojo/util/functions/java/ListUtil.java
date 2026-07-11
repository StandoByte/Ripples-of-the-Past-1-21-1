package com.github.standobyte.jojo.util.functions.java;

import java.util.List;

import com.github.standobyte.jojo.util.OOPMoment;

public class ListUtil {

	public static <E> E getRandom(List<E> list) {
		if (list.isEmpty()) {
			throw new IndexOutOfBoundsException();
		}
		return list.get(OOPMoment.RANDOM.nextInt(list.size()));
	}
}
