package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.util.functions.ClientUtil;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class ClientEntityController {
	@Nullable protected static ClientEntityController instance;

	protected Minecraft mc;
	@ApiStatus.Internal public Entity entity;
	@ApiStatus.Internal public LivingEntity entityAsLiving;

	public ClientEntityController(Entity entity) {
		this.entity = entity;
		this.entityAsLiving = entity instanceof LivingEntity l ? l : null;
		this.mc = Minecraft.getInstance();
	}

	public static void setInstance(ClientEntityController instance) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (ClientEntityController.instance != null) {
			ClientUtil.setCameraEntityPreventShaderSwitch(player);
			ClientEntityController.instance.onUnset();
		}
		if (instance != null) {
			if (player != null) {
				player.setSprinting(false);
				player.setJumping(false);
				player.xxa = 0;
				player.yya = 0;
				player.zza = 0;
			}
			ClientUtil.setCameraEntityPreventShaderSwitch(instance.entity);
			instance.onSet();
		}
		ClientEntityController.instance = instance;
	}

	@Nullable
	public static ClientEntityController getInstance() {
		return instance;
	}
	

	public static void clientTickPre() {
		if (instance != null) {
			Minecraft mc = instance.mc;
			if (instance.entity == null || !instance.entity.isAlive() || mc.level == null) {
				setInstance(null);
			}
			else {
				if (mc.cameraEntity == null || mc.cameraEntity == mc.player) {
					ClientUtil.setCameraEntityPreventShaderSwitch(instance.entity);
				}
			}
			
			if (instance != null) {
				instance.tickPre();
			}
		}
	}

	public static void clientTickPost() {
		if (instance != null) {
			sendLocalPlayerPosition(instance.mc.player);
			instance.tick();
		}
	}
	
	/**
	 * @return true if the local player entity should not turn
	 */
	public boolean turn(double yRot, double xRot) {
		entity.turn(yRot, xRot);
		if (entityAsLiving != null) {
			entityAsLiving.yHeadRot = entity.getYRot();
			entityAsLiving.yHeadRotO = entityAsLiving.yRotO;
		}
		return true;
	}

	public void onSet() {}

	public void onUnset() {}
	
	public static boolean isBeingControlledByClient(Entity entity) {
		return instance != null && instance.isBeingControlled(entity);
	}
	
	public boolean isBeingControlled(Entity entity) {
		return this.entity == entity;
	}

	/**
	 * This lets us override the vanilla keybinds, for things like hotbar keys
	 */
	public void tickPre() {}

	public void tick() {}
	public static void sendLocalPlayerPosition(LocalPlayer player) {
		if (player != null) {
			player.connection
			.send(new ServerboundMovePlayerPacket.PosRot(
					player.getX(), player.getY(), player.getZ(), 
					player.getYRot(), player.getXRot(), player.onGround()/*, player.horizontalCollision*/));
		}
	}
	
	/**
	 * Prevents the player from sneaking on shift, and flying in creative on double space.
	 */
	public static void clearInput(Input input) {
//		input.keyPresses = Input.EMPTY;
		input.up = false;
		input.down = false;
		input.left = false;
		input.right = false;
		input.jumping = false;
		input.shiftKeyDown = false;
		input.forwardImpulse = 0;
		input.leftImpulse = 0;
	}

//	public static void sendLocalPlayerPosition(LocalPlayer player) {
//		if (player != null) {
//			boolean isSprinting = player.isSprinting();
//			if (isSprinting != player.wasSprinting) {
//				ServerboundPlayerCommandPacket.Action serverboundplayercommandpacket$action = isSprinting
//						? ServerboundPlayerCommandPacket.Action.START_SPRINTING
//						: ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
//				player.connection.send(new ServerboundPlayerCommandPacket(player, serverboundplayercommandpacket$action));
//				player.wasSprinting = isSprinting;
//			}
//
//			double xDiff = player.getX() - player.xLast;
//			double yDiff = player.getY() - player.yLast;
//			double zDiff = player.getZ() - player.zLast;
//			double yRotDiff = (double)(player.getYRot() - player.yRotLast);
//			double xRotDiff = (double)(player.getXRot() - player.xRotLast);
//			player.positionReminder++;
//			boolean sendPos = Mth.lengthSquared(xDiff, yDiff, zDiff) > Mth.square(2.0E-4) || player.positionReminder >= 20;
//			boolean sendRot = yRotDiff != 0.0 || xRotDiff != 0.0;
//			if (sendPos && sendRot) {
//				player.connection.send(new ServerboundMovePlayerPacket.PosRot(
//						player.getX(), player.getY(), player.getZ(), 
//						player.getYRot(), player.getXRot(), player.onGround(), player.horizontalCollision));
//			} else if (sendPos) {
//				player.connection.send(new ServerboundMovePlayerPacket.Pos(player.getX(), player.getY(), player.getZ(), player.onGround(), player.horizontalCollision));
//			} else if (sendRot) {
//				player.connection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround(), player.horizontalCollision));
//			} else if (player.lastOnGround != player.onGround() || player.lastHorizontalCollision != player.horizontalCollision) {
//				player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(player.onGround(), player.horizontalCollision));
//			}
//
//			if (sendPos) {
//				player.xLast = player.getX();
//				player.yLast = player.getY();
//				player.zLast = player.getZ();
//				player.positionReminder = 0;
//			}
//
//			if (sendRot) {
//				player.yRotLast = player.getYRot();
//				player.xRotLast = player.getXRot();
//			}
//
//			player.lastOnGround = player.onGround();
//			player.lastHorizontalCollision = player.horizontalCollision;
//			player.autoJumpEnabled = player.minecraft.options.autoJump().get();
//		}
//	}

	public boolean shouldRenderBlockOutline() {
		return false;
	}

	public void renderExtraHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {}

}
