package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.gameplay.standarrow.StandArrowItem;
import com.github.standobyte.jojo.core.packet.fromserver.TrPowerStandInstancePacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrStandSkinPacket;
import com.github.standobyte.jojo.init.core.ModEntityAttributes;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandAwakening.AwakeningStage;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.packet.TrStaminaPacket;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand;
import com.github.standobyte.jojo.util.NBTUtil;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.entitycomponent.PostNbtReadEntityData;
import com.github.standobyte.jojo.util.java.Lerp;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandPower extends Power<StandPower> implements PostNbtReadEntityData {
	protected Optional<StandInstance> standInstance = Optional.empty();
	protected SummonedStand summonedStand;
	
	public UserStandEffects userStandEffects = new UserStandEffects(this);
	protected Lerp.FloatValue staminaLerp = new Lerp.FloatValue();
	protected float staminaAddNextTick = 0;
	
	public ResolveHandler resolveHandler = new ResolveHandler();
	public StandAwakening userStandAwakeningState = new StandAwakening();
	public boolean healingDamageFromArrow = false;
	
	public StandPower(LivingEntity user) {
		super(user);
		addPostNbtReadCallback(user); // to update the user's base attribute values after the attributes are read
	}
	
	
	@Override
	public void tick() {
		super.tick();
		tickStamina();
		tickResolve();
		if (hasPower()) {
			userStandEffects.tick();
		}
		if (!user.level().isClientSide()) {
			if (healingDamageFromArrow && !StandArrowItem.healArrowDamage(user)) {
				healingDamageFromArrow = false;
			}
			if (!canUsePower()) {
				StandType type = getPowerType();
				if (type != null) {
					type.forceUnsummon(user, this);
				}
			}
		}
		if (summonedStand != null) {
			summonedStand.tickStand(getUser(), this);
		}
	}
	
	
	public void setStand(@Nullable StandType stand) {
		setStandInstance(stand != null ? Optional.of(new StandInstance(stand)) : Optional.empty());
	}
	
	public void setStandInstance(Optional<StandInstance> standInstance) {
		StandType oldStand = getPowerType();
		boolean standChanged = standInstance.map(newStand -> oldStand != newStand.getStandType()).orElseGet(() -> oldStand != null);
		if (oldStand != null && standChanged) {
			oldStand.forceUnsummon(user, this);
		}
		
		this.standInstance = standInstance;
		StandType newStand = getPowerType();
		
		LivingEntity user = getUser();
		if (user != null) {
			StandStats.updateStandStatAttributes(this, user);
		}
		
		if (user != null && !user.level().isClientSide()) {
			userStandEffects.onStandChanged(user);
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrPowerStandInstancePacket(user.getId(), standInstance));
		}
		
		if (newStand == null) {
			setStamina(0);
		}
		onSetPowerType(oldStand, newStand);
	}

	@Override
	public StandType getPowerType() {
		return standInstance.map(StandInstance::getStandType).orElse(null);
	}
	
	@Override
	public boolean hasPower() {
		return standInstance.filter(StandInstance::standExists).isPresent();
	}
	
	public Optional<StandInstance> getStandInstance() {
		return standInstance;
	}

	@Override
	public PowerClass<StandPower> getPowerClass() {
		return PowerClass.STAND;
	}
	
	
	public SummonedStand getSummonedStand() {
		return summonedStand;
	}
	
	@Nullable
	public StandEntity getSummonedStandEntity() {
		return summonedStand != null ? summonedStand.getStandEntity() : null;
	}
	
	public boolean isSummoned() {
		return summonedStand != null;
	}
	
	public void setSummonedStand(@Nullable SummonedStand summonedStand) {
		this.summonedStand = summonedStand;
		if (summonedStand != null) {
			summonedStand.setUserAndPower(getUser(), this);
			summonedStand.setSelectedSkin(standInstance.flatMap(StandInstance::getSelectedSkin));
		}
	}
	
	
	@Nullable
	@Override
	public StandTypePersistentData getCurTypeData() {
		// who needs generics, amirite
		return (StandTypePersistentData) super.getCurTypeData();
	}
	
	public void skipProgression() {}
	
	
	public boolean usesStamina() {
		return hasPower() ? getPowerType().usesStamina(this) : false;
	}
	
	public float getStamina() {
		if (isUserCreative()) {
			return getMaxStamina();
		}
		return staminaLerp.get();
	}
	
	public float getMaxStamina() {
		return hasPower() ? getPowerType().getMaxStamina(this) : 0;
	}
	
	public float getStaminaRatio() {
		float maxStamina = getMaxStamina();
		float stamina = getStamina();
		return stamina == maxStamina ? 1 : maxStamina > 0 ? stamina / maxStamina : 0;
	}
	
	public float getStaminaRatio(float partialTick) {
		if (isUserCreative()) {
			return 1;
		}
		float maxStamina = getMaxStamina();
		float stamina = staminaLerp.lerp(partialTick);
		return stamina == maxStamina ? 1 : maxStamina > 0 ? stamina / maxStamina : 0;
	}
	
	public void setStamina(float stamina) {
		boolean clientSide = user.level().isClientSide();
		if (!clientSide) {
			stamina = Mth.clamp(stamina, 0, getMaxStamina());
		}
		if (this.staminaLerp.set(stamina, true)) {
			if (!clientSide) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrStaminaPacket(user.getId(), stamina));
			}
		}
	}
	
	public boolean consumeStamina(float amount) {
		return consumeStamina(amount, false);
	}
	
	public boolean consumeStamina(float amount, boolean ticking) {
		if (isUserCreative()) {
			return true;
		}
		float curAmount = getStamina();
		if (curAmount >= amount) {
			if (ticking) {
				staminaAddNextTick -= amount;
			}
			else {
				setStamina(curAmount - amount);
			}
			return true;
		}
		else {
			setStamina(0);
			return StandUtil.standIgnoresStaminaDebuff(getUser());
		}
	}
	
	protected void tickStamina() {
		if (this.usesStamina()) {
			float staminaRegen = getPowerType().getStaminaRegen(this) + staminaAddNextTick;
			staminaAddNextTick = 0;
			staminaLerp.set(Mth.clamp(staminaLerp.get() + staminaRegen, 0, getMaxStamina()), true);
		}
	}
	
	
	public boolean usesResolve() {
		return hasPower() && getPowerType().usesResolve(this) && userStandAwakeningState.stage == AwakeningStage.FULL_CONTROL;
	}
	
	public float getResolve() {
		return resolveHandler.getResolveValue();
	}
	
	public float getMaxResolve() {
		return hasPower() ? resolveHandler.getMaxResolveValue(this) : 0;
	}
	
	public float getResolveRatio() { return getResolveRatio(1); }
	
	public float getResolveRatio(float partialTick) {
		float maxResolve = getMaxResolve();
		return maxResolve > 0 ? resolveHandler.resolveLerp.lerp(partialTick) / maxResolve : 0;
	}
	
	public ResolveHandler getResolveHandler() {
		return resolveHandler;
	}
	
	protected void tickResolve() {
		resolveHandler.tick(this);
	}
	
	
	public void setSelectedSkin(Optional<ResourceLocation> skin) {
		if (standInstance.isPresent()) {
			standInstance.get().setCustomSkin(skin);
			if (!user.level().isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrStandSkinPacket(user.getId(), getSelectedSkin()));
			}
		}
		if (summonedStand != null) {
			summonedStand.setSelectedSkin(skin);
		}
	}
	
	public Optional<ResourceLocation> getSelectedSkin() {
		if (standInstance.isEmpty()) return Optional.empty();
		return standInstance.get().getSelectedSkin();
	}
	
	
	@Override
	public void afterConfigApply() {
		super.afterConfigApply();
		StandStats.updateStandStatAttributes(this, user);
	}

	@Override
	public void syncToPlayer(ServerPlayer user) {
		PacketDistributor.sendToPlayer(user, new TrPowerStandInstancePacket(user.getId(), standInstance));
		super.syncToPlayer(user);
		syncStaminaFixed(user, user);
		resolveHandler.syncToUser(user);
		PacketDistributor.sendToPlayer(user, new TrStandSkinPacket(user.getId(), getSelectedSkin()));
		userStandEffects.syncToPlayer(user);
		userStandAwakeningState.syncToUser(user);
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new TrPowerStandInstancePacket(user.getId(), standInstance));
		super.syncToTracking(player);
		syncStaminaFixed(player, user);
		resolveHandler.syncToTracking(user, player);
		PacketDistributor.sendToPlayer(player, new TrStandSkinPacket(user.getId(), getSelectedSkin()));
		userStandEffects.syncToTracking(player);
	}
	
	protected void syncStaminaFixed(ServerPlayer player, LivingEntity user) {
		var durabilityAttribute = user.getAttribute(ModEntityAttributes.STAND_DURABILITY);
		if (durabilityAttribute != null) {
			player.connection.send(new ClientboundUpdateAttributesPacket(user.getId(), Collections.singletonList(durabilityAttribute)));
		}
		PacketDistributor.sendToPlayer(player, new TrStaminaPacket(user.getId(), staminaLerp.get()));
	}
	
	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		super.onPlayerClone(newPlayer, wasDeath);
		this.userStandEffects.onPlayerClone(newPlayer, wasDeath);
	}
	
	@Override
	protected void onPlayerCloneData(StandPower newEntityData, boolean wasDeath) {
		super.onPlayerCloneData(newEntityData, wasDeath);
		newEntityData.standInstance = this.standInstance;
		newEntityData.staminaLerp = this.staminaLerp;
		newEntityData.resolveHandler.copyValues(this.resolveHandler, wasDeath);
		newEntityData.userStandEffects = this.userStandEffects;
		newEntityData.userStandEffects.setPowerData(newEntityData);
		newEntityData.userStandAwakeningState = this.userStandAwakeningState;
	}
	
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		standInstance.ifPresent(
				stand -> StandInstance.CODEC.encodeStart(NbtOps.INSTANCE, stand)
				.ifSuccess(standNbt -> nbt.put("StandInstance", standNbt)));
		nbt.putFloat("Stamina", staminaLerp.get());
		nbt.put("ResolveHandler", resolveHandler.writeNBT());
		nbt.put("Effects", userStandEffects.serializeNBT(provider));
		nbt.put("Awakening", userStandAwakeningState.serializeNBT());
		nbt.putBoolean("HealFromArrow", healingDamageFromArrow);
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		standInstance = NBTUtil.getCompoundOptional(nbt, "StandInstance")
				.flatMap(standNbt -> StandInstance.CODEC.decode(NbtOps.INSTANCE, standNbt).result())
				.map(pair -> pair.getFirst());
		staminaLerp.set(nbt.getFloat("Stamina"), false);
		NBTUtil.getCompoundOptional(nbt, "ResolveHandler").ifPresent(resolveHandler::readNBT);
		NBTUtil.getCompoundOptional(nbt, "Effects").ifPresent(effectsNbt -> userStandEffects.deserializeNBT(provider, effectsNbt));
		NBTUtil.getCompoundOptional(nbt, "Awakening").ifPresent(userStandAwakeningState::deserializeNBT);
		healingDamageFromArrow = nbt.getBoolean("HealFromArrow");
	}
	
	/* unlike deserializeNBT, this is called after the entity attributes are read, 
	 * allowing me to edit their base values from the Stand stats
	 */
	@Override
	public void afterNbtRead() {
		StandStats.updateStandStatAttributes(this, user);
	}
	
	
	@Nullable
	public static StandPower get(LivingEntity entity) {
		return PowerClass.STAND.get(entity);
	}
	
	public static Optional<StandPower> getOptional(LivingEntity entity) {
		return PowerClass.STAND.getOptional(entity);
	}
	
	
	public final boolean isUserCreative() {
		LivingEntity user = getUser();
		return user instanceof Player player && player.getAbilities().instabuild;
	}

}
