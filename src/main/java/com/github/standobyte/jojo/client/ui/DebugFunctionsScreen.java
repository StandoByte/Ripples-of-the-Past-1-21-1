package com.github.standobyte.jojo.client.ui;

import com.github.standobyte.jojo.core.packet.fromclient.ClDebugCommandPacket;
import com.github.standobyte.jojo.mc.item.DebugItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class DebugFunctionsScreen extends Screen {

	public DebugFunctionsScreen() {
		super(CommonComponents.EMPTY);
	}

	public void init() {
		super.init();
		String[] commands = DebugItem.getOptions();
		for (int i = 0; i < commands.length; ++i) {
			String command = commands[i];
			Button button = new DebugButton(5, 5 + i * 25, 150, 20, command);
			addRenderableWidget(button);
		}
	}

	public static void onDebugItemUsed() {
		Minecraft.getInstance().setScreen(new DebugFunctionsScreen());
	}

	public static class DebugButton extends Button {
		public String command;

		// this is why the builder approach FUCKING SUCKS DICK, i need to know the mouse button
		// thank fuck the constructor is protected and not private
		public DebugButton(int x, int y, int width, int height, String command) {
			super(x, y, width, height, Component.literal(command), b -> {}, Button.DEFAULT_NARRATION);
			this.command = command;
		}

		@Override
		protected boolean isValidClickButton(int button) {
			return true;
		}

		@Override
		public void onClick(double mouseX, double mouseY, int button) {
			boolean sendPacket = DebugItem.onClientClick(command, button);
			if (sendPacket) {
				PacketDistributor.sendToServer(new ClDebugCommandPacket(command, button));
			}
		}

	}

}
