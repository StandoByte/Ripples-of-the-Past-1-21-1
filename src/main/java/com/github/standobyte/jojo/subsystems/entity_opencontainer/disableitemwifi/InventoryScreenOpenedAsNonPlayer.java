package com.github.standobyte.jojo.subsystems.entity_opencontainer.disableitemwifi;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.entity_opencontainer.OpenContainerAsNonPlayer.ContainerOpenedAsNonPlayer;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class InventoryScreenOpenedAsNonPlayer {
	public static final ResourceLocation BIG_RED_CROSS = JojoMod.resLoc("textures/gui/container/player_inv_disabled.png");
	
	@SubscribeEvent
	public static void onContainerScreenRender(ContainerScreenEvent.Render.Foreground event) {
		AbstractContainerScreen<?> screen = event.getContainerScreen();
		AbstractContainerMenu containerMenu = screen.getMenu();
		Entity nonPlayerEntity = ((ContainerOpenedAsNonPlayer) containerMenu).jojo_ripples$getActualEntity();
		if (nonPlayerEntity != null) {
			if (containerMenu.slots.size() >= 36) {
				Slot firstPlayerInvSlot = containerMenu.slots.get(containerMenu.slots.size() - 36);
				int x = firstPlayerInvSlot.x;
				int y = firstPlayerInvSlot.y;
				GuiGraphics gui = event.getGuiGraphics();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				BlitFloat.blit(gui.pose(), screen.getMinecraft(), BIG_RED_CROSS, 
						x - 16, y - 16, 256, 256, 350, 
						BlitFloat.NO_TINT);
				RenderSystem.disableBlend();
			}
		}
	}
}
