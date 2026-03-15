package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.mob;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.EntityComponentController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.mob.HardcodedMobControlCommands;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.mob.HardcodedMobControlCommands.CommandType;
import com.github.standobyte.jojo.subsystems.entity_useitem.HitResultSync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClControlledMobCommandPacket(CommandType commandType, int slot, HitResultSync target) implements CustomPacketPayload {

	public ClControlledMobCommandPacket(CommandType commandType) { this(commandType, 0, new HitResultSync(null)); }
	public ClControlledMobCommandPacket(CommandType commandType, int slot) { this(commandType, slot, new HitResultSync(null)); }
	public ClControlledMobCommandPacket(CommandType commandType, HitResult target) { this(commandType, 0, new HitResultSync(target)); }
	public ClControlledMobCommandPacket(CommandType commandType, int slot, HitResult target) { this(commandType, slot, new HitResultSync(target)); }
	
	private static CustomPacketPayload.Type<ClControlledMobCommandPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<ClControlledMobCommandPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClControlledMobCommandPacket> type() {
			return type;
		}
		
		@Override
		public void encode(ClControlledMobCommandPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.commandType);
			buf.writeVarInt(packet.slot);
			HitResultSync.STREAM_CODEC.encode(buf, packet.target);
		}
		
		@Override
		public ClControlledMobCommandPacket decode(RegistryFriendlyByteBuf buf) {
			CommandType commandType = buf.readEnum(CommandType.class);
			int slot = buf.readVarInt();
			HitResultSync target = HitResultSync.STREAM_CODEC.decode(buf);
			return new ClControlledMobCommandPacket(commandType, slot, target);
		}

		@Override
		public void handle(ClControlledMobCommandPacket packet, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			
			Entity curControlTarget = EntityComponentController.getControlTarget(player);
			if (curControlTarget instanceof Mob mob) {
				HitResult target = packet.target().resolveEntity(player.level());
				HardcodedMobControlCommands.onHotbarPacket(mob, packet.commandType, packet.slot, target);
				switch (packet.commandType) {
					case PRESS_RMB -> { ((HardcodedMobControlCommands.KeepRMBState) mob).jojo_ripples$setIsHoldingRMB(true); }
					case RELEASE_RMB -> { ((HardcodedMobControlCommands.KeepRMBState) mob).jojo_ripples$setIsHoldingRMB(false); }
					default -> {}
				}
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
