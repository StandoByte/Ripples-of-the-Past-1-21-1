package com.github.standobyte.jojoimpl.stands._entitybase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.core.packet.fromserver.BrokenBlocksParticlesAndSoundsPacket;
import com.github.standobyte.jojo.init.ModCustomExplosions;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.mc.entity.BlockShardEntity;
import com.github.standobyte.jojo.mechanics.KnockbackCollisionImpact;
import com.github.standobyte.jojo.mechanics.explosion.CustomExplosion;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.damage.RipplesModifiedDamageSource;
import com.github.standobyte.jojo.util.network.StreamCodecs;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBlockBulletAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.BrokenBlocksChunkData;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.PrevBlockInfo;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class StandEntityHeavyPunchAbility extends StandEntityAbility {
	public boolean verticalKnockback = false;

	public StandEntityHeavyPunchAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandEntityHeavyPunch::new);
		usageGroup = AbilityUsageGroup.COMBAT;
		setDefaultPhaseLength(ActionPhase.WINDUP, StandStatFormulas.getHeavyAttackWindup(8, 0));
		setDefaultPhaseLength(ActionPhase.PERFORM, 6);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 12);
		noFinisherBarDecay = true;
	}

	@Override
	public Ability replaceWithSubAbility(Power<?> context, AvailableAbilities abilities) {
		StandPower standPower = PowerClass.STAND.cast(context);
		if (standPower != null) {
			StandEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				if (LivingComponentGrab.getEntityGrabbedBy(standEntity) != null) {
					return abilities.getContextVariation("grab_heavy_punch");
				}
			}
		}

		return super.replaceWithSubAbility(context, abilities);
	}


	@Override
	public void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		super.initActionFromConfig(action, level, powerUser, performer);
		((StandEntityHeavyPunch) action).verticalKnockback = this.verticalKnockback;
		if (!level.isClientSide() && performer instanceof StandEntity stand) {
			action.phasesLength.put(ActionPhase.WINDUP, StandStatFormulas.getHeavyAttackWindup(stand.getAttackSpeed(), stand.getFinisherMeter()));
		}
	}

	public static class StandEntityHeavyPunch extends EntityActionInstance {
		protected LivingEntity punchTarget;
		public boolean verticalKnockback = false;
		public float finisherValue;
		public boolean playedSwingSound;
		public boolean playedStandCrySound;

		public StandEntityHeavyPunch(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(0, 2, StandOffsetFromUser.Rotations.HEAD_XY, false);
			keepStandAimedAtTarget();
			aimAs = AimingEntity.STAND;
			if (performer instanceof StandEntity stand) {
				finisherValue = stand.getFinisherMeter();
				if (isGrabVariation() && stand.offsetFromUser.grabIdleOffset != null) {
					stand.offsetFromUser.setOffset(
							stand.offsetFromUser.grabIdleOffset, 
							StandOffsetFromUser.Rotations.HEAD);
				}
			}
			tossStandHeldItems(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);
		}

		@Override
		public void actionTick() {
			Level level = performer.level();
			if (level.isClientSide() && ClientGlobals.canHearStands && !(playedSwingSound && playedStandCrySound) && performer instanceof StandEntity stand) {
				if (!playedSwingSound) {
					// how many ticks are left before the start of the 'perform' phase (when actionPerformStart() is called)
					int ticksDiff = (int) (calcFullTicks(ActionPhase.PERFORM, 0) - getFullTicksPassed());
					if (ticksDiff <= 4) {
						level.playLocalSound(stand.getX(), stand.getEyeY(), stand.getZ(), ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_HEAVY_SWING.get(), stand), 
								stand.getSoundSource(), 1, 1, false);
						playedSwingSound = true;
					}
				}

				if (!playedStandCrySound) {
					if (!stand.isArmsOnlyMode()) {
						ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_HEAVY_CRY.get(), stand), 
								stand.getSoundSource(), 1, 1, stand, stand.level()));
					}
					playedStandCrySound = true;
				}
			}
			
			if (isGrabVariation() && punchTarget == null) {
				int ticksDiff = (int) (calcFullTicks(ActionPhase.PERFORM, 0) - getFullTicksPassed());
				if (ticksDiff <= 4) {
					LivingComponentGrab standGrab = performer.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
					if (standGrab != null) {
						LivingEntity grabbed = standGrab.getGrabbedEntity();
						if (grabbed != null) {
							punchTarget = grabbed;
							
							if (!level.isClientSide()) {
								standGrab.setGrabTarget(null);
								grabbed.setDeltaMovement(0, 0.75, 0);
								grabbed.hurtMarked = true;
							}
						}
					}
				}
			}
		}

		@Override
		public void actionPerformStart() {
			Level level = level();
			if (performer instanceof StandEntity stand) {
				ActionTarget target = getPunchTarget(stand);
				if (!level.isClientSide()) {
					StandPower standPower = StandPower.get(getPowerUser());

					if (StandEntityPunchAbility.playHitSound(target, level)) {
						StandUtil.broadcastSound((ServerLevel) level, target.getCenterPos(), 
								ModSoundEvents.STAND_PUNCH_HEAVY, true, standPower, 
								stand.getSoundSource(), 1, 1);
					}
					DamageSource dmgSource = makePunchDamageSource();
					float dmgAmount = StandStatFormulas.getHeavyAttackDamage(stand.getAttackDamage());
					float explRadius = Math.min((float) stand.getAttackDamage() * 0.175f, 10);

					switch (target.getType()) {
						case ENTITY -> {
							Entity targetEntity = target.getMainEntity();
							if (targetEntity instanceof LivingEntity targetLiving) {
								RipplesModifiedDamageSource knockback = (RipplesModifiedDamageSource) dmgSource;
								if (verticalKnockback) {
									knockback.jojo_ripples$verticalKnockback(1, 0.8f);
								}
								else {
									knockback.jojo_ripples$modifyKnockback(1f, 1);
								}
								boolean hurt = standEntityAttack(stand, targetLiving, dmgSource, dmgAmount);

								if (hurt) {
									Entity knockedBack = targetEntity;
									
									EntityActionInstance targetAction = LivingComponentAction.getCurEntityAction(targetLiving);
									if (targetAction != null) {
										if (targetAction instanceof StandEntityBarrageAbility.StandEntityBarrage) {
											targetAction.setPhaseStart(ActionPhase.RECOVERY);
											targetAction.syncPhaseChanges();
										}
									}
									
									if (targetEntity instanceof StandEntity targetStand) {
										LivingEntity standUser = targetStand.getUser();
										if (standUser != null) {
											knockedBack = standUser;
										}
									}

									Entity _knockedBack = knockedBack;
									KnockbackCollisionImpact kbImpact = KnockbackCollisionImpact.getHandler(_knockedBack);
									if (kbImpact != null) {
										kbImpact
										.onPunchSetKnockbackImpact(_knockedBack.getDeltaMovement(), stand)
										.withImpactExplosion(Math.max(explRadius - 0.5f, 0), null, 0);
									}
								}
							}
						}
						case BLOCK -> {
							BlockPos blockPos = target.getBlockPos();
							Direction face = target.getFace();
							Vec3 pos = Vec3.atCenterOf(blockPos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.6));
							DamageSource aoeDmgSource = dmgSource;
							float aoeDmg = dmgAmount * 0.5f;
							HeavyPunchExplosion explosion = new HeavyPunchExplosion(level, stand, 
									new ActionTarget(blockPos, face), stand.getLookAngle(), 
									aoeDmgSource, 
									pos.x, pos.y, pos.z, 
									explRadius, false, 
									JojoModUtil.breakingBlocksEnabled(level) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP)
									.aoeDamage(aoeDmg)
									.createBlockShards(stand.getAttackDamage(), stand.getPrecision());
							CustomExplosion.explode(explosion);
						}
						default -> {}
					}

					punchedTarget = target;
					standPower.consumeStamina(10);
					stand.consumeFinisherMeter(1.0001f);
				}
				if (target.getType() == TargetType.ENTITY) {
					standRotationTarget = target;
				}
				else {
					aimAs = AimingEntity.CAMERA_ENTITY;
				}
			}
		}

		protected ActionTarget getPunchTarget(StandEntity stand) {
			if (isGrabVariation()) {
				return new ActionTarget(punchTarget);
			}
			return StandEntityPunchAbility.aimAtPunchTarget(stand);
		}

	}



	public static class HeavyPunchExplosion extends CustomExplosion {
		protected LivingEntity attacker;
		@Nullable protected StandEntity attackerAsStand;
		protected ActionTarget hitBlock;
		protected Vec3 explosionDirection;
		protected float aoeDamage;
		public boolean dropBlocks;

		protected boolean createBlockShards = false;
		protected double strength;
		protected double precision;
		protected List<Entity> noDamage = new ArrayList<>();


		public HeavyPunchExplosion(Level pLevel, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius) {
			super(pLevel, pToBlowX, pToBlowY, pToBlowZ, pRadius);
		}

		public HeavyPunchExplosion(Level pLevel, LivingEntity attacker, 
				ActionTarget hitBlock, Vec3 direction, 
				@Nullable DamageSource pDamageSource, 
				double pToBlowX, double pToBlowY, double pToBlowZ, 
				float pRadius, boolean pFire, Explosion.BlockInteraction pBlockInteraction) {
			super(pLevel, attacker, 
					pDamageSource, 
					pToBlowX, pToBlowY, pToBlowZ, 
					pRadius, pFire, pBlockInteraction, 
					null, null, null);
			this.attacker = attacker;
			this.attackerAsStand = attacker instanceof StandEntity ? (StandEntity) attacker : null;
			this.hitBlock = hitBlock;
			this.explosionDirection = direction.normalize();
		}

		public HeavyPunchExplosion createBlockShards(double strength, double precision) {
			this.createBlockShards = true;
			this.strength = strength;
			this.precision = precision;
			return this;
		}

		public HeavyPunchExplosion aoeDamage(float damage) {
			this.aoeDamage = damage;
			return this;
		}

		public HeavyPunchExplosion entityNoDamage(Entity entityNoDamage) {
			this.noDamage.add(entityNoDamage);
			return this;
		}


		@Override
		public float getEntityDamageAmount(Entity entity, double impact) {
			return aoeDamage;
		}

		@Override
		public Optional<Float> getBlockExplosionResistance(BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState) {
			return super.getBlockExplosionResistance(pLevel, pPos, pBlockState, pFluidState);
		}

		@Override
		public boolean shouldBlockExplode(BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, float pExplosionPower) {
			return pBlockState.getBlock() != Blocks.SPAWNER;
		}


		@Override
		public void finalizeExplosion(boolean pSpawnParticles) {
			super.finalizeExplosion(pSpawnParticles);
			remainingBlocksShockWave();
		}

		@Override
		protected void explodeBlocks() {
			if (level instanceof ServerLevel world) {
				List<BlockPos> toBlow = getToBlow();
				LivingEntity standUser = StandUtil.getStandUser(attacker);

				Map<BlockPos, BlockShardEntity[]> blockShardEntities = new HashMap<>();
				if (createBlockShards) {
					RandomSource random = attacker.getRandom();
					float shardsVelocity = 0.5f + (float) strength * 0.05f;
					double shardsInaccuracy = Math.max(100 - precision * 4.5, 0);

					shardsInaccuracy = Math.min(shardsInaccuracy * 0.0075, 1);
					Vec3 vecMaxAccuracy = explosionDirection.normalize();

					for (BlockPos blockPos : toBlow) {
						BlockState blockState = level.getBlockState(blockPos);
						if (CrazyDBlockBulletAbility.hardMaterial(blockState)) {
							BlockShardEntity[] shards = new BlockShardEntity[3];
							for (int i = 0; i < shards.length; i++) {
								BlockShardEntity blockShard = new BlockShardEntity(attacker, level, blockState, blockPos);
								blockShard.setPos(
										blockPos.getX() + random.nextDouble(),
										blockPos.getY() + random.nextDouble(),
										blockPos.getZ() + random.nextDouble());

								Vec3 vecMinAccuracy = blockShard.position().subtract(this.center()).normalize();
								Vec3 shootVec = new Vec3(
										Mth.lerp(shardsInaccuracy, vecMaxAccuracy.x, vecMinAccuracy.x),
										Mth.lerp(shardsInaccuracy, vecMaxAccuracy.y, vecMinAccuracy.y),
										Mth.lerp(shardsInaccuracy, vecMaxAccuracy.z, vecMinAccuracy.z));

								blockShard.shoot(shootVec.x, shootVec.y, shootVec.z, shardsVelocity, 4);
								shards[i] = blockShard;
							}
							blockShardEntities.put(blockPos, shards);
						}
					}
				}

				dropBlocks = JojoModUtil.dropBrokenBlock(standUser);
				JojoModUtil.destroyBlocksInBulk(toBlow, world, attacker, dropBlocks);

				if (!blockShardEntities.isEmpty()) {
					for (Map.Entry<BlockPos, BlockShardEntity[]> blockShards : blockShardEntities.entrySet()) {
						BlockPos pos = blockShards.getKey();
						BlockShardEntity[] shards = blockShards.getValue();

						for (Entity blockShard : shards) {
							level.addFreshEntity(blockShard);
						}

						BrokenBlocksChunkData brokenBlocks = BrokenBlocksChunkData.getChunkData(level, pos);
						if (brokenBlocks != null) {
							PrevBlockInfo brokenBlock = brokenBlocks.getBrokenBlockAt(pos);
							if (brokenBlock != null) {
								brokenBlock.withEntities(shards);
							}
						}
					}
				}
			}
		}

		@Override
		protected void filterEntities(List<Entity> entities) {
			Iterator<Entity> iter = entities.iterator();
			while (iter.hasNext()) {
				Entity entity = iter.next();
				if (!(entity instanceof LivingEntity && JojoModUtil.canHarm(attacker, entity)) || noDamage.contains(entity)) {
					iter.remove();
				}
			}
		}

		@Override
		protected void hurtEntity(Entity entity, float damage, Vec3 knockback) {
			if (attackerAsStand != null) {
				EntityActionInstance.standEntityAttack(attackerAsStand, entity, damageSource, damage);

				entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
				if (entity instanceof Player player) {
					if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
						getHitPlayers().put(player, knockback);
					}
				}
			}
		}

		@Override
		protected void lithiumPerformRayCast(RandomSource random, double vecX, double vecY, double vecZ, LongOpenHashSet touched) {
			// only break blocks in the direction of the punch, not behind the stand
			if (vecX * explosionDirection.x + vecY * explosionDirection.y + vecZ * explosionDirection.z >= 0) {
				super.lithiumPerformRayCast(random, vecX, vecY, vecZ, touched);
			}
		}

		protected void remainingBlocksShockWave() {
			if (!level.isClientSide()) {
				BrokenBlocksParticlesAndSoundsPacket blocksShockwaveVisual = new BrokenBlocksParticlesAndSoundsPacket();
				Vec3 pos = center();
				double radius = this.radius;
				int minX = Mth.floor(pos.x - radius);
				int minY = Mth.floor(pos.y - radius);
				int minZ = Mth.floor(pos.z - radius);
				int maxX = Mth.ceil(pos.x + radius);
				int maxY = Mth.ceil(pos.y + radius);
				int maxZ = Mth.ceil(pos.z + radius);
				boolean test = true;
				JojoModUtil.iterateOverBlocks(minX, minY, minZ, maxX, maxY, maxZ, blockPos -> {
					if (test || pos.distanceToSqr(blockPos.getX() + 0.5, blockPos.getX() + 0.5, blockPos.getX() + 0.5) > radius + 0.5) {
						BlockState blockState = level.getBlockState(blockPos);
						if (!blockState.isAir()) {
							blocksShockwaveVisual.addBlock(blockPos.mutable(), blockState);
						}
					}
				});
				blocksShockwaveVisual.sendToPlayers((ServerLevel) level, minX, minY, minZ, maxX, maxY, maxZ);
			}
		}

		@Override
		protected void playSound() {}

		@Override
		protected void spawnParticles() {}

		@Override
		public void toBuf(FriendlyByteBuf buf) {
			StreamCodecs.VEC_3D_APPROX.encode(buf, explosionDirection);
		}

		@Override
		public void fromBuf(FriendlyByteBuf buf) {
			explosionDirection = StreamCodecs.VEC_3D_APPROX.decode(buf);
		}

		@Override
		public ResourceLocation getExplosionType() {
			return ModCustomExplosions.STAND_HEAVY_PUNCH;
		}
	}

}
