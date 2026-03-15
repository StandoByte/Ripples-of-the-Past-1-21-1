package com.github.standobyte.jojo.subsystems.entity_useitem;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState.HeldInputEntry;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClStandClickPacket(HitResultSync target, short internalKeyId, InteractionHand... hand) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClStandClickPacket> type;
	
	public ClStandClickPacket(HitResult target, short internalKeyId, InteractionHand... hand) {
		this(new HitResultSync(target), internalKeyId, hand);
	}

	public static class Handler implements PacketsRegister.PacketOGHandler<ClStandClickPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClStandClickPacket> type() {
			return type;
		}

		@Override
		public void encode(ClStandClickPacket packet, RegistryFriendlyByteBuf buf) {
			HitResultSync.STREAM_CODEC.encode(buf, packet.target);
			buf.writeShort(packet.internalKeyId);
			buf.writeVarInt(packet.hand.length);
			for (InteractionHand hand : packet.hand) {
				buf.writeEnum(hand);
			}
		}
		
		@Override
		public ClStandClickPacket decode(RegistryFriendlyByteBuf buf) {
			HitResultSync target = HitResultSync.STREAM_CODEC.decode(buf);
			short internalKeyId = buf.readShort();
			InteractionHand[] hand = new InteractionHand[buf.readVarInt()];
			for (int i = 0; i < hand.length; i++) {
				hand[i] = buf.readEnum(InteractionHand.class);
			}
			return new ClStandClickPacket(target, internalKeyId, hand);
		}

		@Override
		public void handle(ClStandClickPacket packet, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			StandEntity standEntity = StandUtil.getSummonedStand(player);
			if (standEntity != null) {
				boolean wasUsingItem = standEntity.isUsingItem();
				
				HitResult target = packet.target().resolveEntity(player.level());
				ServerSideLivingClick.rightClick(standEntity, player, target);
				
				if (!wasUsingItem && standEntity.isUsingItem()) {
					HeldInput action = LivingComponentAction.getComponent(standEntity).setAction(
							new VanillaItemUseAsAction.ItemUsingInstance(), SyncType.TRACKING_AND_SELF);
					if (action != null) {
						EntityActionInputState inputHandler = player.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
						if (inputHandler != null) {
							short internalKeyId = packet.internalKeyId;
							HeldInputEntry heldInput = new HeldInputEntry(internalKeyId, action);
							inputHandler.heldKeys.put(internalKeyId, heldInput);
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
