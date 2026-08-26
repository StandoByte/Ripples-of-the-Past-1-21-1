package com.github.standobyte.jojo;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.entityattachment.SynchronizablePlayerData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.entityattachment.syncheddata.DataParameter;
import com.github.standobyte.jojo.entityattachment.syncheddata.SyncedDataHolderExtended;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataHelper;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataPacket;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataPacketHandler;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.github.standobyte.jojo.util.objects.ToggleTags;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class JojoModEntityVariables<T extends Entity> implements INBTSerializable<CompoundTag>, TickingEntityData, SynchronizablePlayerData, SyncedDataHolderExtended {
	public static final DataParameter<Boolean> INSIDE_TIME_STOP_ZONE = DataParameter.defineId(
			JojoModEntityVariables.class, EntityDataSerializers.BOOLEAN, false);
	public static final DataParameter<Boolean> STOPPED_IN_TIME = DataParameter.defineId(
			JojoModEntityVariables.class, EntityDataSerializers.BOOLEAN, false);
	public static final DataParameter<Boolean> CAN_SEE_IN_STOPPED_TIME = DataParameter.defineId(
			JojoModEntityVariables.class, EntityDataSerializers.BOOLEAN, true);

	protected final T entity;
	public final ToggleTags tags;
	public final SynchedDataHelper synchedData;
	
	private static final String SYNCHED_PACKET_HANDLER_TYPE = "vars";
	static {
		SynchedDataPacket.Handler.specificHandlers.put(SYNCHED_PACKET_HANDLER_TYPE, new SynchedDataPacketHandler() {

			@Override
			public SynchedDataHelper getDataSyncHelper(Entity entity) {
				return get(entity).synchedData;
			}

			@Override
			public SynchedDataHelper getOrCreateDataSyncHelper(Entity entity) {
				var variables = getIfPresent(entity);
				return variables != null ? variables.synchedData : null;
			}
			
		});
	}
	
	public JojoModEntityVariables(T entity) {
		this.entity = entity;
		this.tags = new ToggleTags(entity);
		this.synchedData = new SynchedDataHelper(SYNCHED_PACKET_HANDLER_TYPE, this, () -> entity.level().isClientSide());
		addTicking(entity);
		addSynchronization(entity);
		addSynchedData(entity, synchedData);
	}
	
	@Override
	public void tick() {}

	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {
		tags.sync(trackingPlayer);
	}

	@Override
	public void syncToPlayer(ServerPlayer entityAsPlayer) {
		tags.sync(entityAsPlayer);
	}

	@Override
	public void defineSynchedData(Builder builder) {
		INSIDE_TIME_STOP_ZONE.define(builder);
		STOPPED_IN_TIME.define(builder);
		CAN_SEE_IN_STOPPED_TIME.define(builder);
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
		tags.cloneData(newData.tags, wasDeath);
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("tags", tags.serializeNBT(provider));
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		NBTUtil.getElementOptional(nbt, "tags", ListTag.class).ifPresent(
				tagsNBT -> tags.deserializeNBT(provider, tagsNBT));
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
