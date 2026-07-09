package com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.input;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientStandItemInputs {

	public static KeyMapping keySwap = new KeyMapping("jojo_ripples.key.swap_items", KeyConflictContext.UNIVERSAL, 
			KeyModifier.CONTROL, InputConstants.Type.KEYSYM, InputConstants.KEY_F, "key.categories.inventory");

	@SubscribeEvent
	public static void handle(ClientTickEvent.Pre event) {
		StandEntity standEntity = ClientGlobals.playerStandEntity;
		if (standEntity != null) {
			if (Screen.hasControlDown()) {
				while (keySwap.consumeClick()) {
					PacketDistributor.sendToServer(ClStandItemInputPacket.packet(StandItemInput.Action.SWAP_USER_AND_STAND));
				}
			}
			
			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;
			if (!standEntity.getMainHandItem().isEmpty() || !standEntity.getOffhandItem().isEmpty()) {
				ClientEntityController curController = ClientEntityController.getInstance();
				boolean isControllingTheStand = curController != null && curController.entity == standEntity;
				boolean mainPlayerHandEmpty = player.getMainHandItem().isEmpty(); 
				boolean offPlayerHandEmpty = player.getOffhandItem().isEmpty(); 
				
				if (isControllingTheStand || mainPlayerHandEmpty) {
					while (mc.options.keyDrop.consumeClick()) {
						boolean fullStack = Screen.hasControlDown();
						PacketDistributor.sendToServer(ClStandItemInputPacket.packet(fullStack ? StandItemInput.Action.DROP_FULL_STACK : StandItemInput.Action.DROP));
					}
				}
				if (isControllingTheStand || mainPlayerHandEmpty && offPlayerHandEmpty) {
					while (mc.options.keySwapOffhand.consumeClick()) {
						PacketDistributor.sendToServer(ClStandItemInputPacket.packet(StandItemInput.Action.SWAP_HANDS));
					}
				}
			}
		}
	}

}
