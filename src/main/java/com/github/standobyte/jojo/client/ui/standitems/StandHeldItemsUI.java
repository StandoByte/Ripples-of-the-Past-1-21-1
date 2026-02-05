package com.github.standobyte.jojo.client.ui.standitems;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ui.powerhud.PowerHud;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.missingmethods._LivingEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class StandHeldItemsUI {
	public static final ResourceLocation TEXTURE = JojoMod.resLoc("textures/gui/container/stand_arm_slots.png");
	
	@SubscribeEvent
	public static void render(ContainerScreenEvent.Render.Background event) {
		StandEntity stand = ClientGlobals.playerStandEntity;
		if (stand != null) {
			AbstractContainerScreen<?> screen = event.getContainerScreen();
			boolean creativeScreen = screen.getClass() == CreativeModeInventoryScreen.class;
			GuiGraphics guiGraphics = event.getGuiGraphics();
			PoseStack pose = guiGraphics.pose();
			
			int screenHeight = screen.getYSize();
			if (screen.getClass() == ContainerScreen.class /*AKA chest*/) screenHeight -= 1; // why the fuck
			
			int width = creativeScreen ? 43 : 50;
			int height = creativeScreen ? 32 : 26;
			int leftSlotX = 8;
			int rightSlotX = 26;
			int slotsY = creativeScreen ? 8 : 2;
			int texV = creativeScreen ? 96 : 0;
			int standIconX = creativeScreen ? (leftSlotX + rightSlotX) / 2 : -16;
			int standIconY = creativeScreen ? -16 : slotsY;
			int x = creativeScreen ? screen.getGuiLeft() - width + 3 : screen.getGuiLeft() + screen.getXSize() - width;
			int y = creativeScreen ? screen.getGuiTop() + screenHeight - height : screen.getGuiTop() + screenHeight - 4;

			pose.pushPose();
			pose.translate(x, y, 0);
			BlitFloat.blit(pose, screen.getMinecraft(), TEXTURE, 
					0, 0, width, height, 0, 
					0, texV, width, height, 256, 256, 
					BlitFloat.NO_TINT);
			
			PowerHud.renderClientStandIcon(pose, standIconX, standIconY);

			ItemStack item = _LivingEntity.getItemHeldByArm(stand, HumanoidArm.LEFT);
			if (!item.isEmpty()) guiGraphics.renderItem(stand, item, leftSlotX, slotsY, 0);
			item = _LivingEntity.getItemHeldByArm(stand, HumanoidArm.RIGHT);
			if (!item.isEmpty()) guiGraphics.renderItem(stand, item, rightSlotX, slotsY, 0);

			pose.popPose();
		}
	}
	
}
