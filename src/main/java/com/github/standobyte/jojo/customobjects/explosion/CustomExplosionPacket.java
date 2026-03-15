package com.github.standobyte.jojo.customobjects.explosion;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CustomExplosionPacket implements CustomPacketPayload {
	private final double x;
	private final double y;
	private final double z;
	
	private CustomExplosion srvExplosion;
	
	private final float knockbackX;
	private final float knockbackY;
	private final float knockbackZ;

	public CustomExplosionPacket(CustomExplosion explosion, 
			double pX, double pY, double pZ, 
			@Nullable Vec3 pKnockback) {
		this.x = pX;
		this.y = pY;
		this.z = pZ;
		if (pKnockback != null) {
			this.knockbackX = (float)pKnockback.x;
			this.knockbackY = (float)pKnockback.y;
			this.knockbackZ = (float)pKnockback.z;
		}
		else {
			this.knockbackX = 0;
			this.knockbackY = 0;
			this.knockbackZ = 0;
		}
		this.srvExplosion = explosion;
	}

	
	
	private static CustomPacketPayload.Type<CustomExplosionPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<CustomExplosionPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<CustomExplosionPacket> type() {
			return type;
		}

		@Override
		public void encode(CustomExplosionPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeFloat((float)packet.x);
			buf.writeFloat((float)packet.y);
			buf.writeFloat((float)packet.z);

			buf.writeFloat(packet.knockbackX);
			buf.writeFloat(packet.knockbackY);
			buf.writeFloat(packet.knockbackZ);

			packet.srvExplosion.encode(buf, packet.x, packet.y, packet.z);
		}

		@Override
		public CustomExplosionPacket decode(RegistryFriendlyByteBuf buf) {
			double xPos = (double)buf.readFloat();
			double yPos = (double)buf.readFloat();
			double zPos = (double)buf.readFloat();

			float knockbackX = buf.readFloat();
			float knockbackY = buf.readFloat();
			float knockbackZ = buf.readFloat();

			CustomExplosion explosion = CustomExplosion.decode(buf, xPos, yPos, zPos);
			CustomExplosionPacket packet = new CustomExplosionPacket(explosion, xPos, yPos, zPos, new Vec3(knockbackX, knockbackY, knockbackZ));
			return packet;
		}

		@Override
		public void handle(CustomExplosionPacket payload, IPayloadContext context) {
			if (payload.srvExplosion != null) {
				payload.srvExplosion.finalizeExplosion(true);
			}
			Player player = ClientProxy.getClientPlayer();
			player.setDeltaMovement(player.getDeltaMovement().add((double)payload.knockbackX, (double)payload.knockbackY, (double)payload.knockbackZ));
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
