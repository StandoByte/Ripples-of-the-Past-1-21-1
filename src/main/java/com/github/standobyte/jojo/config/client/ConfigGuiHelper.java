package com.github.standobyte.jojo.config.client;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphInfo;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphsCache;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList.EntryWithButtons;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList.Renderable2;
import com.github.standobyte.jojo.client.ui.utils.Alignment;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.config.ModConfigInterface;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.config.core.types.ConfigEnum;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.core.JojoMod;

import it.unimi.dsi.fastutil.objects.Object2CharArrayMap;
import it.unimi.dsi.fastutil.objects.Object2CharMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Some static methods to add config buttons with less boilerplate
 */
public class ConfigGuiHelper {
	public ClientModSettingsScreen screen;
	public String modId;
	public ModConfigInterface<?, ?, ?> config;
	public ScrolleableButtonList list;
	public EntryWithButtons curEntry;
	
	public ConfigGuiHelper(ClientModSettingsScreen screen, String modId, ModConfigInterface<?, ?, ?> config) {
		list = new ScrolleableButtonList(screen.getMinecraft(), 
				30, 33, 
				screen.width - 30, screen.height - 66, 
				25);
		screen.addConfigWidget(list);
		this.screen = screen;
		this.modId = modId;
		this.config = config;
	}
	
	public void addBooleanOptionButton(ConfigBool cfgOption, 
			ModConfigType type, @Nullable String icon) {
		addOptionButton(cfgOption, type, icon, 
				option -> {
					option.set(!option.get());
				},
				(Component name, Boolean value) -> CommonComponents.optionStatus(name, value));
	}
	
	public <T extends Enum<T>> void addEnumOptionButton(ConfigEnum<T> cfgOption, 
			ModConfigType type, @Nullable String icon, 
			Class<T> enumClass) {
		addOptionButton(cfgOption, type, icon, 
				option -> {
					T[] values = enumClass.getEnumConstants();
					T val = option.get();
					T nextVal = values[(val.ordinal() + 1) % values.length];
					option.set(nextVal);
				}, 
				(Component name, T value) -> Component.translatable("options.generic_value", name, 
						Component.translatable(getPrefix(modId, type) + ".option." + value.name().toLowerCase())));
	}
	
	public <T> void addOptionButton(ConfigOption<T> option, 
			ModConfigType type, @Nullable String icon, 
			Consumer<ConfigOption<T>> changeValueOnClick, 
			BiFunction<Component, T, Component> getValueMessage) {
		if (curEntry == null) {
			curEntry = new EntryWithButtons();
			list.addEntry(curEntry);
		}
		
		Component name = Component.translatable(getPrefix(modId, type) +  "." + option.getFieldName());
		Component tooltip = Component.translatable(getPrefix(modId, type) + "." + option.getFieldName() + ".tooltip");
		Component nameWithSprite = name;
		if (icon != null) {
			ResourceLocation iconPath = toIconPath(icon);
			IconGlyphInfo spriteGlyph = new IconGlyphInfo(new GuiIcon(iconPath, 16, 16), 16, 16, 0, -4, 4);
			//stand aim marker: new GuiIcon(iconPath, 17, 17), 17, 17, 0, -5, 5)
			
			char spriteCode = iconSymbols.computeIfAbsent(iconPath, 
					__ -> IconGlyphsCache.makeCharCodeFor(spriteGlyph));
			nameWithSprite = Component.literal(String.valueOf(spriteCode)).append(name);
		}
		
		Button.OnPress onPress = b -> {
			changeValueOnClick.accept(option);
			switch (type) {
				case CLIENT -> {
					config.saveClient();
				}
				case CLIENT_BROADCAST -> {
					config.saveClient();
					config.sendClientBroadcast();
				}
				case COMMON -> {
					if (ConfigNetworkFunctions.clientIsConnectedToAServer()) {
						ConfigNetworkFunctions.clSendCommonSettingEditToServer(
								modId, option.getFieldName(), option);
					}
				}
			}
		};
		
		int buttonWidth = Math.min((screen.width - 80) / 2, 150);
		int x = switch (curEntry.children().size()) {
			case 0 -> screen.width / 2 - 35 - buttonWidth;
			case 1 -> screen.width / 2 - 25;
			default -> throw new IllegalStateException();
		};
		
		ConfigButton button = new ConfigButton(-1, -1, buttonWidth, 20, 
				nameWithSprite, 
				onPress, Tooltip.create(tooltip), 
				type, option, getValueMessage);
		if (curEntry.children().size() == 0) {
			button.setAlignment(Alignment.RIGHT);
		}
		
		curEntry.add(button, x, 2);
		if (curEntry.children().size() >= 2) {
			curEntry = null;
		}
	}
	
	public static class ConfigButton extends ScrollingStringButton {
		protected ModConfigType configType;
		protected ConfigOption<?> configOption;
		protected Component optionName;
		protected BiFunction<Component, ?, Component> getValueMessage;
		protected Object prevValue;

		public ConfigButton(int pX, int pY, int pWidth, int pHeight, 
				Component optionName, OnPress pOnPress, Tooltip pOnTooltip,
				ModConfigType type, ConfigOption<?> configOption, BiFunction<Component, ?, Component> getValueMessage) {
			super(pX, pY, pWidth, pHeight, makeMessage(getValueMessage, optionName, configOption.get()), pOnPress, pOnTooltip);
			this.configType = type;
			this.configOption = configOption;
			this.optionName = optionName;
			this.getValueMessage = getValueMessage;
		}
		
		protected static <T> Component makeMessage(BiFunction<Component, T, Component> getValueMessage, Component optionName, Object optionValue) {
			return getValueMessage.apply(optionName, (T) optionValue);
		}
		
		protected <T> Component makeMessage() {
			return makeMessage(this.getValueMessage, this.optionName, this.configOption);
		}
		
		protected void updateMessage(Object newValue) {
			Component message = makeMessage(getValueMessage, optionName, newValue);
			this.setMessage(message);
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
		list.addEntry(new EntryWithButtons().addRenderable(new Title(title, list), -15, 12));
		curEntry = null;
	}

	public static class Title extends Renderable2 {
		public AbstractWidget parentList;
		public Component title;
		
		public Title(Component title, AbstractWidget parentList) {
			super(0, 0);
			this.parentList = parentList;
			this.title = title;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			Minecraft mc = Minecraft.getInstance();
			Font font = mc.font;
			int width = parentList.getWidth();
			guiGraphics.drawCenteredString(font, title, x + width / 2, y, 0xC0C0C0);
		}

	}
	

	
	public static boolean clientCanEditCommonConfig() {
		return !ConfigNetworkFunctions.clientIsConnectedToAServer() || ConfigNetworkFunctions.clientHasPermissions();
	}

	public static ResourceLocation toIconPath(String fileName) {
		return JojoMod.resLoc("textures/gui/sprites/settings/" + fileName + ".png");
	}

	public static Object2CharMap<ResourceLocation> iconSymbols = new Object2CharArrayMap<>();
}
