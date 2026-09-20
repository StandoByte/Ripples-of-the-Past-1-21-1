package com.github.standobyte.jojo.config.client;

import java.math.BigDecimal;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.spongepowered.include.com.google.common.base.Objects;

import com.github.standobyte.jojo.config.ModConfigInterface;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.config.core.types.ConfigDouble;
import com.github.standobyte.jojo.config.core.types.ConfigFloat;
import com.github.standobyte.jojo.config.core.types.ConfigInt;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class NumericField extends EditBox {
	protected ModConfigInterface<?, ?, ?> modConfig;
	protected ModConfigType configType;
	protected ConfigOption<?> configOption;
	protected Function<String, ?> valueParser;
	@Nullable protected Either<ScrollingDecimal, ScrollingInteger> scrollDelta;

	public NumericField(int width, int height, Component nameForNarration) {
		super(Minecraft.getInstance().font, width, height, nameForNarration);
	}

	public NumericField(int x, int y, int width, int height, Component nameForNarration) {
		super(Minecraft.getInstance().font, x, y, width, height, nameForNarration);
	}
	

	@Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (configType == ModConfigType.COMMON) {
			this.active = ConfigGuiHelper.clientCanEditCommonConfig();
			this.setEditable(this.active);
		}
    	if (configOption != null) {
    		checkConfigFieldInCaseOfOutsideEdit(configOption);
    	}
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }
	
	protected Object _prevRecordedConfigValue;
	protected void checkConfigFieldInCaseOfOutsideEdit(ConfigOption<?> configOption) {
		Object value = configOption.get();
		if (!Objects.equal(value, _prevRecordedConfigValue) && value != null) {
			this.setValue(value.toString());
		}
		this._prevRecordedConfigValue = value;
	}
	
	
	
	/** There's some of copypaste from Neo's 
	 * {@link net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen}
	 * (Copyright (c) NeoForged and contributors)
	 */
	public <T> void tieToNumericConfigOption(ModConfigInterface<?, ?, ?> modConfig, ModConfigType configType, 
			ConfigOption<T> configOption, String fieldName, Component fieldNameTranslated) {
		this.modConfig = modConfig;
		this.configType = configType;
		this.configOption = configOption;
		
		Function<String, T> parser;
		boolean allowNegative;
		
		switch (configOption) {
			case ConfigInt configInt -> {
				parser = val -> (T) (val.isEmpty() ? Integer.valueOf(0) : Integer.valueOf(val));
				allowNegative = configInt.minValue < 0;
			}
			case ConfigFloat configFloat -> {
				parser = val -> (T) (val.isEmpty() ? Float.valueOf(0f) : Float.valueOf(val));
				allowNegative = configFloat.minValue < 0;
			}
			case ConfigDouble configDouble -> {
				parser = val -> (T) (val.isEmpty() ? Double.valueOf(0.0) : Double.valueOf(val));
				allowNegative = configDouble.minValue < 0;
			}
			default -> throw new IllegalArgumentException(fieldName + " is not a numeric config option");
		}
		
		this.valueParser = parser;
		
		this.setFilter(newValueString -> {
			try {
				parser.apply(newValueString);
				return true;
			} catch (final NumberFormatException e) {
				return isPartialNumber(newValueString, allowNegative);
			}
		});
		
		this.setResponder(newValueString -> {
			int color = 0xFFFF0000;
			Tooltip tooltip = null;
			
			try {
				T newValue = parser.apply(newValueString);
				if (newValue != null) {
					ConditionCheck validation = configOption.isValueValid(newValue, fieldNameTranslated);
					if (validation.positive()) {
						if (!Objects.equal(configOption.get(), newValue)) {
							this._prevRecordedConfigValue = newValue;
							configOption.set(newValue);
							// FIXME the decimals will probably get screwed by floating point error anyway because the value is still sent to the server as a double number... fuck...
							ConfigGuiHelper.applyChange(modConfig, configType, configOption);
						}
						color = EditBox.DEFAULT_TEXT_COLOR;
					}
					else {
						Component negativeMessage = validation.warning();
						if (negativeMessage != null) {
							tooltip = Tooltip.create(negativeMessage);
						}
					}
				}
			} catch (final NumberFormatException e) {
				// field probably is just empty/partial, ignore that
			}
			
			this.setTextColor(color);
			this.setTooltip(tooltip);
		});
		
		this.setValue(String.valueOf(configOption.get()));
	}

	public static boolean isPartialNumber(String value, boolean allowNegative) {
		return switch (value) {
			case "" -> true;
			case "0" -> true; // ?
			case "0x" -> true;
			case "0X" -> true;
			case "#" -> true;
			case "-" -> allowNegative;
			case "-0" -> allowNegative; // ?
			case "-0x" -> allowNegative;
			case "-0X" -> allowNegative;
			default -> false;
		};
	}


	@FunctionalInterface
	public static interface ScrollingDecimal { String getExactDelta(boolean scrollUp); }
	public static final ScrollingDecimal BASIC_DECIMAL = (boolean scrollUp) -> scrollUp ? "1" : "-1";
	public void addScrolling(ScrollingDecimal getDelta) {
		this.scrollDelta = Either.left(getDelta);
	}
	
	@FunctionalInterface
	public static interface ScrollingInteger { int getDelta(boolean scrollUp); }
	public static final ScrollingInteger BASIC_INTEGER = (boolean scrollUp) -> scrollUp ? 1 : -1;
	public void addScrolling(ScrollingInteger getDelta) {
		this.scrollDelta = Either.right(getDelta);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.active && this.scrollDelta != null) {
			boolean scrolledNumericValue = scrollDelta.map(
					getDeltaDecimal -> {
						try {
							BigDecimal number = new BigDecimal(this.getValue());
							String deltaExact = getDeltaDecimal.getExactDelta(scrollY > 0);
							BigDecimal result = number.add(new BigDecimal(deltaExact));
							
							double resultDoubleValue = result.doubleValue();
							double clamped = (double) sanitize(configOption, resultDoubleValue);
							String resultString = clamped != resultDoubleValue ? String.valueOf(clamped) : result.toString();
							
							setValue(resultString);
							return true;
						}
						catch (NumberFormatException e) {
							return false;
						}
					}, 
					getDeltaInteger -> {
						try {
							int number = Integer.parseInt(this.getValue());
							int delta = getDeltaInteger.getDelta(scrollY > 0);
							int result = number + delta;
							
							int clamped = (int) sanitize(configOption, result);
							String resultString = clamped != result ? String.valueOf(clamped) : String.valueOf(result);
							
							setValue(resultString);
							return true;
						}
						catch (NumberFormatException e) {
							return false;
						}
					});
			
			if (scrolledNumericValue) return true;
		}
		
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	
	private static <T> T sanitize(ConfigOption<T> configOption, Number number) {
		T cast = JSONUtil.fromJson(new JsonPrimitive(number), configOption.jsonCodec).get();
		return configOption.sanitize(cast);
	}

}
