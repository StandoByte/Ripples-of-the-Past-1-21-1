package com.github.standobyte.jojo.adventure.npc.ai;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.adventure.npc.NpcPlayerActions;
import com.github.standobyte.jojo.adventure.npc.ai.player_crutch._MobAIVanillaClassesHelper;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;

public class NpcCombatAiPrototype implements Targeting {
	protected LivingEntity entity;
	protected Mob _entityAsMob;
    protected LivingEntity mainTarget;

	public NpcCombatAiPrototype(LivingEntity entity) {
		this.entity = entity;
		this._entityAsMob = entity instanceof Mob mob ? mob : null;
		updateFollowRange();
	}

	@Override
	public LivingEntity getTarget() {
		return mainTarget;
	}
	
	public void setMainTarget(LivingEntity target) {
		this.mainTarget = target;
		if (_entityAsMob != null) {
			_entityAsMob.setTarget(target);
		}
	}


	public double followRange;
	protected boolean prevTickHadTarget;
	public void updateFollowRange() {
		followRange = this.entity.getAttributeValue(Attributes.FOLLOW_RANGE);
	}
	
    public void tick() {
    	updateFollowRange();
    	
		if (_entityAsMob != null) {
			mainTarget = _entityAsMob.getTarget();
		}
    	if (mainTarget != null && !canAttackTarget(mainTarget, true)) {
    		setMainTarget(null);
    	}
    	if (mainTarget == null) {
    		setMainTarget(findTarget());
    	}
    	boolean hasTarget = mainTarget != null;
    	
    	tickTargetUpdated(hasTarget, mainTarget);
    	if (hasTarget) {
    		tickAttackTarget(mainTarget);
    	}
    }
    
    protected int unseenTargetTicks;
    public boolean canAttackTarget(LivingEntity attackTarget, boolean ticking) {
		if (!attackTarget.isAlive()
				|| !entity.canAttack(attackTarget)
				|| attackTarget.isSpectator()
				|| attackTarget instanceof Player targetPlayer && targetPlayer.isCreative()
				//|| _entityAsMob != null && !_entityAsMob.isWithinRestriction(attackTarget.blockPosition())
				) {
			return false;
		}
		else {
			Team team1 = this.entity.getTeam();
			Team team2 = attackTarget.getTeam();
			if (team1 != null && team1 == team2) {
				return false;
			}
		}

		if (ticking && !this.followingTargetEvenIfNotSeen) {
			PathNavigation navigation = _MobAIVanillaClassesHelper.getNavigation(entity, _entityAsMob);
			if (navigation.isDone()) {
				return false;
			}
		}
		
		if (this.entity.distanceToSqr(attackTarget) > followRange * followRange) {
			return false;
		}
		else {
			Sensing sensing = _MobAIVanillaClassesHelper.getSensing(entity, _entityAsMob);
			if (sensing.hasLineOfSight(attackTarget)) {
				if (ticking) {
					this.unseenTargetTicks = 0;
				}
			}
			else {
				if (!ticking || ++this.unseenTargetTicks > 60) {
					return false;
				}
			}
		}
		
		return true;
    }
    
    @Nullable
    public LivingEntity findTarget() {
    	Level level = entity.level();
    	AABB targetSearchArea = entity.getBoundingBox().inflate(followRange, 4, followRange);
    	TargetingConditions targetConditions = TargetingConditions.forCombat().range(followRange).selector(null);
    	List<? extends LivingEntity> entities = level.getEntitiesOfClass(Monster.class, targetSearchArea, 
    			e -> canAttackTarget(e, false));
    	LivingEntity target = level.getNearestEntity(entities, targetConditions, entity,
    			entity.getX(), entity.getEyeY(), entity.getZ());
    	return target;
    }
    
    
    // copied all that from MeleeAttackGoal

    protected double speedModifier = 1;
    protected boolean followingTargetEvenIfNotSeen;
    protected Path path;
    protected double pathedTargetX;
    protected double pathedTargetY;
    protected double pathedTargetZ;
    protected int ticksUntilNextPathRecalculation;
    protected int ticksUntilNextAttack;
    protected int attackInterval = 20;
    protected long lastCanUseCheck;
    protected static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
    protected int failedPathFindingPenalty = 0;
    protected boolean canPenalize = false;
    
    protected void tickTargetUpdated(boolean hasTarget, LivingEntity target) {
    	PathNavigation navigation = _MobAIVanillaClassesHelper.getNavigation(entity, _entityAsMob);
    	if (!prevTickHadTarget && hasTarget) {
    		boolean canUse;
    		if (canPenalize) {
    			if (--this.ticksUntilNextPathRecalculation <= 0) {
    				this.path = navigation.createPath(target, 0);
    				this.ticksUntilNextPathRecalculation = 4 + this.entity.getRandom().nextInt(7);
    				canUse = this.path != null;
    			}
    			else {
    				canUse = true;
    			}
    		}
    		else {
    			this.path = navigation.createPath(target, 0);
    			canUse = this.path != null ? true : isWithinMeleeAttackRange(this.entity, target);
    		}
            
    		navigation.moveTo(this.path, speedModifier);
    		if (_entityAsMob != null) {
    			_entityAsMob.setAggressive(true);
    		}
    		this.ticksUntilNextPathRecalculation = 0;
    		this.ticksUntilNextAttack = 0;
    	}
    	else if (prevTickHadTarget && !hasTarget) {
    		if (_entityAsMob != null) {
    			_entityAsMob.setAggressive(false);
    		}
    		navigation.stop();
    	}
    	prevTickHadTarget = hasTarget;
    }

    public static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F;
    public static boolean isWithinMeleeAttackRange(LivingEntity attacker, LivingEntity target) {
    	Entity entity = attacker.getVehicle();
    	AABB aabb;
    	if (entity != null) {
    		AABB aabb1 = entity.getBoundingBox();
    		AABB aabb2 = attacker.getBoundingBox();
    		aabb = new AABB(
    				Math.min(aabb2.minX, aabb1.minX),
    				aabb2.minY,
    				Math.min(aabb2.minZ, aabb1.minZ),
    				Math.max(aabb2.maxX, aabb1.maxX),
    				aabb2.maxY,
    				Math.max(aabb2.maxZ, aabb1.maxZ));
    	} else {
    		aabb = attacker.getBoundingBox();
    	}
    	double attackReach = attacker.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
    	AABB attackBoundingBox = aabb.inflate(attackReach, 0.0, attackReach);
    	
    	return attackBoundingBox.intersects(target.getHitbox());
    }
    
    public void tickAttackTarget(LivingEntity target) {
    	LookControl lookControl = _MobAIVanillaClassesHelper.getLookControl(entity, _entityAsMob);
    	Sensing sensing = _MobAIVanillaClassesHelper.getSensing(entity, _entityAsMob);
    	lookControl.setLookAt(target, 30.0F, 30.0F);
    	this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
    	if ((this.followingTargetEvenIfNotSeen || sensing.hasLineOfSight(target))
    			&& this.ticksUntilNextPathRecalculation <= 0
    			&& (
    					this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
    					|| target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0
    					|| this.entity.getRandom().nextFloat() < 0.05F
    					)) {
    		this.pathedTargetX = target.getX();
    		this.pathedTargetY = target.getY();
    		this.pathedTargetZ = target.getZ();
    		this.ticksUntilNextPathRecalculation = 4 + this.entity.getRandom().nextInt(7);
    		PathNavigation navigation = _MobAIVanillaClassesHelper.getNavigation(entity, _entityAsMob);
    		double d0 = this.entity.distanceToSqr(target);
    		if (this.canPenalize) {
    			this.ticksUntilNextPathRecalculation += failedPathFindingPenalty;
    			if (navigation.getPath() != null) {
    				Node finalPathPoint = navigation.getPath().getEndNode();
    				if (finalPathPoint != null && target.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
    					failedPathFindingPenalty = 0;
    				else
    					failedPathFindingPenalty += 10;
    			} else {
    				failedPathFindingPenalty += 10;
    			}
    		}	
    		if (d0 > 1024.0) {
    			this.ticksUntilNextPathRecalculation += 10;
    		} else if (d0 > 256.0) {
    			this.ticksUntilNextPathRecalculation += 5;
    		}

    		boolean stayInPlace = isWithinMeleeAttackRange(entity, target);
    		if (stayInPlace) {
    			navigation.moveTo((Path) null, this.speedModifier);
    		}
    		else if (!navigation.moveTo(target, this.speedModifier)) {
    			this.ticksUntilNextPathRecalculation += 15;
    		}
    	}

    	this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
    	this.checkAndPerformAttack(target);
    }
    
    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();
            this.entity.swing(InteractionHand.MAIN_HAND);
            NpcPlayerActions.emulatePlayerAttack(this.entity, target);
        }
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = 20;
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected boolean canPerformAttack(LivingEntity target) {
        return this.isTimeToAttack()
        		&& isWithinMeleeAttackRange(this.entity, target)
        		&& _MobAIVanillaClassesHelper.getSensing(this.entity, _entityAsMob).hasLineOfSight(target);
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

}
