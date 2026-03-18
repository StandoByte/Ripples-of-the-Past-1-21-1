package com.github.standobyte.jojo.powersystem.skill;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClLearnSkillPacket implements CustomPacketPayload {
	public PowerClass<?> powerClass;
	public ResourceLocation powerType;
	public PacketType packetType;
	public String skillName;
	
	public static ClLearnSkillPacket learnSkill(PowerClass<?> powerClass, ResourceLocation powerType, String skillName) {
		return new ClLearnSkillPacket(powerClass, powerType, PacketType.LEARN, skillName);
	}
	
	public static ClLearnSkillPacket learnAll(PowerClass<?> powerClass, ResourceLocation powerType) {
		return new ClLearnSkillPacket(powerClass, powerType, PacketType.LEARN_ALL, null);
	}
	
	public static ClLearnSkillPacket resetAll(PowerClass<?> powerClass, ResourceLocation powerType) {
		return new ClLearnSkillPacket(powerClass, powerType, PacketType.RESET_ALL, null);
	}
	
	public ClLearnSkillPacket(PowerClass<?> powerClass, ResourceLocation powerType, PacketType packetType, String skillName) {
		this.powerClass = powerClass;
		this.powerType = powerType;
		this.packetType = packetType;
		this.skillName = skillName;
	}
	
	public static enum PacketType {
		LEARN,
		LEARN_ALL,
		RESET,
		RESET_ALL
	}
	
	
	private static CustomPacketPayload.Type<ClLearnSkillPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClLearnSkillPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClLearnSkillPacket> type() {
			return type;
		}

		@Override
		public void encode(ClLearnSkillPacket packet, RegistryFriendlyByteBuf buf) {
			PowerClass.NETWORK_CODEC.encode(buf, packet.powerClass);
			ResourceLocation.STREAM_CODEC.encode(buf, packet.powerType);
			buf.writeEnum(packet.packetType);
			switch (packet.packetType) {
				case LEARN_ALL, RESET_ALL -> {}
				default -> buf.writeUtf(packet.skillName);
			}
		}

		@Override
		public ClLearnSkillPacket decode(RegistryFriendlyByteBuf buf) {
			PowerClass<?> powerClass = PowerClass.NETWORK_CODEC.decode(buf);
			ResourceLocation powerType = ResourceLocation.STREAM_CODEC.decode(buf);
			PacketType packetType = buf.readEnum(PacketType.class);
			return switch (packetType) {
				case LEARN_ALL, RESET_ALL -> {
					yield new ClLearnSkillPacket(powerClass, powerType, packetType, null);
				}
				default -> {
					String skillName = buf.readUtf();
					yield new ClLearnSkillPacket(powerClass, powerType, packetType, skillName);
				}
			};
		}
		

		@Override
		public void handle(ClLearnSkillPacket payload, IPayloadContext context) {
			Player player = context.player();
			Power<?> power = payload.powerClass.get(player);
			if (power != null && power.hasPower() && power.getPowerType().getId().equals(payload.powerType)) {
				PowerData powerData = power.getCurTypeData();
				if (powerData != null) {
					switch (payload.packetType) {
						case LEARN -> {
							powerData.unlockSkill(power, payload.skillName);
						}
						case LEARN_ALL -> {
							// unlock all skills and sync
							for (var skillEntry : powerData.getAllSkills().entrySet()) {
								UnlockableSkill skill = skillEntry.getValue();
								if (!skill.isStarting) {
									String skillName = skillEntry.getKey();
									powerData._setSkillUnlocked(skillName, true, false);
								}
							}
							powerData.syncOnUpdate(player);
						}
//						case RESET -> {
//							// remove skill
//						}
						case RESET_ALL -> {
							powerData.resetUnlockedSkills(power);
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
