package com.github.standobyte.jojo.config.client;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.text.StandSkinComponent;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphInfo;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphsCache;
import com.github.standobyte.jojo.client.ui.screen_widgets.ButtonWithImage;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList.EntryWithButtons;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList.Renderable2;
import com.github.standobyte.jojo.client.ui.utils.Alignment;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.OnButtonClick;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MultiLineScreenTooltip;
import com.github.standobyte.jojo.config.BoolOrPlayerPref;
import com.github.standobyte.jojo.config.ModConfigInterface;
import com.github.standobyte.jojo.config.client.NumericField.ScrollingDecimal;
import com.github.standobyte.jojo.config.client.stand.StandNameForScrolleableList;
import com.github.standobyte.jojo.config.client.stand.StandStatsHexagonForScrolleableList;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.config.core.types.ConfigDouble;
import com.github.standobyte.jojo.config.core.types.ConfigEnum;
import com.github.standobyte.jojo.config.internal.ConfigEventHandler;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.config.stand.StandCommonConfig;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandStats.StatWithValue;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.functions.EnumUtil;
import com.mojang.blaze3d.platform.InputConstants;

import it.unimi.dsi.fastutil.objects.Object2CharArrayMap;
import it.unimi.dsi.fastutil.objects.Object2CharMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.KeyModifier;

/**
 * Some static methods to add config buttons with less boilerplate
 */
public class ConfigGuiHelper {
	public ClientModSettingsScreen screen;
	public String modId;
	public ModConfigInterface<?, ?, ?> config;
	public ScrolleableButtonList list;
	public EntryWithButtons curEntry;
	
	public ConfigGuiHelper(ClientModSettingsScreen screen, String modId) {
		this.screen = screen;
		this.modId = modId;
	}

	
	public ScrolleableButtonList addModSettingsList(ModConfigInterface<?, ?, ?> config) {
		this.config = config;
		this.list = new ScrolleableButtonList(screen.getMinecraft(), 
				30, 33, 
				screen.width - 30, screen.height - 66, 
				25);
		screen.addConfigWidget(list);
		return list;
	}
	
	public static ModConfig<?, ?, ?> getStandConfig(@Nullable StandType standType) {
		return standType != null ? ConfigEventHandler.ALL_CONFIGS.getStandConfig(standType.getId()) : null;
	}
	
	public void generateStandConfigUI(String modId, @Nullable StandType standType, @Nullable ModConfig<?, ?, ?> standConfig) {
		this.config = standConfig;
		addStandsList(modId);
		if (standType != null) {
			StandCommonConfig standCommonConfig = null;
			if (standConfig != null && standConfig.commonConfig instanceof StandCommonConfig _standConfig) {
				standCommonConfig = _standConfig;
			}
			if (standCommonConfig != null) {
				addStandSettingsList(standType);
				addStandAbilityConfigSettings(standCommonConfig);
			}
		}
	}
	
	public void addStandsList(String modId) {
		Minecraft mc = Minecraft.getInstance();
		
		ScrolleableButtonList standsList = new ScrolleableButtonList(mc, screen.width - 50, 33, 50, screen.height - 66, 18);
		StandSkinsLoader standSkins = StandSkinsLoader.getInstance();
		for (var standEntry : ConfigEventHandler.ALL_CONFIGS.getAllConfigsFromMod(modId).standConfigs.entrySet()) {
			ResourceLocation standId = standEntry.getKey();
			StandSkin standSkin = standSkins.getDefaultSkin(standId);
			StandType standType = JojoRegistries.DEFAULT_STANDS_REG.get(standId);
			if (standType != null) {
				ButtonWithImage button = new ClientModSettingsScreen.StandSelectionButton(screen, standType, standSkin);
				standsList.addEntry(new EntryWithButtons().add(button, 20, 0));
			}
		}
		screen.addConfigWidget(standsList);
	}
	
	public static final ScrollingDecimal STAT_SCROLLING = (boolean scrollUp) -> {
		KeyModifier curModifier = 
				KeyModifier.CONTROL.isActive(null) ? KeyModifier.CONTROL : 
				KeyModifier.SHIFT.isActive(null) ? KeyModifier.SHIFT : 
				KeyModifier.NONE;
		return switch (curModifier) {
			case CONTROL -> scrollUp ? "0.1" : "-0.1";
			case SHIFT -> scrollUp ? "3.0" : "-3.0";
			default -> scrollUp ? "1" : "-1";
		};
	};
	
	public ScrolleableButtonList addStandSettingsList(StandType standType) {
		Minecraft mc = Minecraft.getInstance();
		
		list = new ScrolleableButtonList(screen.getMinecraft(), 
				30, 33, 
				screen.width - 80, screen.height - 66, 
				25);
		StandStats stats = standType.getStandStats();
		
		EntryWithButtons standTitleAndStuff = new EntryWithButtons();
		standTitleAndStuff.addRenderable(new ConfigGuiHelper.TextLine(
				Component.translatable("jojo_ripples.stand_config.stand_stats"), 
				Alignment.CENTER, ConfigGuiHelper.TextLine.GRAY), list.getWidth() / 2 + 10, 0);
		StandNameForScrolleableList standName = StandNameForScrolleableList.fromDefaultSkin(
				standType, Alignment.LEFT);
		standTitleAndStuff.addRenderable(standName, 
				Math.max(list.getWidth() / 2 - mc.font.width(standName.line) - 100, 2), 0);
		list.addEntry(standTitleAndStuff);
		list.addAlwaysRenderedObject(new StandStatsHexagonForScrolleableList(standType, stats), 
				Math.min(-1 + list.getWidth() / 2, list.getWidth() - 154), 7);
		
		for (StatWithValue stat : StatWithValue.values()) {
			Component statName = stat.name;
			ConfigDouble statField = stats.getField(stat);
			NumericField statNumField = new NumericField(67, 20, statName);
			statNumField.tieToNumericConfigOption(config, ModConfigType.COMMON, statField, stat.nameInJson, statName);
			statNumField.addScrolling(STAT_SCROLLING);

			MultiLineScreenTooltip tooltip = new MultiLineScreenTooltip(CommonComponents.EMPTY, stat.description);
			list.addEntry(new EntryWithButtons()
					.add(statNumField, list.getWidth() / 2 - 73, 0)
					.addRenderable(new ConfigGuiHelper.TextLine(statName, Alignment.RIGHT, ConfigGuiHelper.TextLine.WHITE) {
						@Override
						protected void renderTooltip(Screen curScreen) {
							Component message = Component.translatable("options.generic_value", statName, statNumField.getValue());
							tooltip.setTitle(message.copy().withStyle(ChatFormatting.BOLD));
							curScreen.setTooltipForNextRenderPass(tooltip, DefaultTooltipPositioner.INSTANCE, true);
						}
					}, -78 + list.getWidth() / 2, 6));
		}
		
		screen.addConfigWidget(list);
		return list;
	}
	
	public void addStandAbilityConfigSettings(StandCommonConfig standConfig) {
		for (var abilityEntry : standConfig.abilityConfigs.entrySet()) {
			String abilityName = abilityEntry.getKey();

			list.addEntry(new EntryWithButtons().addRenderable(new TextLine(
					StandSkinComponent.translatable(standConfig.standId, Optional.empty(), Ability.tlKey(abilityName)),
					Alignment.CENTER, TextLine.GRAY), 
					list.getWidth() / 2 + 10, 12));
			curEntry = null;
			
			for (var abilityConfigEntry : abilityEntry.getValue().entrySet()) {
				ConfigOption<?> field = abilityConfigEntry.getValue();
				switch (field) {
					case ConfigBool fieldBool -> {
						addBooleanOptionButton(fieldBool, ModConfigType.COMMON, null);
					}
					case ConfigEnum<?> fieldEnum -> {
						addEnumOptionButton(fieldEnum, ModConfigType.COMMON, null);
					}
					default -> {}
				}
			}
		}
	}
	
	
	public void addBooleanOptionButton(ConfigBool cfgOption, 
			ModConfigType type, @Nullable String icon) {
		addOptionButton(cfgOption, type, icon, 
				option -> {
					option.set(!option.get());
				},
				cfgOption::makeButtonMessage);
	}
	
	public void addBooleanOrPlayerPreferenceOptionButton(ConfigEnum<BoolOrPlayerPref> cfgOption, 
			ModConfigType type, @Nullable String icon) {
		addOptionButton(cfgOption, type, icon, 
				option -> {
					option.set(EnumUtil.getNextWrapped(option.get(), cfgOption.enumConstants));
				}, 
				(Component name, BoolOrPlayerPref value) -> value.optionStatus(name));
	}

	public <T extends Enum<T>> void addEnumOptionButton(ConfigEnum<T> cfgOption, 
			ModConfigType type, @Nullable String icon) {
		addOptionButton(cfgOption, type, icon, 
				option -> {
					option.set(EnumUtil.getNextWrapped(option.get(), cfgOption.enumConstants));
				}, 
				(Component name, T value) -> Component.translatable("options.generic_value", name, 
						Component.translatable(getPrefix(modId, type)
								+ "." + cfgOption.getFieldName()
								+ ".option." + value.name().toLowerCase())));
	}
	
	public <T> void addOptionButton(ConfigOption<T> option, 
			ModConfigType type, @Nullable String icon, 
			Consumer<ConfigOption<T>> changeValueOnClick, 
			BiFunction<Component, T, MutableComponent> getValueMessage) {
		Component name = makeName(option, modId, type);
		Component tooltipBody = makeTooltipBody(option, modId, type);
		Component nameWithSprite = icon != null ? prependIcon(name, toIconPath(icon)) : name;
		
		OnButtonClick onPress = (b, mouseButton) -> {
			switch (mouseButton) {
				case InputConstants.MOUSE_BUTTON_LEFT -> {
					changeValueOnClick.accept(option);
					option.onChanged(Minecraft.getInstance().player);
				}
				case InputConstants.MOUSE_BUTTON_RIGHT -> {
					option.resetWithCallback(Minecraft.getInstance().player);
				}
			}
		};
		
		ConfigButton button = new ConfigButton(-1, -1, -1, -1, 
				name, nameWithSprite, 
				onPress, tooltipBody, 
				type, option, (BiFunction<Component, Object, MutableComponent>) getValueMessage);
		addButton(button);
	}
	
	public void addButton(ScrollingStringButton button) {
		if (curEntry == null) {
			curEntry = new EntryWithButtons();
			list.addEntry(curEntry);
		}
		
		int buttonWidth = Math.min((screen.width - 80) / 2, 150);
		int x = switch (curEntry.children().size()) {
			case 0 -> screen.width / 2 - 35 - buttonWidth;
			case 1 -> screen.width / 2 - 25;
			default -> throw new IllegalStateException();
		};
		
		button.setWidth(buttonWidth);
		button.setHeight(20);
		
		if (curEntry.children().size() == 0) {
			button.setAlignment(Alignment.RIGHT);
		}
		
		curEntry.add(button, x, 2);
		if (curEntry.children().size() >= 2) {
			curEntry = null;
		}
	}
	
	public static Component makeName(ConfigOption<?> option, String modId, ModConfigType type) {
		return Component.translatable(getPrefix(modId, type) +  "." + option.getFieldName());
	}
	
	public static Component makeTooltipBody(ConfigOption<?> option, String modId, ModConfigType type) {
		return Component.translatable(getPrefix(modId, type) + "." + option.getFieldName() + ".tooltip");
	}
	
	
	public <T extends Number> void addNumericField(ConfigOption<T> option,
			ModConfigType type, @Nullable String icon) {
		curEntry = new EntryWithButtons();
		list.addEntry(curEntry);
		
		Component name = Component.translatable(getPrefix(modId, type) +  "." + option.getFieldName());
		Component tooltipBody = Component.translatable(getPrefix(modId, type) + "." + option.getFieldName() + ".tooltip");
		Component nameWithSprite = icon != null ? prependIcon(name, toIconPath(icon)) : name;
		
		NumericField field = new NumericField(70, 20, nameWithSprite);
		field.tieToNumericConfigOption(config, ModConfigType.COMMON, option, option.getFieldName(), name);

		int buttonsWidth = Math.min((screen.width - 80) / 2, 150);
		int leftButtonsX0 = screen.width / 2 - 35 - buttonsWidth;
		int rightButtonsX0 = screen.width / 2 - 25;
		int offsetFromRightButtonToRightEdge = screen.width - rightButtonsX0 + buttonsWidth;
		
		MultiLineScreenTooltip tooltip = new MultiLineScreenTooltip(CommonComponents.EMPTY, tooltipBody);
		ConfigGuiHelper.TextLine nameRenderable = new ConfigGuiHelper.TextLine(nameWithSprite, Alignment.LEFT, ConfigGuiHelper.TextLine.WHITE) {
			
			@Override
			protected void renderTooltip(Screen curScreen) {
				Component message = Component.translatable("options.generic_value", nameWithSprite, field.getValue());
				tooltip.setTitle(message.copy().withStyle(ChatFormatting.BOLD));
				curScreen.setTooltipForNextRenderPass(tooltip, DefaultTooltipPositioner.INSTANCE, true);
			}
		};
		curEntry.addRenderable(nameRenderable, leftButtonsX0, 6);
		curEntry.add(field, offsetFromRightButtonToRightEdge - field.getWidth() - 50, 0);
		
		curEntry = null;
	}
	
	
	public static MutableComponent prependIcon(Component text, ResourceLocation iconPath) {
		return prependIcon(text, new GuiIcon(iconPath, 16, 16));
	}
	
	public static MutableComponent prependIcon(Component text, GuiIcon icon) {
		IconGlyphInfo spriteGlyph = new IconGlyphInfo(icon, 16, 16, 0, -4, 4);
		//stand aim marker: new GuiIcon(iconPath, 17, 17), 17, 17, 0, -5, 5)
		
		char spriteCode = iconSymbols.computeIfAbsent(icon.file, 
				__ -> IconGlyphsCache.makeCharCodeFor(spriteGlyph));
		return Component.literal(String.valueOf(spriteCode)).append(text);
	}
	
	public static class ConfigButton extends ScrollingStringButton {
		protected OnButtonClick onPress2;
		protected ModConfigType configType;
		protected ConfigOption<?> configOption;
		protected Component optionNameNoSprite;
		protected Component optionNameWithSprite;
		protected BiFunction<Component, Object, MutableComponent> getValueMessage;
		protected Object prevValue;
		protected MultiLineScreenTooltip tooltip;

		public ConfigButton(int pX, int pY, int pWidth, int pHeight, 
				Component optionNameNoSprite, Component optionNameWithSprite, 
				OnButtonClick pOnPress, Component tooltipBody,
				ModConfigType type, ConfigOption<?> configOption, BiFunction<Component, Object, MutableComponent> getValueMessage) {
			super(pX, pY, pWidth, pHeight, CommonComponents.EMPTY, pOnPress, null);
			this.onPress2 = pOnPress;
			this.configType = type;
			this.configOption = configOption;
			this.optionNameNoSprite = optionNameNoSprite;
			this.optionNameWithSprite = optionNameWithSprite;
			this.getValueMessage = getValueMessage;
			
			this.tooltip = new MultiLineScreenTooltip(CommonComponents.EMPTY, tooltipBody);
			updateMessage(configOption.get());
			setTooltip(tooltip);
		}


		@Override
	    protected boolean isValidClickButton(int button) {
			return true;
	    }
		
		@Override
		public void onClick(double mouseX, double mouseY, int button) {
			onPress2.onPress(this, button);
		}
		
		
		protected void updateMessage(Object newValue) {
			Component message = getValueMessage.apply(optionNameWithSprite, newValue);
			this.setMessage(message);
			this.tooltip.setTitle(message.copy().withStyle(ChatFormatting.BOLD));
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			if (configType == ModConfigType.COMMON) {
				this.active = clientCanEditCommonConfig();
			}
			
			Object newValue = configOption.get();
			if (prevValue != null && !prevValue.equals(newValue)) {
				updateMessage(newValue);
			}
			this.prevValue = newValue;
			
	    	super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
	    }
		
	}
	
	public static String getPrefix(String modId, ModConfigType type) {
		String prefix = modId + ".config";
		if (type == ModConfigType.CLIENT || type == ModConfigType.CLIENT_BROADCAST) {
			prefix += ".client";
		}
		return prefix;
	}
	
	
	public void addCategoryTitle(Component title) {
		list.addEntry(new EntryWithButtons().addRenderable(new TextLine(title, 
				Alignment.CENTER, TextLine.GRAY), 
				-15 + list.getWidth() / 2, 12));
		curEntry = null;
	}

	public static class TextLine extends Renderable2 {
		public static final int GRAY = 0xFFC0C0C0;
		public static final int WHITE = 0xFFFFFF;
		public Component line;
		public Alignment alignment;
		public int color;
		
		public TextLine(Component line, Alignment alignment, int color) {
			super(0, 0);
			this.line = line;
			this.alignment = alignment;
			this.color = color;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			Minecraft mc = Minecraft.getInstance();
			Font font = mc.font;
			int width = font.width(line);
			int x0 = switch (alignment) {
				case LEFT -> x;
				case RIGHT -> x - width;
				case CENTER -> x - width / 2;
			};
			
			guiGraphics.drawString(font, line, x0, y, color);
			
			if (mouseX >= x0 && mouseX <= x0 + width && mouseY >= y && mouseY <= y + font.lineHeight) {
				Screen screen = Minecraft.getInstance().screen;
				if (screen != null) {
					renderTooltip(screen);
				}
			}
		}
		
		protected void renderTooltip(Screen curScreen) {}

	}
	

	
	public static boolean clientCanEditCommonConfig() {
		return !ConfigNetworkFunctions.clientIsConnectedToAServer() || ConfigNetworkFunctions.clientHasPermissions();
	}

	public static ResourceLocation toIconPath(String fileName) {
		return JojoMod.resLoc("textures/gui/sprites/settings/" + fileName + ".png");
	}

	public static Object2CharMap<ResourceLocation> iconSymbols = new Object2CharArrayMap<>();
}
