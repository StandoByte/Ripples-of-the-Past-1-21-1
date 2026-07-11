package com.github.standobyte.jojo.mechanics.voiceline;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayVoiceLinePacket(int entityId, Holder<SoundEvent> soundEvent, 
		Holder<StoryCharacter> character, @Nullable Holder<StoryPart> storyPart,
		@Nullable ResourceLocation standType, boolean canInterrupt, 
		SoundSource soundCategory, float volume, float pitch) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<PlayVoiceLinePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<PlayVoiceLinePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<PlayVoiceLinePacket> type() {
			return type;
		}

		@Override
		public void encode(PlayVoiceLinePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			SoundEvent.STREAM_CODEC.encode(buf, packet.soundEvent);
			StoryCharacter.STREAM_CODEC.encode(buf, packet.character);
			NetworkUtil.writeOptionally(packet.storyPart, buf, StoryPart.STREAM_CODEC);
			NetworkUtil.writeOptionally(packet.standType, buf, ResourceLocation.STREAM_CODEC);
			buf.writeBoolean(packet.canInterrupt);
			buf.writeEnum(packet.soundCategory);
			buf.writeFloat(packet.volume);
			buf.writeFloat(packet.pitch);
		}

		@Override
		public PlayVoiceLinePacket decode(RegistryFriendlyByteBuf buf) {
			return new PlayVoiceLinePacket(
					buf.readInt(),
					SoundEvent.STREAM_CODEC.decode(buf),
					StoryCharacter.STREAM_CODEC.decode(buf),
					NetworkUtil.readOptional(buf, StoryPart.STREAM_CODEC).orElse(null),
					NetworkUtil.readOptional(buf, ResourceLocation.STREAM_CODEC).orElse(null),
					buf.readBoolean(),
					buf.readEnum(SoundSource.class),
					buf.readFloat(),
					buf.readFloat());
		}

		@Override
		public void handle(PlayVoiceLinePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity != null) {
				SoundEvent sound = payload.soundEvent.value();
				if (sound != null) {
					VoiceLineClientSide.play(entity, payload.soundEvent, 
							payload.character, payload.storyPart, 
							payload.standType, payload.canInterrupt, 
							payload.soundCategory, payload.volume, payload.pitch);
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
