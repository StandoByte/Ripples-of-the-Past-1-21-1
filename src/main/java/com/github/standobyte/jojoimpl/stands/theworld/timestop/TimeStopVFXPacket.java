package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.shader.ModShaders;
import com.github.standobyte.jojo.client.shader.core.ManualInitPostChain;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.util.functions_network.StreamCodecs;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopClientState;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopShader;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TimeStopVFXPacket(ResourceLocation standId, Optional<ResourceLocation> selectedSkin, Optional<Vec3> pos, int userId, TimeStopVFXState state) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TimeStopVFXPacket> type;
	
	public enum TimeStopVFXState {
		STARTUP,
		ACTIVE,
		FADE_OUT
	}
	
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TimeStopVFXPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TimeStopVFXPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, TimeStopVFXPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TimeStopVFXPacket> STREAM_CODEC = StreamCodec.composite(
				ResourceLocation.STREAM_CODEC, TimeStopVFXPacket::standId,
				ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), TimeStopVFXPacket::selectedSkin,
				StreamCodecs.VEC_3D_APPROX.apply(ByteBufCodecs::optional), TimeStopVFXPacket::pos, 
				ByteBufCodecs.INT, TimeStopVFXPacket::userId, 
				NeoForgeStreamCodecs.enumCodec(TimeStopVFXState.class), TimeStopVFXPacket::state, 
				TimeStopVFXPacket::new);

		@Override
		public void handle(TimeStopVFXPacket payload, IPayloadContext context) {
			TimeStopShader timeStopShader = ModShaders.getInstance().timeStop;
			StandSkin standSkin = StandSkinsLoader.getInstance().getSkinFromId(payload.standId, payload.selectedSkin());
			if (standSkin != null) {
				// TODO play sound
				ManualInitPostChain vfx = standSkin.getShaderPostChain(TimeStopShader.SHADER_PATH);
				if (vfx != null) {
					timeStopShader.set(vfx);
				}
			}
			
			TimeStopClientState.partialTick = ClientUtil.partialTick();
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
