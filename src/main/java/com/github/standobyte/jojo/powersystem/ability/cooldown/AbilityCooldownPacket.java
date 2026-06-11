package com.github.standobyte.jojo.powersystem.ability.cooldown;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AbilityCooldownPacket implements CustomPacketPayload {
	
	static record Entry(AbilityId abilityId, int value, int totalCooldown) {
		
		void toBuf(FriendlyByteBuf buf) {
			buf.writeUtf(abilityId.toString());
			buf.writeVarInt(value);
			buf.writeVarInt(totalCooldown);
		}
		
		static Entry fromBuf(FriendlyByteBuf buf) {
			return new Entry(AbilityId.parse(buf.readUtf()), buf.readVarInt(), buf.readVarInt());
		}
	}
	
	private final int entityId;
	private final boolean resetAll;
	private final boolean remove;
	private final Collection<Entry> cooldowns;

	public static AbilityCooldownPacket singleCooldown(int entityId, AbilityId abilityId, int value) {
		return new AbilityCooldownPacket(entityId, false, false, Collections.singletonList(new Entry(abilityId, value, value)));
	}

	public static AbilityCooldownPacket removeCooldown(int entityId, AbilityId abilityId) {
		return new AbilityCooldownPacket(entityId, false, true, Collections.singletonList(new Entry(abilityId, 0, 0)));
	}

	public static AbilityCooldownPacket createForMultipleSync(int entityId) {
		return new AbilityCooldownPacket(entityId, false, false, new ArrayList<>());
	}

	public static AbilityCooldownPacket resetAll(int entityId) {
		return new AbilityCooldownPacket(entityId, true, false, Collections.emptyList());
	}

	private AbilityCooldownPacket(int entityId, boolean resetAll, boolean remove, Collection<Entry> cooldowns) {
		this.entityId = entityId;
		this.resetAll = resetAll;
		this.remove = remove;
		this.cooldowns = cooldowns;
	}
	
	public void addCooldown(AbilityId abilityId, int value, int totalCooldown) {
		cooldowns.add(new Entry(abilityId, value, totalCooldown));
	}



	private static CustomPacketPayload.Type<AbilityCooldownPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<AbilityCooldownPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<AbilityCooldownPacket> type() {
			return type;
		}

		@Override
		public void encode(AbilityCooldownPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeBoolean(packet.resetAll);
			if (!packet.resetAll) {
				buf.writeBoolean(packet.remove);
				NetworkUtil.writeOptionally(packet.cooldowns, buf, 
						(_buf, list) -> NetworkUtil.writeCollection(_buf, list, 
								(__buf, entry) -> entry.toBuf(__buf)));
			}
		}

		@Override
		public AbilityCooldownPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			boolean resetAll = buf.readBoolean();
			if (resetAll) {
				return resetAll(entityId);
			}
			boolean remove = buf.readBoolean();
			Collection<Entry> cooldowns = NetworkUtil.readOptional(buf, 
					(_buf) -> NetworkUtil.readCollection(_buf, Entry::fromBuf))
					.orElse(null);
			return new AbilityCooldownPacket(entityId, false, remove, cooldowns);
		}

		@Override
		public void handle(AbilityCooldownPacket payload, IPayloadContext context) {
			if (ClientProxy.getEntityById(payload.entityId) instanceof LivingEntity entity) {
				if (payload.resetAll) {
					AbilityCooldownTracker cooldowns = AbilityCooldownTracker.get(entity);
					if (cooldowns != null) {
						cooldowns.resetCooldowns();
					}
				}
				else {
					AbilityCooldownTracker cooldowns = AbilityCooldownTracker.getOrCreate(entity);
					for (Entry entry : payload.cooldowns) {
						if (entry.abilityId != null) {
							if (payload.remove) {
								cooldowns.removeCooldown(entry.abilityId);
							}
							else {
								cooldowns.setCooldown(entry.abilityId, entry.value, entry.totalCooldown);
							}
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
