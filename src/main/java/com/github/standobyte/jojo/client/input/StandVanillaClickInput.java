package com.github.standobyte.jojo.client.input;

import java.util.List;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.input.controlscheme.AbilityControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.AbilityControlsEntry;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKey;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_useitem.ClStandClickPacket;
import com.github.standobyte.jojo.subsystems.entity_useitem.ServerSideLivingClick;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class StandVanillaClickInput {

	@SubscribeEvent
	public static void onVanillaClickInput(InteractionKeyMappingTriggered event) {
		StandEntity stand = ClientGlobals.playerStandEntity;
		if (stand != null) {
			int keyCode = event.isAttack() ? 0 : event.isUseItem() ? 1 : 2;
			switch (keyCode) {
				case 0 -> {} // LMB
				case 1 -> { // RMB
					if (standCanRightClickItems && (stand.isManuallyControlled() || !InputHandler.inputsDisabled && ServerSideLivingClick.isEntityHoldingAnItem(stand))) {
						event.setCanceled(true);
						event.setSwingHand(false);
						
						ClientKey key = ClientKey.make(InputConstants.Type.MOUSE, keyCode);
						InputHandler.getInstance().putHeldKeyTimer(key, new HeldKeyTimer(key, false, KeyModifier.NONE));

						HitResult target = Minecraft.getInstance().hitResult;
						PacketDistributor.sendToServer(new ClStandClickPacket(target, key.keyId(), InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND));
					}
				}
				default -> {}
			}
		}
	}
	
	public static void onMovesUpdate(Power<?> power, AvailableAbilities abilities) {
		StandPower standPower = PowerClass.STAND.cast(power);
		if (standPower != null) {
			hideMouseButtonStandKeybinds(standPower, abilities);
		}
	}
	
	private static boolean standCanRightClickItems = true;
	
	private static void hideMouseButtonStandKeybinds(StandPower power, AvailableAbilities abilities) {
		if (!standCanRightClickItems) return;
		
		if (ServerSideLivingClick.isEntityHoldingAnItem(power.getSummonedStandEntity())) {
			AbilityControlScheme controlScheme = AllControlSchemes.getForPowerType(power.getPowerType());
			if (controlScheme != null) {
				ClientKey RMB = ClientKey.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT);
				for (InputMethod inputMethod : InputMethod.values()) {
					List<AbilityControlsEntry> rmbAbilities = controlScheme.getBindsWithModifier(inputMethod, RMB, KeyModifier.NONE);
					for (AbilityControlsEntry abilityName : rmbAbilities) {
						var ability = abilities._inMoveset.get(abilityName.abilityName());
						if (ability != null) {
							AbilityInputState inputState = AbilityInputState.withValue(ability.clientInputState);
							if (!inputState.getFlag(AbilityInputState.WITH_ITEM_HELD)) {
								inputState.setFlag(AbilityInputState.IS_ACTIVE, false);
								inputState.setFlag(AbilityInputState.VISIBLE_WHEN_INACTIVE, false);
							}
							ability.clientInputState = inputState._value;
						}
					}
				}
			}
		}
	}
	
}
