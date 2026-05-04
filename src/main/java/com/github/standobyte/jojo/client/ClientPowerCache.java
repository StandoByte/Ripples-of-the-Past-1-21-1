package com.github.standobyte.jojo.client;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.StandVanillaClickInput;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientPowerCache {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onTick(ClientTickEvent.Pre event) {
		cache(Minecraft.getInstance());
	}
	
	private static void cache(Minecraft mc) {
		if (!mc.isPaused()) {
			for (int i = 0; i < powersCache.length; i++) {
				powersCache[i] = null;
				availableAbilitiesCache[i] = null;
			}
			if (mc.player != null) {
				for (int i = 0; i < PowerClass.VALUES.length; i++) {
					PowerClass<?> powerClass = PowerClass.VALUES[i];
					// XXX only re-cache power when the object changes (it's not that often)
					Power<?> power = powerClass.get(mc.player);
					if (power != null) {
						powersCache[i] = power;
					}
				}
			}
		}
		
		if (mc.player != null) {
			for (int i = 0; i < PowerClass.VALUES.length; i++) {
				Power<?> power = powersCache[i];
				if (power != null) {
					availableAbilitiesCache[i] = power.updateAvailableMoves();
					
					StandVanillaClickInput.onMovesUpdate(power, availableAbilitiesCache[i]);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <P extends Power<P>> P getPower(PowerClass<P> powerClass) {
		return (P) powersCache[powerClass.ordinal()];
	}
	
	/**
	 * @param powerClass
	 * @param power Just in case, you don't have to provide it
	 */
	public static AvailableAbilities getAvailableAbilities(PowerClass<?> powerClass, @Nullable Power<?> power) {
		int i = powerClass.ordinal();
		if (availableAbilitiesCache[i] == null && power != null) {
			powersCache[i] = power;
			availableAbilitiesCache[i] = power.updateAvailableMoves();
		}
		return availableAbilitiesCache[i];
	}
	
	public static AvailableAbilities getAvailableAbilities(PowerClass<?> powerClass) { return getAvailableAbilities(powerClass, null); }

	private static Power<?>[] powersCache = new Power<?>[PowerClass.values().length];
	private static AvailableAbilities[] availableAbilitiesCache = new AvailableAbilities[PowerClass.values().length];
}
