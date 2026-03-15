package com.github.standobyte.jojo.mechanics.clothes.sewing;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClSetSewingMachineItemPacket(Holder<ClothesSet> clothesSet) implements CustomPacketPayload {
	
	
	private static CustomPacketPayload.Type<ClSetSewingMachineItemPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<ClSetSewingMachineItemPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClSetSewingMachineItemPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClSetSewingMachineItemPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ClSetSewingMachineItemPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.holderRegistry(JojoRegistries.CLOTHES_SETS_REG_KEY), ClSetSewingMachineItemPacket::clothesSet,
				ClSetSewingMachineItemPacket::new);

		@Override
		public void handle(ClSetSewingMachineItemPacket payload, IPayloadContext context) {
			Player player = context.player();
			if (player.containerMenu instanceof SewingMachineContainer container) {
				container.craftingSlots.fillFrom(payload.clothesSet);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
