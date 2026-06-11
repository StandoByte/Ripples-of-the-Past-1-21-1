package com.github.standobyte.jojo.entityattachment.custom_effect;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataHelper;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public abstract class EntityCustomEffect {
	public static final AtomicInteger EFFECTS_COUNTER = new AtomicInteger();
	@Nonnull public final EntityCustomEffectType<?> effectType;

	public SynchedDataHelper synchedData = new SynchedDataHelper("eff", this, () -> this.level.isClientSide());

	private int id = EFFECTS_COUNTER.incrementAndGet();
	public int tickCount = 0;
	protected boolean toBeRemoved = false;

	protected Entity entity;
	public Level level;

	public boolean removeOnUserDeath = true;
	public boolean removeOnUserLogout = true;


	public EntityCustomEffect(@Nonnull EntityCustomEffectType<?> effectType) {
		this.effectType = effectType;
	}
	
	public boolean is(Holder<EntityCustomEffectType<?>> supplier) {
		return this.effectType == supplier.value();
	}

	public EntityCustomEffect withEntity(Entity entity) {
		setEntity(entity);
		setLevel(entity.level());
		return this;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
	public void setLevel(Level level) {
		if (this.level != level) {
			this.level = level;
		}
	}

	@ApiStatus.Internal
	public EntityCustomEffect withId(int id) {
		this.id = id;
		return this;
	}

	public Entity getEntity() {
		return entity;
	}

	public void onStart() {
		start();
	}

	public void onTick() {
		if (!toBeRemoved) {
			tickCount++;
			tick();
		}
	}

	public void onStop() {
		toBeRemoved = true;
		stop();
	}

	protected abstract void start();
	protected abstract void tick();
	protected abstract void stop();
	
	public void onFrame(float tickDelta) {}

	public int getId() {
		return id;
	}

	public void remove() {
		toBeRemoved = true;
	}

	public boolean isStopped() {
		return toBeRemoved;
	}

	public void syncWithUserOnly(ServerPlayer user) {
	}

	public void syncWithTrackingOrUser(ServerPlayer player) {
	}

	public CompoundTag toNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("Type", effectType.registryKey.toString());
		nbt.putInt("TickCount", tickCount);
		writeAdditionalSaveData(nbt);
		return nbt;
	}

	public static EntityCustomEffect fromNBT(CompoundTag nbt, Level level) {
		EntityCustomEffectType<?> effectType = JojoRegistries.STAND_EFFECTS_REG.get(ResourceLocation.parse(nbt.getString("Type")));
		if (effectType == null) return null;
		EntityCustomEffect effect = effectType.create(level);
		effect.tickCount = nbt.getInt("TickCount");
		effect.readAdditionalSaveData(nbt);
		return effect;
	}

	public void writeAdditionalPacketData(FriendlyByteBuf buf, boolean sendingToUser) {}

	public void readAdditionalPacketData(FriendlyByteBuf buf, boolean clientIsUser) {}

	protected void writeAdditionalSaveData(CompoundTag nbt) {}

	protected void readAdditionalSaveData(CompoundTag nbt) {}
}
