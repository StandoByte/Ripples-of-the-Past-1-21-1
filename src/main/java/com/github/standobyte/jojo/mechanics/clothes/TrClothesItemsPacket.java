package com.github.standobyte.jojo.mechanics.clothes;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrClothesItemsPacket(int entityId, List<Pair<ClothesSlotType, ItemStack>> slots) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrClothesItemsPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<TrClothesItemsPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrClothesItemsPacket> type() {
			return type;
		}

		@Override
		public void encode(TrClothesItemsPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeVarInt(packet.entityId);
			int size = packet.slots.size();

			for (int i = 0; i < size; i++) {
				Pair<ClothesSlotType, ItemStack> pair = packet.slots.get(i);
				ClothesSlotType slot = pair.getFirst();
				boolean notLast = i != size - 1;
				int slotI = slot.ordinal();
				buf.writeByte(notLast ? slotI | -128 : slotI);
				ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, pair.getSecond());
			}
		}

		@Override
		public TrClothesItemsPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readVarInt();
			List<Pair<ClothesSlotType, ItemStack>> slots = new ArrayList<>();

			int i;
			do {
				i = buf.readByte();
				ClothesSlotType slot = ClothesSlotType.values()[i & 127];
				ItemStack item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
				slots.add(Pair.of(slot, item));
			} while ((i & -128) != 0);
			
			return new TrClothesItemsPacket(entityId, slots);
		}


		@Override
		public void handle(TrClothesItemsPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				EntityClothesInventory livingClothes = living.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
				for (var slot : payload.slots()) {
					livingClothes.setItemSlot(slot.getFirst(), slot.getSecond());
				}
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
