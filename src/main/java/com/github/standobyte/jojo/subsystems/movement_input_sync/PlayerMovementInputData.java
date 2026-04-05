package com.github.standobyte.jojo.subsystems.movement_input_sync;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Lets us know which movement keys the player is pressing, 
 * also sending the data to the server side and other clients
 */
@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class PlayerMovementInputData {
	public float left;
	public float forward;
	public boolean jumping;
	public boolean shiftKeyDown;
	public boolean sprint;
	
	public int leftTimer;
	public int forwardTimer;
	public int jumpingTimer;
	public int shiftKeyDownTimer;
	public int sprintTimer;
	
	public void setValues(float left, float forward, boolean jumping, boolean shiftKeyDown, boolean sprint) {
		if (this.left != left) {
			this.left = left;
			this.leftTimer = 0;
		}
		if (this.forward != forward) {
			this.forward = forward;
			this.forwardTimer = 0;
		}
		if (this.jumping != jumping) {
			this.jumping = jumping;
			this.jumpingTimer = 0;
		}
		if (this.shiftKeyDown != shiftKeyDown) {
			this.shiftKeyDown = shiftKeyDown;
			this.shiftKeyDownTimer = 0;
		}
		if (this.sprint != sprint) {
			this.sprint = sprint;
			this.sprintTimer = 0;
		}
	}
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		PlayerMovementInputData input = get(player);
		input.tickTimers(player, player.isLocalPlayer());
	}
	
	public void tickTimers(Entity player, boolean isLocalPlayer) {
		leftTimer++;
		forwardTimer++;
		jumpingTimer++;
		shiftKeyDownTimer++;
		sprintTimer++;
		
		if (isLocalPlayer) {
			Input vanillaInput = ((LocalPlayer) player).input;
			float left = vanillaInput.leftImpulse;
			float forward = vanillaInput.forwardImpulse;
			boolean jumping = vanillaInput.jumping;
			boolean shift = vanillaInput.shiftKeyDown;
			boolean sprint = player.isSprinting();
			
			setValues(left, forward, jumping, shift, sprint);
			boolean changed = leftTimer == 0 || forwardTimer == 0 || jumpingTimer == 0 || shiftKeyDownTimer == 0 || sprintTimer == 0;
			if (changed) {
				PacketDistributor.sendToServer(new ClPlayerMovementInputPacket(
						player.getId(), left, forward, jumping, shift, sprint));
			}
		}
	}
	
	public static void handleServerboundPacket(ClPlayerMovementInputPacket packet, Player sender) {
		Entity entity = sender.level().getEntity(packet.entityId());
		PlayerMovementInputData input = get(entity);
		if (input != null) {
			input.setValues(packet.left(), packet.forward(), packet.jumping(), packet.shift(), packet.sprint());
			PacketDistributor.sendToPlayersTrackingEntity(sender, new TrPlayerMovementInputPacket(
					entity.getId(), input.left, input.forward, input.jumping, input.shiftKeyDown, input.sprint));
		}
	}
	
	public static void handleTrackingClientboundPacket(TrPlayerMovementInputPacket packet) {
		Entity entity = ClientProxy.getEntityById(packet.entityId());
		PlayerMovementInputData input = get(entity);
		if (input != null) {
			input.setValues(packet.left(), packet.forward(), packet.jumping(), packet.shift(), packet.sprint());
		}
	}
	
	
	public static PlayerMovementInputData get(Entity player) {
		if (player != null) {
			return player.getData(ModDataAttachmentTypes.SYNCHED_MOVEMENT_INPUT);
		}
		return null;
	}
}
