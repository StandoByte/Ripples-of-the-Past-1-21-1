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
import com.github.standobyte.jojo.client.ui.screen_widgets.ItemButton;
import com.github.standobyte.jojo.config.RotpConfig;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mixin.client.screen.ScreenAccessor;
import com.github.standobyte.jojo.util.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
	
	public String curModId;
	public ConfigType configType;

	public ClientModSettingsScreen(Screen lastScreen) {
		this(lastScreen, RotpConfig.ID, ConfigType.CLIENT);
	}

	public ClientModSettingsScreen(Screen lastScreen, String curModId, ConfigType configType) {
		super(CommonComponents.EMPTY);
		this.lastScreen = lastScreen;
		this.curModId = curModId;
		this.configType = configType;
		ScrollingStringButton.onScreenOpened();
	}
	
	protected void setConfigTab(String curModId, ConfigType configType) {
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
		if (this.curModId != null && this.configType != null) {
			updateConfigButtons(this.curModId, this.configType);
		}

		// back button
		addRenderableWidget(new Button.Builder(
				CommonComponents.GUI_DONE, button -> minecraft.setScreen(lastScreen))
				.bounds(this.width / 2 - 100, 
						this.height - 26, 
						200, 20)
				.build(/*Button::new*/));
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


	
	public enum ConfigType {
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
	
	public void addCategoryTitle(Component title, int y) {
		addConfigWidget(new Title(title, y));
	}

	protected static record Title(Component title, int y) implements Renderable {

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			Minecraft mc = Minecraft.getInstance();
			Font font = mc.font;
			int width = mc.getWindow().getGuiScaledWidth();
			guiGraphics.drawCenteredString(font, title, width / 2, y, 0xC0C0C0);
		}

	}

	public static ResourceLocation toIconPath(String fileName) {
		return JojoMod.resLoc("textures/gui/sprites/settings/" + fileName + ".png");
	}
	

	
	public static final Map<String, BiConsumer<ConfigType, ClientModSettingsScreen>> CONFIG_GUI_LAYOUTS = new LinkedHashMap<>();
	
	protected Collection<Object> configButtons = new ArrayList<>();
	
	protected void updateConfigButtons(String curModId, ConfigType configType) {
		clearConfigButtons();
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



	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addToScreen(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screen instanceof OptionsScreen optionsScreen) {
			List<AbstractWidget> otherModdedButtons = event.getListenersList().stream()
					.filter(b -> b instanceof AbstractWidget)
					.map(b -> (AbstractWidget) b)
					.toList();
			
			Component tooltip = Component.translatable("jojo_ripples.options.client.title");
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
