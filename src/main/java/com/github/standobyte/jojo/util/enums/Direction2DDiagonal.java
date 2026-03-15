package com.github.standobyte.jojo.util.enums;

import javax.annotation.Nullable;

public enum Direction2DDiagonal {
	UP,
	UP_RIGHT,
	RIGHT,
	DOWN_RIGHT,
	DOWN,
	DOWN_LEFT,
	LEFT,
	UP_LEFT;
	
	@Nullable
	public static Direction2DDiagonal fromDirs(boolean up, boolean down, boolean left, boolean right) {
		int b = (up == down ? 0 : up ? 1 : 2) | (left == right ? 0 : left ? 4 : 8);
		return switch (b) {
			case 1 -> UP;
			case 2 -> DOWN;
			case 4 -> LEFT;
			case 8 -> RIGHT;
			case 5 -> UP_LEFT;
			case 9 -> UP_RIGHT;
			case 6 -> DOWN_LEFT;
			case 10 -> DOWN_RIGHT;
			default /*0*/ -> null;
		};
	}
}
