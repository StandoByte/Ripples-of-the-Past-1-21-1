package com.github.standobyte.jojo.client.input;

import java.util.List;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.input.controlscheme.AbilityControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.AbilityControlsEntry;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKey;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHudControlsElement;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHudControlsElement.AbilityBindUI;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHudControlsElement.BindUI;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;
import com.github.standobyte.jojo.subsystems.entity_useitem.ClStandClickPacket;
import com.github.standobyte.jojo.subsystems.entity_useitem.ServerSideLivingClick;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
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
					if (standCanRightClickItemsOrBlocks) {
						boolean rightClickWithStandInsteadOfPlayer = stand.isManuallyControlled() || 
								!InputHandler.inputsDisabled && ServerSideLivingClick.isEntityHoldingAnItem(stand);
						if (rightClickWithStandInsteadOfPlayer) {
							event.setCanceled(true);
							event.setSwingHand(false);
							
							ClientKey key = ClientKey.make(InputConstants.Type.MOUSE, keyCode);
							InputHandler.getInstance().putHeldKeyTimer(key, new HeldKeyTimer(key, false, KeyModifier.NONE));
							
							HitResult target = Minecraft.getInstance().hitResult;
							PacketDistributor.sendToServer(new ClStandClickPacket(target, key.keyId(), InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND));
						}
					}
				}
				default -> {}
			}
		}
	}
	
	public static void onMovesUpdate(Power<?> power, AvailableAbilities abilities) {
		StandPower standPower = PowerClass.STAND.cast(power);
		
		if (standPower != null) {
			standCanRightClickItemsOrBlocks = standCanRightClick(standPower);
			hideMouseButtonStandKeybinds(standPower, abilities);
		}
	}
	
	public static boolean standCanRightClickItemsOrBlocks = true;
	
	private static boolean standCanRightClick(StandPower standPower) {
		if (InputHandler.getInstance().isModifierKeyPressed(KeyModifier.SHIFT)) {
			return false;
		}
		
		StandEntity standEntity = ClientGlobals.playerStandEntity;
		if (standEntity == null) return false;
		EntityActionInstance action = standEntity.getCurStandAction();
		if (action != null
				&& action.ability != ModSpecialActions.RMB_CLICK_ITEM.get()
				&& action.ability != ModSpecialActions.RMB_USING_ITEM.get()) {
			return false;
		}
		
		if (standPower == null) return false;
		StandTypePersistentData unlockedSkills = standPower.getCurTypeData();
		if (unlockedSkills == null) return false;
		return unlockedSkills.isSkillUnlocked("item_use");
	}
	
	private static void hideMouseButtonStandKeybinds(StandPower power, AvailableAbilities abilities) {
		if (!standCanRightClickItemsOrBlocks) return;
		
		if (ServerSideLivingClick.isEntityHoldingAnItem(power.getSummonedStandEntity())) {
			AbilityControlScheme controlScheme = AllControlSchemes.getForPowerType(power.getPowerType());
			if (controlScheme != null) {
				ClientKey RMB = InputHandler.RMB;
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

	public static void addItemRightClickBindIcon(AbilityControlScheme controlScheme, PowerHudControlsElement ui) {
		if (controlScheme.powerClassCosmetic == PowerClass.STAND && standCanRightClickItemsOrBlocks) {
			StandEntity standEntity = ClientGlobals.playerStandEntity;
			if (standEntity != null) {
				// for example, on Ctrl the grab is active instead, so item usage won't trigger - in this case don't add it to the UI
				boolean anotherMoveIsActive = false;
				
				ClientKey RMB = InputHandler.RMB;
				KeyModifier curModifier = InputHandler.getInstance().getCurModifier();
				AvailableAbilities abilities = ClientPowerCache.getAvailableAbilities(PowerClass.STAND);
				
				for (InputMethod inputMethod : InputMethod.values()) {
					List<AbilityControlsEntry> rmbAbilities = controlScheme.getBindsWithModifier(inputMethod, RMB, curModifier);
					for (AbilityControlsEntry abilityName : rmbAbilities) {
						var ability = abilities._inMoveset.get(abilityName.abilityName());
						if (ability != null) {
							AbilityInputState inputState = AbilityInputState.withValue(ability.clientInputState);
							if (inputState.getFlag(AbilityInputState.IS_ACTIVE)) {
								anotherMoveIsActive |= true;
								break;
							}
						}
					}
					
					if (anotherMoveIsActive) break;
				}
				
				if (!anotherMoveIsActive) {
					for (InteractionHand hand : InteractionHand.values()) {
						ItemStack item = standEntity.getItemInHand(hand);
						if (!item.isEmpty()) {

							BindUI bindUI = new BindUI(RMB);
							Component bindName = PowerHudControlsElement.getKeyName(RMB, KeyModifier.NONE);

							AbilityBindUI abilityBindUI = new AbilityBindUI(
									RMB, KeyModifier.NONE, InputMethod.CLICK, 
									bindName, Component.translatable("ripples_hud.key_ability", bindName, 
											Component.translatable("jojo_ripples.ability.item_use")), 
									0 /* TODO tie stand item cooldowns to the user's item cooldowns */) {

								@Override
								public void renderInside(GuiGraphics guiGraphics, float x, float y, Minecraft mc, float partialTick, int alpha) {
									guiGraphics.renderItem(standEntity, item, (int) x + 3, (int) y + 3, 67);
								}

							};
							bindUI.abilities.put(InputMethod.CLICK, abilityBindUI);

							ui.addBind(bindUI, InputHandler.RMB);
							break;
						}
					}
				}
			}
		}
	}
	
}
