package com.github.standobyte.jojo.adventure.npc.ai.player_crutch;

import java.util.Map;

import com.github.standobyte.jojo.adventure.npc.ai.NpcCombatAiPrototype;
import com.google.common.collect.Maps;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.pathfinder.PathType;

public class _MobAIVanillaClassesHelper {
	public LookControl lookControl;
	public MoveControl moveControl;
	public JumpControl jumpControl;
	public PathNavigation navigation;
	public Sensing sensing;
	public BodyRotationControl bodyRotationControl;
	public final Map<PathType, Float> pathfindingMalus = Maps.newEnumMap(PathType.class);
	public NpcCombatAiPrototype customCombatAI;

	
	public _MobAIVanillaClassesHelper(LivingEntity player) {
		lookControl = new LookControl2(this, player);
		moveControl = new MoveControl2(this, player);
		jumpControl = new JumpControl2(this, player);
		navigation = new GroundPathNavigation2(this, player);
		sensing = new Sensing2(this, player);
		bodyRotationControl = new BodyRotationControl2(this, player);
		
		pathfindingMalus.put(PathType.DANGER_FIRE, 16.0F);
		pathfindingMalus.put(PathType.DAMAGE_FIRE, -1.0F);
		
		customCombatAI = new NpcCombatAiPrototype(player);
	}
	
	
	public void tickPre() {
		sensing.tick();
	}
	
	//customCombatAI.tick();

	public void tickPost() {
		navigation.tick();

		moveControl.tick();
		lookControl.tick();
		jumpControl.tick();
	}

	public float tickHeadTurn(float yRot, float animStep) {
		this.bodyRotationControl.clientTick();
		return animStep;
	}


	public static MoveControl getMoveControl(LivingEntity entity, Mob asMob) {
		if (asMob != null) return asMob.getMoveControl();
		_MobAIVanillaClassesHelper playerAI = getPlayerHelper(entity);
		return playerAI.moveControl;
	}

	public static LookControl getLookControl(LivingEntity entity, Mob asMob) {
		if (asMob != null) return asMob.getLookControl();
		_MobAIVanillaClassesHelper playerAI = getPlayerHelper(entity);
		return playerAI.lookControl;
	}

	public static JumpControl getJumpControl(LivingEntity entity, Mob asMob) {
		if (asMob != null) return asMob.getJumpControl();
		_MobAIVanillaClassesHelper playerAI = getPlayerHelper(entity);
		return playerAI.jumpControl;
	}

	public static PathNavigation getNavigation(LivingEntity entity, Mob asMob) {
		if (asMob != null) return asMob.getNavigation();
		_MobAIVanillaClassesHelper playerAI = getPlayerHelper(entity);
		return playerAI.navigation;
	}

	public static Sensing getSensing(LivingEntity entity, Mob asMob) {
		if (asMob != null) return asMob.getSensing();
		_MobAIVanillaClassesHelper playerAI = getPlayerHelper(entity);
		return playerAI.sensing;
	}


	public static void setZza(LivingEntity entity, float strafeForwards) {
		entity.zza = strafeForwards;
	}

	public static void setXxa(LivingEntity entity, float strafeRight) {
		entity.xxa = strafeRight;
	}
	
	// the only things that are ACTUALLY FUCKING SPECIFIC TO MOB ENTITY

	public static float getHeadRotSpeed(LivingEntity entity) {
		return 10;
	}

	public static float getMaxHeadXRot(LivingEntity entity) {
		return 90;
	}

	public static float getMaxHeadYRot(LivingEntity entity) {
		return 75;
	}
	
	public static float getPathfindingMalus(LivingEntity entity, PathType pathType) {
		LivingEntity pathfindingEntity = entity;
		if (entity.getControlledVehicle() instanceof Mob vehicle) {
			// protected method in Mob, i ain't adding that to the ATs
			boolean shouldPassengersInheritMalus = vehicle instanceof Strider;
			if (shouldPassengersInheritMalus) {
				pathfindingEntity = vehicle;
			}
		}

		return _getPathfindingMalus(pathfindingEntity, pathType);
	}
	
	public static float _getPathfindingMalus(LivingEntity pathfindingEntity, PathType pathType) {
		if (pathfindingEntity instanceof Mob mob) {
			return mob.getPathfindingMalus(pathType);
		}

		_MobAIVanillaClassesHelper playerAI = getPlayerHelper(pathfindingEntity);
		Map<PathType, Float> pathfindingMalus = playerAI.pathfindingMalus;
		Float f = pathfindingMalus.get(pathType);
		return f == null ? pathType.getMalus() : f;
	}
	
	
	public static _MobAIVanillaClassesHelper getPlayerHelper(LivingEntity player) {
		return null;
	}

}
