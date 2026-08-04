package com.github.standobyte.jojo.init;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.github.standobyte.jojo.client.CustomRenderCreativeTab;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.init.power.ModStands;
import com.github.standobyte.jojo.mechanics.clothes.ClothesItem;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.mechanics.standdisc.StandDiscItem;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.subsystems.StoryPart;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemsCreativeTab extends CreativeModeTab implements CustomRenderCreativeTab {
	protected int standsRow = -1;
	protected int clothesRow = -1;

	public ModItemsCreativeTab(Builder builder) {
		super(builder);
	}
	
	@Override
	public void buildContents(CreativeModeTab.ItemDisplayParameters parameters) {
		// add regular items
		super.buildContents(parameters);

		this.displayItems = new ArrayList<>(getDisplayItems());
		Collection<ItemStack> tabItems = getDisplayItems();
		Collection<ItemStack> searchItems = getSearchTabDisplayItems();
		
		// OOP was a mistake.
//		CreativeModeTab.Output output = new CreativeModeTab.Output() {
//			@Override
//			public void accept(ItemStack stack, TabVisibility tabVisibility) {
//				FeatureFlagSet featureFlagSet = parameters.enabledFeatures();
//				if (stack.getItem().isEnabled(featureFlagSet)) {
//					switch (tabVisibility) {
//						case PARENT_AND_SEARCH_TABS -> {
//							tabItems.add(stack);
//							searchItems.add(stack);
//						}
//						case PARENT_TAB_ONLY -> {
//							tabItems.add(stack);
//						}
//						case SEARCH_TAB_ONLY -> {
//							searchItems.add(stack);
//						}
//					}
//				}
//			}
//		};
		
		standsRow = (tabItems.size() - 1) / ROWLEN + 1;
		rowBreak(tabItems, true);
		
		// add Stand discs
		Stream<StandType> stands = StandType.getAllEnabledStands()
				.filter(stand -> !ModStands.EXCLUDE_FROM_CREATIVE_TAB.contains(stand));
		stands
		.map(StandInstance::new)
		.sorted(discsOrder(parameters.holders()))
		.map(StandDiscItem::withStand)
		.forEach(item -> {
			tabItems.add(item);
			searchItems.add(item);
		});

		clothesRow = (tabItems.size() - 1) / ROWLEN + 1;
		rowBreak(tabItems, true);

		// add clothes items
		ClothesItem clothesFactory = ModItems.CLOTHES_BASE_ITEM.get();
		parameters.holders()
		.lookup(JojoRegistries.CLOTHES_SETS_REG_KEY)
		.ifPresent(
				clothesSets -> clothesSets.listElements()
				.map(setHolder -> {
					List<ItemStack> items = new ArrayList<>(ClothesSlotType.values().length);
					for (ClothesSlotType slot : ClothesSlotType.values()) {
						ClothesDataComponent component = ClothesItem.makeItemComponent(setHolder, slot);
						if (component != null) {
							ItemStack item = clothesFactory.makeClothesPieceStack(component);
							items.add(item);
						}
					}
					return items;
				})
				.forEach(setItems -> {
					int setSize = setItems.size();
					int rowSpace = ROWLEN - tabItems.size() % ROWLEN;
					
					int spacesToAdd;
					// if the set doesn't fit on the row, move it to the next row
					if (rowSpace < setSize)									spacesToAdd = rowSpace;
					// if it's not the beginning of the row, add a space between the previous and this sets
					else if (rowSpace < ROWLEN && rowSpace >= setSize + 1)	spacesToAdd = 1;
					else													spacesToAdd = 0;
					for (int i = 0; i < spacesToAdd; i++) {
						tabItems.add(ItemStack.EMPTY);
					}
					
					for (ItemStack item : setItems) {
						tabItems.add(item);
						searchItems.add(item);
					}
				}));
	}
	
	public static final int ROWLEN = 9;
	public static void rowBreak(Collection<ItemStack> items, boolean addExtraRow) {
		int size = items.size();
		int toAdd = size > 0 ? ROWLEN - ((size - 1) % ROWLEN + 1) : 0;
		if (addExtraRow) {
			toAdd += 9;
		}
		for (int i = 0; i < toAdd; i++) {
			items.add(ItemStack.EMPTY);
		}
	}
	
	public static Comparator<StandInstance> discsOrder(HolderLookup.Provider registries) {
		return Comparator
				.comparingInt((StandInstance stand) -> stand.getStandType().discCategoryPriority)
				.thenComparing((StandInstance stand) -> StoryPart.getStoryPart(stand, registries), StoryPart.COMPARATOR)
				.thenComparingInt((StandInstance stand) -> stand.getStandType().discStoryPartPriority);
	}


	public static final ResourceLocation CATEGORY_STANDS = JojoMod.resLoc("textures/gui/container/creative_stands.png");
	public static final ResourceLocation CATEGORY_CLOTHES = JojoMod.resLoc("textures/gui/container/creative_clothes.png");
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, 
			CreativeModeInventoryScreen screen, int rowScrolled) {
		if (standsRow >= 0) {
			renderCategoryNameAt(guiGraphics, standsRow - rowScrolled, 
					CATEGORY_STANDS, 
					Component.translatable("item.jojo_ripples.creative_tab.stand_discs"),
					screen);
		}
		
		if (clothesRow >= 0) {
			renderCategoryNameAt(guiGraphics, clothesRow - rowScrolled, 
					CATEGORY_CLOTHES, 
					Component.translatable("jojo_ripples.menu.player.clothes"),
					screen);
		}
	}
	
	public void renderCategoryNameAt(GuiGraphics guiGraphics, int row, ResourceLocation background, Component name, CreativeModeInventoryScreen screen) {
		if (row >= 0 && row <= 4) {
			Minecraft mc = screen.getMinecraft();
			int x = screen.getGuiLeft() + 8;
			int y = screen.getGuiTop() + 17 + row * 18;
			BlitFloat.blit(guiGraphics.pose(), mc, background, 
					x, y, 162, 18, 0,
					0, 0, 162, 18, 256, 32, 
					BlitFloat.NO_TINT);
			guiGraphics.drawString(mc.font, name, x, y + 5, this.getLabelColor(), false);
		}
	}
	
}
