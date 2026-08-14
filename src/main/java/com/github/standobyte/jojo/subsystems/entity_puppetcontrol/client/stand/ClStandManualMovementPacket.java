package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.google.common.primitives.Floats;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClStandManualMovementPacket(double x, double y, double z, 
		float xRot, float yRot, boolean hasInput, boolean sendInputPacketToTracking) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClStandManualMovementPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<ClStandManualMovementPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClStandManualMovementPacket> type() {
			return type;
		}
		
		@Override
		public void encode(ClStandManualMovementPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeDouble(packet.x);
			buf.writeDouble(packet.y);
			buf.writeDouble(packet.z);
			buf.writeFloat(packet.xRot);
			buf.writeFloat(packet.yRot);
			buf.writeBoolean(packet.hasInput);
			buf.writeBoolean(packet.sendInputPacketToTracking);
		}
		
		@Override
		public ClStandManualMovementPacket decode(RegistryFriendlyByteBuf buf) {
			return new ClStandManualMovementPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(),
					buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readBoolean());
		}
		
		@Override
		public void handle(ClStandManualMovementPacket packet, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			if (isInvalid(packet)) {
				player.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_stand_movement"));
			}
			StandPower power = StandPower.get(player);
			if (power != null) {
				StandEntity stand = power.getSummonedStandEntity();
				if (stand != null) {
					manualControlPacket(stand, packet);
				}
			}
		}
		
		public void manualControlPacket(StandEntity stand, ClStandManualMovementPacket msg) {
//			ServerLevel level = player.getLevel();
			double posX1 = stand.getX(); // d0
			double posY1 = stand.getY(); // d1
			double posZ1 = stand.getZ(); // d2
			double posXcl = msg.x(); // d3
			double posYcl = msg.y(); // d4
			double posZcl = msg.z(); // d5
			float xRot = msg.xRot();
			float yRot = msg.yRot();
//			double diffX = posXcl - firstGoodX;
//			double diffY = posYcl - firstGoodY;
//			double diffZ = posZcl - firstGoodZ;
			double diffX = posXcl - posX1; // d6
			double diffY = posYcl - posY1; // d7
			double diffZ = posZcl - posZ1; // d8
//			double motionSq = stand.getDeltaMovement().lengthSqr(); // d9
//			double diffSq = diffX * diffX + diffY * diffY + diffZ * diffZ; // d10
//			if (diffSq - motionSq > 100.0D) {
//				JojoMod.getLogger().warn("{} ({}'s stand) moved too quickly! {},{},{}", stand.getName().getString(), player.getName().getString(), diffX, diffY, diffZ);
//				PacketManager.sendToClient(new standCancelManualMovementPacket(stand.getX(), stand.getY(), stand.getZ()), player);
//				return;
//			}
//			boolean flag = world.noCollision(stand, stand.getBoundingBox().deflate(0.0625D));
//			diffX = posXcl - lastGoodX;
//			diffY = posYcl - lastGoodY;
//			diffZ = posZcl - lastGoodZ;
			stand.move(MoverType.PLAYER, new Vec3(diffX, diffY, diffZ)); // also moves the stand towards user if the client tries to go to far away
//			diffX = posXcl - entity.getX();
//			diffY = posYcl - entity.getY();
//			if (diffY > -0.5D || diffY < 0.5D) {
//			   diffY = 0.0D;
//			}
//			diffZ = posZcl - entity.getZ();
//			diffSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
//			boolean flag1 = false;
//			if (diffSq > 0.0625D) {
//			   flag1 = true;
//			   LOGGER.warn("{} ({}'s stand) moved wrongly! {}", stand.getName().getString(), player.getName().getString(), Math.sqrt(diffSq));
//			}
			stand.absMoveTo(posXcl, posYcl, posZcl, yRot, xRot);
//			boolean flag2 = world.noCollision(stand, stand.getBoundingBox().deflate(0.0625D));
//			if (flag && (flag1 || !flag2)) {
//			   stand.absMoveTo(d0, d1, d2);
//			   PacketManager.sendToClient(new standCancelManualMovementPacket(stand.getX(), stand.getY(), stand.getZ()), player);
//			   return;
//			}
//			lastGoodX = stand.getX();
//			lastGoodY = stand.getY();
//			lastGoodZ = stand.getZ();
			if (msg.hasInput()) {
				stand.setDeltaMovement(Vec3.ZERO);
			}
			if (msg.sendInputPacketToTracking()) {
				PacketDistributor.sendToPlayersTrackingEntity(stand, new OnStandManualMovementPacket(stand.getId()));
			}
		}

		private boolean isInvalid(ClStandManualMovementPacket msg) {
			return Double.isNaN(msg.x) || Double.isNaN(msg.y) || Double.isNaN(msg.z)
					|| !Floats.isFinite(msg.xRot) || !Floats.isFinite(msg.yRot);
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
