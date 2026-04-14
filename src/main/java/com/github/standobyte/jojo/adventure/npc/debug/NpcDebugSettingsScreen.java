package com.github.standobyte.jojo.adventure.npc.debug;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.adventure.npc.NpcInventoryExchangeContainer;
import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.mechanics.clothes.client.ui.PlayerClothesScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class NpcDebugSettingsScreen extends AbstractContainerScreen<NpcInventoryExchangeContainer> {
	public PowerUserMobEntity npc;

	public ScrolleableList flagsList;
	public EditBox name;
	public Button setName;
	public Button setSkin;

	public NpcDebugSettingsScreen(NpcInventoryExchangeContainer menu, Inventory playerInventory, Component name) {
		super(menu, playerInventory, menu.character.getDisplayName());
		this.npc = menu.character;
	}

	@Override
	public void init() {
		super.init();
		
        this.leftPos = 250;
        this.topPos = 60;
        this.titleLabelY -= 34;
		
		int WIDTH = 150;
		flagsList = new ScrolleableList(minecraft, WIDTH, 
				minecraft.getWindow().getGuiScaledHeight(), 0, 25);
		NpcFlags[] flags = NpcFlags.values();
		for (int i = 0; i < flags.length; ++i) {
			NpcFlags flag = flags[i];
			SettingButton button = new SettingButton(this, 5, 5 + i * 25, WIDTH, 20);
			button.setBooleanFlag(flag);
			EntryWithLiterallyJustAButton entry = new EntryWithLiterallyJustAButton(button);
			flagsList.addEntry(entry);
		}
		addRenderableWidget(flagsList);
		
		name = addRenderableWidget(new EditBox(minecraft.font, WIDTH + 15, 2, 150, 20, CommonComponents.EMPTY));
		if (npc.hasCustomName()) {
			name.setValue(npc.getDisplayName().getString());
		}
		
		setName = addRenderableWidget(Button.builder(Component.literal("name"), b -> {
			String newName = name.getValue();
			PacketDistributor.sendToServer(ClNpcDebugFlagTogglePacket.name(npc.getId(), newName));
		}).bounds(320, 2, 40, 20).build());
		
		setSkin = addRenderableWidget(Button.builder(Component.literal("skin"), b -> {
			String newName = name.getValue();
			PacketDistributor.sendToServer(ClNpcDebugFlagTogglePacket.skin(npc.getId(), newName));
		}).bounds(365, 2, 40, 20).build());
	}
	
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    	if (npc == null || !npc.isAlive()) {
    		onClose();
    		return;
    	}
    	// WHAT'S THE POINT OF MAKING THEM FINAL
    	//this.playerInventoryTitle = minecraft.player.getDisplayName();
    	//this.title = npc.getDisplayName();
    	super.render(guiGraphics, mouseX, mouseY, partialTick);

    	setSkin.active = PowerUserMobEntity.isLegitPlayerName(name.getValue());
		PlayerClothesScreen.renderEntityInInventoryFollowsMouse(guiGraphics, 
				100, 0, 270, 180, 30, 0.0625F, mouseX, mouseY, npc);
		PlayerClothesScreen.renderEntityInInventoryFollowsMouse(guiGraphics, 
				100, 100, 270, 280, 30, 0.0625F, mouseX, mouseY, minecraft.player);

		this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {}

	public static class SettingButton extends Button {
		public NpcDebugSettingsScreen screen;
		public NpcFlags flag;

		public SettingButton(NpcDebugSettingsScreen screen, int x, int y, int width, int height) {
			super(x, y, width, height, CommonComponents.EMPTY, b -> {}, Button.DEFAULT_NARRATION);
			this.screen = screen;
		}
		
		public void setBooleanFlag(NpcFlags flag) {
			this.flag = flag;
			setMessage(Component.literal(flag.name().toLowerCase()));
		}

		@Override
		protected boolean isValidClickButton(int button) {
			return true;
		}

		@Override
		public void onClick(double mouseX, double mouseY, int button) {
			if (flag != null && screen != null && screen.npc != null) {
				boolean curValue = screen.npc.getFlag(flag);
				if (screen.minecraft.isPaused()) {
					screen.npc.setFlag(flag, !curValue);
				}
				PacketDistributor.sendToServer(ClNpcDebugFlagTogglePacket.flag(screen.npc.getId(), flag, !curValue));
			}
		}

		@Override
	    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
			if (flag != null && screen != null && screen.npc != null) {
				boolean value = screen.npc.getFlag(flag);
				color = value ? 0xFF00FF00 : 0xFFFF0000;
			}
			super.renderString(guiGraphics, font, color);
	    }

	}
	
	
	
	public static class ScrolleableList extends ContainerObjectSelectionList<EntryWithLiterallyJustAButton> {

		public ScrolleableList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
			super(minecraft, width, height, y, itemHeight);
		}

		@Override
	    public int getRowWidth() {
	    	return this.width - 10;
	    }

		// what's the point of making it protected...
		@Override
	    public int addEntry(EntryWithLiterallyJustAButton entry) {
	    	return super.addEntry(entry);
	    }
		
	}
	
	public static class EntryWithLiterallyJustAButton extends ContainerObjectSelectionList.Entry<EntryWithLiterallyJustAButton> {
		public Button button;
		public final List<Button> allButtons = new ArrayList<>(1);
		
		public EntryWithLiterallyJustAButton(Button button) {
			this.button = button;
			this.allButtons.add(button);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, 
				int top, int left, int width, int height, 
				int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = left - 2 - (button.getWidth() - width) / 2;
			int j = top - 2;
			this.button.setPosition(i, j);
			this.button.render(guiGraphics, mouseX, mouseY, partialTick);
		}
		
        @Override
        public List<? extends GuiEventListener> children() {
            return allButtons;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return allButtons;
        }
		
	}
}
