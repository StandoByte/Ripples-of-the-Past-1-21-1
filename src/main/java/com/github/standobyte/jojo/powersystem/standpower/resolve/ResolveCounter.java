package com.github.standobyte.jojo.powersystem.standpower.resolve;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ResolveCounter {
	public static final Double[] DEFAULT_MAX_RESOLVE_VALUES = { 500.0, 1500.0, 3500.0, 7500.0 };
	public static final int MAX_STAGE = DEFAULT_MAX_RESOLVE_VALUES.length - 1;
	public static final int[] RESOLVE_EFFECT_MIN = { 300, 400, 500, 600 };
	public static final int[] RESOLVE_EFFECT_MAX = { 600, 1200, 1800, 2400 };

	public static final float RESOLVE_FOR_DMG_POINT = 1F;
	public static final float RESOLVE_FOR_DMG_TAKEN = 2F;
	
	protected float value;
	protected int unlockedStage = 0;
	protected boolean passedLastStage = false;
	public int resolveModeTimer = -1;
	public int resolveModeInitial = -1;
	protected boolean activatedEffectOnMaxStage = false;


	public ResolveCounter() {}
	
	public void copyValues(ResolveCounter prev, boolean wasDeath) {
		this.unlockedStage = prev.unlockedStage;
		this.passedLastStage = prev.passedLastStage;
		if (!wasDeath) {
			this.value = prev.value;
			this.resolveModeTimer = prev.resolveModeTimer;
			this.resolveModeInitial = prev.resolveModeInitial;
			this.activatedEffectOnMaxStage = prev.activatedEffectOnMaxStage;
		}
	}


	public void syncToTracking(LivingEntity user, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new TrResolvePacket(user.getId(), false, this));
	}

	public void syncToUser(ServerPlayer user) {
		PacketDistributor.sendToPlayer(user, new TrResolvePacket(user.getId(), true, this));
	}
	
	protected void sync(LivingEntity user, boolean toTracking) {
		if (!user.level().isClientSide()) {
			if (toTracking) {
				PacketDistributor.sendToPlayersTrackingEntity(user, new TrResolvePacket(user.getId(), false, this));
			}
			if (user instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, new TrResolvePacket(user.getId(), true, this));
			}
		}
	}
	
	public void toBuf(FriendlyByteBuf buf, boolean sendToUser) {
		buf.writeFloat(value);
		buf.writeVarInt(unlockedStage);
		buf.writeBoolean(activatedEffectOnMaxStage);
		if (sendToUser) {
			buf.writeInt(resolveModeTimer);
			buf.writeInt(resolveModeInitial);
			buf.writeBoolean(passedLastStage);
		}
	}
	
	public void fromBuf(FriendlyByteBuf buf, boolean sentToUser) {
		value = buf.readFloat();
		unlockedStage = buf.readVarInt();
		activatedEffectOnMaxStage = buf.readBoolean();
		if (sentToUser) {
			resolveModeTimer = buf.readInt();
			resolveModeInitial = buf.readInt();
			passedLastStage = buf.readBoolean();
		}
	}

	public CompoundTag writeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("Resolve", value);
		nbt.putInt("Stage", unlockedStage);
		nbt.putBoolean("PassedLast", passedLastStage);
		nbt.putInt("ResolveModeInitial", resolveModeInitial);
		nbt.putInt("ResolveMode", resolveModeTimer);
		nbt.putBoolean("DododoEffect", activatedEffectOnMaxStage);

		return nbt;
	}

	public void readNBT(CompoundTag nbt) {
		value = nbt.getFloat("Resolve");
		setUnlockedStage(nbt.getInt("Stage"));
		resolveModeTimer = nbt.getInt("ResolveMode");
		passedLastStage = nbt.getBoolean("PassedLast");
		resolveModeInitial = nbt.getInt("ResolveModeInitial");
		activatedEffectOnMaxStage = nbt.getBoolean("DododoEffect");
	}
	
	public void reset(LivingEntity user) {
		for (var resolveEffect : ResolveModeEffect.RESOLVE_EFFECTS) {
			user.removeEffect((Holder) resolveEffect);
		}
		value = 0;
		setUnlockedStage(0);
		passedLastStage = false;
		resolveModeInitial = -1;
		resolveModeTimer = -1;
		activatedEffectOnMaxStage = false;
		sync(user, true);
	}


	
	public float getResolveBarFill() {
		int curStage = this.getCurStage();
		if (curStage >= ResolveCounter.MAX_STAGE) {
			return 1;
		}
		float value = this.getResolveValue();
		float prevValue = curStage >= 0 ? getMaxResolveValue(curStage) : 0;
		float nextValue = getMaxResolveValue(curStage + 1);
		float lerp = Mth.inverseLerp(value, prevValue, nextValue);
		return 1f / (ResolveCounter.MAX_STAGE + 1) /*0.25f*/ * (curStage + 1 + lerp);
	}

	public void tick(StandPower stand) {
		if (stand.usesResolve()) {
			LivingEntity user = stand.getUser();
			
			if (resolveModeTimer > 0) {
				resolveModeTimer--;
				if (ResolveStageBuffs.keepResolveModeAtHalfPassively(stand, this)) {
					keepResolveModeMinTimerAtHalf();
				}
			}
			else if (resolveModeTimer == 0) {
				if (!user.level().isClientSide()) {
					user.removeEffect(ModStatusEffects.RESOLVE);
				}
				resolveModeInitial = -1;
				resolveModeTimer = -1;
			}
			
			if (user.level().isClientSide()) {
				if (!user.isInvisible() && user.tickCount % 3 == 0 && 
						(getResolveValue() >= getMaxResolveUnlocked() || activatedEffectOnMaxStage)) {
					user.level().addParticle(ModParticles.KATAKANA_DO.get(), 
							user.getRandomX(2.5), user.getY(user.getRandom().nextDouble() * 1.5), user.getRandomZ(2.5), 0, 0, 0);
				}
			}
		}
	}
	
	public float getResolveValue() {
		return value;
	}
	
	public float getMaxResolveUnlocked() {
		int stage = getUnlockedStage();
		return getMaxResolveValue(stage);
	}
	
	public int getUnlockedStage() {
		return unlockedStage;
	}
	
	public boolean passedLastStageUnlock() {
		return passedLastStage;
	}
	
	protected void setUnlockedStage(int stage) {
		this.unlockedStage = Mth.clamp(stage, 0, MAX_STAGE);
	}
	
	public int getCurStage() {
		for (int i = getUnlockedStage(); i >= 0; i--) {
			if (this.value >= getMaxResolveValue(i)) {
				return i;
			}
		}
		return -1;
	}
	
	protected static float getMaxResolveValue(int stage) {
		return DEFAULT_MAX_RESOLVE_VALUES[Mth.clamp(stage, 0, DEFAULT_MAX_RESOLVE_VALUES.length - 1)].floatValue();
	}



	public void setResolveValue(StandPower stand, float resolve) {
		resolve = Mth.clamp(resolve, 0, getMaxResolveUnlocked());
		this.value = resolve;

		LivingEntity user = stand.getUser();
		if (!user.level().isClientSide()) {
			sync(user, true);
		}
	}

	public void addResolveValue(StandPower stand, float resolve) {
		LivingEntity user = stand.getUser();
		MobEffectInstance resolveMode = user.getEffect(ModStatusEffects.RESOLVE);
		
		if (resolveMode != null) {
			keepResolveModeMinTimerAtHalf();
		}
		// will also sync the timer above
		setResolveValue(stand, getResolveValue() + resolve);
		//if (!user.level().isClientSide() && getResolveValue() >= getMaxResolveUnlocked(user)) {
		//	startResolveMode(stand);
		//}
	}
	
	protected void keepResolveModeMinTimerAtHalf() {
		resolveModeTimer = Math.max(resolveModeTimer, resolveModeInitial / 2);
	}
	
	
	public boolean canEnterResolveMode(StandPower stand) {
		LivingEntity user = stand.getUser();
		return user != null && getCurStage() >= 0 && !user.hasEffect(ModStatusEffects.RESOLVE);
	}
	
	public boolean startResolveMode(StandPower stand) {
		if (canEnterResolveMode(stand)) {
			LivingEntity user = stand.getUser();
			if (!user.level().isClientSide()) {
				int resolveLevel = getCurStage();
				int duration = RESOLVE_EFFECT_MAX[Mth.clamp(resolveLevel, 0, RESOLVE_EFFECT_MAX.length - 1)];
				stand.getUser().addEffect(new MobEffectInstance(ModStatusEffects.RESOLVE, 
						duration, resolveLevel, false, false, true));
			}
			return true;
		}
		return false;
	}
	
	public void onResolveEffectStart(StandPower stand, LivingEntity user, MobEffectInstance resolveEffect) {
		if (user != null) {
			boolean hasMinDuration = false;
			if (resolveEffect.is(ModStatusEffects.RESOLVE)) {
				int resolveLevel = resolveEffect.getAmplifier();
				if (resolveLevel < RESOLVE_EFFECT_MIN.length) {
					hasMinDuration = true;
					resolveModeInitial = RESOLVE_EFFECT_MIN[resolveLevel];
				}

				int stage = resolveLevel + 1;
				activatedEffectOnMaxStage = stage > getUnlockedStage();
				setUnlockedStage(Math.max(getUnlockedStage(), stage));
			}
			if (!hasMinDuration) {
				resolveModeInitial = resolveEffect.getDuration();
			}
			resolveModeTimer = resolveModeInitial;
			
			if (!user.level().isClientSide()) {
				sync(user, true);
			}
		}
	}
	
	public void onResolveEffectEnd(StandPower stand, LivingEntity user, MobEffectInstance resolveEffect) {
		//if (hasAnotherResolveEffect()) {
		//	onResolveEffectStart(stand, user, resolveEffect);
		//}
		//else {
			this.value = 0;
			resolveModeInitial = -1;
			resolveModeTimer = -1;
			activatedEffectOnMaxStage = false;
			int stage = resolveEffect.getAmplifier() + 1;
			if (stage >= MAX_STAGE) { // after Resolve IV is over
				passedLastStage = true;
			}
			if (!user.level().isClientSide()) {
				sync(user, true);
			}
		//}
	}


	//public void soulAddResolveLook() {
	//	setResolveValue(getResolveValue() + getMaxResolveValue() / 60);
	//}

	//public void soulAddResolveTeammate() {
	//	setResolveValue(getResolveValue() + getMaxResolveValue() / 300);
	//}
	
	
	
	
	
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

		else if (dmgSource.getEntity() instanceof LivingEntity attacker) {
			StandPower attackerStand = StandPower.get(attacker);
			if (attackerStand != null && attackerStand.isSummoned()) {
				addResolve(attackerStand, target, points * 0.5F);
			}
		}
	}
    

	public static void addResolve(StandPower attackerStand, LivingEntity attackTarget, float dmgAmount) {
		if (attackerStand == null || !attackerStand.usesResolve()) return;
		attackTarget = StandUtil.getStandUser(attackTarget);
		LivingEntity attacker = attackerStand.getUser();
		boolean hitSelf = attackTarget != null && attacker != null && attackTarget.is(attacker);
		if (!hitSelf && attackTarget.isAlive() && attackingTargetGivesResolve(attackTarget)) {
			ResolveCounter resolve = attackerStand.getResolveCounter();
			float points = dmgAmount * RESOLVE_FOR_DMG_POINT;

			//for (PowerClass<?> classification : PowerClass.values()) {
			//	points *= classification.getOptional(attackTarget).map(power -> {
			//		if (power.hasPower()) {
			//			return power.getPowerType().getTargetResolveMultiplier(getThis(), attackerStand);
			//		}
			//		return 1F;
			//	}).orElse(1F);
			//}

			if (ResolveModeEffect.getResolveEffectLvl(attackTarget) >= 0) {
				points *= 1 + resolve.getResolveBarFill() * 3;
			}

			float multiplier = resolve.totalMultiplier(attacker);
			resolve.addResolveValue(attackerStand, points * multiplier);
		}
	}

	public static boolean attackingTargetGivesResolve(Entity target) {
		if (target.getClassification(false) == MobCategory.MONSTER || target.getType() == EntityType.PLAYER) {
			return true;
		}
		if (target instanceof LivingEntity livingEntity) {
			if (livingEntity instanceof StandEntity) {
				return true;
			}
			if (livingEntity instanceof Mob mob) {
				return livingEntity instanceof Monster || mob.isAggressive();
			}
		}
		return false;
	}
	
	
	public float totalMultiplier(LivingEntity user) {
		return missingHpMultiplier(user, 0);
	}
	
	public float missingHpMultiplier(LivingEntity user, float dmgBeingTaken) {
		float hpResulting = Math.max(user.getHealth() - dmgBeingTaken, 0);
		float maxHealth = user.getMaxHealth();
		float upperBar = maxHealth * 0.5f;
		if (hpResulting <= upperBar) {
			float ratio = 1 + 3 * Mth.inverseLerp(hpResulting, upperBar, 5.0f);
			return ratio;
		}
		return 1;
	}

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void resolveOnTakingDamage(LivingDamageEvent.Pre event) {
    	LivingEntity target = event.getEntity();
    	StandPower stand = StandPower.get(target);
    	if (stand != null && stand.usesResolve()) {
    		LivingEntity user = target;
    		ResolveCounter resolve = stand.getResolveCounter();
    		DamageSource dmgSource = event.getSource();
    		float dmgAmount = event.getNewDamage();
    		
    		Entity attacker = dmgSource.getEntity();
    		if (attacker != null && !attacker.level().isClientSide() && stand.usesResolve() && attacker != null && !attacker.is(user)) {
    			float missingHpMult = resolve.missingHpMultiplier(user, dmgAmount);
    			if (missingHpMult > 1) {
    				float points = dmgAmount * RESOLVE_FOR_DMG_TAKEN;
    				float multiplier = resolve.totalMultiplier(user)
    						/ resolve.missingHpMultiplier(user, 0) * missingHpMult; // correcting the multiplier to count for the user's hp *after* the hit
    				resolve.addResolveValue(stand, points * multiplier);
    			}
    		}
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
        	float dmgReduction = ResolveStageBuffs.getDamageResistance(stand, stand.resolveCounter, target);
        	if (dmgReduction > 0) {
        		event.setNewDamage(event.getNewDamage() * (1 - dmgReduction));
        	}
        }
    }
    
}
