package com.github.standobyte.jojo.mechanics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mc.entity.BlockShardEntity;
import com.github.standobyte.jojo.mechanics.CollisionHelper.BlockCollisionResult;
import com.github.standobyte.jojo.mechanics.explosion.CustomExplosion;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas.BlockMiningTier;
import com.github.standobyte.jojo.powersystem.standpower.resolve.ResolveCounter;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.NBTUtil;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.entitycomponent.TickingEntityData;
import com.github.standobyte.jojo.util.java.ReuseableStream;
import com.github.standobyte.jojo.util.mc.AttributeUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility.HeavyPunchExplosion;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBlockBulletAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class KnockbackCollisionImpact implements TickingEntityData, INBTSerializable<CompoundTag> {
	protected final Entity entity;
	protected final LivingEntity asLiving;

	protected LivingEntity attacker;
	protected LivingEntity attackerStandUser;
	protected boolean attackerIsStand;
	protected Vec3 knockbackVec = null;
	protected double knockbackImpactStrength;
	protected double minCos;
	protected boolean hadImpactWithBlock = false;
	protected Vec3 prevTickPos;

	protected float explosionRadius = 0;
	protected DamageSource explosionDmgSource;
	protected float explosionDamage;
	public List<BlockPos> blocksDestroyedByLastExplosion;

	public float syoPunchBaseDamage = 0;
	public int scarletOverdriveFireTicks = 0;
	public ParticleOptions hamonParticles;

	public KnockbackCollisionImpact(Entity entity) {
		this.entity = entity;
		this.asLiving = entity instanceof LivingEntity ? (LivingEntity) entity : null;
		addTicking(entity);
	}


	public KnockbackCollisionImpact onPunchSetKnockbackImpact(Vec3 knockbackVec, LivingEntity attacker) {
		double kbMultiplier = 1 - (asLiving != null ? AttributeUtil.getValueOrDefault(asLiving, Attributes.KNOCKBACK_RESISTANCE, 0) : 0);
		if (kbMultiplier <= 0) return this;

		this.knockbackImpactStrength = knockbackVec.length();
		this.knockbackVec = knockbackVec.scale(1 / knockbackImpactStrength);
		this.knockbackImpactStrength *= kbMultiplier;
		this.minCos = 1;
		this.hadImpactWithBlock = false;
		this.attacker = attacker;
		this.attackerStandUser = attacker instanceof LivingEntity ? (StandUtil.getStandUser((LivingEntity) attacker)) : null;
		this.attackerIsStand = attacker instanceof StandEntity;
		this.blocksDestroyedByLastExplosion = null;
		return this;
	}

	public KnockbackCollisionImpact withImpactExplosion(float radius, DamageSource aoeDamageSource, float aoeDamage) {
		if (this.knockbackVec == null) return this;

		this.explosionRadius = radius;
		this.explosionDmgSource = aoeDamageSource;
		this.explosionDamage = aoeDamage;
		return this;
	}

//	public KnockbackCollisionImpact hamonDamage(float punchBaseDamage, int fireTicks, ParticleOptions sparkParticles) {
//		if (this.knockbackVec == null) return this;
//
//		this.syoPunchBaseDamage = punchBaseDamage;
//		this.scarletOverdriveFireTicks = fireTicks;
//		this.hamonParticles = sparkParticles;
//		return this;
//	}

	public void reset() {
		this.knockbackVec = null;
		this.knockbackImpactStrength = 0;
		this.explosionRadius = 0;
		this.explosionDmgSource = null;
		this.explosionDamage = 0;
		this.syoPunchBaseDamage = 0;
		this.scarletOverdriveFireTicks = 0;
		this.hamonParticles = null;
	}

	public void setKnockbackImpactStrength(double strength) {
		if (strength <= 0) {
			reset();
		}
		else {
			this.knockbackImpactStrength = strength;
		}
	}

	public boolean collideBreakBlocks(Vec3 movementVec, Vec3 collidedVec, Level level) {
		if (!isActive() || movementVec.lengthSqr() < 1E-07) {
			return false;
		}

		boolean canBreakBlocks = JojoModUtil.breakingBlocksEnabled(level);
		boolean collidedWithBlocks = !movementVec.equals(collidedVec);
		collideBoundingBox(entity, movementVec, collidedWithBlocks, canBreakBlocks);
		return canBreakBlocks && collidedWithBlocks;
	}

	@Override
	public void tick() {
		if (isActive()) {
			if (knockbackImpactStrength <= 0) {
				reset();
				return;
			}

			Vec3 deltaMovement = entity.getDeltaMovement();
			if (Math.abs(deltaMovement.x) < 1E-7 && Math.abs(deltaMovement.z) < 1E-7) {
				reset();
				return;
			}

			double deltaMovementLen = deltaMovement.length();
			Vec3 deltaMovementNormalized = deltaMovement.scale(1 / deltaMovementLen);
			
			double cos = deltaMovementNormalized.dot(knockbackVec);
			if (cos <= 0) {
				reset();
				return;
			}
			minCos = Math.min(minCos, cos);
			
			knockbackImpactStrength = Math.min(knockbackImpactStrength, deltaMovementLen);

//			// spiders get stuck in cave corners not triggering the impact, so we try to manually trigger it here
//			Vec3 entityPos = entity.position();
//			if (prevTickPos != null && Math.abs(prevTickPos.x - entityPos.x) < 1E-7 && Math.abs(prevTickPos.z - entityPos.z) < 1E-7) {
//				collideBreakBlocks(deltaMovement, deltaMovement, entity.level());
//			}
//			prevTickPos = entityPos;
		}
	}

	public double getKnockbackImpactStrength() {
		return knockbackImpactStrength * minCos;
	}

	public void setHadImpactWithBlock() {
		hadImpactWithBlock = true;
	}

	public boolean getHadImpactWithBlock() {
		return hadImpactWithBlock;
	}

	public boolean isActive() {
		return knockbackVec != null;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		if (isActive()) {
			Vec3.CODEC.encodeStart(NbtOps.INSTANCE, knockbackVec).ifSuccess(vecNbt -> nbt.put("Vec", vecNbt));
			nbt.putDouble("MinCos", minCos);
			nbt.putBoolean("HadBlockImpact", hadImpactWithBlock);
			nbt.putFloat("ExplosionRadius", explosionRadius);
			nbt.putFloat("ExplosionDamage", explosionDamage);

			nbt.putFloat("HamonPunchDmg", syoPunchBaseDamage);
			nbt.putInt("HamonFireTicks", scarletOverdriveFireTicks);
			NBTUtil.put(nbt, "HamonSparks", hamonParticles, ParticleTypes.CODEC);
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		knockbackVec = Vec3.CODEC.decode(NbtOps.INSTANCE, nbt.get("Vec")).result().map(
				com.mojang.datafixers.util.Pair::getFirst).orElse(null);
		if (knockbackVec != null) {
			knockbackImpactStrength = nbt.getDouble("Power");
			minCos = nbt.getDouble("MinCos");
			hadImpactWithBlock = nbt.getBoolean("HadBlockImpact");
			explosionRadius = nbt.getFloat("ExplosionRadius");
			explosionDamage = nbt.getFloat("ExplosionDamage");

			syoPunchBaseDamage = nbt.getFloat("HamonPunchDmg");
			scarletOverdriveFireTicks = nbt.getInt("HamonFireTicks");
			hamonParticles = NBTUtil.getOptional(nbt, "HamonSparks", ParticleTypes.CODEC).orElse(null);
		}
	}



	protected void collideBoundingBox(Entity entity, Vec3 movementVec, boolean collideBlocks, boolean breakBlocks) {
		Level level = entity.level();
		if (level.isClientSide()) return;
		LivingEntity living = entity instanceof LivingEntity __ ? __ : null;

		AABB aabb = entity.getBoundingBox().inflate(0.25);
		CollisionContext selectionContext = CollisionContext.of(entity);
		ServerLevel serverWorld = (ServerLevel) level;

		VoxelShape worldBorder = level.getWorldBorder().getCollisionShape();
		ReuseableStream<VoxelShape> worldBorderCollision = new ReuseableStream<>(
				Shapes.joinIsNotEmpty(worldBorder, Shapes.create(aabb.deflate(1.0E-7D)), BooleanOp.AND) ? Stream.empty() : Stream.of(worldBorder));

		ReuseableStream<Pair<Entity, VoxelShape>> potentialEntityCollisions = new ReuseableStream<>(
				CollisionHelper.getEntityCollisions(level, entity, aabb.expandTowards(movementVec), 
						EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(
								e -> e.isPickable()
								&& (attackerStandUser == null || JojoModUtil.canHarm(attackerStandUser, e))
								&& !(living != null && !JojoModUtil.canHarm(living, e))
								)));
		Collection<Entity> entitiesCollided = new ArrayList<>();
		CollisionHelper.collideEntities(aabb, movementVec, level, 
				worldBorderCollision, potentialEntityCollisions, 
				selectionContext, entitiesCollided);

		if (!entitiesCollided.isEmpty()) {
			Vec3 vec = entity.getDeltaMovement();

			entitiesCollided.forEach(targetEntity -> {
				LivingEntity asLiving = targetEntity instanceof LivingEntity __ ? __ : null;
				onCollideWith(targetEntity, asLiving, vec);
			});
		}


		MutableBoolean doGlassBleeding = new MutableBoolean();
		float bleedingChance = asLiving != null ? BlockShardEntity.glassShardBleedingChance(asLiving) : 0;

		MutableFloat wallDamage = new MutableFloat(0);

		if (collideBlocks) {
			BlockCollisionResult collision = CollisionHelper.collideBoundingBox(movementVec, aabb, serverWorld, selectionContext);
			
			MutableFloat impactStrengthNew = new MutableFloat(knockbackImpactStrength);
			if (collision.blocks.size() > 0) {
				collision.blocks.stream()
				.distinct()
				.sorted(Comparator.comparingDouble(block -> {
					AABB blockBB = block.getRight().bounds();
					return MathUtil.getManhattanDist(blockBB, entity.getBoundingBox());
				}))
				.map(Pair::getLeft)
				.allMatch(blockPos -> {
					BlockState blockState = level.getBlockState(blockPos);
					float hardness = StandStatFormulas.getBlockHardness(BlockMiningTier.EMPTY_ARMS, blockState, level, blockPos);
					float useImpactStrength = 0;
					if (hardness >= 0) {
						useImpactStrength = hardness * 0.05f;
					}
					else if (hardness < 0) {
						useImpactStrength = 1;
					}
					if (useImpactStrength > 0) {
						setHadImpactWithBlock();
						float impactLeft = (float) getKnockbackImpactStrength();
						if (impactLeft < useImpactStrength) {
							useImpactStrength = (impactLeft + useImpactStrength) / 2;
						}
						useImpactStrength = Math.min(impactLeft, useImpactStrength);

						float damage = useImpactStrength * 4;
						wallDamage.add(damage);

						blockState.entityInside(serverWorld, blockPos, entity);

						// episode #158 of me being on the spectrum
						if (!doGlassBleeding.booleanValue() && asLiving != null 
								&& CrazyDBlockBulletAbility.isGlassBlock(blockState, level, blockPos)
								&& asLiving.getRandom().nextFloat() < bleedingChance) {
							doGlassBleeding.setTrue();
						}
						if (blockState.getBlock() instanceof CactusBlock) {
							hurtTarget(entity, level.damageSources().cactus(), 2);
						}
						if (entity.isOnFire()) {
							JojoModUtil.blockCatchFire(level, blockPos, blockState, null, asLiving);
						}

						impactStrengthNew.setValue(impactStrengthNew.floatValue() - Math.max(useImpactStrength, 0.05f));
					}

					return impactStrengthNew.floatValue() > 0;
				});

				Vec3 collisionDir = new Vec3(collision.movementX - collision.x, collision.movementY - collision.y, collision.movementZ - collision.z);
				Direction faceHit = Direction.getNearest(collisionDir.x, collisionDir.y, collisionDir.z);
				if (breakBlocks) {
					if (explosionRadius > 0) {
						AABB entityBB = entity.getBoundingBox();
						Vec3 hitPos = new Vec3(
								Mth.lerp(faceHit.getStepX() * 0.5 + 0.5, entityBB.minX, entityBB.maxX), 
								Mth.lerp(faceHit.getStepY() * 0.5 + 0.5, entityBB.minY, entityBB.maxY), 
								Mth.lerp(faceHit.getStepZ() * 0.5 + 0.5, entityBB.minZ, entityBB.maxZ));
						BlockPos hitBlockPos = BlockPos.containing(hitPos.add(Vec3.atBottomCenterOf(faceHit.getNormal()).scale(0.5)));

						HeavyPunchExplosion explosion = new HeavyPunchExplosion(level, attacker, new ActionTarget(hitBlockPos, faceHit.getOpposite()), 
								movementVec, explosionDmgSource, 
								hitPos.x, hitPos.y, hitPos.z, 
								explosionRadius, false, Explosion.BlockInteraction.DESTROY)
								.aoeDamage(explosionDamage)
								.entityNoDamage(entity);
						if (CustomExplosion.explode(explosion)) {
							this.blocksDestroyedByLastExplosion = explosion.getToBlow();
							if (doGlassBleeding.booleanValue()) {
								BlockShardEntity.glassShardBleeding(asLiving);
							}
						}
					}
				}

				if (wallDamage.floatValue() > 0) {
					hurtTarget(entity, level.damageSources().flyIntoWall(), wallDamage.floatValue());
				}

//				setKnockbackImpactStrength(impactStrengthNew.floatValue());
				reset();
			}
		}

	}

	protected boolean onCollideWith(Entity target, LivingEntity targetAsLiving, Vec3 thisEntityMotion) {
		if (targetAsLiving != null) {
			// TODO kb impact with SYO
//			if (syoPunchBaseDamage > 0) {
//			DamageUtil.dealHamonDamage(targetAsLiving, syoPunchBaseDamage * 0.5f, entity, attacker, attack -> {
//				if (hamonParticles != null) {
//					attack.hamonParticle(hamonParticles);
//				}
//			});
//		}
			boolean hurt = DamageUtil.dealDamageAndSetOnFire(target, 
					e -> {
						DamageSource dmgSource = DamageUtil.make(target.level(), 
								ModDamageTypes.ENTITY_FLEW_INTO, entity, entity, null);
						return hurtTarget(e, dmgSource, (float) getKnockbackImpactStrength() * 5);
					}, 
					scarletOverdriveFireTicks, false);
			if (hurt) {
				targetAsLiving.knockback((float) getKnockbackImpactStrength(), -thisEntityMotion.x, -thisEntityMotion.z);
			}
			return hurt;
		}

		return false;
	}

	protected boolean hurtTarget(Entity target, DamageSource dmgSource, float amount) {
		boolean hurt = DamageUtil.hurtThroughInvulTicks(target, dmgSource, amount);
		if (attackerIsStand && attackerStandUser != null && target instanceof LivingEntity targetLiving) {
			StandPower attackerStand = StandPower.get(attackerStandUser);
			if (attackerStand != null) {
				ResolveCounter.addResolve(attackerStand, targetLiving, amount);
			}
		}
		return hurt;
	}



	public static KnockbackCollisionImpact getHandler(Entity entity) {
		return entity.getData(ModDataAttachmentTypes.KB_IMPACT);
	}
}
