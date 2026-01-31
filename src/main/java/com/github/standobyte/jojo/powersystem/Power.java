package com.github.standobyte.jojo.powersystem;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.skill.UserUnlockedSkills;
import com.github.standobyte.jojo.util.NBTUtil;
import com.github.standobyte.jojo.util.entitycomponent.SynchronizablePlayerData;
import com.github.standobyte.jojo.util.entitycomponent.TickingEntityData;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class Power<P extends Power<P>> implements SynchronizablePlayerData, TickingEntityData, INBTSerializable<CompoundTag> {
	@Nonnull protected final LivingEntity user;
	protected final Optional<ServerPlayer> serverPlayerUser;
	protected Moveset moveset;
	protected UserUnlockedSkills unlockedSkills = new UserUnlockedSkills();
	
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
		// TODO (skill unlocking) sync to user
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
	}
	
	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		P newPower = getPowerClass().attachGet(newPlayer);
		onPlayerCloneData(newPower, wasDeath);
	}
	
	protected void onPlayerCloneData(P newEntityData, boolean wasDeath) {
		newEntityData.moveset = this.moveset;
	}
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("skills", unlockedSkills.toNBT());
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		NBTUtil.getCompoundOptional(nbt, "skills").ifPresent(unlockedSkills::fromNBT);
	}
	
	
	@SuppressWarnings("unchecked")
	protected final P getThis() {
		return (P) this;
	}
}
