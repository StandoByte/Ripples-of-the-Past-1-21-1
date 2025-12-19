package com.github.standobyte.jojo.client.input;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.core.packet.fromclient.ClAimTargetPacket;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityPunchAbility;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTargetAim;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientsideAim {
	public static ActionTarget cameraEntityAimTarget = ActionTarget.EMPTY;
	public static ActionTargetAim playerAim = new ActionTargetAim();
	public static ActionTargetAim standAim = new ActionTargetAim();
	
	public static void updateTarget(Minecraft mc, float partialTick) {
		cameraEntityAimTarget = mc.hitResult != null ? ActionTarget.fromVanilla(mc.hitResult) : ActionTarget.EMPTY;

		if (mc.level != null && mc.player != null) {
			boolean isPlayerCameraEntity = mc.player == mc.cameraEntity || mc.cameraEntity == null;
			if (isPlayerCameraEntity) {
				playerAim.setTarget(cameraEntityAimTarget);
			}
			else {
				playerAim.setTarget(ActionTarget.EMPTY);
			}
			
			StandEntity stand = ClientGlobals.playerStandEntity;
			if (stand != null) {
				EntityActionInstance curAction = LivingComponentAction.getCurEntityAction(stand);

				LivingEntity aiming;
				if (isPlayerCameraEntity && curAction == null) {
					aiming = mc.player;
				}
				else if (curAction != null) {
					aiming = switch (curAction.aimAs) {
						case PLAYER -> mc.player;
						case STAND -> stand;
						case CAMERA_ENTITY -> isPlayerCameraEntity ? mc.player : stand;
					};
				}
				else {
					aiming = stand;
				}

				/*
				 * A crutch that patches aiming for abilities like Crazy Diamond's healing (hold and aim at an entity).
				 * Due to stand user offset interpolation, when I tell the stand to move in front of me, 
				 * on the next tick it's still a tiny bit behind me, which may cause the target that is just in the range for the player 
				 * to still be out of reach for the stand, until it's completely in front of me
				 */
				boolean standOffsetLerping = aiming == stand && isPlayerCameraEntity && stand.isFollowingUser() && stand.offsetFromUser.getLerpAmount() < 1;
				if (!standOffsetLerping) {
					Vec3 lookPos = aiming.getEyePosition(partialTick);
					Vec3 lookVec = aiming.calculateViewVector(
							Mth.clamp(partialTick, aiming.xRotO, aiming.getXRot()), 
							Mth.clamp(partialTick, aiming.yRotO, aiming.getYRot()));
					ActionTarget target = HitResultUtil.clip(lookPos, lookVec, 
							stand.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), stand.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE), 
							aiming.level(), entity -> StandEntityPunchAbility.canStandHit(stand, entity), aiming, precisionAimingDisabled(mc) ? 0 : stand.getPrecision());
					standAim.setTarget(target);
				}
			}
			else {
				standAim.setTarget(ActionTarget.EMPTY);
			}
		}
		else {
			playerAim.setTarget(ActionTarget.EMPTY);
			standAim.setTarget(ActionTarget.EMPTY);
		}
	}
	
	public static void updateTargetWithServer(Minecraft mc) {
		if (mc.level != null) {
			if (playerAim.checkDirty()) {
				PacketDistributor.sendToServer(new ClAimTargetPacket(playerAim.getTarget(), ClAimTargetPacket.PacketType.PLAYER));
			}
			
			if (standAim.checkDirty()) {
				PacketDistributor.sendToServer(new ClAimTargetPacket(standAim.getTarget(), ClAimTargetPacket.PacketType.STAND));
			}
		}
	}
	
	public static boolean precisionAimingDisabled(Minecraft mc) {
		return mc.player == null || mc.player.isShiftKeyDown();
	}

}
