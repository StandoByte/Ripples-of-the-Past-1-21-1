package com.github.standobyte.jojo.network.s2c;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojo.util.functions_network.StreamCodecs;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ItemBreakVisualsPacket implements CustomPacketPayload {
	private final int entityId;
	private final Optional<Vec3> pos;
	private final Optional<ItemStack> item;
	
	public ItemBreakVisualsPacket(int entityId, Optional<Vec3> pos, Optional<ItemStack> item) {
		this.entityId = entityId;
		this.pos = pos;
		this.item = item;
	}
	
	@Nullable
	public static ItemBreakVisualsPacket fromParams(Entity entity, Vec3 pos, ItemStack item) {
		if (entity instanceof ItemEntity itemEntity) {
			return new ItemBreakVisualsPacket(itemEntity.getId(), Optional.empty(), Optional.empty());
		}
		else if (item != null) {
			if (entity != null) {
				return new ItemBreakVisualsPacket(entity.getId(), Optional.empty(), Optional.of(item));
			}
			else if (pos != null) {
				return new ItemBreakVisualsPacket(-1, Optional.of(pos), Optional.of(item));
			}
		}
		return null;
	}
	
	
	private static CustomPacketPayload.Type<ItemBreakVisualsPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ItemBreakVisualsPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ItemBreakVisualsPacket> type() {
			return type;
		}

		@Override
		public void encode(ItemBreakVisualsPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			NetworkUtil.writeOptional(packet.pos, buf, StreamCodecs.VEC_3D_APPROX);
			NetworkUtil.writeOptional(packet.item, buf, ItemStack.STREAM_CODEC);
		}

		@Override
		public ItemBreakVisualsPacket decode(RegistryFriendlyByteBuf buf) {
			return new ItemBreakVisualsPacket(
					buf.readInt(), 
					NetworkUtil.readOptional(buf, StreamCodecs.VEC_3D_APPROX),
					NetworkUtil.readOptional(buf, ItemStack.STREAM_CODEC));
		}

		@Override
		public void handle(ItemBreakVisualsPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			ItemStack item = payload.item.orElseGet(() -> {
				if (entity instanceof ItemEntity itemEntity) {
					return itemEntity.getItem();
				}
				return null;
			});
			Vec3 pos = payload.pos.orElseGet(() -> entity != null ? entity.getBoundingBox().getCenter() : null);
			if (item != null && pos != null && !item.isEmpty()) {
				Level clientLevel = ClientProxy.getClientWorld();
	    		RandomSource random = clientLevel.random;
	    		if (entity == null || !entity.isSilent()) {
	    			clientLevel.playLocalSound(pos.x, pos.y, pos.z, 
	    					item.getBreakingSound(), entity != null ? entity.getSoundSource() : SoundSource.AMBIENT,
	    					0.8F, 0.8F + random.nextFloat() * 0.4F, false);
	    		}

	    		for (int i = 0; i < 25; i++) {
	    			float xRot = (random.nextFloat() - 0.5f) * (float) Math.PI;
	    			float yRot = random.nextFloat() * (float) Math.PI * 2;
	    			Vec3 particleSpeed = new Vec3(
	    					(random.nextFloat() - 0.5) * 0.05, 
	    					Math.random() * 0.1 + 0.1, 
	    					0.0);
	    			particleSpeed = particleSpeed.xRot(xRot);
	    			particleSpeed = particleSpeed.yRot(yRot);
	    			
	    			Vec3 particlePos = new Vec3(
	    					(random.nextFloat() - 0.5) * 0.15, 
	    					(-random.nextFloat()) * 0.6 - 0.3, 
	    					0.3);
	    			particlePos = particlePos.xRot(yRot);
	    			particlePos = particlePos.yRot(yRot);
	    			particlePos = particlePos.add(pos.x, pos.y, pos.z);
	    			clientLevel.addParticle(new ItemParticleOption(ParticleTypes.ITEM, item), 
	    					particlePos.x, particlePos.y, particlePos.z, 
	    					particleSpeed.x, particleSpeed.y + 0.05, particleSpeed.z);
	    		}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
