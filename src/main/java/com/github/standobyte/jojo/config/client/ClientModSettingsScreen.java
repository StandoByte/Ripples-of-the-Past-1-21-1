package com.github.standobyte.jojo.config.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.itemrender.ItemIconModels;
import com.github.standobyte.jojo.client.ui.screen_widgets.ButtonInLayout;
import com.github.standobyte.jojo.client.ui.screen_widgets.ButtonWithImage;
import com.github.standobyte.jojo.client.ui.screen_widgets.ItemButton;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList.EntryWithButtons;
import com.github.standobyte.jojo.client.ui.screen_widgets.utils.ButtonDecoration;
import com.github.standobyte.jojo.config.ModConfigInterface;
import com.github.standobyte.jojo.config.RotpConfig;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.internal.ConfigEventHandler;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mixin.client.screen.ScreenAccessor;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

// just noticed that, when the screen is opened, the main menu panorama rotates twice as fast
// obviously that's not worth anyone's brain cells to go and figure out why, but that's so random
@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientModSettingsScreen extends Screen {
	public final Screen lastScreen;
	
	public ModConfig<?, ?, ?> curConfig;
	public String curModId;
	public ConfigTabType configType;

	public ClientModSettingsScreen(Screen lastScreen) {
		this(lastScreen, RotpConfig.ID, ConfigTabType.CLIENT);
	}

	public ClientModSettingsScreen(Screen lastScreen, String curModId, ConfigTabType configType) {
		super(CommonComponents.EMPTY);
		this.lastScreen = lastScreen;
		this.curModId = curModId;
		this.configType = configType;
		ScrollingStringButton.onScreenOpened();
	}
	
	public boolean switchMod(String modId) {
		ModConfig<?, ?, ?> config = ConfigEventHandler.ALL_CONFIGS.get(modId);
		ConfigTabType tab = this.configType;
		ConfigTabType[] tabs = ConfigTabType.values();
		do {
			if (configExists(config, tab)) {
				setConfigTab(modId, tab);
				return true;
			}
			tab = tabs[(tab.ordinal() + 1) % tabs.length];
		}
		while (tab != this.configType);
		
		return false;
	}
	
	public boolean switchTab(ConfigTabType tab) {
		if (configExists(curConfig, tab)) {
			setConfigTab(curModId, tab);
			return true;
		}
		return false;
	}
	
	public static boolean configExists(ModConfig<?, ?, ?> config, ConfigTabType tab) {
		if (config == null) return false;
		return switch (tab) {
			case CLIENT -> config.clientConfig.exists();
			case COMMON -> config.commonConfig.exists();
		};
	}
	
	protected void setConfigTab(String curModId, ConfigTabType configType) {
		this.curModId = curModId;
		this.configType = configType;
		updateConfigButtons(curModId, configType);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	protected void init() {
		addPermanentButtons();
		if (this.curModId != null && this.configType != null) {
			updateConfigButtons(this.curModId, this.configType);
		}
	}

	//@Override
	//public void removed() {
	//	settings.saveToFileSystem();
	//}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	protected void addPermanentButtons() {
		ModConfigInterface<?, ?, ?> config = ConfigEventHandler.ALL_CONFIGS.get(curModId);
		// Client/Gameplay tab buttons
		if (config != null) {
			int buttonWidth = Math.min((width / 4 - 10), 100);
			
			Button clientButton = new ConfigTypeButton(new Button.Builder(
					Component.translatable("jojo_ripples.config_category.client"), 
					button -> switchTab(ConfigTabType.CLIENT))
					.bounds(this.width / 2 - 5 - buttonWidth, 
							4, 
							buttonWidth, 20), 
					this, ConfigTabType.CLIENT);

			Button commonButton = new ConfigTypeButton(new Button.Builder(
					Component.translatable("jojo_ripples.config_category.common"), 
					button -> switchTab(ConfigTabType.COMMON))
					.bounds(this.width / 2 + 5, 
							4, 
							buttonWidth, 20), 
					this, ConfigTabType.COMMON);

			addRenderableWidget(clientButton);
			addRenderableWidget(commonButton);
		}

		// back button
		addRenderableWidget(new Button.Builder(
				CommonComponents.GUI_DONE, button -> minecraft.setScreen(lastScreen))
				.bounds(this.width / 2 - 50, 
						this.height - 26, 
						100, 20)
				.build(/*Button::new*/));

		// reset button
		//addRenderableWidget(new Button.Builder(
		//		Component.translatable("jojo_ripples.config.reset"), button -> {})
		//		.bounds(this.width - 66, 
		//				this.height - 26, 
		//				60, 20)
		//		.build(/*Button::new*/));
		
		// mod switch buttons
		ScrolleableButtonList modsList = new ScrolleableButtonList(minecraft, 0, 33, 30, height - 66, 18);
		for (var modEntry : ConfigEventHandler.ALL_CONFIGS.entrySet()) {
			String modId = modEntry.getKey();
			SelectModButton button = new SelectModButton(CONFIG_TAB_ICONS.get(modId), this, modId);
			modsList.addEntry(new EntryWithButtons().add(button, 0, 0));
		}
		addRenderableWidget(modsList);
	}

	
	public enum ConfigTabType {
		CLIENT,
		COMMON
	}
	
	@SuppressWarnings("unchecked")
	public void addConfigWidget(Object widget) {
		if (widget instanceof Renderable renderable) {
			this.renderables.add(renderable);
		}
		if (widget instanceof NarratableEntry narratable) {
			((ScreenAccessor) this).getNarratables().add(narratable);
		}
		if (widget instanceof GuiEventListener button) {
			((List<GuiEventListener>) this.children()).add(button);
		}
		this.configButtons.add(widget);
	}
	

	
	public static final Map<String, BiConsumer<ConfigTabType, ClientModSettingsScreen>> CONFIG_GUI_LAYOUTS = new LinkedHashMap<>();
	
	protected Collection<Object> configButtons = new ArrayList<>();
	
	protected void updateConfigButtons(String curModId, ConfigTabType configType) {
		clearConfigButtons();
		this.curConfig = ConfigEventHandler.ALL_CONFIGS.get(curModId);
		if (curModId != null && configType != null) {
			var layout = CONFIG_GUI_LAYOUTS.get(curModId);
			if (layout != null) {
				layout.accept(configType, this);
			}
		}
	}
	
	protected void clearConfigButtons() {
		if (!configButtons.isEmpty()) {
			var children = this.children();
			var narratables = ((ScreenAccessor) this).getNarratables();
			for (Object configButton : configButtons) {
				renderables.remove(configButton);
				children.remove(configButton);
				narratables.remove(configButton);
			}
			configButtons.clear();
		}
	}

	@Override
    protected void removeWidget(GuiEventListener listener) {
    	super.removeWidget(listener);
    	configButtons.remove(listener);
    }

	@Override
    protected void clearWidgets() {
    	super.clearWidgets();
    	configButtons.clear();
    }

	
	public static final WidgetSprites SPRITES_BLUE_DABADEE_DABADI = new WidgetSprites(
			JojoMod.resLoc("widget/button_blue"),
	        ResourceLocation.withDefaultNamespace("widget/button_disabled"),
			JojoMod.resLoc("widget/button_blue_highlighted"));

	public static class ConfigTypeButton extends Button {
		protected ClientModSettingsScreen screen;
		protected ConfigTabType cfgTab;

		public ConfigTypeButton(Builder builder, ClientModSettingsScreen screen, ConfigTabType cfgTab) {
			super(builder);
			this.screen = screen;
			this.cfgTab = cfgTab;
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.active = configExists(screen.curConfig, cfgTab);
			
			Minecraft minecraft = Minecraft.getInstance();
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			WidgetSprites sprites = cfgTab == screen.configType ? SPRITES_BLUE_DABADEE_DABADI : SPRITES;
			guiGraphics.blitSprite(sprites.get(this.active, this.isHoveredOrFocused()), 
					this.getX(), this.getY(), this.getWidth(), this.getHeight());
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			int i = getFGColor();
			this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
		}
	}
	
	
	public static final Map<String, ButtonDecoration> CONFIG_TAB_ICONS = new LinkedHashMap<>();

	public static class SelectModButton extends ButtonWithImage {
		protected static final WidgetSprites SPRITES = new WidgetSprites(
				JojoMod.resLoc("widget/button_pressable"),
				JojoMod.resLoc("widget/button_pressable_disabled"),
				JojoMod.resLoc("widget/button_pressable_highlighted"));
		public static final ResourceLocation SPRITE_PRESSED = JojoMod.resLoc("widget/button_pressable_selected");

		public ClientModSettingsScreen screen;
		public String modId;
		
		public SelectModButton(@Nullable ButtonDecoration icon, ClientModSettingsScreen screen, String modId) {
			super(-1, -1, 18, 18, icon, 
					b -> {
						if (!modId.equals(screen.curModId)) {
							screen.switchMod(modId);
						}
					}, 
					Tooltip.create(Component.translatable(modId + ".config.title")), 
					Component.translatable(modId + ".config.title"));
			this.screen = screen;
			this.modId = modId;
		}
		
		@Override
		protected ResourceLocation pickSprite() {
			if (this.modId.equals(screen.curModId)) {
				return SPRITE_PRESSED;
			}
			return getGud(SPRITES, this);
		}
		
		public static ResourceLocation getGud(WidgetSprites sprites, AbstractWidget button) {
			return sprites.get(button.active, button.isHoveredOrFocused());
		}
		
	}



	// FIXME do we need this button now that you can always open the mod list into the config screen?
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addToScreen(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screen instanceof OptionsScreen optionsScreen) {
			List<AbstractWidget> otherModdedButtons = event.getListenersList().stream()
					.filter(b -> b instanceof AbstractWidget)
					.map(b -> (AbstractWidget) b)
					.toList();
			
			Component tooltip = Component.translatable("jojo_ripples.config");
			ItemButton settingsButton = new ItemButton(-1, -1, 20, 20, 
					ItemIconModels.makeIconItem(ItemIconModels.MOD_LOGO),
					__ -> optionsScreen.getMinecraft().setScreen(new ClientModSettingsScreen(optionsScreen)),
					Tooltip.create(tooltip),
					tooltip);
			Layout fuckTheseAbstractions = new ButtonInLayout(settingsButton, button -> {
				int[] buttonPos = findPosForButton(optionsScreen, otherModdedButtons, button);
				button.setX(buttonPos[0]);
				button.setY(buttonPos[1]);
			});
			ClientReflection.getLayout(optionsScreen).addToContents(fuckTheseAbstractions);
			fuckTheseAbstractions.arrangeElements();
			event.addListener(settingsButton);
		}
	}
    
    private static int[] findPosForButton(Screen optionsScreen, List<AbstractWidget> otherModdedButtons, AbstractWidget except) {
		final int minY = 87;
		final int maxY = minY + 100;

		final int minX1 = 0;
		final int maxX1 = optionsScreen.width / 2 - 155 - 20 - 5;
		final int minX2 = optionsScreen.width / 2 + 160;
		final int maxX2 = optionsScreen.width - 20;

		final int minX3 = optionsScreen.width / 2 - 155;
		final int maxX3 = minX3 + 290;
		final int y3 = maxY + 24;

		int[] buttonPos = null;

		// try placing the button to the right side
		for (int x = minX2; x <= maxX2 && buttonPos == null; x += 25) {
			int y = maxY;
			if (ModList.get().isLoaded("essential")) y -= 24; // for fuck's sake
			for (; y >= minY && buttonPos == null; y -= 24) {
				buttonPos = noOverlapPos(otherModdedButtons, except, x, y);
			}
		}
		// ...or to the left side
		if (buttonPos == null) {
			for (int x = maxX1; x >= minX1 && buttonPos == null; x -= 25) {
				for (int y = maxY; y >= minY && buttonPos == null; y -= 24) {
					buttonPos = noOverlapPos(otherModdedButtons, except, x, y);
				}
			}
		}
		// ...or below the vanilla options
		if (buttonPos == null) {
			for (int x = minX3; x <= maxX3 && buttonPos == null; x += 29) {
				buttonPos = noOverlapPos(otherModdedButtons, except, x, y3);
			}
		}
		// ...how many new buttons are there?? fuck it, just put it at the "Done" button
		if (buttonPos == null) {
			buttonPos = new int[] { optionsScreen.width / 2 + 110, optionsScreen.height - 26 };
		}
		
		return buttonPos;
    }

	@Nullable
	private static int[] noOverlapPos(List<AbstractWidget> buttonsList, AbstractWidget except, int x, int y) {
		int x2 = x + 20;
		int y2 = y + 20;
		return buttonsList.stream().filter(button -> button != except).anyMatch(button -> {
			return button.getX() < x2 && button.getX() + button.getWidth() > x && button.getY() < y2 && button.getY() + button.getHeight() > y;
		}) ? null : new int[] { x, y };
	}

}
