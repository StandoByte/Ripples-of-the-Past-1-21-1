package com.github.standobyte.jojo.mechanics.explorer_map_creation.client;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.config.client.NumericField;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.CartographyExplorerMapContainerMenu;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.CartographyTableMode;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.packets.ClExplorerMapDataPacket;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.neoforge.network.PacketDistributor;

public class CartographyExplorerMapMenuScreen extends AbstractContainerScreen<CartographyExplorerMapContainerMenu> {
	private static final ResourceLocation BG_LOCATION = JojoMod.resLoc("textures/gui/container/cartography_table2.png");

	protected MapIconButton targetIcon;
	protected ScrolleableButtonList targetIconsList;
	protected MapIconWithSprite chosenIcon;
	
	protected NumericField xField;
	protected NumericField zField;
	
	protected EditBox itemName;

	public CartographyExplorerMapMenuScreen(CartographyExplorerMapContainerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		this.titleLabelY -= 2;
	}
	
	protected static record MapIconWithSprite(Holder<MapDecorationType> iconType, TextureAtlasSprite sprite) {}
	
	@Override
    protected void init() {
    	super.init();
		Button[] modeSwitchButtons = AddButtonsToVanillaCartographyTable.onCartographyTableInit(this, CartographyTableMode.EXPLORER);
		for (Button button : modeSwitchButtons) {
			addRenderableWidget(button);
		}

		
		List<MapIconWithSprite> icons = new ArrayList<>();
		MapDecorationTextureManager mapIconsAtlas = minecraft.getMapDecorationTextures();
		RegistryAccess reg = minecraft.level.registryAccess();
		HolderLookup.RegistryLookup<MapDecorationType> decorations = reg.lookupOrThrow(Registries.MAP_DECORATION_TYPE);
		decorations.listElements().forEach(iconTypeHolder -> {
			MapDecorationType iconType = iconTypeHolder.value();
			if (!iconType.trackCount()) {
				TextureAtlasSprite sprite = mapIconsAtlas.textureAtlas.getSprite(iconType.assetId());
				MapIconWithSprite iconWithSprite = new MapIconWithSprite(iconTypeHolder, sprite);
				icons.add(iconWithSprite);
				if (iconTypeHolder.is(menu.components.destinationIcon)) {
					chosenIcon = iconWithSprite;
				}
			}
		});

		int ICONS_IN_ROW = 5;
		targetIconsList = new ScrolleableButtonList(minecraft, 
				leftPos + 40, topPos + 15, 
				MapIconButton.SIZE * ICONS_IN_ROW + 6 + ScrolleableButtonList.SCROLL_BAR_WIDTH, 54, MapIconButton.SIZE);
		ScrolleableButtonList.EntryWithButtons entry = null;
		int i = 0;
		for (MapIconWithSprite icon : icons) {
			if (i % ICONS_IN_ROW == 0) {
				entry = new ScrolleableButtonList.EntryWithButtons();
				targetIconsList.addEntry(entry);
			}
			MapIconButton button = new MapIconButton(-1, -1, b -> {
				if (menu.setTargetIcon(icon.iconType)) {
					chosenIcon = icon;
					targetIcon.setIcon(icon.iconType, icon.sprite);
					PacketDistributor.sendToServer(ClExplorerMapDataPacket.destinationIcon(icon.iconType));
				}
				targetIconsList.visible = false;
			});
			button.setIcon(icon.iconType, icon.sprite);
			entry.add(button, i % ICONS_IN_ROW * MapIconButton.SIZE, 0);
			i++;
		}
		targetIconsList.visible = false;
		addRenderableWidget(targetIconsList);
		
		targetIcon = new MapIconButton(leftPos + 27, topPos + 41, b -> {
			targetIconsList.visible = !targetIconsList.visible;
		});
		targetIcon.setIcon(chosenIcon.iconType, chosenIcon.sprite);
		addRenderableWidget(targetIcon);
		
		
		WorldBorder border = minecraft.level.getWorldBorder();
		
		xField = new NumericField(leftPos + 57, topPos + 34, 70, 14, Component.literal("X"));
		xField.setValue(String.valueOf(menu.components.posX));
		xField.forInt((int) border.getMinX(), (int) border.getMaxX(), value -> {
			if (menu.setTargetPosX(value)) {
				PacketDistributor.sendToServer(ClExplorerMapDataPacket.posX(value));
			}
		});
		addRenderableWidget(xField);
		
		zField = new NumericField(leftPos + 57, topPos + 50, 70, 14, Component.literal("Z"));
		zField.setValue(String.valueOf(menu.components.posZ));
		zField.forInt((int) border.getMinZ(), (int) border.getMaxZ(), value -> {
			if (menu.setTargetPosZ(value)) {
				PacketDistributor.sendToServer(ClExplorerMapDataPacket.posZ(value));
			}
		});
		addRenderableWidget(zField);
		
		itemName = new EditBox(font, leftPos + 39, topPos + 18, 103, 12, CommonComponents.EMPTY);
		itemName.setTextColor(-1);
		itemName.setTextColorUneditable(-1);
		itemName.setBordered(false);
		itemName.setMaxLength(50);
		itemName.setResponder(name -> {
			if (menu.setMapItemName(name)) {
				PacketDistributor.sendToServer(ClExplorerMapDataPacket.name(name));
			}
		});
		addRenderableWidget(itemName);
    }
	
	protected void setChosenIcon(MapIconWithSprite icon) {
		
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		targetIcon.visible = !targetIconsList.visible;
		xField.visible = !targetIconsList.visible;
		zField.visible = !targetIconsList.visible;
		itemName.visible = !targetIconsList.visible;
		
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos;
		guiGraphics.blit(BG_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
		if (itemName.visible) {
			BlitFloat.blit(guiGraphics.pose(), minecraft, JojoMod.resLoc("textures/gui/container/cartography_table2_name_field.png"), 
					x + 36, y + 14, 110, 16, 0, BlitFloat.NO_TINT);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderLabels(guiGraphics, mouseX, mouseY);
		
		if (xField.visible) {
			guiGraphics.drawString(this.font, "X", 
					xField.getX() - leftPos - 9, xField.getY() - topPos + 3, 
					0x404040, false);
		}
		if (zField.visible) {
			guiGraphics.drawString(this.font, "Z", 
					zField.getX() - leftPos - 9, zField.getY() - topPos + 3, 
					0x404040, false);
		}
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputConstants.KEY_ESCAPE) {
			minecraft.player.closeContainer();
		}

		return 
					itemName.keyPressed(keyCode, scanCode, modifiers) || itemName.canConsumeInput()
				|| 	xField.keyPressed(keyCode, scanCode, modifiers) || xField.canConsumeInput()
				|| 	zField.keyPressed(keyCode, scanCode, modifiers) || zField.canConsumeInput()
				|| 	super.keyPressed(keyCode, scanCode, modifiers);
	}

}
