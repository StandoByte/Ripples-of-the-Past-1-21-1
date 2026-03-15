package com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.ModdedContainerClickType;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClExtendedContainerClickPacket implements CustomPacketPayload {
	private static final int MAX_SLOT_COUNT = 128;
	private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<ItemStack>> SLOTS_STREAM_CODEC = ByteBufCodecs.map(
			Int2ObjectOpenHashMap::new, ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue), ItemStack.OPTIONAL_STREAM_CODEC, MAX_SLOT_COUNT);
	/** The id of the window which was clicked. 0 for player inventory. */
	private final int containerId;
	private final boolean isExternalContainer;
	private final int stateId;
	/** Id of the clicked slot */
	private final int slotNum;
	/** Button used */
	private final int buttonNum;
	/** Inventory operation mode */
	private final ModdedContainerClickType clickType;
	private final ItemStack carriedItem;
	private final Int2ObjectMap<ItemStack> changedSlots;

	public ClExtendedContainerClickPacket(int containerId, boolean isExternalContainer, 
			int stateId, int slotNum, int buttonNum, 
			ModdedContainerClickType clickType, 
			ItemStack carriedItem, Int2ObjectMap<ItemStack> changedSlots) {
		this.containerId = containerId;
		this.isExternalContainer = isExternalContainer;
		this.stateId = stateId;
		this.slotNum = slotNum;
		this.buttonNum = buttonNum;
		this.clickType = clickType;
		this.carriedItem = carriedItem;
		this.changedSlots = Int2ObjectMaps.unmodifiable(changedSlots);
	}



	private static CustomPacketPayload.Type<ClExtendedContainerClickPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<ClExtendedContainerClickPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClExtendedContainerClickPacket> type() {
			return type;
		}

		@Override
		public void encode(ClExtendedContainerClickPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeByte(packet.containerId);
			buf.writeBoolean(packet.isExternalContainer);
			buf.writeVarInt(packet.stateId);
			buf.writeShort(packet.slotNum);
			buf.writeByte(packet.buttonNum);
			buf.writeEnum(packet.clickType);
			SLOTS_STREAM_CODEC.encode(buf, packet.changedSlots);
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.carriedItem);
		}

		@Override
		public ClExtendedContainerClickPacket decode(RegistryFriendlyByteBuf buf) {
			int containerId = buf.readByte();
			boolean isExternalContainer = buf.readBoolean();
			int stateId = buf.readVarInt();
			int slotNum = buf.readShort();
			int buttonNum = buf.readByte();
			ModdedContainerClickType clickType = buf.readEnum(ModdedContainerClickType.class);
			Int2ObjectMap<ItemStack> changedSlots = Int2ObjectMaps.unmodifiable(SLOTS_STREAM_CODEC.decode(buf));
			ItemStack carriedItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
			return new ClExtendedContainerClickPacket(containerId, isExternalContainer, stateId, slotNum, buttonNum, clickType, carriedItem, changedSlots);
		}

		@Override
		public void handle(ClExtendedContainerClickPacket payload, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			player.resetLastActionTime();
			
			AbstractContainerMenu containerMenu;
			if (payload.isExternalContainer) {
				containerMenu = PlayerExternalContainers.get(player).getContainer(payload.containerId);
			}
			else {
				containerMenu = player.containerMenu;
				if (containerMenu.containerId != payload.containerId) {
					JojoMod.getLogger().debug("Player {} interacted with mismatching menu {}. Sent id {}, player has {}.", player, containerMenu, payload.containerId, containerMenu.containerId);
					return;
				}
			}
			
			if (containerMenu != null) {
				if (player.isSpectator()) {
					containerMenu.sendAllDataToRemote();
				}
				else if (!containerMenu.stillValid(player)) {
					JojoMod.getLogger().debug("Player {} interacted with invalid menu {}", player, containerMenu);
				}
				else {
					if (!containerMenu.isValidSlotIndex(payload.slotNum)) {
						JojoMod.getLogger().debug("Player {} clicked invalid slot index: {}, available slots: {}", player.getName(), payload.slotNum, containerMenu.slots.size());
					}
					else {
						boolean syncFullState = payload.stateId != containerMenu.getStateId();
						containerMenu.suppressRemoteUpdates();
						PlayerExternalContainers.click(containerMenu, payload.slotNum, payload.buttonNum, payload.clickType, player);

						for (Entry<ItemStack> entry : Int2ObjectMaps.fastIterable(payload.changedSlots)) {
							containerMenu.setRemoteSlotNoCopy(entry.getIntKey(), entry.getValue());
						}

						containerMenu.setRemoteCarried(payload.carriedItem);
						containerMenu.resumeRemoteUpdates();
						if (syncFullState) {
							containerMenu.broadcastFullState();
						}
						else {
							containerMenu.broadcastChanges();
						}
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
