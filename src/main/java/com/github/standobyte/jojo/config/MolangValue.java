package com.github.standobyte.jojo.config;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.MochaFunction;

public sealed interface MolangValue extends DoubleSupplier, IntSupplier, BooleanSupplier permits MolangValue.Literal, MolangValue.Molang {
	default float getAsFloat() { return (float) getAsDouble(); }
	default int getAsInt() { return (int) getAsDouble(); }
	default boolean getAsBoolean() { return getAsDouble() != 0; }
	boolean isNumericLiteral();
	
	public static MolangValue parse(String string, MochaEngine<?> molangEngine, Predicate<String> tryCompile) {
		try {
			double number = Double.parseDouble(string);
			return new MolangValue.Literal(number);
		}
		catch (NumberFormatException e1) {
			try {
				return new MolangValue.Molang(string, molangEngine, tryCompile != null ? tryCompile.test(string) : false);
			}
			catch (Exception e2) {
				return MolangValue.Literal.ZERO;
			}
		}
	}
	
	public static MolangValue fromJson(JsonElement json, MochaEngine<?> molangEngine) {
		if (!json.isJsonPrimitive()) {
			throw new IllegalArgumentException();
		}
		JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
		try {
			return new MolangValue.Literal(jsonPrimitive.getAsFloat());
		}
		catch (NumberFormatException e1) {
			String string = jsonPrimitive.getAsString();
			boolean tryCompile = !string.contains(AnimMolangQuery.NAMESPACE);
			try {
				return new MolangValue.Molang(string, molangEngine, tryCompile);
			}
			catch (Exception e2) {
				return MolangValue.Literal.ZERO;
			}
		}
	}
	
	public static final class Literal implements MolangValue {
		public static final Literal ZERO = new Literal(0);
		private final double value;
		
		public Literal(double value) {
			this.value = value;
		}
		
		@Override
		public double getAsDouble() {
			return value;
		}
		
		@Override
		public boolean isNumericLiteral() {
			return true;
		}
		
		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}
	
	public static final class Molang implements MolangValue {
		private final String expression;
		private MochaFunction function;
		
		public Molang(String molangExpr, MochaEngine<?> molangEngine, boolean tryCompile) {
			this.expression = molangExpr;
			this.function = makeFunction(molangExpr, molangEngine, tryCompile);
		}
		
		private static MochaFunction makeFunction(String molangExpr, MochaEngine<?> molangEngine, boolean tryCompile) {
//			if (tryCompile) {
//				try {
//					MochaFunction function = molangEngine.compile(molangExpr);
//					return function;
//				}
//				catch (Exception e) {
//					JojoMod.getLogger().error("Failed to compile a Molang expression ({}) into bytecode.", molangExpr, e);
//					throw e;
//				}
//			}
			MochaFunction function = molangEngine.prepareEval(molangExpr);
			return function;
		}
		
		@Override
		public double getAsDouble() {
			return function.evaluate();
		}
		
		@Override
		public boolean isNumericLiteral() {
			return false;
		}
		
		@Override
		public String toString() {
			return "Molang {" + expression + "}";
		}
	}
}
