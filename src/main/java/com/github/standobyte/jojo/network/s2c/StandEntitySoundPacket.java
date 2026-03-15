package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StandEntitySoundPacket(int entityId, Holder<SoundEvent> sound, boolean onlyForStandUsers, 
		float volume, float pitch) implements CustomPacketPayload {
	
	public StandEntitySoundPacket(StandEntity standEntity, Holder<SoundEvent> sound, float volume, float pitch) {
		this(standEntity.getId(), sound, standEntity.onlyVisibleToStandUsers(), volume, pitch);
	}
	
	private static CustomPacketPayload.Type<StandEntitySoundPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<StandEntitySoundPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<StandEntitySoundPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, StandEntitySoundPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, StandEntitySoundPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, StandEntitySoundPacket::entityId,
				SoundEvent.STREAM_CODEC, StandEntitySoundPacket::sound,
				ByteBufCodecs.BOOL, StandEntitySoundPacket::onlyForStandUsers,
				ByteBufCodecs.FLOAT, StandEntitySoundPacket::volume,
				ByteBufCodecs.FLOAT, StandEntitySoundPacket::pitch,
				StandEntitySoundPacket::new);

		@Override
		public void handle(StandEntitySoundPacket payload, IPayloadContext context) {
			if (!payload.onlyForStandUsers || ClientGlobals.canHearStands) {
				SoundEvent soundEvent = payload.sound.value();
				if (soundEvent != null) {
					Entity entity = ClientProxy.getEntityById(payload.entityId);
					if (entity instanceof StandEntity stand) {
						EntityLingeringSoundInstance sound = new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
								soundEvent, stand), 
								stand.getSoundSource(), payload.volume, payload.pitch, stand, stand.level());
						ClientsideSoundsHelper.playNonVanillaClassSound(sound);
					}
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
