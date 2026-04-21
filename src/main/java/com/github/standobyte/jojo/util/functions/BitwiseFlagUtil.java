package com.github.standobyte.jojo.util.functions;

public class BitwiseFlagUtil {
	
	// Overloaded for all integer primitive types

	public static byte set(byte stored, int ordinal, boolean value) {
		if (ordinal < 0) throw new IllegalArgumentException();
		if (ordinal >= 8) throw new IllegalArgumentException("Use short, int or long to store the flags");
		byte bit = (byte) (1 << ordinal);
		if (value)	stored |= bit;
		else		stored &= ~bit;
		return stored;
	}
	
	public static boolean get(byte stored, int ordinal) {
		if (ordinal < 0) throw new IllegalArgumentException();
		if (ordinal >= 8) throw new IllegalArgumentException("Use short, int or long to store the flags");
		byte bit = (byte) (1 << ordinal);
		return (stored & bit) != 0;
	}
	
	
	public static short set(short stored, int ordinal, boolean value) {
		if (ordinal < 0) throw new IllegalArgumentException();
		if (ordinal >= 16) throw new IllegalArgumentException("Use int or long to store the flags");
		short bit = (short) (1 << ordinal);
		if (value)	stored |= bit;
		else		stored &= ~bit;
		return stored;
	}
	
	public static boolean get(short stored, int ordinal) {
		if (ordinal < 0) throw new IllegalArgumentException();
		if (ordinal >= 16) throw new IllegalArgumentException("Use int or long to store the flags");
		short bit = (short) (1 << ordinal);
		return (stored & bit) != 0;
	}
	
	
	public static int set(int stored, int ordinal, boolean value) {
		if (ordinal < 0) throw new IllegalArgumentException();
		if (ordinal >= 32) throw new IllegalArgumentException("Use long to store the flags");
		int bit = 1 << ordinal;
		if (value)	stored |= bit;
		else		stored &= ~bit;
		return stored;
	}
	
	public static boolean get(int stored, int ordinal) {
		if (ordinal < 0) throw new IllegalArgumentException();
		if (ordinal >= 32) throw new IllegalArgumentException("Use long to store the flags");
		int bit = 1 << ordinal;
		return (stored & bit) != 0;
	}
	
	
	public static long set(long stored, int ordinal, boolean value) {
		if (ordinal < 0) throw new IllegalArgumentException();
		if (ordinal >= 64) throw new IllegalArgumentException("> 64 flags can't fit into long, you're cooked");
		long bit = 1L << ordinal;
		if (value)	stored |= bit;
		else		stored &= ~bit;
		return stored;
	}
	
	public static boolean get(long stored, int ordinal) {
		if (ordinal < 0) throw new IllegalArgumentException();
		if (ordinal >= 64) throw new IllegalArgumentException("> 64 flags can't fit into long, you're cooked");
		long bit = 1L << ordinal;
		return (stored & bit) != 0;
	}

	
	// Enum version
	
	public static byte set(byte stored, Enum<?> enumConstant, boolean value) {
		return set(stored, enumConstant.ordinal(), value);
	}
	
	public static boolean get(byte stored, Enum<?> enumConstant) {
		return get(stored, enumConstant.ordinal());
	}
	
	
	public static short set(short stored, Enum<?> enumConstant, boolean value) {
		return set(stored, enumConstant.ordinal(), value);
	}
	
	public static boolean get(short stored, Enum<?> enumConstant) {
		return get(stored, enumConstant.ordinal());
	}
	
	
	public static int set(int stored, Enum<?> enumConstant, boolean value) {
		return set(stored, enumConstant.ordinal(), value);
	}
	
	public static boolean get(int stored, Enum<?> enumConstant) {
		return get(stored, enumConstant.ordinal());
	}
	
	
	public static long set(long stored, Enum<?> enumConstant, boolean value) {
		return set(stored, enumConstant.ordinal(), value);
	}
	
	public static boolean get(long stored, Enum<?> enumConstant) {
		return get(stored, enumConstant.ordinal());
	}
}
