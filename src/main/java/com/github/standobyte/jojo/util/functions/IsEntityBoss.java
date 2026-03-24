package com.github.standobyte.jojo.util.functions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.util.reflection.ReflectionUtil;

import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class IsEntityBoss {

	private static Map<Class<? extends Entity>, Boolean> CHECKED_CLASSES = new HashMap<>();
	public static boolean check(Mob entity) {
		Class<? extends Entity> clazz = entity.getClass();
		Boolean cache = CHECKED_CLASSES.get(clazz);
		if (cache != null) {
			return cache;
		}
		
		boolean hasBossEvent = false;
		for (Field field : ReflectionUtil.getFieldsIncludingSuperclasses(clazz, Mob.class)) {
			if (BossEvent.class.isAssignableFrom(field.getType())) {
				hasBossEvent = true;
				break;
			}
		}
		CHECKED_CLASSES.put(clazz, hasBossEvent);
		return hasBossEvent;
	}
}
