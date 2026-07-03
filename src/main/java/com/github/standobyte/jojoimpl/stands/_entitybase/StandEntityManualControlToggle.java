package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.EntityComponentController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand.ClientStandController;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
	
	
	@Override
	public void renderAbilityIcon(Power<?> context, GuiGraphics guiGraphics, TextureAtlasSprite sprite, float x, float y, int color) {
		StandPower standPower = PowerClass.STAND.cast(context);
		if (standPower != null) {
			ClientEntityController curControlledEntity = ClientEntityController.getInstance();
			boolean isControlled = curControlledEntity != null && curControlledEntity.entity == ClientGlobals.playerStandEntity;
			PoseStack poseStack = guiGraphics.pose();
			guiGraphics.enableScissor((int) x, (int) y, (int) x + 16, (int) y + 16);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			if (isControlled) {
				PowerHud.renderStandIcon(standPower, poseStack, (int) x + 4, y - 4, 0x40FFFFFF);
				poseStack.pushPose();
				poseStack.scale(0.5f, 0.5f, 1);
				poseStack.translate(x, y, 0);
				ClientUtil.renderEntityFace(poseStack, x + 4, y + 10, context.getUser(), BlitFloat.NO_TINT, false);
				poseStack.popPose();
			}
			else {
				poseStack.pushPose();
				poseStack.scale(0.5f, 0.5f, 1);
				poseStack.translate(x, y, 0);
				ClientUtil.renderEntityFace(poseStack, x + 16, y, context.getUser(), 0x80FFFFFF, false);
				poseStack.popPose();
				PowerHud.renderStandIcon(standPower, poseStack, x - 2, y, BlitFloat.NO_TINT);
			}
			guiGraphics.disableScissor();
		}
	}

}
