package com.github.standobyte.jojo.mechanics.itemtracking;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.itemtracking.internal.TrackedItemPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class ItemTracker {
	protected static final RandomSource RANDOM = RandomSource.create();

	public final UUID trackerUuid;
	protected final ItemTracking trackerSystem;

	@Nullable protected ItemStack itemStack;
	@Nullable protected UUID trackingPlayerId;

	@Nullable protected ResourceKey<Level> positionDimension;
	protected OptionalInt positionEntity = OptionalInt.empty();
	@Nullable protected BlockPos positionBlock = null;
	@Nullable protected BlockState containerBlockState;
	@Nullable protected Predicate<UUID> itemStillThere;
	@Nullable protected KnownItemState itemState;

	public ItemTracker(UUID id, ItemTracking trackerSystem) {
		this.trackerUuid = id;
		this.trackerSystem = trackerSystem;
	}

	public void setTrackedByPlayer(Player player) { setTrackedByPlayer(player.getUUID()); }
	public void setTrackedByPlayer(UUID playerId) {
		this.trackingPlayerId = playerId;
		trackerSystem.setDirty();
	}
	
	public void setAtEntity(ItemStack item, int entityId, Level level, KnownItemState itemState) {
		setItemStack(item);
		this.positionEntity = OptionalInt.of(entityId);
		this.positionBlock = null;
		this.containerBlockState = null;
		this.positionDimension = level.dimension();
		this.itemState = itemState;
		trackerSystem.setDirty();
		if (!level.isClientSide()) {
			syncToPlayer((ServerLevel) level);
		}
	}

	public void setAtBlockPos(ItemStack item, BlockPos blockPos, Level level, KnownItemState itemState) {
		setItemStack(item);
		this.positionEntity = OptionalInt.empty();
		this.positionBlock = blockPos;
		this.containerBlockState = level.getBlockState(blockPos);
		this.positionDimension = level.dimension();
		this.itemState = itemState;
		trackerSystem.setDirty();
		if (!level.isClientSide()) {
			syncToPlayer((ServerLevel) level);
		}
	}

	public void setItemStillThereCheck(@Nullable Predicate<UUID> check) {
		this.itemStillThere = check;
		trackerSystem.setDirty();
	}

	public void setDisappeared(ServerLevel level) {
		setItemStack(null);
		this.positionEntity = OptionalInt.empty();
		this.positionBlock = null;
		this.containerBlockState = null;
		this.positionDimension = null;
		this.itemStillThere = null;
		this.itemState = null;
		trackerSystem.setDirty();
		syncToPlayer(level);
	}
	
	@Nullable
	public Entity getAtEntity(Level world) {
		return positionEntity.isPresent() ? world.getEntity(positionEntity.getAsInt()) : null;
	}

	public OptionalInt getAtEntityId() {
		return positionEntity;
	}

	@Nullable
	public BlockPos getAtBlockPos() {
		return positionBlock;
	}

	@Nullable
	public KnownItemState getItemState() {
		return itemState;
	}

	public ItemStack getItem() {
		return itemStack;
	}

	public void tick(MinecraftServer server) {
		if (this.positionDimension != null) {
			ServerLevel level = server.getLevel(positionDimension);
			if (level != null && !checkItemIsThere(level)) {
				setDisappeared(level);
			}
		}
	}

	public boolean checkItemIsThere(ServerLevel level) {
		if (this.positionDimension == null) return false;

		if (positionEntity.isPresent()) {
			Entity entity = level.getEntity(positionEntity.getAsInt());
			if (entity == null || entity.isRemoved()) {
				return false;
			}
		}
		else if (positionBlock != null && containerBlockState != null) {
			BlockState blockState = level.getBlockState(positionBlock);
			if (this.containerBlockState.getBlock() != blockState.getBlock()) {
				return false;
			}
		}

		return itemStillThere == null || itemStillThere.test(trackerUuid);
	}
	
	
	protected void setItemStack(ItemStack itemStack) {
		if (this.itemStack != null) {
			clearTrackingFromItem(this.itemStack);
		}
		this.itemStack = itemStack;
		trackerSystem.setDirty();
	}
	
	public static void clearTrackingFromItem(ItemStack itemStack) {
		itemStack.remove(ModItemDataComponents.TRACKER_ID);
	}


//	/* when an item is being added to inventory, the original ItemStack's count is being taken from (to split the item between slots),
//	 * so we have to find the new ItemStack inside the inventory first
//	 */
//	public static Optional<TrackerItemStack> getItemTrackerInInventory(ItemStack originalItemStack, Stream<ItemStack> inventoryItems, boolean allowEmpty) {
//		return getItemTracker(originalItemStack, allowEmpty).flatMap(oldTracker -> {
//			UUID trackerId = oldTracker.getTrackerId();
//			Optional<TrackerItemStack> newTracker = inventoryItems
//					.map(movedItem -> movedItem.getCapability(TrackerItemStackProvider.CAPABILITY).resolve().map(tracker -> {
//						if (trackerId.equals(tracker.getTrackerId())) {
//							return tracker;
//						}
//						return null;
//					}))
//					.filter(Optional::isPresent)
//					.map(Optional::get)
//					.findFirst();
//			return newTracker;
//		});
//	}
//
//	public static Predicate<ItemStack> trackerIdCheck(UUID trackerId) {
//		return invItem -> hasTrackerId(invItem, trackerId);
//	}
//
//	public static boolean hasTrackerId(ItemStack item, UUID trackerId) {
//		return trackerId.equals(TrackerItemStack.getItemTracker(item).map(TrackerItemStack::getTrackerId).orElse(null));
//	}


	public void syncToPlayer(ServerLevel level) {
		Player player = getTrackingPlayer(level);
		if (player instanceof ServerPlayer serverPlayer) {
			PacketDistributor.sendToPlayer(serverPlayer, new TrackedItemPacket(
					trackerUuid, itemStack, positionEntity, Optional.ofNullable(positionBlock)));
		}
	}

	public Player getTrackingPlayer(ServerLevel level) {
		return trackingPlayerId != null ? level.getPlayerByUUID(trackingPlayerId) : null;
	}
	
	
//	public void copy(TrackerItemStack oldTracker) {
//		this.trackerUuid = oldTracker.trackerUuid;
//		this.trackingPlayerId = oldTracker.trackingPlayerId;
//
//		this.positionDimension = oldTracker.positionDimension;
//		this.positionEntity = oldTracker.positionEntity;
//		this.positionBlock = oldTracker.positionBlock;
//		this.containerBlockState = oldTracker.containerBlockState;
//		this.itemStillThere = oldTracker.itemStillThere;
//		this.itemState = oldTracker.itemState;
//		updateSyncedTag();
//	}
//
//	public void clear() {
//		this.trackerUuid = null;
//
//		this.trackingPlayerId = null;
//
//		this.positionDimension = null;
//		this.positionEntity = OptionalInt.empty();
//		this.positionBlock = null;
//		this.containerBlockState = null;
//		this.itemStillThere = null;
//		this.itemState = null;
//		updateSyncedTag();
////		forceItemNbtToSync();
//	}
//
//	public void moveToItem(ItemStack newItem, ServerLevel level) {
//		TrackerItemStack.getItemTracker(newItem).ifPresent(newTracker -> {
//			newTracker.copy(this);
//			SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getItemsTracker().updateTracker(newTracker.getTrackerId(), newTracker, world);
//		});
//		this.clear();
//	}

	public Vec3 markerPos(Level level, float partialTick) {
		if (positionEntity.isPresent()) {
			Entity entity = level.getEntity(positionEntity.getAsInt());
			if (entity != null) {
				Vec3 position;
				if (level.isClientSide()) {
					position = entity.getPosition(partialTick);
				}
				else {
					position = entity.position();
				}
				return position.add(0, entity.getBbHeight() + 0.25, 0);
			}
		}
		if (positionBlock != null) {
			return Vec3.upFromBottomCenterOf(positionBlock, 1.0);
		}

		return null;
	}


	public Tag toNBT() {
		return toNBT(true);
	}

	public Tag toNBT(boolean savePlayerId) {
		CompoundTag nbt = new CompoundTag();
		nbt.putUUID("Id", trackerUuid);
		if (trackingPlayerId != null) {
			nbt.putUUID("Player", trackingPlayerId);
		}
		return nbt;
	}

	public static ItemTracker fromNBT(Tag inbt, ItemTracking trackerSystem) {
		CompoundTag nbt = (CompoundTag) inbt;
		UUID trackerUuid = nbt.getUUID("Id");
		ItemTracker tracker = new ItemTracker(trackerUuid, trackerSystem);
		tracker.trackingPlayerId = nbt.hasUUID("Player") ? nbt.getUUID("Player") : null;
		return tracker;
	}


//	public void onShrink(ServerLevel level) {
//		if (positionBlock != null) {
//			BlockEntity tileEntity = level.getBlockEntity(positionBlock);
//			if (tileEntity instanceof JukeboxBlockEntity) {
//				JukeboxBlockEntity jukebox = (JukeboxBlockEntity) tileEntity;
//				BlockState blockState = level.getBlockState(positionBlock);
//				level.levelEvent(1010, positionBlock, 0);
//				jukebox.clearContent();
//				blockState = blockState.setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(false));
//				level.setBlock(positionBlock, blockState, 2);
//			}
//		}
//		else if (positionEntity.isPresent()) {
//			Entity entity = getAtEntity(level);
//			if (entity instanceof ItemFrame itemFrame) {
//				itemFrame.setItem(ItemStack.EMPTY);
//			}
//			else if (entity instanceof Villager villager) {
//				Player thiefPlayer = getTrackingPlayer(level);
//				if (thiefPlayer != null) {
//					villager.getGossips().add(thiefPlayer.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
//					level.broadcastEntityEvent(entity, MCUtil.EntityEvents.VILLAGER_ANGRY);
//					entity.getCapability(MerchantDataProvider.CAPABILITY).ifPresent(merchantData -> {
//						merchantData.setRefuseTrading(thiefPlayer.getUUID(), true);
//					});
//				}
//			}
//			else if (entity instanceof Piglin piglin && itemStack.getItem() == PiglinTasks.BARTERING_ITEM) {
//				Player thiefPlayer = getTrackingPlayer(level);
//				if (thiefPlayer != null) {
//					PiglinTasksAccess.onPiglinScammed(piglin, thiefPlayer);
//				}
//			}
//		}
//	}
//
////	private static class PiglinTasksAccess extends PiglinTasks {
////
////		protected static void onPiglinScammed(PiglinEntity piglin, LivingEntity player) {
////			PiglinTasks.wasHurtBy(piglin, player);
////			/*
////			 * TODO piglin scam counter
////			 *     if > 0, when receiving a gold ingot, they don't give an item back and instead decrement the counter
////			 *         if after the decrement scam counter == 0, stop attacking
////			 */
////			// incrementScamCounter(piglin);
////		}
////	}
}
