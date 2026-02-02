package com.github.standobyte.jojo.powersystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.util.NBTUtil;
import com.github.standobyte.jojo.util.entitycomponent.SynchronizablePlayerData;
import com.github.standobyte.jojo.util.entitycomponent.TickingEntityData;
import com.mojang.datafixers.util.Either;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class Power<P extends Power<P>> implements SynchronizablePlayerData, TickingEntityData, INBTSerializable<CompoundTag> {
	@Nonnull protected final LivingEntity user;
	protected final Optional<ServerPlayer> serverPlayerUser;
	protected Moveset moveset;
	protected Map<ResourceLocation, Either<PowerData, CompoundTag>> powerData = new HashMap<>();
	
	public Power(LivingEntity user) {
		this.user = user;
		this.serverPlayerUser = Optional.ofNullable(user instanceof ServerPlayer player ? player : null);
		addSynchronization(user);
		addTicking(user);
	}
	
	
	@Override
	public void tick() {
		cachedMovesThisTick = false;
	}
	
	public boolean canUsePower() {
		return !user.isSpectator();
	}
	
	
	@Nullable
	public abstract PowerType getPowerType();
	
	protected void onSetPowerType(@Nullable PowerType oldPower, @Nullable PowerType newPower) {
		moveset = initMoveset(newPower);

		if (!user.level().isClientSide()) {
			PowerData curData = getCurTypeData();
			if (curData != null) {
				curData.syncToAllTracking(user);
				if (user instanceof ServerPlayer player) {
					curData.syncToPlayer(player);
				}
			}
		}
	}
	
	@Nullable
	public PowerData getCurTypeData() {
		PowerType powerType = getPowerType();
		return powerType != null ? getPowerTypeData(powerType) : null;
	}
	
	public PowerData getPowerTypeData(PowerType powerType) {
		ResourceLocation id = powerType.getId();
		Either<PowerData, CompoundTag> dataEntry = powerData.get(id);
		if (dataEntry == null) {
			PowerData data = powerType.newDataInstance();
			this.powerData.put(id, Either.left(data));
			return data;
		}
		else {
			return dataEntry.map(Function.identity(), readNbt -> {
				PowerData data = powerType.newDataInstance();
				RegistryAccess provider = getUser().registryAccess();
				data.deserializeNBT(provider, readNbt);
				this.powerData.put(id, Either.left(data));
				return data;
			});
		}
	}
	
	@Nonnull
	public Moveset getMoveset() {
		if (moveset == null) {
			moveset = initMoveset(getPowerType());
		}
		return moveset;
	}
	
	protected Moveset initMoveset(@Nullable PowerType powerType) {
		return powerType != null ? powerType.makeMoveset(this) : Moveset.empty();
	}
	
	@Nullable
	public Ability getAbility(String name) {
		if (name == null) return null;
		if (!hasPower()) {
			JojoMod.getLogger().warn("Invalid state: {} tried to use ability {} with no {} power.", 
					user.getDisplayName().getString(), name, getClass());
			return null;
		}
		Ability ability = getMoveset().getAbility(name);
		if (ability == null) {
			JojoMod.getLogger().warn("Invalid ability id: {} tried to use ability {} with {} power {}.", 
					user.getDisplayName().getString(), name, getClass(), getPowerType().getId());
		}
		return ability;
	}
	
	@ApiStatus.Internal
	public AvailableAbilities _curAvailableMoves = new AvailableAbilities();
	protected boolean cachedMovesThisTick;
	public AvailableAbilities updateAvailableMoves() {
		if (!cachedMovesThisTick) {
			_curAvailableMoves.update(this, getMoveset());
			cachedMovesThisTick = true;
		}
		return _curAvailableMoves;
	}
	
	public boolean hasPower() {
		return getPowerType() != null;
	}
	
	public abstract PowerClass<P> getPowerClass();
	
	public LivingEntity getUser() {
		return user;
	}

	public boolean isAbilityUnlocked(String abilityName) {
		Moveset moveset = getMoveset();
		return moveset != null && moveset.abilities.containsKey(abilityName);
	}
	
	
	public Component getName() {
		return getPowerType().getName(this);
	}

	
	public void afterConfigApply() {
		initMoveset(getPowerType());
	}

	@Override
	public void syncToPlayer(ServerPlayer user) {
		PowerData curData = getCurTypeData();
		if (curData != null) {
			curData.syncToPlayer(user);
		}
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
		PowerData curData = getCurTypeData();
		if (curData != null) {
			curData.syncToTracking(user, player);
		}
	}
	
	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		P newPower = getPowerClass().attachGet(newPlayer);
		onPlayerCloneData(newPower, wasDeath);
	}
	
	protected void onPlayerCloneData(P newEntityData, boolean wasDeath) {
		newEntityData.moveset = this.moveset;
		newEntityData.powerData = this.powerData;
	}
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		if (!powerData.isEmpty()) {
			CompoundTag dataNbt = new CompoundTag();
			for (var dataEntry : powerData.entrySet()) {
				String key = dataEntry.getKey().toString();
				dataEntry.getValue()
				.ifLeft(data -> dataNbt.put(key, data.serializeNBT(provider)))
				.ifRight(danaEntryNbt -> dataNbt.put(key, danaEntryNbt));
			}
			nbt.put("powersData", dataNbt);
		}
		
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		NBTUtil.getCompoundOptional(nbt, "powersData").ifPresent(dataNbt -> {
			for (String key : dataNbt.getAllKeys()) {
				if (dataNbt.get(key) instanceof CompoundTag compound) {
					this.powerData.put(ResourceLocation.parse(key), Either.right(compound));
				}
			}
		});
	}
	
	
	@SuppressWarnings("unchecked")
	protected final P getThis() {
		return (P) this;
	}
}
