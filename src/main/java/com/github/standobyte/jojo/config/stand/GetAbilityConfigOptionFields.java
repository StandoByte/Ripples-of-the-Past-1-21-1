package com.github.standobyte.jojo.config.stand;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.Ability;

public class GetAbilityConfigOptionFields {
	private static final Map<Class<? extends Ability>, Field[]> FIELDS_CACHE = new IdentityHashMap<>();
	private static final Field[] NOT_FOUND_IN_CLASS = new Field[0];
	
	public static Map<String, ConfigOption<?>> get(Ability ability) {
		if (ability.__configOptionsCache == null) {
			Class<? extends Ability> abilityClass = ability.getClass();
			Field[] configOptionFields = getOrCreate(abilityClass);

			if (configOptionFields.length > 0) {
				ability.__configOptionsCache = HashMap.newHashMap(configOptionFields.length);
				for (Field configOptionField : configOptionFields) {
					ConfigOption<?> fieldValue;
					try {
						fieldValue = (ConfigOption<?>) configOptionField.get(ability);
						ability.__configOptionsCache.put(configOptionField.getName(), fieldValue);
					}
					catch (IllegalArgumentException | IllegalAccessException e) {
						JojoMod.getLogger().error("", e);
					}
				}
			}
			else {
				ability.__configOptionsCache = Collections.emptyMap();
			}
		}

		return ability.__configOptionsCache;
	}
	
	private static Field[] getOrCreate(Class<? extends Ability> abilityClass) {
		Field[] configOptionFields = FIELDS_CACHE.get(abilityClass);
		if (configOptionFields != null) {
			return configOptionFields;
		}
		
		List<Field> configFieldsFound = new ArrayList<>();
		
        Field[] declaredFields = abilityClass.getDeclaredFields();
        for (final Field field : declaredFields) {
        	if (field.getAnnotation(AbilityConfigField.class) != null && ConfigOption.class.isAssignableFrom(field.getType())) {
        		field.setAccessible(true);
        		configFieldsFound.add(field);
        	}
        }
        
        Class<?> superclass = abilityClass.getSuperclass();
        if (superclass != Ability.class) {
        	Field[] allSuperclassesFields = getOrCreate((Class<? extends Ability>) superclass);
        	Collections.addAll(configFieldsFound, allSuperclassesFields);
        }
        
        Field[] result = !configFieldsFound.isEmpty() ? configFieldsFound.toArray(Field[]::new) : NOT_FOUND_IN_CLASS;
        FIELDS_CACHE.put(abilityClass, result);
        return result;
	}
	
}
