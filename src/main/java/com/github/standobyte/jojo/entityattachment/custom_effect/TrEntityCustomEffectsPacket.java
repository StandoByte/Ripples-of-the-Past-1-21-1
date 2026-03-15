package com.github.standobyte.jojo.entityattachment.custom_effect;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrEntityCustomEffectsPacket implements CustomPacketPayload {
	private final EntityCustomEffectsClass effectsClass;
	private final PacketType packetType;
	private final int userId;
	private final int effectId;
	private final EntityCustomEffectType<?> effectFactory;
	private final EntityCustomEffect effect;
	private final boolean isUser;
	private final FriendlyByteBuf buf;

	public static TrEntityCustomEffectsPacket add(EntityCustomEffectsClass type, EntityCustomEffect effect, boolean sentToOwner) {
		return new TrEntityCustomEffectsPacket(type, PacketType.ADD, effect.getEntity().getId(), effect.getId(), 
				effect.effectType, effect, sentToOwner, null);
	}

	public static TrEntityCustomEffectsPacket remove(EntityCustomEffectsClass type, EntityCustomEffect effect) {
		return new TrEntityCustomEffectsPacket(type, PacketType.REMOVE, effect.getEntity().getId(), effect.getId(), 
				effect.effectType, effect, false, null);
	}

	public static TrEntityCustomEffectsPacket updateTarget(StandEffectInstance effect) {
		return new TrEntityCustomEffectsPacket(EntityCustomEffectsClass.STAND_EFFECT, PacketType.UPDATE_TARGET, effect.getEntity().getId(), effect.getId(), 
				effect.effectType, effect, false, null);
	}

	private TrEntityCustomEffectsPacket(EntityCustomEffectsClass effectsClass, PacketType packetType, int userId, int effectId, 
			EntityCustomEffectType<?> effectFactory, EntityCustomEffect effect, boolean isUser, FriendlyByteBuf buf) {
		this.effectsClass = effectsClass;
		this.packetType = packetType;
		this.userId = userId;
		this.effectId = effectId;
		this.effectFactory = effectFactory;
		this.effect = effect;
		this.isUser = isUser;
		this.buf = buf;
	}



	private static CustomPacketPayload.Type<TrEntityCustomEffectsPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<TrEntityCustomEffectsPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrEntityCustomEffectsPacket> type() {
			return type;
		}

		@Override
		public void encode(TrEntityCustomEffectsPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.effectsClass);
			buf.writeEnum(packet.packetType);
			switch (packet.packetType) {
				case ADD:
					buf.writeInt(packet.userId);
					buf.writeInt(packet.effectId);
					NetworkUtil.registryCodec(JojoRegistries.ENTITY_CUSTOM_EFFECTS_REG_KEY).encode(buf, packet.effectFactory);
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
		public TrEntityCustomEffectsPacket decode(RegistryFriendlyByteBuf buf) {
			EntityCustomEffectsClass effectsClass = buf.readEnum(EntityCustomEffectsClass.class);
			PacketType packetType = buf.readEnum(PacketType.class);
			return switch (packetType) {
				case ADD -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					EntityCustomEffectType<?> effectFactory = NetworkUtil.registryCodec(JojoRegistries.ENTITY_CUSTOM_EFFECTS_REG_KEY).decode(buf);
					boolean isUser = buf.readBoolean();
					yield new TrEntityCustomEffectsPacket(effectsClass, packetType, userId, effectId, 
							effectFactory, null, isUser, NetworkUtil.extraPacketData(buf));
				}
				case REMOVE -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					yield new TrEntityCustomEffectsPacket(effectsClass, packetType, userId, effectId, 
							null, null, false, null);
				}
				case UPDATE_TARGET -> {
					int userId = buf.readInt();
					int effectId = buf.readInt();
					yield new TrEntityCustomEffectsPacket(effectsClass, packetType, userId, effectId, 
							null, null, false, NetworkUtil.extraPacketData(buf));
				}
			};
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void handle(TrEntityCustomEffectsPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.userId);
			EntityCustomEffectsMap effects = payload.effectsClass.get(entity, payload.packetType == PacketType.ADD);
			if (effects == null) return;

			switch (payload.packetType) {
				case ADD:
					EntityCustomEffect newEffect = payload.effectFactory.create(entity.level()).withId(payload.effectId);
					newEffect.withEntity(entity);
					newEffect.tickCount = payload.buf.readVarInt();
					newEffect.readAdditionalPacketData(payload.buf, payload.isUser);
					effects.addEffect(newEffect);
					break;
				case REMOVE:
					effects.removeEffect(payload.effectId);
				case UPDATE_TARGET:
					StandEffectInstance effect = (StandEffectInstance) effects.getById(payload.effectId);
					if (effect != null) {
						int targetEntityId = payload.buf.readInt();
						effect.withTargetEntityId(targetEntityId);
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
	
}
