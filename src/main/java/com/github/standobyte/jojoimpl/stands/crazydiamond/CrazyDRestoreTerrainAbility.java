package com.github.standobyte.jojoimpl.stands.crazydiamond;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.mechanics.ServerBlockDestroyTracker;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracker;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracking;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.syncdata.SyncedDataHolderExtended;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.OOPMoment;
import com.github.standobyte.jojo.util.entitycomponent.ComponentUtil;
import com.github.standobyte.jojo.util.mc.XpFormulas;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDAnchorBlockAbility.FoundAnchor;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.BlockBreaking;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.BrokenBlocksChunkData;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.CDBlocksRestoredPacket;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.EntityMadeFromBlock;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.EntityMadeFromBlock.EntityReference;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.PrevBlockInfo;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class CrazyDRestoreTerrainAbility extends StandEntityAbility {

	public CrazyDRestoreTerrainAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, TerrainRestoration::new);
		setButtonHoldPhase(ActionPhase.PERFORM);
	}
	
	
	@Override
	public Ability replaceWithSubAbility(Power<?> context, AvailableAbilities abilities) {
		FoundAnchor anchor = CrazyDAnchorBlockAbility.getItemToUseAsAnchor(PowerClass.STAND.cast(context));
		if (anchor != null) {
			Ability anchorAbility = abilities.getContextVariation("block_anchor");
			if (anchorAbility != null && anchorAbility.isAbilityAvailable(context)) {
				return anchorAbility;
			}
		}
		
		return super.replaceWithSubAbility(context, abilities);
	}

	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
//		StandPower standPower = PowerClass.STAND.cast(context);
//		if (standPower.userStandEffects.getEffects().anyMatch(
//				effect -> effect.effectType == ModStandAbilities._PLACEHOLDER_CD_TURN_INTO_ANGELO_ROCK_EFFECT.get())) {
//			return ConditionCheck.NEGATIVE;
//		}
//		LivingEntity user = context.getUser();
//		Entity cameraEntity = restorationCenterEntity(user, standPower);
//		Vec3i eyePosI = eyePos(cameraEntity);
//		boolean hasResolveEffect = user.hasEffect(ModStatusEffects.RESOLVE);
//		boolean onlyAimedAt = user.isShiftKeyDown();
//		Level level = user.level();
//		if (getBlocksInRange(level, user, eyePosI, restorationDistManhattan(hasResolveEffect), 
//				block -> blockPosSelectedForRestoration(block, cameraEntity, cameraEntity.getLookAngle(), 
//						cameraEntity.getEyePosition(1.0F), eyePosI, hasResolveEffect, onlyAimedAt)).count() == 0) {
//			// FIXME !!!! NEGATIVE_CONTINUE_HOLD
//			return ConditionCheck.NEGATIVE;
//		}
		return super.checkSpecificConditions(context);
	}
	
	
	public static class TerrainRestoration extends EntityActionInstance implements SyncedDataHolderExtended {
		public boolean useOtherPlayersInventories = false;

		public TerrainRestoration(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				setPhaseStart(ActionPhase.RECOVERY);
				syncPhaseChanges();
			}
		}

		// FIXME (1.16.5) try to mitigate the fps drops when lots of blocks are restored simultaneously
		@Override
		public void actionTick() {
			LivingEntity user = getPowerUser();
			Level level = user.level();
			if (!level.isClientSide()) {
				StandPower userPower = StandPower.get(user);
				LivingEntity standEntity = getPerformer();
				ServerLevel serverLevel = (ServerLevel) level;
				
				Player playerUser = user instanceof Player ? (Player) user : null;
				boolean creative = playerUser != null ? playerUser.getAbilities().instabuild : false;
				Entity cameraEntity = restorationCenterEntity(user, userPower);
				boolean resolveEffect = ModStatusEffects.isInResolveEffect(user);
				int manhattanRange = restorationDistManhattan(resolveEffect);
				Vec3i eyePos = eyePos(cameraEntity);
				Vec3 lookVec = cameraEntity.getLookAngle();
				Vec3 eyePosD = cameraEntity.getEyePosition(1.0F);
				// TODO terrain restoration stamina cost
				float staminaPerBlock = 0; // getStaminaCostPerBlock(userPower);
				
				int blocksToRestore;
				if (resolveEffect) blocksToRestore = 64;
				else {
					blocksToRestore = blocksPerTick((StandEntity) standEntity);
					if (staminaPerBlock > 0) {
						blocksToRestore = Math.min(blocksToRestore, (int) (userPower.getStamina() / staminaPerBlock));
					}
				}
				boolean onlyAimedAt = user.isShiftKeyDown();
				
				Collection<Map.Entry<BlockPos, ?>> blocks = getFixableBlocksInRange(serverLevel, user, eyePos, manhattanRange, 
						blockPos -> blockPosSelectedForRestoration(blockPos, cameraEntity, 
								lookVec, eyePosD, eyePos, manhattanRange, 
								resolveEffect, onlyAimedAt));
				
				AABB area = cameraEntity.getBoundingBox().inflate(manhattanRange * 2);
				Vec3 center = area.getCenter();
				List<ItemStack> itemsSource = sourceItemStacks(area, center, user, level, 
						SourceType.MOB_HELD.fromAllNearby(), 
						SourceType.ITEM_ENTITY.fromAllNearby(), 
						playerUser != null ? SourceType.PLAYER_INVENTORY.from(playerUser) : null, 
								useOtherPlayersInventories ? SourceType.PLAYER_INVENTORY.fromAllNearby().sort() : null);
				
				RestoreResult result = restoreBlocks(serverLevel, standEntity, blocks.stream(), 
						blocksToRestore, eyePos, 
						creative, resolveEffect && !onlyAimedAt, true, 
						playerUser, itemsSource);
				
				setSynchedData(IS_RESTORING, result.isRestoring);
				if (result.blockForStaminaCost > 0) {
					userPower.consumeStamina(staminaPerBlock * result.blockForStaminaCost);
				}
			}
		}

		protected int blocksPerTick(StandEntity standEntity) {
			return MathUtil.fractionRandomInc(CrazyDHealAbility.crazyDRestorationSpeed(standEntity) * 3);
		}


		public static final EntityDataAccessor<Boolean> IS_RESTORING = SynchedEntityData.defineId(
				TerrainRestoration.class, EntityDataSerializers.BOOLEAN);
		@Override
		public void defineSynchedData(Builder builder) {
			builder.define(IS_RESTORING, false);
		}

		@Override
		public <T> void onSyncedDataUpdated(T oldValue, T newValue, EntityDataAccessor<T> dataAccessor) {
			if (dataAccessor == IS_RESTORING) {
				Level level = level();
				if (level.isClientSide() && performer instanceof StandEntity standEntity) {
					if ((Boolean) newValue) {
						ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.CRAZY_DIAMOND_FIX_STARTED.get(), standEntity), 
								standEntity.getSoundSource(), 1, 1, standEntity, level));
						
						ClientsideSoundsHelper.playNonVanillaClassSound(new EntityStoppableSoundInstance(ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.CRAZY_DIAMOND_FIX_LOOP.get(), standEntity), 
								standEntity.getSoundSource(), 1, 1, standEntity, level.random.nextLong(), 
								() -> this.isOver() || this.phase != ActionPhase.PERFORM || !this.getSynchedData(IS_RESTORING)));
					}
					else {
						ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.CRAZY_DIAMOND_FIX_ENDED.get(), standEntity), 
								standEntity.getSoundSource(), 1, 1, standEntity, level));
					}
				}
			}
		}
	}

	public static Collection<Map.Entry<BlockPos, ?>> getFixableBlocksInRange(ServerLevel level, LivingEntity user, Vec3i center, int blockRange, Predicate<BlockPos> filter) {
		Stream<Map.Entry<BlockPos, PrevBlockInfo>> blocksDestroyed = getBrokenBlocksInRange(level, user, center, blockRange, 
				(BlockPos targetPos, PrevBlockInfo block) -> filter.test(targetPos));
		Collection<Map.Entry<BlockPos, ?>> list = blocksDestroyed.collect(Collectors.toCollection(ArrayList::new));
		
		ServerBlockDestroyTracker trackers = ComponentUtil.getExistingDataOrNull(level, ModDataAttachmentTypes.BLOCK_DESTROY);
		if (trackers != null) {
			trackers.blockDestroy.entrySet().stream()
					.filter(entry -> filter.test(entry.getKey()))
					.forEach(list::add);;
		}
		
		// doesn't work anyway
//		for (ServerPlayer player : level.players()) {
//			PlayerBreakingBlock block = BlockBreakingVanilla.getBlockBeingBrokenByPlayer(player);
//			if (block != null && filter.test(block.blockPos)) {
//				list.add(new AbstractMap.SimpleEntry<>(block.blockPos, block));
//			}
//		}
//		
//		AABB lookForZombies = new AABB(
//				center.getX() - blockRange,
//				center.getY() - blockRange,
//				center.getZ() - blockRange,
//				center.getX() + blockRange + 1,
//				center.getY() + blockRange + 1,
//				center.getZ() + blockRange + 1);
//		level.getEntitiesOfClass(Zombie.class, lookForZombies).forEach(zombie -> {
//			ZombieBreakingDoor block = BlockBreakingVanilla.getDoorBeingBrokenByZombie(zombie);
//			if (block != null && filter.test(block.blockPos)) {
//				list.add(new AbstractMap.SimpleEntry<>(block.blockPos, block));
//			}
//		});

		return list;
	}

	public static RestoreResult restoreBlocks(ServerLevel level, Entity trackedEntity, Stream<Map.Entry<BlockPos, ?>> blocks, 
			float limit, Vec3i eyePos, 
			boolean isCreative, boolean randomizePos, boolean forgetFailed, 
			@Nullable Player playerWithXp, List<ItemStack> itemsSource) {
		RestoreResult result = new RestoreResult();
		if (limit == 0) return result;

		blocks = blocks
				.filter(blockEntry -> {
					BlockPos targetPos = blockEntry.getKey();
					return switch (blockEntry.getValue()) {
						case PrevBlockInfo block -> {
							if (restorationExclude(block, targetPos, level)) {
								yield false;
							}
							if (blockCanBePlaced(level, targetPos, block.state)) {
								yield true;
							}
							if (forgetFailed) {
								result.blocksToForget.add(block.pos);
							}
							yield false;
						}
						default -> true;
					};
				});
		
		blocks = blocks.sorted(Comparator.comparingInt(
				(Map.Entry<BlockPos, ?> blockEntry) -> restorationPriority(blockEntry.getValue(), blockEntry.getKey(), level)).thenComparingInt(
				(Map.Entry<BlockPos, ?> blockEntry) -> blockEntry.getKey().distManhattan(eyePos)));

		blocks.forEach(blockEntry -> {
			BlockPos targetBlockPos = blockEntry.getKey();
			switch (blockEntry.getValue()) {
				case PrevBlockInfo block -> {
					float blockToRestore = 1;
					if (result.blockForStaminaCost + blockToRestore <= limit) {
						boolean placeBlockNow = true;
						boolean didSmthElse = false;
						if (block.blockShards != null && !block.blockShards.isEmpty()) {
							didSmthElse |= true;
							for (EntityReference shardRef : block.blockShards) {
								EntityMadeFromBlock shard = shardRef.get();
								if (shard != null && shard.isEntityAlive()) {
									placeBlockNow &= shard.crazyDRestore(targetBlockPos);
								}
							}
						}
						
						for (ItemStack item : block.drops) {
							// remote anchor block
							ItemTracker itemTracker = ItemTracking.getItemTracker(item, level);
							if (itemTracker != null) {
								ItemStack actualItem = itemTracker.getItem();
								if (actualItem != null) {
									Entity entity = itemTracker.getAtEntity(level);
									if (entity instanceof ItemFrame itemFrame) {
										ItemStack itemInFrame = itemFrame.getItem();
										itemFrame.setItem(ItemStack.EMPTY);
										itemInFrame = itemInFrame.copy();
										itemFrame.removeFramedMap(itemInFrame);
										itemFrame.playSound(itemFrame.getRemoveItemSound(), 1.0F, 1.0F);
										ItemEntity itemEntity = itemFrame.spawnAtLocation(itemInFrame);
										entity = itemEntity;
									}
									
									if (entity != null && entity.isAlive()) {
										entity = CrazyDAnchorBlockAbility.entityToMove(entity);
										Vec3 posD = Vec3.atCenterOf(targetBlockPos);
						                boolean isCloseToAnchorPos = CrazyDAnchorBlockAbility.isCloseToAnchorPos(entity, posD);
						                if (!isCloseToAnchorPos) {
						                	CrazyDAnchorBlockAbility.moveWithAnchor(entity, posD);
						                    didSmthElse |= true;
						                }
						                placeBlockNow &= isCloseToAnchorPos;
										itemsSource.add(0, actualItem);
										// TODO if it's a living entity, only add particles to the item itself
										result.entitiesFixParticles.add(entity.getId());
									}
								}
								
								// XXX take out items from containers
							}
						}
						
						if (placeBlockNow) {
//							result.blocksTried.add(block.pos);
							if (tryPlaceBlock(level, targetBlockPos, block.state, isCreative, randomizePos, 
									block.drops, block.getDroppedXp(), playerWithXp, itemsSource)) {
								result.blockForStaminaCost += blockToRestore;
								result.blocksFixParticles.add(targetBlockPos);
								result.blocksToForget.add(block.pos);
								result.isRestoring |= true;
							}
						}
						else if (didSmthElse) {
							result.blockForStaminaCost += blockToRestore;
							result.blocksFixParticles.add(targetBlockPos);
							result.isRestoring |= true;
						}
					}
				}
				case BlockBreaking blockBeingBroken -> {
					float curProgress = blockBeingBroken.getProgress();
					float breakProgressToFix = Mth.clamp(limit - result.blockForStaminaCost, 0, curProgress);
					if (breakProgressToFix > 0) {
						blockBeingBroken.setAndSyncProgress(curProgress - breakProgressToFix, targetBlockPos, level);
						result.blockForStaminaCost += breakProgressToFix;
						result.blocksFixParticles.add(targetBlockPos);
						result.isRestoring |= true;
					}
				}
				default -> {}
			}
		});

		if (!result.blocksFixParticles.isEmpty() || !result.entitiesFixParticles.isEmpty()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(trackedEntity, 
					new CDBlocksRestoredPacket(result.blocksFixParticles, result.entitiesFixParticles));
		}
		forgetBrokenBlocks(level, result.blocksToForget);

		return result;
	}

	public static class RestoreResult {
		public boolean isRestoring = false;
		public float blockForStaminaCost = 0;
//		public final Set<BlockPos> blocksTried = new HashSet<>();
		public final Set<BlockPos> blocksFixParticles = new HashSet<>();
		public final IntSet entitiesFixParticles = new IntArraySet();
		public final Set<BlockPos> blocksToForget = new HashSet<>();
	}

	public static Stream<Map.Entry<BlockPos, PrevBlockInfo>> getBrokenBlocksInRange(Level level, LivingEntity user, 
			Vec3i center, int blockRange, BiPredicate<BlockPos, PrevBlockInfo> filter) {
		int chunkXMin = center.getX() - blockRange >> 4;
		int chunkXMax = center.getX() + blockRange >> 4;
		int chunkZMin = center.getZ() - blockRange >> 4;
		int chunkZMax = center.getZ() + blockRange >> 4;
		Stream.Builder<LevelChunk> builder = Stream.builder();
		for (int x = chunkXMin; x <= chunkXMax; x++) {
			for (int z = chunkZMin; z <= chunkZMax; z++) {
				LevelChunk chunk = level.getChunk(x, z);
				if (chunk != null) {
					builder.add(chunk);
				}
			}
		}
		Stream<LevelChunk> chunks = builder.build();
		Stream<Map.Entry<BlockPos, PrevBlockInfo>> brokenBlocks = chunks.flatMap(chunk -> {
			BrokenBlocksChunkData data = BrokenBlocksChunkData.getExistingData(chunk);
			if (data != null) {
				return data.brokenBlocks.entrySet().stream()
						// remap the block position (entry key) here
						.filter(blockEntry -> {
							PrevBlockInfo block = blockEntry.getValue();
							BlockPos targetPos = blockEntry.getKey();
							return filter.test(targetPos, block) && !user.getBoundingBox().intersects(new AABB(targetPos));
						});
			}
			else return Stream.empty();
		});
		return brokenBlocks;
	}



	// this whole junk somewhat fixes janky restoration of sand blocks, e.g. an explosion in a desert
	protected static boolean restorationExclude(PrevBlockInfo block, BlockPos targetPos, Level level) {
		if (block.state.getBlock() instanceof FallingBlock) {
			BlockPos blockBelow = targetPos.below();
			if (level.isEmptyBlock(blockBelow)) {
				BrokenBlocksChunkData data = BrokenBlocksChunkData.getExistingData(level, targetPos);
				if (data != null && data.brokenBlocks.containsKey(blockBelow)) {
					return true;
				}
			}
		}

		return !block.state.canSurvive(level, targetPos);
	}

	protected static int restorationPriority(Object blockToFix, BlockPos targetPos, Level level) {
		if (blockToFix instanceof PrevBlockInfo block) {
			if (block.state.getBlock() instanceof FallingBlock && !level.isEmptyBlock(targetPos)) {
				return 0;
			}
		}
		return 1;
	}


	public static boolean tryPlaceBlock(Level level, BlockPos blockPos, BlockState blockState, boolean isCreative, boolean randomizePos, 
			List<ItemStack> restorationCost, int xpCost, @Nullable Player consumeXpFrom, List<ItemStack> itemsSource) {
		if (!isCreative && xpCost > 0 && (consumeXpFrom == null || XpFormulas.getTotalExperience(consumeXpFrom) < xpCost)) {
			return false;
		}
		if (randomizePos) {
			RandomSource RANDOM = OOPMoment.RANDOM;
			BlockPos randomPos = blockPos = blockPos.offset(
					RANDOM.nextBoolean() ? RANDOM.nextInt(3) - 1 : 0, 
					RANDOM.nextInt(2) + 1,
					RANDOM.nextBoolean() ? RANDOM.nextInt(3) - 1 : 0);
			if (blockCanBePlaced(level, blockPos, blockState)) {
				boolean differentBlockAtRandomPos = false;
				BrokenBlocksChunkData data = BrokenBlocksChunkData.getChunkData(level, randomPos);
				if (data != null && data.wasBlockBroken(randomPos)) {
					differentBlockAtRandomPos = true;
				}
				if (!differentBlockAtRandomPos) {
					blockPos = randomPos;
				}
			}
		}
		if (blockCanBePlaced(level, blockPos, blockState) && blockState.canSurvive(level, blockPos)
				&& (consumeNeededItems(restorationCost, itemsSource, null) || isCreative)) {
			if (!isCreative && consumeXpFrom != null && xpCost > 0) {
				consumeXpFrom.giveExperiencePoints(-xpCost);
			}
			blockState = Block.updateFromNeighbourShapes(blockState, level, blockPos);
			level.setBlockAndUpdate(blockPos, blockState);
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean blockCanBePlaced(Level level, BlockPos targetPos, BlockState placedBlockState) {
		BlockState curBlockState = level.getBlockState(targetPos);
		return FallingBlock.isFree(curBlockState);
	}

	// methods to be called from outside

	@Nullable
	public static PrevBlockInfo rememberBrokenBlock(Level level, BlockPos pos, BlockState state, 
			Optional<BlockEntity> tileEntity, List<ItemStack> drops) {
		Block block = state.getBlock();
		if (block instanceof FireBlock) return null;

		BrokenBlocksChunkData data = BrokenBlocksChunkData.getChunkData(level, pos);
		if (data != null) {
			return data.saveBrokenBlock(pos, state, tileEntity, drops);
		}
		return null;
	}

	public static void forgetBrokenBlocks(Level level, Collection<BlockPos> posCollection) {
		posCollection.stream()
		.map(pos -> BrokenBlocksChunkData.getExistingData(level, pos))
		.distinct()
		.filter(Objects::nonNull)
		.forEach(data -> {
			if (data != null) {
				posCollection.forEach(pos -> data.removeBrokenBlock(pos));
			}
		});
	}

	// item cost stuff
	
	public static enum SourceType {
		MOB_HELD {
			@Override
			protected void addItems(List<ItemStack> items, Stream<Entity> entities) {
				entities.map(entity -> ((LivingEntity) entity)).forEach(mob -> {
					for (InteractionHand hand : InteractionHand.values()) {
						ItemStack item = mob.getItemInHand(hand);
						if (!item.isEmpty()) {
							items.add(item);
						}
					}
				});
			}
		},
		ITEM_ENTITY {
			@Override
			protected void addItems(List<ItemStack> items, Stream<Entity> entities) {
				entities.map(entity -> ((ItemEntity) entity).getItem())
				.filter(item -> !item.isEmpty())
				.forEach(items::add);
			}
		},
		PLAYER_INVENTORY {
			@Override
			protected void addItems(List<ItemStack> items, Stream<Entity> entities) {
				entities.map(entity -> ((Player) entity).getInventory())
				.forEach(inventory -> {
					int size = inventory.getContainerSize();
					for (int i = 0; i < size; i++) {
						ItemStack inventoryItem = inventory.getItem(i);
						if (inventoryItem != null && !inventoryItem.isEmpty()) {
							items.add(inventoryItem);
						}
					}
				});
			}
		},
		OTHER {
			@Override
			protected void addItems(List<ItemStack> items, Stream<Entity> entities) {}
		};

		public ItemsSource fromAllNearby() {
			return new ItemsSource(this, (Entity[]) null);
		}

		public ItemsSource from(Entity... entity) {
			return new ItemsSource(this, entity);
		}

		protected abstract void addItems(List<ItemStack> items, Stream<Entity> from);
	}

	public static class ItemsSource {
		protected final SourceType type;
		protected boolean sort = false;
		@Nullable protected final Stream<Entity> entity;

		protected ItemsSource(SourceType type, Entity... entities) {
			this.type = type;
			this.entity = entities != null ? Stream.of(entities) : null;
		}

		public ItemsSource sort() {
			this.sort = true;
			return this;
		}
	}

	public static List<ItemStack> sourceItemStacks(AABB entitiesArea, Vec3 center, LivingEntity user, Level level, 
			ItemsSource... order) {
		Map<SourceType, List<Entity>> entitiesAround = level.getEntities(user, entitiesArea,
				EntitySelector.NO_SPECTATORS.and(e -> !e.isRemoved()))
				.stream().collect(Collectors.groupingBy(e -> {
					if (e instanceof ItemEntity) {
						return SourceType.ITEM_ENTITY;
					}
					if (e instanceof LivingEntity) {
						if (e instanceof Player) {
							return SourceType.PLAYER_INVENTORY;
						}
						if (e instanceof Mob) {
							return SourceType.MOB_HELD;
						}
					}
					return SourceType.OTHER;
				}));
		List<ItemStack> itemsSource = new ArrayList<>();

		for (ItemsSource itemsHandler : order) {
			if (itemsHandler == null) continue;

			Stream<Entity> entities = itemsHandler.entity;
			if (entities == null && entitiesAround.containsKey(itemsHandler.type)) {
				entities = entitiesAround.get(itemsHandler.type).stream();
			}
			if (entities == null) continue;

			entities = entities.filter(Objects::nonNull);
			if (itemsHandler.sort) {
				entities = entities.sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center)));
			}
			itemsHandler.type.addItems(itemsSource, entities);
		}

		return itemsSource;
	}

	public static boolean consumeNeededItems(List<ItemStack> restorationCost, List<ItemStack> itemsSource, 
			@Nullable List<ItemStack> collectConsumedItems) {
		if (restorationCost.isEmpty()) {
			return true;
		}
		if (restorationCost.size() == 1 && restorationCost.get(0).getCount() == 1) {
			return consumeSingleItem(restorationCost.get(0), itemsSource, collectConsumedItems);
		}

		List<ItemStack> costCopied = restorationCost.stream().map(ItemStack::copy).collect(Collectors.toList());
		Map<ItemStack, Pair<List<ItemStack>, MutableInt>> itemsFound = Util.make(new HashMap<>(), map -> {
			costCopied.forEach(item -> map.put(item, Pair.of(new ArrayList<>(), new MutableInt())));
		});

		for (ItemStack item : itemsSource) {
			sortItem(itemsFound, costCopied, item);
		}


		if (itemsFound.entrySet().stream().allMatch(entry -> {
			ItemStack neededItem = entry.getKey();
			Pair<List<ItemStack>, MutableInt> existingItems = entry.getValue();
			return existingItems.getRight().getValue() >= neededItem.getCount();
		})) {
			itemsFound.entrySet().stream().forEach(entry -> {
				ItemStack neededItem = entry.getKey();
				Pair<List<ItemStack>, MutableInt> existingItems = entry.getValue();
				existingItems.getLeft().stream().anyMatch(consumedItem -> {
					int count = Math.min(neededItem.getCount(), consumedItem.getCount());
					if (collectConsumedItems != null) {
						ItemStack remembered = consumedItem.copy();
						remembered.setCount(count);
					}
					consumedItem.shrink(count);
					neededItem.shrink(count);
					return neededItem.isEmpty();
				});
			});
			return true;
		}
		return false;
	}

	protected static boolean consumeSingleItem(ItemStack neededSingleItem, List<ItemStack> itemsSource, 
			@Nullable List<ItemStack> collectConsumedItems) {
		for (ItemStack item : itemsSource) {
			if (stacksMatch(neededSingleItem, item)) {
				if (collectConsumedItems != null) {
					ItemStack remember = item.copy();
					remember.setCount(1);
					collectConsumedItems.add(remember);
				}
				item.shrink(1);
				return true;
			}
		}

		return false;
	}

	protected static void sortItem(Map<ItemStack, Pair<List<ItemStack>, MutableInt>> sortMap, List<ItemStack> cost, ItemStack existingItem) {
		cost.stream().filter(costItem -> stacksMatch(costItem, existingItem)).findFirst().ifPresent(neededItem -> {
			if (!sortMap.containsKey(neededItem)) {
				// i made sure to fill the map, so it should instead fail-fast if this actually happens somehow
				return;
			}
			Pair<List<ItemStack>, MutableInt> entry = sortMap.get(neededItem);
			entry.getLeft().add(existingItem);
			entry.getRight().add(existingItem.getCount());
		});
	}

	public static boolean stacksMatch(ItemStack neededItem, ItemStack itemInQuestion) {
		return (!itemInQuestion.isEmpty() && itemInQuestion.getItem() == neededItem.getItem()
				&& ItemStack.isSameItemSameComponents(itemInQuestion, neededItem));
	}

	// position math

	public static Entity restorationCenterEntity(LivingEntity user, StandPower power) {
		StandEntity standEntity = power.getSummonedStandEntity();
		if (standEntity != null && standEntity.isManuallyControlled()) {
			return standEntity;
		}
		return user;
	}

	public static Vec3i eyePos(Entity entity) {
		Vec3 pos = entity.getEyePosition(1.0F);
		return new Vec3i((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z));
	}

	public static boolean blockPosSelectedForRestoration(BlockPos blockPos, Entity cameraEntity, 
			Vec3 entityLookVec, Vec3 entityEyePos, Vec3i restorationCenter, int rangeManhattan, 
			boolean resolve, boolean aimedOnly) {
		if (blockPos.distManhattan(restorationCenter) > rangeManhattan) {
			return false;
		}
		if (aimedOnly) {
			Vec3 pos2 = entityEyePos.add(entityLookVec.scale(rangeManhattan * 2));
			return new AABB(blockPos).clip(entityEyePos, pos2).isPresent();
		}
		else {
			return entityLookVec.dot(Vec3.atCenterOf(blockPos).subtract(entityEyePos).normalize()) >= (resolve ? 0 : 0.7071);
		}
	}

	public static int restorationDistManhattan(boolean resolve) {
		return 12;
	}


//	@Override
//	public void phaseTransition(Level level, StandEntity standEntity, StandPower standPower, 
//			@Nullable ActionPhase from, @Nullable ActionPhase to, StandEntityTask task, int nextPhaseTicks) {
//		if (level.isClientSide()) {
//			if (to == ActionPhase.PERFORM) {
//				ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, 
//						ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), this, ActionPhase.PERFORM, 1.0F, 1.0F, true);
//			}
//			else if (from == ActionPhase.PERFORM) {
//				standEntity.playSound(ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F, ClientUtil.getClientPlayer());
//			}
//		}
//	}
//
//	@Override
//	public float getStaminaCostTicking(StandPower power) {
//		return 0;
//	}
//
//	public float getStaminaCostPerBlock(StandPower power) {
//		return super.getStaminaCostTicking(power);
//	}


	public static void addParticlesAroundBlock(Level level, BlockPos blockPos, RandomSource random) {
		if (level.isClientSide() && ClientGlobals.canSeeStands) {
			Vec3 posLLCorner = Vec3.atLowerCornerOf(blockPos).subtract(0.25, 0.25, 0.25);
			for (int i = 0; i < 12; i++) {
				level.addParticle(ModParticles.CD_RESTORATION.get(), 
						posLLCorner.x + random.nextDouble() * 1.5, 
						posLLCorner.y + random.nextDouble() * 1.5, 
						posLLCorner.z + random.nextDouble() * 1.5, 
						0, 0, 0);
			}
		}
	}
	

	protected String resolveSpriteName;
	
	@Override
	protected void initVariationAssets() {
		this.resolveSpriteName = this.spriteName + "_improper";
	}
	
	@Override
	public String getSpriteName(Power<?> context) {
		if (context != null) {
			LivingEntity user = context.getUser();
			if (user != null && ModStatusEffects.isInResolveEffect(user)) {
				return resolveSpriteName;
			}
		}

		return super.getSpriteName(context);
	}

}
