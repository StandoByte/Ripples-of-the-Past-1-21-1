package com.github.standobyte.jojoimpl.powers.vampirism.client;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class VampirismHudStuff {

	@SubscribeEvent
	public static void disableHudElements(RenderGuiLayerEvent.Pre event) {
		PlayerPower playerPower = ClientPowerCache.getPower(PowerClass.PLAYER_POWER);
		if (playerPower != null && playerPower.getPowerType() == ModPlayerPowers.VAMPIRISM.get()) {
			ResourceLocation layerName = event.getName();
			boolean foodBar = layerName.equals(VanillaGuiLayers.FOOD_LEVEL);
			boolean airBar = !foodBar && layerName.equals(VanillaGuiLayers.AIR_LEVEL);
			
			if (foodBar || airBar) {
				event.setCanceled(true);
			}
		}
	}
}
