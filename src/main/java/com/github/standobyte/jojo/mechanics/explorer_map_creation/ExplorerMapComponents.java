package com.github.standobyte.jojo.mechanics.explorer_map_creation;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ExplorerMapComponents {
	public int posX;
	public int posZ;
	public Holder<MapDecorationType> destinationIcon = MapDecorationTypes.TARGET_X;
	public int itemColor = MapItemColor.DEFAULT.rgb();
	public String itemName = "";
	
	public Component makeItemName() {
		return itemName.isEmpty() ? Component.translatable("item.minecraft.filled_map") : Component.literal(itemName);
	}
	
	public static void updateMapItem(ItemStack item, Component name, 
			int posX, int posZ, Holder<MapDecorationType> destinationIcon, int itemColor) {
		 item.set(DataComponents.ITEM_NAME, name);
		 item.update(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY, decorations -> decorations.withDecoration(
				 "+", new MapDecorations.Entry(destinationIcon, posX, posZ, 180.0F)));
		 item.set(DataComponents.MAP_COLOR, new MapItemColor(itemColor));
	}
	
	public static void finalizeMapItem(ItemStack item, ServerLevel level, 
			int posX, int posZ) {
		MapItemSavedData mapData = MapItemSavedData.createFresh(
				posX, posZ, (byte) 2, true, true, level.dimension());
		MapId mapId = level.getFreeMapId();
		level.setMapData(mapId, mapData);
		item.set(DataComponents.MAP_ID, mapId);

		MapItem.renderBiomePreviewMap((ServerLevel) level, item);
	}
}
