package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.List;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@FunctionalInterface
public interface SynchedDataPacketHandler {
	void handle(Entity entity, List<SynchedEntityData.DataValue<?>> packedItems, 
			SynchedDataPacket payload, IPayloadContext context);
}
