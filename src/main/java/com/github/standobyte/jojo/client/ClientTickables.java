package com.github.standobyte.jojo.client;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ClientTickables {
	public static List<Tickable> tickables = new LinkedList<>();
	
	public static void add(Tickable tickable) {
		tickables.add(tickable);
	}
	
	public static void _tick() {
		Iterator<Tickable> iter = tickables.iterator();
		while (iter.hasNext()) {
			Tickable tickable = iter.next();
			if (!tickable.tick()) {
				iter.remove();
			}
		}
	}
	
	@FunctionalInterface
	public static interface Tickable {
		boolean tick();
	}
}
