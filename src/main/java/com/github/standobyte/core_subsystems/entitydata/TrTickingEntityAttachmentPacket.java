package com.github.standobyte.core_subsystems.entitydata;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.core.NotYetImplemented;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrTickingEntityAttachmentPacket implements CustomPacketPayload {
	private final AttachmentType attachmentType;
	private final PacketType packetType;
	private final int userId;
	private final int effectId;
	private final EntityAttachmentType<?> effectFactory;
	private final TickingEntityAttachment effect;
	private final boolean isUser;
	private final FriendlyByteBuf buf;

	public static TrTickingEntityAttachmentPacket add(AttachmentType type, TickingEntityAttachment effect, boolean sentToOwner) {
		return new TrTickingEntityAttachmentPacket(type, PacketType.ADD, effect.getEntity().getId(), effect.getId(), 
				effect.effectType, effect, sentToOwner, null);
	}

	public static TrTickingEntityAttachmentPacket remove(AttachmentType type, TickingEntityAttachment effect) {
		return new TrTickingEntityAttachmentPacket(type, PacketType.REMOVE, effect.getEntity().getId(), effect.getId(), 
				effect.effectType, effect, false, null);
	}

	public static TrTickingEntityAttachmentPacket updateTarget(StandEffectInstance effect) {
		return new TrTickingEntityAttachmentPacket(AttachmentType.STAND_EFFECT, PacketType.UPDATE_TARGET, effect.getEntity().getId(), effect.getId(), 
				effect.effectType, effect, false, null);
	}

	private TrTickingEntityAttachmentPacket(AttachmentType attachmentType, PacketType packetType, int userId, int effectId, 
			EntityAttachmentType<?> effectFactory, TickingEntityAttachment effect, boolean isUser, FriendlyByteBuf buf) {
		this.attachmentType = attachmentType;
		this.packetType = packetType;
		this.userId = userId;
		this.effectId = effectId;
		this.effectFactory = effectFactory;
		this.effect = effect;
		this.isUser = isUser;
		this.buf = buf;
	}



	private static CustomPacketPayload.Type<TrTickingEntityAttachmentPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<TrTickingEntityAttachmentPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrTickingEntityAttachmentPacket> type() {
			return type;
		}

		@Override
		public void encode(TrTickingEntityAttachmentPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.attachmentType);
			buf.writeEnum(packet.packetType);
			switch (packet.packetType) {
				case ADD:
					buf.writeInt(packet.userId);
					buf.writeInt(packet.effectId);
					NetworkUtil.registryCodec(JojoRegistries.ENTITY_ATTACHMENTS_REG_KEY).encode(buf, packet.effectFactory);
					buf.writeBoolean(packet.isUser);
	
					buf.writeVarInt(packet.effect.tickCount);
					packet.effect.writeAdditionalPacketData(buf, packet.isUser);
					break;
				case REMOVE:
					buf.writeInt(packet.userId);
					buf.writeInt(packet.effectId);
					break;
				case UPDATE_TARGET:
					buf.writeInt(packet.userId);
					buf.writeInt(packet.effectId);
					buf.writeInt(((StandEffectInstance) packet.effect).getTargetEntityId());
					break;
			}
		}

		@Override
		public TrTickingEntityAttachmentPacket decode(RegistryFriendlyByteBuf buf) {
			AttachmentType attachmentType = buf.readEnum(AttachmentType.class);
			PacketType packetType = buf.readEnum(PacketType.class);
			return switch (packetType) {
				case ADD -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					EntityAttachmentType<?> effectFactory = NetworkUtil.registryCodec(JojoRegistries.ENTITY_ATTACHMENTS_REG_KEY).decode(buf);
					boolean isUser = buf.readBoolean();
					yield new TrTickingEntityAttachmentPacket(attachmentType, packetType, userId, effectId, 
							effectFactory, null, isUser, NetworkUtil.extraPacketData(buf));
				}
				case REMOVE -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					yield new TrTickingEntityAttachmentPacket(attachmentType, packetType, userId, effectId, 
							null, null, false, null);
				}
				case UPDATE_TARGET -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					yield new TrTickingEntityAttachmentPacket(attachmentType, packetType, userId, effectId, 
							null, null, false, NetworkUtil.extraPacketData(buf));
				}
			};
		}

		@Override
		public void handle(TrTickingEntityAttachmentPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.userId);
			if (entity instanceof LivingEntity livingEntity) {
				StandPower stand = StandPower.get(livingEntity);
				if (payload.attachmentType == AttachmentType.STAND_EFFECT && stand == null) return;

				switch (payload.packetType) {
					case ADD:
						TickingEntityAttachment newEffect = payload.effectFactory.create(entity.level()).withId(payload.effectId);
						newEffect.withEntity(livingEntity);
						newEffect.tickCount = payload.buf.readVarInt();
						newEffect.readAdditionalPacketData(payload.buf, payload.isUser);
						
						switch (payload.attachmentType) {
							case STAND_EFFECT -> {
								stand.userStandEffects.addEffect((StandEffectInstance) newEffect);
							}
							case OTHER -> {
								throw new NotYetImplemented();
							}
						}
						break;
					case REMOVE:
						switch (payload.attachmentType) {
							case STAND_EFFECT -> {
								UserStandEffects effects = stand.userStandEffects;
								effects.removeEffect(effects.getById(payload.effectId));
							}
							case OTHER -> {
								throw new NotYetImplemented();
							}
						}
					case UPDATE_TARGET:
						StandEffectInstance effect = stand.userStandEffects.getById(payload.effectId);
						if (effect != null) {
							int targetEntityId = payload.buf.readInt();
							effect.withTargetEntityId(targetEntityId);
						}
				}
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

	private enum PacketType {
		ADD,
		REMOVE,
		UPDATE_TARGET
	}
	
	public enum AttachmentType {
		STAND_EFFECT,
		OTHER
	}
}
