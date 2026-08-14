package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.config.util.JsonConfigurable;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.functions.AttributeUtil;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class StandStats implements JsonConfigurable {
	private final DefaultedValue.Double power;
	private final DefaultedValue.Double speed;
	private final DefaultedValue.Double rangeEffective;
	private final DefaultedValue.Double rangeMax;
	private final DefaultedValue.Double durability;
	private final DefaultedValue.Double precision;
	
	public StandStats(double power, double speed, double rangeEffective, double rangeMax, double durability, double precision) {
		this.power = new DefaultedValue.Double(power);
		this.speed = new DefaultedValue.Double(speed);
		this.rangeEffective = new DefaultedValue.Double(rangeEffective);
		this.rangeMax = new DefaultedValue.Double(rangeMax);
		this.durability = new DefaultedValue.Double(durability);
		this.precision = new DefaultedValue.Double(precision);
	}

	public double getValue(StatWithValue field) {
		return getField(field).value;
	}

	protected DefaultedValue.Double getField(StatWithValue field) {
		return switch (field) {
			case POWER -> power;
			case SPEED -> speed;
			case RANGE_EFFECTIVE -> rangeEffective;
			case RANGE_MAX -> rangeMax;
			case DURABILITY -> durability;
			case PRECISION -> precision;
			default -> throw new IllegalArgumentException("Unexpected value: " + field);
		};
	}
	
	public double power() { return power.value; }
	public double speed() { return speed.value; }
	public double rangeEffective() { return rangeEffective.value; }
	public double rangeMax() { return rangeMax.value; }
	public double durability() { return durability.value; }
	public double precision() { return precision.value; }
	
	/*
	 * "A" - 14+
	 * "B" - 11-14
	 * "C" - 8-11
	 * "D" - 5-8
	 * "E" - 0-5
	 * "∅" - 0
	 */
	public static class Builder {
		private double power;
		private double speed;
		private double rangeEffective;
		private double rangeMax;
		private double durability;
		private double precision;
		
		public Builder power(double power) {
			this.power = power;
			return this;
		}
		
		public Builder speed(double speed) {
			this.speed = speed;
			return this;
		}
		
		public Builder range(double rangeEffective, double rangeMax) {
			this.rangeEffective = rangeEffective;
			this.rangeMax = rangeMax;
			return this;
		}
		
		public Builder durability(double durability) {
			this.durability = durability;
			return this;
		}
		
		public Builder precision(double precision) {
			this.precision = precision;
			return this;
		}
		
		public Builder fieldValue(StatWithValue field, double value) {
			switch (field) {
				case POWER -> power = value;
				case SPEED -> speed = value;
				case RANGE_EFFECTIVE -> rangeEffective = value;
				case RANGE_MAX -> rangeMax = value;
				case DURABILITY -> durability = value;
				case PRECISION -> precision = value;
				default -> throw new IllegalArgumentException("Unexpected value: " + field);
			};
			return this;
		}
		
		public StandStats build() {
			return new StandStats(power, speed, rangeEffective, rangeMax, durability, precision);
		}
	}
	
	public enum StatWithValue {
		POWER("power"),
		SPEED("speed"),
		RANGE_EFFECTIVE("rangeEffective"),
		RANGE_MAX("rangeMax"),
		DURABILITY("durability"),
		PRECISION("precision");
		
		private final String nameInJson;
		
		private StatWithValue(String nameInJson) {
			this.nameInJson = nameInJson;
		}
	}
	
	public enum CosmeticStat {
		POWER,
		SPEED,
		RANGE,
		DURABILITY,
		PRECISION,
		DEV_POTENTIAL
	}
	
	
	public StandStats.Builder defaultToBuilder() {
		return new StandStats.Builder()
				.power(power.defaultValue)
				.speed(speed.defaultValue)
				.range(rangeEffective.defaultValue, rangeMax.defaultValue)
				.durability(durability.defaultValue)
				.precision(precision.defaultValue);
	}
	
	public static StandStats fromJson(JsonObject json) {
		var builder = new StandStats.Builder();
		for (StatWithValue field : StatWithValue.values()) {
			Optional.ofNullable(json.getAsJsonPrimitive(field.nameInJson)).ifPresent(jsonValue -> {
				builder.fieldValue(field, jsonValue.getAsDouble());
			});
		}
		return builder.build();
	}
	
	@Override
	public JsonElement makeConfigTemplate() {
		JsonObject json = new JsonObject();
		for (StatWithValue field : StatWithValue.values()) {
			json.addProperty(field.nameInJson, getField(field).defaultValue);
		}
		return json;
	}
	
	@Override
	public void applyConfig(JsonElement json) {
		JsonObject config = json.getAsJsonObject();
		for (StatWithValue field : StatWithValue.values()) {
			Optional.ofNullable(config.getAsJsonPrimitive(field.nameInJson)).ifPresent(jsonValue -> {
				getField(field).value = jsonValue.getAsDouble();
			});
		}
	}
	
	@Override
	public void restoreDefaults() {
		for (StatWithValue field : StatWithValue.values()) {
			getField(field).reset();
		}
	}
	
	
	public static void updateStandStatAttributes(@Nonnull StandPower standPower, @Nonnull LivingEntity user) {
		if (user.level().isClientSide()) return;
		StandStats stats = standPower.getStandInstance().map(StandInstance::getStandType).map(StandType::getStandStats).orElse(null);
		AttributeMap attributes = user.getAttributes();
		AttributeUtil.setBaseValue(attributes, ModEntityAttributes.STAND_STRENGTH, stats != null ? stats.power() : 0);
		AttributeUtil.setBaseValue(attributes, ModEntityAttributes.STAND_SPEED, stats != null ? stats.speed() : 0);
		AttributeUtil.setBaseValue(attributes, ModEntityAttributes.STAND_EFFECTIVE_RANGE, stats != null ? stats.rangeEffective() : 0);
		AttributeUtil.setBaseValue(attributes, ModEntityAttributes.STAND_MAX_RANGE, stats != null ? stats.rangeMax() : 0);
		AttributeUtil.setBaseValue(attributes, ModEntityAttributes.STAND_DURABILITY, stats != null ? stats.durability() : 0);
		AttributeUtil.setBaseValue(attributes, ModEntityAttributes.STAND_PRECISION, stats != null ? stats.precision() : 0);
	}
	
}
