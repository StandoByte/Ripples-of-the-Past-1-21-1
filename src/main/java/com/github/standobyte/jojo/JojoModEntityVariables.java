package com.github.standobyte.jojo;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.entityattachment.SynchronizablePlayerData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.entityattachment.syncheddata.DataParameter;
import com.github.standobyte.jojo.entityattachment.syncheddata.SyncedDataHolderExtended;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataHelper;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataPacket;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class JojoModEntityVariables<T extends Entity> implements INBTSerializable<CompoundTag>, TickingEntityData, SynchronizablePlayerData, SyncedDataHolderExtended {
	protected final T entity;
	public final SynchedDataHelper synchedData;
	
	private static final String SYNCHED_PACKET_HANDLER_TYPE = "vars";
	static {
		SynchedDataPacket.Handler.specificHandlers.put(SYNCHED_PACKET_HANDLER_TYPE, 
				(Entity entity, List<SynchedEntityData.DataValue<?>> packedItems, 
						SynchedDataPacket payload, IPayloadContext context) -> {
							JojoModEntityVariables<?> vars = get(entity);
							vars.synchedData.getDataSyncher().assignValues(packedItems);
						});
	}
	
	public JojoModEntityVariables(T entity) {
		this.entity = entity;
		this.synchedData = new SynchedDataHelper(SYNCHED_PACKET_HANDLER_TYPE, this, () -> entity.level().isClientSide());
		addTicking(entity);
		addSynchronization(entity);
		addSynchedData(entity, synchedData);
	}
	
	@Override
	public void tick() {}

	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {
	}

	@Override
	public void syncToPlayer(ServerPlayer entityAsPlayer) {
	}

	@Override
	public void defineSynchedData(Builder builder) {
	}

	@Override
	public <V> void onSyncedDataUpdated(V oldValue, V newValue, EntityDataAccessor<V> dataAccessor) {
	}

	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		JojoModEntityVariables<?> newData = get(newPlayer);
		cloneData(newData, wasDeath);
	}
	
	protected void cloneData(JojoModEntityVariables<?> newData, boolean wasDeath) {
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
	}
	
	
	public static JojoModEntityVariables<?> get(Entity entity) {
		return entity.getData(ModDataAttachmentTypes.ENTITY_VARS);
	}
	
	@Nullable
	public static JojoModEntityVariables<?> getIfPresent(Entity entity) {
		return ComponentUtil.getExistingDataOrNull(entity, ModDataAttachmentTypes.ENTITY_VARS);
	}
	
	public static SynchedDataHelper getSynchedIfPresent(Entity entity) {
		JojoModEntityVariables<?> vars = getIfPresent(entity);
		return vars != null ? vars.synchedData : null;
	}
	
	public static JojoModEntityVariables<?> createObjFor(Entity entity) {
		if (entity instanceof LivingEntity living) {
			return new JojoModLivingVariables<>(living);
		}
		
		return new JojoModEntityVariables<>(entity);
	}
	
}
