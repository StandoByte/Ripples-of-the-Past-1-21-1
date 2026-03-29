package com.github.standobyte.jojo.util.objects_java;

import net.minecraft.util.Mth;

public class Lerp {

	public static class FloatValue {
		public float prevValue;
		public float value;
		boolean firstUpdate;
		
		public FloatValue() {
			this(0);
		}
		
		public FloatValue(float value) {
			this.prevValue = value;
			this.value = value;
		}

		public void lerpTick() {
			this.prevValue = this.value;
		}

		public float lerp(float partialTick) {
			return partialTick == 1 ? value : Mth.lerp(partialTick, prevValue, value);
		}
		
		/**
		 * @param lerp false if lerpTick() is called from an outside ticking method, true if it's not
		 */
		public boolean set(float value, boolean lerp) {
			if (lerp) {
				this.prevValue = this.value;
			}
			if (this.value != value) {
				this.value = value;
				if (this.firstUpdate) {
					this.firstUpdate = false;
					this.prevValue = this.value;
				}
				return true;
			}
			return false;
		}

		public float get() {
			return value;
		}
	}

	public static class DoubleValue {
		public double prevValue;
		public double value;
		boolean firstUpdate;
		
		public DoubleValue() {
			this(0);
		}
		
		public DoubleValue(double value) {
			this.prevValue = value;
			this.value = value;
		}

		public void preTick() {
			this.prevValue = this.value;
		}

		public double lerp(float partialTick) {
			return partialTick == 1 ? value : Mth.lerp(partialTick, prevValue, value);
		}

		/**
		 * @param lerp false if lerpTick() is called from an outside ticking method, true if it's not
		 */
		public boolean set(double value, boolean lerp) {
			if (this.value != value) {
				if (lerp) {
					this.prevValue = this.value;
				}
				this.value = value;
				if (this.firstUpdate) {
					this.firstUpdate = false;
					this.prevValue = this.value;
				}
				return true;
			}
			return false;
		}

		public double get() {
			return value;
		}
	}

}
