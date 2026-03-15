package com.github.standobyte.jojo.customobjects.explosion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModCustomExplosions;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
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
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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
		lithiumInit(level, source, damageSource, 
				damageCalculator, 
				x, y, z, 
				radius, fire, blockInteraction, 
				smallParticles, largeParticles, sound);
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
//		calculateBlocksToBlow(getToBlow());
		lithiumCollectBlocksToBlow(getToBlow());

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

	// Replaced by an optimized version from Lithuim (below)
	protected void calculateBlocksToBlow(Collection<BlockPos> dest) {
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
							if (!this.level.isInWorldBounds(blockPos)) {
								break;
							}

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

		getToBlow().addAll(blocksToBlow);
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


	/* 
	 * Code below are optimizations taken from Lithium mod made by JellySquid and 2No2Name, 
	 * licensed under LGPL-3.0 - all credit goes to the devs.
	 * (https://github.com/CaffeineMC/lithium/blob/1.21.1/common/src/main/java/net/caffeinemc/mods/lithium/mixin/world/explosions/block_raycast/ExplosionMixin.java)
	 */
	
	// You can tell I didn't make that just by the amount of code documentation :P


	// The cached mutable block position used during block traversal.
	protected final BlockPos.MutableBlockPos lithiumCachedPos = new BlockPos.MutableBlockPos();

	// The chunk coordinate of the most recently stepped through block.
	protected int lithiumPrevChunkX = Integer.MIN_VALUE;
	protected int lithiumPrevChunkZ = Integer.MIN_VALUE;

	// The chunk belonging to prevChunkPos.
	protected ChunkAccess lithiumPrevChunk;

	/**
	 * Whether the explosion cares about air blocks. If false, air blocks do not have to be added to the set of destroyed blocks.
	 * Skipping air blocks reduces the number of BlockPos allocations, shuffling and getBlockState calls in {@link Explosion#finalizeExplosion(boolean)}
	 */
	protected boolean explodeAirBlocks;

	protected int bottomY, topY;

	protected void lithiumInit(Level world, Entity entity, DamageSource damageSource, ExplosionDamageCalculator behavior, 
			double x, double y, double z, 
			float power, boolean createFire, Explosion.BlockInteraction destructionType, 
			ParticleOptions particle, ParticleOptions emitterParticle, Holder<?> soundEvent) {
		this.bottomY = this.level.getMinBuildHeight();
		this.topY = this.level.getMaxBuildHeight();

		boolean explodeAir = this.fire; // air blocks are only relevant for the explosion when fire should be created inside them
		if (!explodeAir && this.level.dimension() == Level.END && this.level.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
			float overestimatedExplosionRange = (8 + (int) (6f * this.radius));
			int endPortalX = 0;
			int endPortalZ = 0;
			if (overestimatedExplosionRange > Math.abs(this.x - endPortalX) && overestimatedExplosionRange > Math.abs(this.z - endPortalZ)) {
				explodeAir = true;
				// exploding air works around accidentally fixing vanilla bug: an explosion cancelling the dragon fight start can destroy the newly placed end portal
			}
		}
		this.explodeAirBlocks = explodeAir;
	}

	public void lithiumCollectBlocksToBlow(Collection<BlockPos> dest) {
		// Using integer encoding for the block positions provides a massive speedup and prevents us from needing to
		// allocate a block position for every step we make along each ray, eliminating essentially all the memory
		// allocations of this function. The overhead of packing block positions into integer format is negligible
		// compared to a memory allocation and associated overhead of hashing real objects in a set.
		final LongOpenHashSet touched = new LongOpenHashSet(0);

		final RandomSource random = this.level.random;

		// Explosions work by casting many rays through the world from the origin of the explosion
		for (int rayX = 0; rayX < 16; ++rayX) {
			boolean xPlane = rayX == 0 || rayX == 15;
			double vecX = (((float) rayX / 15.0F) * 2.0F) - 1.0F;

			for (int rayY = 0; rayY < 16; ++rayY) {
				boolean yPlane = rayY == 0 || rayY == 15;
				double vecY = (((float) rayY / 15.0F) * 2.0F) - 1.0F;

				for (int rayZ = 0; rayZ < 16; ++rayZ) {
					boolean zPlane = rayZ == 0 || rayZ == 15;

					// We only fire rays from the surface of our origin volume
					if (xPlane || yPlane || zPlane) {
						double vecZ = (((float) rayZ / 15.0F) * 2.0F) - 1.0F;

						this.lithiumPerformRayCast(random, vecX, vecY, vecZ, touched);
					}
				}
			}
		}

		// We can now iterate back over the set of positions we modified and re-build BlockPos objects from them
		// This will only allocate as many objects as there are in the set, where otherwise we would allocate them
		// each step of a every ray.
		LongIterator it = touched.iterator();

		while (it.hasNext()) {
			dest.add(BlockPos.of(it.nextLong()));
		}
	}

	protected void lithiumPerformRayCast(RandomSource random, double vecX, double vecY, double vecZ, LongOpenHashSet touched) {
		double dist = Math.sqrt((vecX * vecX) + (vecY * vecY) + (vecZ * vecZ));

		double normX = (vecX / dist) * 0.3D;
		double normY = (vecY / dist) * 0.3D;
		double normZ = (vecZ / dist) * 0.3D;

		float strength = this.radius * (0.7F + (random.nextFloat() * 0.6F));

		double stepX = this.x;
		double stepY = this.y;
		double stepZ = this.z;

		int prevX = Integer.MIN_VALUE;
		int prevY = Integer.MIN_VALUE;
		int prevZ = Integer.MIN_VALUE;

		float prevResistance = 0.0F;

		int boundMinY = this.bottomY;
		int boundMaxY = this.topY;

		// Step through the ray until it is finally stopped
		while (strength > 0.0F) {
			int blockX = Mth.floor(stepX);
			int blockY = Mth.floor(stepY);
			int blockZ = Mth.floor(stepZ);

			float resistance;

			// Check whether we have actually moved into a new block this step. Due to how rays are stepped through,
			// over-sampling of the same block positions will occur. Changing this behaviour would introduce differences in
			// aliasing and sampling, which is unacceptable for our purposes. As a band-aid, we can simply re-use the
			// previous result and get a decent boost.
			if (prevX != blockX || prevY != blockY || prevZ != blockZ) {
				if (blockY < boundMinY || blockY >= boundMaxY || blockX < -30000000 || blockZ < -30000000 || blockX >= 30000000 || blockZ >= 30000000) {
					return;
				}
				//The coordinates are within the world bounds, so we can safely traverse the block
				resistance = this.lithiumTraverseBlock(strength, blockX, blockY, blockZ, touched);

				prevX = blockX;
				prevY = blockY;
				prevZ = blockZ;

				prevResistance = resistance;
			} else {
				resistance = prevResistance;
			}

			strength -= resistance;
			// Apply a constant fall-off
			strength -= 0.22500001F;

			stepX += normX;
			stepY += normY;
			stepZ += normZ;
		}
	}

	/**
	 * Called for every step made by a ray being cast by an explosion.
	 *
	 * @param strength The strength of the ray during this step
	 * @param blockX   The x-coordinate of the block the ray is inside of
	 * @param blockY   The y-coordinate of the block the ray is inside of
	 * @param blockZ   The z-coordinate of the block the ray is inside of
	 * @return The resistance of the current block space to the ray
	 */
	protected float lithiumTraverseBlock(float strength, int blockX, int blockY, int blockZ, LongOpenHashSet touched) {
		BlockPos pos = this.lithiumCachedPos.set(blockX, blockY, blockZ);

		int chunkX = blockX >> 4;
			int chunkZ = blockZ >> 4;

			// Avoid calling into the chunk manager as much as possible through managing chunks locally
			if (this.lithiumPrevChunkX != chunkX || this.lithiumPrevChunkZ != chunkZ) {
				this.lithiumPrevChunk = this.level.getChunk(chunkX, chunkZ);

				this.lithiumPrevChunkX = chunkX;
				this.lithiumPrevChunkZ = chunkZ;
			}

			final ChunkAccess chunk = this.lithiumPrevChunk;

			BlockState blockState = Blocks.AIR.defaultBlockState();
			float totalResistance = 0.0F;
			Optional<Float> blastResistance;

			labelGetBlastResistance:
			{
				// If the chunk is missing or out of bounds, assume that it is air
				if (chunk != null) {
					// We operate directly on chunk sections to avoid interacting with BlockPos and to squeeze out as much
					// performance as possible here
					int sectionCoord = SectionPos.blockToSectionCoord(blockY);
					int sectionYIndex = sectionCoord - chunk.getMinSection();
					LevelChunkSection section = chunk.getSections()[sectionYIndex];

					// If the section doesn't exist or it's empty, assume that the block is air
					if (section != null && !section.hasOnlyAir()) {
						// Retrieve the block state from the chunk section directly to avoid associated overhead
						blockState = section.getBlockState(blockX & 15, blockY & 15, blockZ & 15);

						// If the block state is air, it cannot have fluid or any kind of resistance, so just leave
						if (blockState.getBlock() != Blocks.AIR) {
							// Rather than query the fluid state from the container as we just did with the block state, we can
							// simply ask the block state we retrieved what fluid it has. This is exactly what the call would
							// do anyways, except that it would have to retrieve the block state a second time, adding overhead.
							FluidState fluidState = blockState.getFluidState();

							// Get the explosion resistance like vanilla
							blastResistance = this.damageCalculator.getBlockExplosionResistance(this, this.level, pos, blockState, fluidState);
							break labelGetBlastResistance;
						}
					}
				}
				blastResistance = this.damageCalculator.getBlockExplosionResistance(this, this.level, pos, Blocks.AIR.defaultBlockState(), Fluids.EMPTY.defaultFluidState());
			}
			// Calculate how much this block will resist an explosion's ray
			if (blastResistance.isPresent()) {
				totalResistance = (blastResistance.get() + 0.3F) * 0.3F;
			}

			// Check if this ray is still strong enough to break blocks, and if so, add this position to the set
			// of positions to destroy
			float reducedStrength = strength - totalResistance;
			if (reducedStrength > 0.0F && (this.explodeAirBlocks || !blockState.isAir())) {
				if (this.damageCalculator.shouldBlockExplode((Explosion) (Object) this, this.level, pos, blockState, reducedStrength)) {
					touched.add(pos.asLong());
				}
			}

			return totalResistance;
	}

}
