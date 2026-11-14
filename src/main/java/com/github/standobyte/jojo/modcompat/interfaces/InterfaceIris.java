package com.github.standobyte.jojo.modcompat.interfaces;

public interface InterfaceIris {
	boolean isShaderPackInUse();
	
	public static InterfaceIris DUMMY = new InterfaceIris() {
		@Override public boolean isShaderPackInUse() { return false; }
	};
}
