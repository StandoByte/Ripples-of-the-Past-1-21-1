package com.github.standobyte.jojo.powersystem.standpower;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.config.DefaultedValue;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.packet.ResolveBoostsPacket;
import com.github.standobyte.jojo.powersystem.standpower.packet.TrResolvePacket;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.java.Lerp;
import com.github.standobyte.jojo.util.java.OptionalFloat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ResolveHandler {
	public static final float RESOLVE_DMG_REDUCTION = 0.6F;
//	public static final Double[] DEFAULT_MAX_RESOLVE_VALUES = { 5000.0, 10000.0, 20000.0, 30000.0 };
	public static final float RESOLVE_FOR_DMG_POINT = 1F;
//	public static final int[] RESOLVE_EFFECT_MIN = { 300, 400, 500, 600, 600 };
//	public static final int[] RESOLVE_EFFECT_MAX = { 600, 1200, 1500, 1800, 2400 };
	
	public static final float DEFAULT_MAX_RESOLVE_VALUE = 30000;
	public static final int RESOLVE_EFFECT_MIN = 600;
	public static final int RESOLVE_EFFECT_MAX = 2400;


	public static final float BOOST_ATTACK_MAX = 5F;
	public static final float BOOST_PER_DMG_DEALT = 0.05F;
	public static final int NO_BOOST_ATTACK_DECAY_TICKS = 400;

	public static final float BOOST_MISSING_HP_MAX = 10F;
	public static final float BOOST_MIN_HP = 5F;
	public static final float BOOST_MAX_HP = 15F;

	public static final float BOOST_REMOTE_MAX = 5F;
	public static final float BOOST_REMOTE_PER_TICK = 0.025F;

	public static final float BOOST_CHAT_MAX = 1.25F;
	public static final float BOOST_PER_CHARACTER = 0.05F;

	public Lerp.FloatValue resolveLerp = new Lerp.FloatValue();
	public DefaultedValue.Int resolveModeTimer = new DefaultedValue.Int(-1);
	
	public float boostAttack = 1;
	public float boostRemoteControl = 1;
	public float boostChat = 1;
	public OptionalFloat hpOnGettingAttacked = OptionalFloat.empty();
	public int noBoostDecayTicks = 0;


	public ResolveHandler() {}
	
	public void copyValues(ResolveHandler prev, boolean wasDeath) {
		this.resolveLerp = prev.resolveLerp;
		this.resolveModeTimer = prev.resolveModeTimer;
		if (!wasDeath) {
			this.boostAttack = prev.boostAttack;
			this.boostChat = prev.boostChat;
			this.hpOnGettingAttacked = prev.hpOnGettingAttacked;
			this.noBoostDecayTicks = prev.noBoostDecayTicks;
		}
		else {
			clearBoosts();
		}
	}
	
	public void clearBoosts() {
		this.boostAttack = 1;
		this.boostRemoteControl = 1;
		this.boostChat = 1;
		hpOnGettingAttacked = OptionalFloat.empty();
		this.noBoostDecayTicks = 0;
	}


	public void tick(StandPower stand) {
		if (stand.usesResolve()) {
			resolveLerp.lerpTick();
			LivingEntity user = stand.getUser();
			float curResolve = getResolveValue();
			
			if (resolveModeTimer.value > 0) {
				resolveModeTimer.value--;
			}
			else {
				if (!user.level().isClientSide()) {
					user.removeEffect(ModStatusEffects.RESOLVE);
				}
				resolveModeTimer.defaultValue = -1;
				resolveModeTimer.reset();
			}
			
			if (noBoostDecayTicks > 0) {
				noBoostDecayTicks--;
			}
			else {
				boolean hadValue = curResolve > 0;
				if (hadValue) {
					boostAttack = 1;
				}
				if (hadValue && curResolve == 0) {
					boostChat = 1;
					hpOnGettingAttacked = OptionalFloat.empty();
				}
				else if (user != null && user.getHealth() == user.getMaxHealth()) {
					hpOnGettingAttacked = OptionalFloat.empty();
				}
			}
			
			tickBoostRemoteControl(stand);
		}
	}
	
	public float getResolveValue() {
		return resolveLerp.get();
	}
	
	public float getMaxResolveValue(StandPower stand) {
//		StandTypePersistentData data = stand.getCurTypeData();
//		int alreadyHadResolveWithThisStand = data != null ? data.getResolveReached() : 0;
//		int index = Mth.clamp(alreadyHadResolveWithThisStand, 0, DEFAULT_MAX_RESOLVE_VALUES.length - 1);
//		return DEFAULT_MAX_RESOLVE_VALUES[index].floatValue();
		return DEFAULT_MAX_RESOLVE_VALUE;
	}
	
	public float getResolveModeTimerRatio(StandPower stand, float partialTick) {
		LivingEntity user = stand.getUser();
		MobEffectInstance resolveEffect = ModStatusEffects.maxDurationResolveEffect(user);
		if (resolveEffect != null) {
			int duration = resolveEffect.getDuration();
			if (resolveModeTimer.defaultValue > -1 && resolveModeTimer.value > -1) {
				duration = Math.min(resolveModeTimer.value, duration);
			}
			float value = duration + 1 - partialTick;
			if (value > 0) {
				if (resolveModeTimer.defaultValue > 0) {
					return value / resolveModeTimer.defaultValue;
				}
				return 1;
			}
		}
		return -1;
	}



	public void setResolveValue(StandPower stand, float resolve) {
		resolve = Mth.clamp(resolve, 0, getMaxResolveValue(stand));
		resolveLerp.set(resolve, true);

		LivingEntity user = stand.getUser();
		if (!user.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrResolvePacket(user.getId(), getResolveValue()));
			autoResolveModeActivation(stand);
		}
	}

	public void addResolveValue(StandPower stand, float resolve) {
		LivingEntity user = stand.getUser();
		MobEffectInstance resolveMode = user.getEffect(ModStatusEffects.RESOLVE);
		
		if (resolveMode == null) {
			setResolveValue(stand, getResolveValue() + boostAddedValue(resolve, user));
			noBoostDecayTicks = NO_BOOST_ATTACK_DECAY_TICKS;
		}
		else {
//			int resolveLevel = resolveMode.getAmplifier();
//			if (resolveLevel < RESOLVE_EFFECT_MAX.length) {
//				resolveModeTimer.value = Math.max(resolveModeTimer.value, resolveModeTimer.defaultValue / 2);
//			}
			resolveModeTimer.value = Math.max(resolveModeTimer.value, resolveModeTimer.defaultValue / 2);
		}
		
		if (user instanceof ServerPlayer player) {
			PacketDistributor.sendToPlayer(player, new ResolveBoostsPacket(this));
		}
	}

	protected float boostAddedValue(float value, LivingEntity entity) {
		value *= boostAttack * boostFromGettingAttacked(entity);
		return value;
	}

	protected float boostFromGettingAttacked(LivingEntity user) {
		PlayerPower playerPower = PlayerPower.get(user);
		if (playerPower != null && playerPower.getPowerType() == ModPlayerPowers.VAMPIRISM.get()) {
			return 1;
		}
		float hp = user.getHealth();
		if (hpOnGettingAttacked.isPresent() && hpOnGettingAttacked.getAsFloat() < hp) {
			hp = hpOnGettingAttacked.getAsFloat();
		}
		hp = Mth.clamp(hp, BOOST_MIN_HP, BOOST_MAX_HP);
		float boost = Mth.clamp((BOOST_MAX_HP - hp) * (BOOST_MISSING_HP_MAX - 1) / (BOOST_MAX_HP - BOOST_MIN_HP) + 1, 0, BOOST_MAX_HP);
		return boost;
	}

	public float getTotalBoostVisible(LivingEntity user) {
		float boost = boostAttack * boostFromGettingAttacked(user) * boostChat * boostRemoteControl;
		return boost;
	}
	
	
	@Deprecated
	protected void autoResolveModeActivation(StandPower stand) {
		if (canEnterResolveMode(stand)) {
			startResolveMode(stand);
		}
	}
	
	public boolean canEnterResolveMode(StandPower stand) {
		LivingEntity user = stand.getUser();
		return user != null && getResolveValue() >= getMaxResolveValue(stand) && !ModStatusEffects.isInResolveEffect(user);
	}
	
	public boolean startResolveMode(StandPower stand) {
		if (canEnterResolveMode(stand)) {
			LivingEntity user = stand.getUser();
			if (!user.level().isClientSide()) {
//				int resolveLevel = Math.min(stand.getCurTypeData().getResolveReached(), RESOLVE_EFFECT_MAX.length - 1);
//				stand.getUser().addEffect(new MobEffectInstance(ModStatusEffects.RESOLVE, 
//						RESOLVE_EFFECT_MAX[resolveLevel], resolveLevel, false, 
//						false, true));
				stand.getUser().addEffect(new MobEffectInstance(ModStatusEffects.RESOLVE, 
						RESOLVE_EFFECT_MAX, 0, false, 
						false, true));
			}
			return true;
		}
		return false;
	}
	
	public void onResolveEffectStart(StandPower stand, LivingEntity user, MobEffectInstance resolveEffect) {
		if (user != null) {
			var data = stand.getCurTypeData();
			data.incResolveReached(user);
			data.syncOnUpdate(user);
			setResolveValue(stand, stand.getMaxResolve());
			
			boolean hasMinDuration = false;
			if (resolveEffect.is(ModStatusEffects.RESOLVE)) {
//				int resolveLevel = resolveEffect.getAmplifier();
//				if (resolveLevel < RESOLVE_EFFECT_MAX.length) {
//					hasMinDuration = true;
//					resolveModeTimer.defaultValue = RESOLVE_EFFECT_MIN[resolveLevel];
//				}
				hasMinDuration = true;
				resolveModeTimer.defaultValue = RESOLVE_EFFECT_MIN;
			}
			if (!hasMinDuration) {
				resolveModeTimer.defaultValue = resolveEffect.getDuration();
			}
			resolveModeTimer.reset();
			
			if (user instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, new ResolveBoostsPacket(this));
			}
		}
	}
	
	public void onResolveEffectEnd(StandPower stand, LivingEntity user) {
//		if (hasAnotherResolveEffect()) {
//			onResolveEffectStart(stand, user, resolveEffect);
//		}
//		else {
			resolveLerp.set(0, false);
			resolveModeTimer.defaultValue = -1;
			resolveModeTimer.reset();
			if (!user.level().isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrResolvePacket(user.getId(), getResolveValue()));
				if (user instanceof ServerPlayer player) {
					PacketDistributor.sendToPlayer(player, new ResolveBoostsPacket(this));
				}
			}
//		}
	}


	public void addResolveOnAttack(StandPower stand, float dmgAmount) {
		if (stand.usesResolve()) {
			LivingEntity user = stand.getUser();
			float points = dmgAmount * RESOLVE_FOR_DMG_POINT;
			addResolveValue(stand, points);
			if (!user.level().isClientSide() && boostAttack < BOOST_ATTACK_MAX) {
				float boost = dmgAmount * BOOST_PER_DMG_DEALT;
				boostAttack = Math.min(boostAttack + boost, BOOST_ATTACK_MAX);
				if (user instanceof ServerPlayer player) {
					PacketDistributor.sendToPlayer(player, new ResolveBoostsPacket(this));
				}
			}
		}
	}

	public void onGettingAttacked(DamageSource dmgSource, float dmgAmount, StandPower stand, LivingEntity user) {
		Entity attacker = dmgSource.getEntity();
		if (attacker != null && !attacker.level().isClientSide() && stand.usesResolve() && attacker != null && !attacker.is(user)) {
			float hp = Math.max(user.getHealth() - dmgAmount, 0);
			if (hpOnGettingAttacked.isPresent()) {
				hp = Math.min(hp, hpOnGettingAttacked.getAsFloat());
			}
			hpOnGettingAttacked = OptionalFloat.of(hp);

			if (user instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, new ResolveBoostsPacket(this));
			}

			if (dmgAmount >= user.getMaxHealth() * 0.4F) {
				addResolveValue(stand, dmgAmount * BOOST_PER_DMG_DEALT * 10);
			}
		}
	}

	protected void tickBoostRemoteControl(StandPower stand) {
		if (stand.isSummoned() && stand.getUser() != null) {
			StandEntity standEntity = stand.getSummonedStandEntity();
			if (standEntity != null && standEntity.isManuallyControlled() /*&& ((StandEntity) standManifestation).distanceToSqr(stand.getUser()) >= 25*/) {
				boostRemoteControl = Math.min(boostRemoteControl + BOOST_REMOTE_PER_TICK, BOOST_REMOTE_MAX);
				return;
			}
		}
		boostRemoteControl = 1;
	}

	public void onChatMessage(StandPower stand, String message) {
		if (boostAttack > 1 || hpOnGettingAttacked.isPresent()) {
			int length = message.length();
			boostChat = Math.min(boostChat + length * BOOST_PER_CHARACTER, BOOST_CHAT_MAX);
			LivingEntity user = stand.getUser();
			if (user instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, new ResolveBoostsPacket(this));
			}
		}
	}


//	public void soulAddResolveLook() {
//		setResolveValue(getResolveValue() + getMaxResolveValue() / 60);
//	}
//
//	public void soulAddResolveTeammate() {
//		setResolveValue(getResolveValue() + getMaxResolveValue() / 300);
//	}


	public void syncToTracking(LivingEntity user, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new TrResolvePacket(user.getId(), resolveLerp.get()));
	}

	public void syncToUser(ServerPlayer user) {
		PacketDistributor.sendToPlayer(user, new TrResolvePacket(user.getId(), resolveLerp.get()));
		PacketDistributor.sendToPlayer(user, new ResolveBoostsPacket(this));
	}

	public void readNBT(CompoundTag nbt) {
		resolveLerp.set(nbt.getFloat("Resolve"), false);
		resolveModeTimer.defaultValue = nbt.getInt("ResolveModeMax");
		resolveModeTimer.value = nbt.getInt("ResolveMode");
		boostAttack = nbt.getFloat("BoostAttack");
		boostRemoteControl = nbt.getFloat("BoostRemoteControl");
		boostChat = nbt.getFloat("BoostChat");
		hpOnGettingAttacked = nbt.contains("HpOnGettingAttacked") ? OptionalFloat.of(nbt.getFloat("HpOnGettingAttacked")) : OptionalFloat.empty();
		noBoostDecayTicks = nbt.getInt("NoDecayTicks");
	}

	public CompoundTag writeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("Resolve", resolveLerp.get());
		nbt.putInt("ResolveModeMax", resolveModeTimer.defaultValue);
		nbt.putInt("ResolveMode", resolveModeTimer.value);
		nbt.putFloat("BoostAttack", boostAttack);
		nbt.putFloat("BoostRemoteControl", boostRemoteControl);
		nbt.putFloat("BoostChat", boostChat);
		hpOnGettingAttacked.ifPresent(hp -> nbt.putFloat("HpOnGettingAttacked", hp));
		nbt.putInt("NoDecayTicks", noBoostDecayTicks);

		return nbt;
	}
	
	
	
	
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onAttack(LivingIncomingDamageEvent event) {
		LivingEntity target = event.getEntity();
		DamageSource dmgSource = event.getSource();
		float dmgAmount = event.getAmount();
		
		if (target.is(dmgSource.getEntity()) || !target.isAlive()) return;
		float points = dmgAmount;
//		float points = Math.min(dmgAmount, target.getHealth());

		if (dmgSource.is(ModDamageTypes.ADDS_RESOLVE)) {
			Entity attacker = dmgSource.getEntity();
			if (attacker instanceof LivingEntity living) {
				LivingEntity standUser = StandUtil.getStandUser(living);
				StandPower attackerStand = StandPower.get(standUser);
				if (attackerStand != null && attackerStand.hasPower()) {
					addResolve(attackerStand, target, points);
				}
			}
		}

		else if (dmgSource.getEntity() instanceof LivingEntity) {
			LivingEntity attacker = (LivingEntity) dmgSource.getEntity();
//			UserStandEffects.getEffectsTargetedBy(attacker, ModStandEffects.GE_CREATED_LIFEFORM.get()).findAny().ifPresent(geLifeform -> {
//				StandPower geUserPower = geLifeform.getUserPower();
//				addResolve(geUserPower, target, points * 1.25F);
//			});

			StandPower attackerStand = StandPower.get(attacker);
			if (attackerStand != null && attackerStand.isSummoned()) {
				addResolve(attackerStand, target, points * 0.5F);
			}
		}
	}
    

	public static void addResolve(StandPower attackerStand, LivingEntity attackTarget, float dmgAmount) {
		if (attackerStand == null) return;
		attackTarget = StandUtil.getStandUser(attackTarget);
		boolean hitSelf = attackTarget != null && attackerStand.getUser() != null && attackTarget.is(attackerStand.getUser());
		if (!hitSelf && attackTarget.isAlive() && attackingTargetGivesResolve(attackTarget)) {
//			for (PowerClass<?> classification : PowerClass.values()) {
//				points *= classification.getOptional(attackTarget).map(power -> {
//					if (power.hasPower()) {
//						return power.getPowerType().getTargetResolveMultiplier(getThis(), attackerStand);
//					}
//					return 1F;
//				}).orElse(1F);
//			}
			if (ModStatusEffects.isInResolveEffect(attackTarget)) {
				dmgAmount *= Math.max(1 / (attackerStand.getResolveRatio() + 0.2F), 1);
			}

			attackerStand.resolveHandler.addResolveOnAttack(attackerStand, dmgAmount);
		}
	}

	public static boolean attackingTargetGivesResolve(Entity target) {
		if (target.getClassification(false) == MobCategory.MONSTER || target.getType() == EntityType.PLAYER) {
			return true;
		}
		if (target instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) target;
			if (livingEntity instanceof StandEntity) {
				return true;
			}
			if (livingEntity instanceof Mob mob) {
				return livingEntity instanceof Monster || mob.isAggressive();
			}
		}
		return false;
	}
	

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void resolveOnTakingDamage(LivingDamageEvent.Pre event) {
    	LivingEntity target = event.getEntity();
    	StandPower stand = StandPower.get(target);
    	if (stand != null && stand.usesResolve()) {
    		stand.getResolveHandler().onGettingAttacked(event.getSource(), event.getNewDamage(), stand, target);
    	}
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void reduceDamageFromResolve(LivingDamageEvent.Pre event) {
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        LivingEntity target = event.getEntity();
        StandPower stand = StandPower.get(target);
        if (stand != null) {
        	float dmgReduction = stand.resolveHandler.getResolveDmgReduction(stand, target);
        	if (dmgReduction > 0F) {
        		event.setNewDamage(event.getNewDamage() * (1 - dmgReduction));
        	}
        }
    }
    
    public float getResolveDmgReduction(StandPower stand, LivingEntity user) {
    	PlayerPower playerPower = PlayerPower.get(user);
    	if (playerPower != null && playerPower.getPowerType() == ModPlayerPowers.VAMPIRISM.get()) {
    		return 0;
    	}
        if (ModStatusEffects.isInResolveEffect(user)) {
            return RESOLVE_DMG_REDUCTION;
        }
        if (stand.usesResolve()) {
            return stand.getResolveRatio() * RESOLVE_DMG_REDUCTION;
        }
        return 0;
    }

    
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onChatMessage(ServerChatEvent event) {
		LivingEntity entity = event.getPlayer();
		StandPower stand = StandPower.get(entity);
		if (stand != null) {
			stand.resolveHandler.onChatMessage(stand, event.getRawText());
		}
	}
	
}
