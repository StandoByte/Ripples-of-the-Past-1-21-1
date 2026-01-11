package com.github.standobyte.jojo.mechanics.explosion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModCustomExplosions;
import com.github.standobyte.jojo.util.network.NetworkUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class CustomExplosion extends Explosion {
	protected Vec3 whyTheFuckDidTheyDeleteThis;
	
	public CustomExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, 
			double x, double y, double z, 
			float radius, boolean fire, BlockInteraction blockInteraction) {
		this(level, source, damageSource, x, y, z, radius, fire, blockInteraction, 
				ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
	}

	public CustomExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, 
			double x, double y, double z, 
			float radius, boolean fire, BlockInteraction blockInteraction,
			@Nullable ParticleOptions smallParticles, @Nullable ParticleOptions largeParticles, @Nullable Holder<SoundEvent> sound) {
		super(level, source, damageSource, 
				null, 
				x, y, z, 
				radius, fire, blockInteraction, 
				smallParticles, largeParticles, sound);
		this.whyTheFuckDidTheyDeleteThis = super.center();
		this.damageCalculator = new ExplosionDamageCalculatorWasAStupidIdeaJustSaying(this);
	}
	
	/** Client-side cpnstructor that can be used as a CustomExplosionSupplier. 
	 * The rest of the fields are filled in later ({@link CustomExplosion#decode(FriendlyByteBuf, double, double, double)})*/
	public CustomExplosion(Level level, double x, double y, double z, float radius) {
		this(level, null, null, 
				x, y, z, 
				radius, false, BlockInteraction.KEEP, 
				null, null, null);
	}


	public abstract ResourceLocation getExplosionType();

	@FunctionalInterface
	public static interface CustomExplosionSupplier {
		CustomExplosion createExplosion(Level pLevel, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius);
	}

	public void encode(RegistryFriendlyByteBuf buf, double x, double y, double z) {
		buf.writeResourceLocation(this.getExplosionType());
		buf.writeFloat(this.radius);
		buf.writeEnum(blockInteraction);
		
		List<BlockPos> toBlow = this.getToBlow();
		buf.writeInt(toBlow.size());
		int xInt = Mth.floor(x);
		int yInt = Mth.floor(y);
		int zInt = Mth.floor(z);
		for (BlockPos blockPos : toBlow) {
			buf.writeByte(blockPos.getX() - xInt);
			buf.writeByte(blockPos.getY() - yInt);
			buf.writeByte(blockPos.getZ() - zInt);
		}
		
		NetworkUtil.writeOptionally(smallExplosionParticles, buf, ParticleTypes.STREAM_CODEC);
		NetworkUtil.writeOptionally(largeExplosionParticles, buf, ParticleTypes.STREAM_CODEC);
		NetworkUtil.writeOptionally(explosionSound, buf, SoundEvent.STREAM_CODEC);
		
		toBuf(buf);
	}
	
	public static CustomExplosion decode(RegistryFriendlyByteBuf buf, double x, double y, double z) {
		ResourceLocation type = buf.readResourceLocation();
		float power = buf.readFloat();
		BlockInteraction blockInteraction = buf.readEnum(BlockInteraction.class);
		
		int blockCount = buf.readInt();
		List<BlockPos> toBlow = Lists.newArrayListWithCapacity(blockCount);
		int xInt = Mth.floor(x);
		int yInt = Mth.floor(y);
		int zInt = Mth.floor(z);
		for (int i = 0; i < blockCount; ++i) {
			toBlow.add(new BlockPos(
					buf.readByte() + xInt, 
					buf.readByte() + yInt, 
					buf.readByte() + zInt));
		}
		
		@Nullable ParticleOptions smallParticles = NetworkUtil.readOptional(buf, ParticleTypes.STREAM_CODEC).orElse(null);
		@Nullable ParticleOptions largeParticles = NetworkUtil.readOptional(buf, ParticleTypes.STREAM_CODEC).orElse(null);
		@Nullable Holder<SoundEvent> sound = NetworkUtil.readOptional(buf, SoundEvent.STREAM_CODEC).orElse(null);
		
		CustomExplosionSupplier explosionSupplier = ModCustomExplosions.REGISTER.get(type);
		if (explosionSupplier != null) {
			CustomExplosion explosion = explosionSupplier.createExplosion(
					ClientProxy.getClientWorld(), 
					x, y, z, power);
			
			explosion.getToBlow().addAll(toBlow);
			explosion.blockInteraction = blockInteraction;
			explosion.smallExplosionParticles = smallParticles;
			explosion.largeExplosionParticles = largeParticles;
			explosion.explosionSound = sound;
			explosion.fromBuf(buf);
			return explosion;
		}
		return null;
	}

	public void toBuf(FriendlyByteBuf buf) {
		
	}

	public void fromBuf(FriendlyByteBuf buf) {
		
	}
	

	/**
	* Does the first part of the explosion (destroy blocks)
	* Is only called on server
	*/
	@Override
	public void explode() {
		getToBlow().addAll(calculateBlocksToBlow());

		AABB area = entityDamageArea();
		List<Entity> entities = getAffectedEntities(area);
		filterEntities(entities);
		EventHooks.onExplosionDetonate(level, this, entities, radius * 2);
		hurtEntities(entities);
	}

	/**
	 * Does the second part of the explosion (sound, particles, drop spawn)
	 * Is called on both sides
	 */
	@Override
	public void finalizeExplosion(boolean pSpawnParticles) {
		if (level.isClientSide()) {
			playSound();
		}

		if (pSpawnParticles) {
			spawnParticles();
		}

		if (blockInteraction != BlockInteraction.KEEP) {
			explodeBlocks();
		}

		if (fire) {
			spawnFire();
		}
	}

 
	protected AABB entityDamageArea() {
		double diameter = radius * 2;
		Vec3 pos = center();
		return new AABB(
				Mth.floor(pos.x - diameter - 1), 
				Mth.floor(pos.y - diameter - 1), 
				Mth.floor(pos.z - diameter - 1), 
				Mth.floor(pos.x + diameter + 1), 
				Mth.floor(pos.y + diameter + 1), 
				Mth.floor(pos.z + diameter + 1));
	}

	protected List<Entity> getAffectedEntities(AABB area) {
		return level.getEntities(getDirectSourceEntity(), area);
	}

	protected Set<BlockPos> calculateBlocksToBlow() {
		Set<BlockPos> blocksToBlow = Sets.newHashSet();

		for (int xStep = 0; xStep < 16; ++xStep) {
			for (int yStep = 0; yStep < 16; ++yStep) {
				for (int zStep = 0; zStep < 16; ++zStep) {
					if (xStep == 0 || xStep == 15 || yStep == 0 || yStep == 15 || zStep == 0 || zStep == 15) {
						double xd = (xStep / 15.0F * 2.0F - 1.0F);
						double yd = (yStep / 15.0F * 2.0F - 1.0F);
						double zd = (zStep / 15.0F * 2.0F - 1.0F);
						double len = Math.sqrt(xd * xd + yd * yd + zd * zd);
						xd = xd / len;
						yd = yd / len;
						zd = zd / len;
						float power = radius * (0.7F + level.random.nextFloat() * 0.6F);
						Vec3 pos = center();
						double x = pos.x;
						double y = pos.y;
						double z = pos.z;

						for (; power > 0.0F; power -= 0.225F) {
							BlockPos blockPos = BlockPos.containing(x, y, z);
							BlockState blockState = level.getBlockState(blockPos);
							FluidState fluidState = level.getFluidState(blockPos);
							Optional<Float> resistance = damageCalculator.getBlockExplosionResistance(this, level, blockPos, blockState, fluidState);
							if (resistance.isPresent()) {
								power -= (resistance.get() + 0.3F) * 0.3F;
							}

							if (power > 0.0F && damageCalculator.shouldBlockExplode(this, level, blockPos, blockState, power)) {
								blocksToBlow.add(blockPos);
							}

							x += xd * 0.3;
							y += yd * 0.3;
							z += zd * 0.3;
						}
					}
				}
			}
		}

		return blocksToBlow;
	}
 
	public Optional<Float> getBlockExplosionResistance(BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid) {
		Optional<Float> resistance = state.isAir() && fluid.isEmpty() ? Optional.empty()
				: Optional.of(Math.max(
						state.getExplosionResistance(reader, pos, this), 
						fluid.getExplosionResistance(reader, pos, this)));
		Entity source = getDirectSourceEntity();
		if (source != null) {
			resistance = resistance.map(res -> source.getBlockExplosionResistance(this, reader, pos, state, fluid, res));
		}
		return resistance;
	}

	public boolean shouldBlockExplode(BlockGetter reader, BlockPos pos, BlockState state, float power) {
		Entity source = getDirectSourceEntity();
		if (source != null) {
			return source.shouldBlockExplode(this, reader, pos, state, power);
		}
		return true;
	}


	protected void filterEntities(List<Entity> entities) {}

	protected void hurtEntities(Collection<Entity> entities) {
		double diameter = radius * 2.0F;
		Vec3 pos = center();

		for (Entity entity : entities) {
			if (!entity.ignoreExplosion(this)) {
				double distRatio = Math.sqrt(entity.distanceToSqr(pos)) / diameter;
				if (distRatio <= 1) {
					Vec3 entityPos = entity instanceof PrimedTnt ? entity.position() : entity.getEyePosition(1.0F);
					Vec3 diff = entityPos.subtract(pos);
					
					double lengthSqr = diff.lengthSqr();
					if (lengthSqr != 0.0) {
						diff = diff.scale(1 / Math.sqrt(lengthSqr));

						double seenPercent = (double)getSeenPercent(pos, entity);
						double impact = (1.0 - distRatio) * seenPercent;
						double knockback = impact * (double)this.damageCalculator.getKnockbackMultiplier(entity);
						if (entity instanceof LivingEntity livingentity) {
							knockback *= 1.0 - livingentity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
						}

						diff = diff.scale(knockback);
						diff = EventHooks.getExplosionKnockback(this.level, this, entity, diff);

						float damage = getEntityDamageAmount(entity, impact);
						if (damage > 0) {
							hurtEntity(entity, damage, diff);
						}
						entity.onExplosionHit(getDirectSourceEntity());
					}
				}
			}
		}
	}

	protected void hurtEntity(Entity entity, float damage, Vec3 knockbackVec) {
		entity.hurt(this.damageSource, damage);
		entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackVec));
		if (entity instanceof Player player) {
			if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
				getHitPlayers().put(player, knockbackVec);
			}
		}
	}

	// ExplosionDamageCalculator delegates to this method. 
	// Unlike the vanilla explosion, we aren't using the ExplosionDamageCalculator for this.
	@Deprecated
	public boolean shouldDamageEntity(Entity entity) {
		return getEntityDamageAmount(entity) <= 0;
	}

	// Same as above. Why the hell do they calculate the same variables twice????
	@Deprecated
	public float getEntityDamageAmount(Entity entity) {
		float diameter = radius() * 2.0F;
		Vec3 pos = center();
		double distRatio = Math.sqrt(entity.distanceToSqr(pos)) / (double)diameter;
		double seenPercent = (double)getSeenPercent(pos, entity);
		double impact = (1.0 - distRatio) * seenPercent;
		return getEntityDamageAmount(entity, impact);
	}
	
	public float getEntityDamageAmount(Entity entity, double impact) {
		double diameter = radius() * 2;
		return (float)((impact * impact + impact) / 2.0 * 7.0 * diameter + 1.0);
	}

	public float getKnockbackMultiplier(Entity entity) {
		return entity instanceof Player player && player.getAbilities().flying ? 0 : 1;
	}

	protected void explodeBlocks() {
		List<Pair<ItemStack, BlockPos>> dropPositions = new ArrayList<>();
		Util.shuffle(getToBlow(), this.random);

		for (BlockPos blockPos : getToBlow()) {
			BlockState blockState = level.getBlockState(blockPos);
			blockState.onExplosionHit(level, blockPos, this,
					(item, _blockPos) -> addOrAppendStack(dropPositions, item, _blockPos));
		}

		for (Pair<ItemStack, BlockPos> pair : dropPositions) {
			Block.popResource(level, pair.getRight(), pair.getLeft());
		}
	}

	protected void playSound() {
		Holder<SoundEvent> soundHolder = getExplosionSound();
		if (soundHolder != null) {
			SoundEvent sound = soundHolder.value();
			if (sound != null) {
				Vec3 pos = center();
				level.playLocalSound(pos.x, pos.y, pos.z, getExplosionSound().value(), SoundSource.BLOCKS, 
						4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
			}
		}
	}

	protected void spawnParticles() {
		Vec3 pos = center();
		ParticleOptions particles;
		if (radius >= 2.0F && blockInteraction != BlockInteraction.KEEP) {
			particles = this.getLargeExplosionParticles();
		} else {
			particles = this.getSmallExplosionParticles();
		}
		if (particles != null) {
			level.addParticle(particles, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
		}
	}

	protected void spawnFire() {
		for (BlockPos blockPos : getToBlow()) {
			if (random.nextInt(3) == 0 && level.getBlockState(blockPos).isAir()
					&& level.getBlockState(blockPos.below()).isSolidRender(level, blockPos.below())) {
				level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
			}
		}
	}


	public static void addOrAppendStack(List<Pair<ItemStack, BlockPos>> pDropPositionArray, ItemStack pStack, BlockPos pPos) {
		for (int i = 0; i < pDropPositionArray.size(); ++i) {
			Pair<ItemStack, BlockPos> pair = pDropPositionArray.get(i);
			ItemStack itemstack = pair.getLeft();
			if (ItemEntity.areMergable(itemstack, pStack)) {
				ItemStack itemstack1 = ItemEntity.merge(itemstack, pStack, 16);
				pDropPositionArray.set(i, Pair.of(itemstack1, pair.getRight()));
				if (pStack.isEmpty()) {
					return;
				}
			}
		}

		pDropPositionArray.add(Pair.of(pStack, pPos.immutable()));
	}

	@Override
	public Vec3 center() {
		return whyTheFuckDidTheyDeleteThis;
	}



	public static boolean explode(CustomExplosion explosion) {
		Level level = explosion.level;
		if (EventHooks.onExplosionStart(level, explosion)) {
			return false;
		}
		explosion.explode();
		explosion.finalizeExplosion(true);

		if (!level.isClientSide()) {
			if (!explosion.interactsWithBlocks()) {
				explosion.clearToBlow();
			}

			ResourceLocation explosionType = explosion.getExplosionType();
			if (explosionType != null) {
				Vec3 pos = explosion.center();
				for (ServerPlayer player : ((ServerLevel) level).players()) {
					if (player.distanceToSqr(pos.x, pos.y, pos.z) < 4096) {
						Vec3 playerKnockback = explosion.getHitPlayers().get(player);
						PacketDistributor.sendToPlayer(player, new CustomExplosionPacket(explosion, 
								pos.x, pos.y, pos.z, 
								playerKnockback));
					}
				}
			}
		}

		return true;
	}

}
