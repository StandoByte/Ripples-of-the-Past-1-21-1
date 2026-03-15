package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.EntityComponentController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand.ClientStandController;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityManualControlToggle extends Ability {

	public StandEntityManualControlToggle(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		usageGroup = AbilityUsageGroup.UTILITY;
	}
	
	@Override
	public void writeExtraInput(FriendlyByteBuf serverboundBuf, LivingEntity user, boolean isClientPlayer) {
		if (isClientPlayer) {
			boolean shift = InputHandler.getInstance().isKeyHeld(InputConstants.KEY_LSHIFT);
			serverboundBuf.writeBoolean(shift);
		}
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide() || ClientProxy.getClientPlayer() == user) {
			boolean shift = extraClientInput.readBoolean();
			StandEntity stand = StandUtil.getSummonedStand(user);
			if (stand != null) {
				if (!stand.isManuallyControlled()) {
					on(level, stand);
				}
				else {
					off(level, stand, shift);
				}
			}
		}
	}
	
	public static void on(Level level, StandEntity stand) {
		stand.setCanFollowUser(true);
		stand.setManuallyControlled(true);
		LivingEntity user = stand.getUser();
		if (level.isClientSide()) {
			if (user == ClientProxy.getClientPlayer()) {
				ClientEntityController.setInstance(new ClientStandController(stand));
			}
		}
		else {
			EntityComponentController.setControlTarget(user, stand, "stand");
		}
	}
	
	public static void off(Level level, StandEntity stand, boolean keepPosition) {
		stand.setCanFollowUser(!keepPosition);
		stand.setManuallyControlled(false);
		LivingEntity user = stand.getUser();
		if (level.isClientSide()) {
			if (user == ClientProxy.getClientPlayer()) {
				ClientEntityController.setInstance(null);
			}
		}
		else {
			EntityComponentController component = ComponentUtil.getExistingDataOrNull(user, ModDataAttachmentTypes.CONTROLLER);
			if (component != null) {
				component.stopControlling();
			}
		}
	}

}
