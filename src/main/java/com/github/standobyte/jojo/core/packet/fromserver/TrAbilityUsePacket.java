package com.github.standobyte.jojo.core.packet.fromserver;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId.AbilityInputNetwork;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.AbilityInput;
import com.github.standobyte.jojo.powersystem.ability.input.AbilityInput.InputEventType;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrAbilityUsePacket implements CustomPacketPayload {
	private final int entityId;
	private final short key;
	private final InputEventType inputType;
	private final Ability abilityEncode;
	private final AbilityInputNetwork abilityDecoded;
	private final float timeTookToResolve;
	private final LivingEntity senderUser;
	private RegistryFriendlyByteBuf extraData;
	
	public static TrAbilityUsePacket keyPress(int entityId, short key, 
			Ability ability, InputMethod inputMethod, float timeTookToResolve, LivingEntity serverUser) {
		InputEventType inputEvent = switch (inputMethod) {
			case CLICK -> InputEventType.PRESS_CLICK;
			case HOLD -> InputEventType.PRESS_HOLD;
		};
		return new TrAbilityUsePacket(entityId, key, inputEvent, ability, null, timeTookToResolve, serverUser);
	}
	
	public static TrAbilityUsePacket releaseHold(int entityId, short key) {
		return new TrAbilityUsePacket(entityId, key, InputEventType.RELEASE, null, null, 0, null);
	}
	
	private TrAbilityUsePacket(int entityId, short key, InputEventType inputType, 
			@Nullable Ability abilityEncode, @Nullable AbilityInputNetwork abilityDecoded, 
			float timeTookToResolve, LivingEntity serverUser) {
		this.entityId = entityId;
		this.key = key;
		this.inputType = inputType;
		this.abilityEncode = abilityEncode;
		this.abilityDecoded = abilityDecoded;
		this.timeTookToResolve = timeTookToResolve;
		this.senderUser = serverUser;
	}

	
	
	private static CustomPacketPayload.Type<TrAbilityUsePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrAbilityUsePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrAbilityUsePacket> type() {
			return type;
		}

		@Override
		public void encode(TrAbilityUsePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeShort(packet.key);
			buf.writeEnum(packet.inputType);
			if (packet.inputType != InputEventType.RELEASE) {
				AbilityInputNetwork.encodeInput(buf, packet.senderUser, packet.abilityEncode);
				buf.writeFloat(packet.timeTookToResolve);
				if (packet.abilityEncode != null) {
					packet.abilityEncode.writeExtraInput(buf, packet.senderUser, false);
				}	
			}
		}

		@Override
		public TrAbilityUsePacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			short key = buf.readShort();
			InputEventType inputType = buf.readEnum(InputEventType.class);
			return switch (inputType) {
				case RELEASE -> TrAbilityUsePacket.releaseHold(entityId, key);
				default -> {
					AbilityInputNetwork ability = AbilityInputNetwork.decodeInput(buf);
					float timeTookToResolve = buf.readFloat();
					
					TrAbilityUsePacket packet = new TrAbilityUsePacket(entityId, key, inputType, null, ability, timeTookToResolve, null);
					packet.extraData = NetworkUtil.extraPacketData(buf, buf.registryAccess());
					yield packet;
				}
			};
		}

		@Override
		public void handle(TrAbilityUsePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity user) {
				switch (payload.inputType) {
					case PRESS_CLICK, PRESS_HOLD -> {
						Ability ability = payload.abilityDecoded.getAbility(user, null);
						AbilityInput.keyPress(payload.key, ability, user, payload.extraData, 
								payload.inputType.inputMethod, payload.timeTookToResolve, BufferingState.clickOnly(), null);
					}
					case RELEASE -> {
						AbilityInput.keyRelease(payload.key, user);
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
