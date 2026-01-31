package com.github.standobyte.jojo.core.packet.fromclient;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId.AbilityInputNetwork;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.input.AbilityInput;
import com.github.standobyte.jojo.powersystem.ability.input.AbilityInput.InputEventType;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClAbilityInputPacket implements CustomPacketPayload {
	private final short key;
	private final InputEventType inputEvent;
	
	private final Ability abilityEncode;
	private final AbilityInputNetwork abilityDecoded;
	private final float timeTookToResolve;

	private final LivingEntity clUser;
	private FriendlyByteBuf extraData;
	
	public static ClAbilityInputPacket keyPress(short key, LivingEntity user, 
			Ability baseAbility, InputEventType inputEvent, float timeTookToResolve) {
		return new ClAbilityInputPacket(key, inputEvent, user, baseAbility, null, timeTookToResolve);
	}
	
	public static ClAbilityInputPacket releaseHold(short key) {
		return new ClAbilityInputPacket(key, InputEventType.RELEASE, null, null, null, 0);
	}
	
	private ClAbilityInputPacket(short key, InputEventType inputEvent, LivingEntity user, 
			@Nullable Ability abilityEncode, @Nullable AbilityInputNetwork abilityDecoded, float timeTookToResolve) {
		this.key = key;
		this.inputEvent = inputEvent;
		this.clUser = user;
		this.abilityEncode = abilityEncode;
		this.abilityDecoded = abilityDecoded;
		this.timeTookToResolve = timeTookToResolve;
	}

	
	
	private static CustomPacketPayload.Type<ClAbilityInputPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClAbilityInputPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClAbilityInputPacket> type() {
			return type;
		}

		@Override
		public void encode(ClAbilityInputPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeShort(packet.key);
			buf.writeEnum(packet.inputEvent);
			if (packet.inputEvent != InputEventType.RELEASE) {
				AbilityInputNetwork.encodeInput(buf, packet.clUser, packet.abilityEncode);
				if (packet.abilityEncode != null) {
					buf.writeFloat(packet.timeTookToResolve);
					packet.abilityEncode.writeExtraInput(buf, packet.clUser, true);
				}	
			}
		}

		@Override
		public ClAbilityInputPacket decode(RegistryFriendlyByteBuf buf) {
			short key = buf.readShort();
			InputEventType inputEvent = buf.readEnum(InputEventType.class);
			return switch (inputEvent) {
				case RELEASE -> ClAbilityInputPacket.releaseHold(key);
				default -> {
					AbilityInputNetwork ability = AbilityInputNetwork.decodeInput(buf);
					float timeTookToResolve = ability != null ? buf.readFloat() : 0;
					
					ClAbilityInputPacket packet = new ClAbilityInputPacket(key, inputEvent, null, null, ability, timeTookToResolve);
					packet.extraData = NetworkUtil.extraPacketData(buf);
					yield packet;
				}
			};
		}

		@Override
		public void handle(ClAbilityInputPacket payload, IPayloadContext context) {
			Player player = context.player();
			switch (payload.inputEvent) {
				case PRESS_CLICK, PRESS_HOLD -> {
					Ability baseAbility = payload.abilityDecoded != null ? payload.abilityDecoded.getAbility(player, null) : null;
					if (baseAbility != null) {
						Power<?> power = baseAbility.getUserPower(player);
						if (power != null) {
							AbilityConditionCheck ability = power.updateAvailableMoves().getAbilityResolved(baseAbility);
							if (AbilityInput.withConditionCheck(ability, player)) {
								AbilityInput.keyPress(payload.key, ability.ability, player, payload.extraData, 
										payload.inputEvent.inputMethod, payload.timeTookToResolve, BufferingState.clickCanBuffer(), baseAbility.abilityId);
							}
						}
					}
				}
				case RELEASE -> AbilityInput.keyRelease(payload.key, player);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
