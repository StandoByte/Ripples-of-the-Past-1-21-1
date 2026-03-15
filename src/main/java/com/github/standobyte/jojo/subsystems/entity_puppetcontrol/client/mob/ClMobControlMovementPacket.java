package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.mob;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.mob.accessors.EntityFallDamageInvoker;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.EntityComponentController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.mob.MobControlUtil;
import com.google.common.primitives.Floats;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClMobControlMovementPacket(int entityId, double x, double y, double z, float xRot, float yRot, boolean onGround) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClMobControlMovementPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<ClMobControlMovementPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClMobControlMovementPacket> type() {
			return type;
		}
		
		@Override
		public void encode(ClMobControlMovementPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeDouble(packet.x);
			buf.writeDouble(packet.y);
			buf.writeDouble(packet.z);
			buf.writeFloat(packet.xRot);
			buf.writeFloat(packet.yRot);
			buf.writeBoolean(packet.onGround);
		}
		
		@Override
		public ClMobControlMovementPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			double x = buf.readDouble();
			double y = buf.readDouble();
			double z = buf.readDouble();
			float xRot = buf.readFloat();
			float yRot = buf.readFloat();
			boolean onGround = buf.readBoolean();
			return new ClMobControlMovementPacket(entityId, x, y, z, xRot, yRot, onGround);
		}

		@Override
		public void handle(ClMobControlMovementPacket packet, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			if (isInvalid(packet)) {
				player.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
			}
			
			Entity curControlTarget = EntityComponentController.getControlTarget(player);
			if (curControlTarget != null && curControlTarget.getId() == packet.entityId) {
				manualControlPacket(MobControlUtil.getMobOrMobVehicle(curControlTarget), packet);
			}
		}

		public void manualControlPacket(Entity entity, ClMobControlMovementPacket msg) {
			double posXcl = clampHorizontal(msg.x); // d0
			double posYcl = clampVertical(msg.y); // d1
			double posZcl = clampHorizontal(msg.z); // d2
			float yRot = Mth.wrapDegrees(msg.yRot); // f
			float xRot = Mth.wrapDegrees(msg.xRot); // f1
			
			LivingEntity living = entity instanceof LivingEntity __ ? __ : null;
			
			if (entity.isPassenger()) {
				entity.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), yRot, xRot);
			} else {
				double posX1 = entity.getX(); // d3
				double posY1 = entity.getY(); // d4
				double posZ1 = entity.getZ(); // d5
				double diffX = posXcl - posX1; // d6
				double diffY = posYcl - posY1; // d7
				double diffZ = posZcl - posZ1; // d8

				boolean upwardsMotion = diffY > 0.0;
				if (entity.onGround() && !msg.onGround && upwardsMotion && living != null) {
					living.jumpFromGround();
				}

				float prevFallDistance = entity.fallDistance;
				entity.move(MoverType.PLAYER, new Vec3(diffX, diffY, diffZ));
				// no idea why it works correctly with players in vanilla, 
				// all i know is that fall distance was counted twice, in Entity#move and in LivingEntity#checkFallDamage
				entity.fallDistance = prevFallDistance;

				entity.absMoveTo(posXcl, posYcl, posZcl, yRot, xRot);
				Vec3 motion = new Vec3(entity.getX() - posX1, entity.getY() - posY1, entity.getZ() - posZ1);
				entity.setOnGroundWithMovement(msg.onGround, motion);
				doCheckFallDamage(entity, entity.getX() - posX1, entity.getY() - posY1, entity.getZ() - posZ1, msg.onGround);
				entity.setDeltaMovement(motion);
				if (upwardsMotion) {
					entity.resetFallDistance();
				}
			}
			
			if (living != null) {
				living.yHeadRot = msg.yRot;
			}
		}

		public static void doCheckFallDamage(Entity entity, double movementX, double movementY, double movementZ, boolean onGround) {
			if (!entity.touchingUnloadedChunk()) {
				EntityFallDamageInvoker entity_ = (EntityFallDamageInvoker) entity;
				entity_.invokeCheckSupportingBlock(onGround, new Vec3(movementX, movementY, movementZ));
				BlockPos blockpos = entity.getOnPosLegacy();
				BlockState blockstate = entity.level().getBlockState(blockpos);
				entity_.invokeCheckFallDamage(movementY, onGround, blockstate, blockpos);
			}
		}

		public static double clampHorizontal(double value) {
			return Mth.clamp(value, -3.0E7, 3.0E7);
		}

		public static double clampVertical(double value) {
			return Mth.clamp(value, -2.0E7, 2.0E7);
		}

		private boolean isInvalid(ClMobControlMovementPacket msg) {
			return Double.isNaN(msg.x) || Double.isNaN(msg.y) || Double.isNaN(msg.z)
					|| !Floats.isFinite(msg.xRot) || !Floats.isFinite(msg.yRot);
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
