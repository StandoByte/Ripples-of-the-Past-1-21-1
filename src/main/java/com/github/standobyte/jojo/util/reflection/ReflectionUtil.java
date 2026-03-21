package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.google.common.base.Preconditions;

public final class ReflectionUtil {
	static final Logger LOGGER = LogManager.getLogger();
	static final Marker REFLECTION = MarkerManager.getMarker("REFLECTION");

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T, E> T getFieldValue(Field field, E instance) {
		try {
			return (T) field.get(instance);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	
	public static <T, E> void setFieldValue(Field field, E instance, @Nullable final T value) {
		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T, E> T invokeMethod(Method method, E instance, Object... args) {
		try {
			return (T) method.invoke(instance, args);
		} catch (IllegalAccessException e) {
			LOGGER.error(REFLECTION, "Unable to access method {} on an object of type {}", method.getName(), instance.getClass().getName(), e);
			e.printStackTrace();
			throw new UnableToAccessMethodException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new ReflectivelyAccessedMethodException(e.getCause());
		}
	}
	
	// primitive equivalents for Field#get and Field#set

	public static <E> int getIntFieldValue(Field field, E instance) {
		try {
			return field.getInt(instance);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}

	public static <E> long getLongFieldValue(Field field, E instance) {
		try {
			return field.getLong(instance);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}

	public static <E> boolean getBooleanFieldValue(Field field, E instance) {
		try {
			return field.getBoolean(instance);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}

	public static <E> float getFloatFieldValue(Field field, E instance) {
		try {
			return field.getFloat(instance);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}

	public static <E> double getDoubleFieldValue(Field field, E instance) {
		try {
			return field.getDouble(instance);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}

	public static <E> byte getByteFieldValue(Field field, E instance) {
		try {
			return field.getByte(instance);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}

	public static <E> char getCharFieldValue(Field field, E instance) {
		try {
			return field.getChar(instance);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	

	
	public static <E> void setIntFieldValue(Field field, E instance, int value) {
		try {
			field.setInt(instance, value);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	
	public static <E> void setLongFieldValue(Field field, E instance, long value) {
		try {
			field.setLong(instance, value);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	
	public static <E> void setBooleanFieldValue(Field field, E instance, boolean value) {
		try {
			field.setBoolean(instance, value);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	
	public static <E> void setFloatFieldValue(Field field, E instance, float value) {
		try {
			field.setFloat(instance, value);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	
	public static <E> void setDoubleFieldValue(Field field, E instance, double value) {
		try {
			field.setDouble(instance, value);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	
	public static <E> void setByteFieldValue(Field field, E instance, byte value) {
		try {
			field.setByte(instance, value);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}
	
	public static <E> void setCharFieldValue(Field field, E instance, char value) {
		try {
			field.setChar(instance, value);
		} catch (IllegalAccessException e) {
			throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
		}
	}

	//

	public static <T> Field findField(final Class<? super T> clazz, final String fieldName) {
		Preconditions.checkNotNull(clazz, "Class to find field on cannot be null.");
		Preconditions.checkNotNull(fieldName, "Name of field to find cannot be null.");
		Preconditions.checkArgument(!fieldName.isEmpty(), "Name of field to find cannot be empty.");

		try {
			Field f = clazz.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f;
		} catch (Exception e) {
			throw new UnableToFindFieldException(e);
		}
	}

	//

	@SuppressWarnings("serial")
	public static class UnableToAccessFieldException extends RuntimeException {

		public static UnableToAccessFieldException onIllegalAccessException(IllegalAccessException e, Field field, Object instance) {
			LOGGER.error(REFLECTION, "Unable to access field {} on an object of type {}", field.getName(), instance.getClass().getName(), e);
			return new UnableToAccessFieldException(e);
		}
		
		public UnableToAccessFieldException(IllegalAccessException e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class UnableToFindFieldException extends RuntimeException {
		public UnableToFindFieldException(Exception e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class UnableToAccessMethodException extends RuntimeException {
		public UnableToAccessMethodException(IllegalAccessException e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class ReflectivelyAccessedMethodException extends RuntimeException {
		public ReflectivelyAccessedMethodException(Throwable e) {
			super(e);
		}
	}
	
	
	
	public static List<Field> getFieldsIncludingSuperclasses(Class<?> clazz, @Nullable Class<?> until) {
		List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null && superclass != until) {
			fields.addAll(getFieldsIncludingSuperclasses(superclass, until));
		}
		return fields;
	}
}
