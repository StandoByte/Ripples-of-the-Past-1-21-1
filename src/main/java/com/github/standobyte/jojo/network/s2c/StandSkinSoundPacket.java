package com.github.standobyte.jojo.network.s2c;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojo.util.functions_network.StreamCodecs;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StandSkinSoundPacket(Vec3 position, Holder<SoundEvent> sound, 
		ResourceLocation standType, Optional<ResourceLocation> standSkin, 
		SoundSource soundCategory, float volume, float pitch) implements CustomPacketPayload {
	
	public static StandSkinSoundPacket play(Vec3 position, Holder<SoundEvent> sound, 
			StandPower userPower, SoundSource soundCategory, float volume, float pitch) {
		StandInstance stand = userPower.getStandInstance().orElse(null);
		if (stand == null) {
			throw new IllegalArgumentException();
		}
		return new StandSkinSoundPacket(position, sound, 
				stand.getStandType().getId(), stand.getSelectedSkin(), 
				soundCategory, volume, pitch);
	}
	
	private static CustomPacketPayload.Type<StandSkinSoundPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<StandSkinSoundPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<StandSkinSoundPacket> type() {
			return type;
		}

		@Override
		public void encode(StandSkinSoundPacket packet, RegistryFriendlyByteBuf buf) {
			StreamCodecs.VEC_3D_APPROX.encode(buf, packet.position);
			SoundEvent.STREAM_CODEC.encode(buf, packet.sound);
			ResourceLocation.STREAM_CODEC.encode(buf, packet.standType);
			NetworkUtil.writeOptional(packet.standSkin, buf, ResourceLocation.STREAM_CODEC);
			buf.writeEnum(packet.soundCategory);
			buf.writeFloat(packet.volume);
			buf.writeFloat(packet.pitch);
		}

		@Override
		public StandSkinSoundPacket decode(RegistryFriendlyByteBuf buf) {
			return new StandSkinSoundPacket(
					StreamCodecs.VEC_3D_APPROX.decode(buf),
					SoundEvent.STREAM_CODEC.decode(buf),
					ResourceLocation.STREAM_CODEC.decode(buf),
					NetworkUtil.readOptional(buf, ResourceLocation.STREAM_CODEC),
					buf.readEnum(SoundSource.class),
					buf.readFloat(),
					buf.readFloat());
		}

		@Override
		public void handle(StandSkinSoundPacket payload, IPayloadContext context) {
			// the server checks if the player can hear stands
			SoundEvent sound = payload.sound.value();
			if (sound != null) {
				Level level = ClientProxy.getClientWorld();
				Vec3 pos = payload.position;
				level.playLocalSound(pos.x, pos.y, pos.z, ClientsideSoundsHelper.withStandSkin(
						sound, payload.standType, payload.standSkin), 
						payload.soundCategory, payload.volume, payload.pitch, false);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
