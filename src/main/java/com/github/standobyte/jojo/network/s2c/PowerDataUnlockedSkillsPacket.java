package com.github.standobyte.jojo.network.s2c;

import java.util.Collection;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PowerDataUnlockedSkillsPacket implements CustomPacketPayload {
	private final int entityId;
	private final PowerClass<?> powerClass;
	private final Collection<String> unlockedSkills;
	
	public PowerDataUnlockedSkillsPacket(int entityId, PowerClass<?> powerClass, Collection<String> unlockedSkills) {
		this.entityId = entityId;
		this.powerClass = powerClass;
		this.unlockedSkills = unlockedSkills;
	}
	
	
	private static CustomPacketPayload.Type<PowerDataUnlockedSkillsPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<PowerDataUnlockedSkillsPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<PowerDataUnlockedSkillsPacket> type() {
			return type;
		}

		@Override
		public void encode(PowerDataUnlockedSkillsPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			PowerClass.NETWORK_CODEC.encode(buf, packet.powerClass);
			NetworkUtil.writeCollection(buf, packet.unlockedSkills, FriendlyByteBuf::writeUtf);
		}

		@Override
		public PowerDataUnlockedSkillsPacket decode(RegistryFriendlyByteBuf buf) {
			PowerDataUnlockedSkillsPacket packet = new PowerDataUnlockedSkillsPacket(
					buf.readInt(), 
					PowerClass.NETWORK_CODEC.decode(buf), 
					NetworkUtil.readCollection(buf, FriendlyByteBuf::readUtf));
			return packet;
		}

		@Override
		public void handle(PowerDataUnlockedSkillsPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				Power<?> power = payload.powerClass.attachGet(living);
				if (power != null) {
					PowerData perTypePlayerData = power.getCurTypeData();
					if (perTypePlayerData != null) {
						perTypePlayerData.clSetUnlockedSkills(payload.unlockedSkills);
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
