package com.github.standobyte.jojo.mechanics.itemtracking;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModItemDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Currently item stacks are being tracked in:<br>
 *   - ItemEntity & ItemFrame (ItemStackMixin)<br>
 *   - BaseContainerBlockEntity (ContainerTileEntityMixin)<br>
 *   - Hoppers (HopperTileEntityMixin)<br>
 *   - Jukeboxes (JukeboxTileEntityMixin)<br>
 *   - Player inventory (PlayerInventoryMixin; ContainerSlotMixin (i forgor what this one actually achieves though))<br>
 *   - Horse chest inventory (HorseInventoryMixin)<br>
 *   - Minecart with chest/hopper, boat with chest (ContainerEntityMixin)<br>
 *   - Mobs, players and armor stands equipment (LivingEntityEquipMixin)<br>
 *   - Items picked up by mobs (e.g. villagers picking up items like wheat from the ground) (LivingEntityEquipMixin)<br>
 *   - Stands taking items (LivingEntityEquipMixin; HandItemsAsInventory#setToSlot(int, ItemStack) calling LivingEntity#onEquipItem(EquipmentSlot, ItemStack, ItemStack))<br>
 *   - Shot arrows, knives, clackers, blade hats (ItemTrackingEventHandler, and the projectile item classes from ROTP)<br>
 *   - Thrown ender pearls, snowballs, eggs, splash/lingering potions (ProjectileItemEntityMixin)<br>
 *   - Placed & shot firework rockets (FireworkRocketEntityMixin)<br>
 *   - Stuck arrows and knives (AbstractArrowStuckOnHitMixin)<br>
 *   - Items sold to villagers (MerchantResultSlotMixin)<br>
 *   - Piglins picking up gold (LivingEntityEquipMixin)<br>
 */
public class ItemTracker {
	protected static final RandomSource RANDOM = RandomSource.create();

	public final UUID trackerId;
	protected final ItemTracking trackerSystem;

	@Nullable protected ItemStack itemStack;
	@Nullable protected UUID trackingPlayerId;
	@Nullable public String context;

	@Nullable protected ResourceKey<Level> positionDimension;
	protected OptionalInt positionEntity = OptionalInt.empty();
	@Nullable protected BlockPos positionBlock = null;
	@Nullable protected BlockState containerBlockState;
	@Nullable protected Predicate<UUID> itemStillThere;
	@Nullable protected KnownItemState itemState;

	public ItemTracker(UUID id, ItemTracking trackerSystem) {
		this.trackerId = id;
		this.trackerSystem = trackerSystem;
	}

	public void setTrackedByPlayer(Player player) { setTrackedByPlayer(player.getUUID()); }
	public void setTrackedByPlayer(UUID playerId) {
		this.trackingPlayerId = playerId;
		trackerSystem.setDirty();
	}
	
	public void setAtEntity(ItemStack item, int entityId, Level level, KnownItemState itemState, 
			@Nullable Predicate<UUID> itemStillThereCheck) {
		setItemStack(item, level);
		this.positionEntity = OptionalInt.of(entityId);
		this.positionBlock = null;
		this.containerBlockState = null;
		this.positionDimension = level.dimension();
		this.itemState = itemState;
		this.itemStillThere = itemStillThereCheck;
		trackerSystem.setDirty();
		if (!level.isClientSide()) {
			syncToPlayer((ServerLevel) level);
		}
	}

	public void setAtBlockPos(ItemStack item, BlockPos blockPos, Level level, KnownItemState itemState, 
			@Nullable Predicate<UUID> itemStillThereCheck) {
		setItemStack(item, level);
		this.positionEntity = OptionalInt.empty();
		this.positionBlock = blockPos;
		this.containerBlockState = level.getBlockState(blockPos);
		this.positionDimension = level.dimension();
		this.itemState = itemState;
		this.itemStillThere = itemStillThereCheck;
		trackerSystem.setDirty();
		if (!level.isClientSide()) {
			syncToPlayer((ServerLevel) level);
		}
	}

	public void setDisappeared(ServerLevel level) {
		setItemStack(null, level);
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

		return itemStillThere == null || itemStillThere.test(trackerId);
	}
	
	
	protected void setItemStack(ItemStack itemStack, Level level) {
		if (!level.isClientSide() && this.itemStack != itemStack) {
			if (this.itemStack != null) {
				clearTrackingFromItem(this.itemStack);
			}
			if (itemStack != null) {
				itemStack.set(ModItemDataComponents.TRACKER_ID, this.trackerId);
			}
		}
		this.itemStack = itemStack;
		trackerSystem.setDirty();
	}
	
	public static void clearTrackingFromItem(ItemStack itemStack) {
		if (itemStack != null) {
			itemStack.remove(ModItemDataComponents.TRACKER_ID);
		}
	}
	
	
	@Nullable
	public ItemStack clearAndCopyItem(ServerLevel level) {
		if (itemStack != null && !itemStack.isEmpty()) {
			ItemStack copy = itemStack.copy();
			itemStack.setCount(0);
			if (positionEntity.isPresent()) {
				Entity entity = level.getEntity(positionEntity.getAsInt());
				if (entity != null) {
					switch (entity) {
						case ItemFrame itemFrame -> {
							itemFrame.setItem(ItemStack.EMPTY);
						}
						case Projectile projectile -> {
							if (itemState == KnownItemState.ENTITY_IS_ITEM) {
								projectile.remove(RemovalReason.DISCARDED);
							}
						}
						case AbstractHorse horse -> {
							horse.getInventory().setChanged();
						}
						default -> {}
					}
				}
			}
			if (positionBlock != null) {
				BlockState blockState = level.getBlockState(positionBlock);
				if (blockState.hasBlockEntity()) {
					BlockEntity blockEntity = level.getBlockEntity(positionBlock);
					if (blockEntity != null) {
						switch (blockEntity) {
							case JukeboxBlockEntity jukebox -> {
								// copypaste of notifyItemChangedInJukebox because they made it fucking private
								level.setBlock(positionBlock, blockState.setValue(JukeboxBlock.HAS_RECORD, false), 2);
								BlockState newBlockState = jukebox.getBlockState();
								level.gameEvent(GameEvent.BLOCK_CHANGE, positionBlock, GameEvent.Context.of(newBlockState));
								jukebox.getSongPlayer().stop(level, newBlockState);
							}
							default -> {}
						}
					}
				}
			}
			return copy;
		}
		return null;
	}


	public void syncToPlayer(ServerLevel level) {
		Player player = getTrackingPlayer(level);
		if (player instanceof ServerPlayer serverPlayer) {
			PacketDistributor.sendToPlayer(serverPlayer, new TrackedItemPacket(
					trackerId, itemStack, context, 
					positionEntity, Optional.ofNullable(positionBlock)));
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

	public Vec3 getPos(Level level, float partialTick) {
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
				return position.add(0, entity.getBbHeight() * 0.5, 0);
			}
		}
		if (positionBlock != null) {
			return Vec3.atCenterOf(positionBlock);
		}

		return null;
	}


	public Tag toNBT() {
		return toNBT(true);
	}

	public Tag toNBT(boolean savePlayerId) {
		CompoundTag nbt = new CompoundTag();
		nbt.putUUID("Id", trackerId);
		if (trackingPlayerId != null) {
			nbt.putUUID("Player", trackingPlayerId);
		}
		if (context != null) {
			nbt.putString("context", context);
		}
		return nbt;
	}

	public static ItemTracker fromNBT(Tag inbt, ItemTracking trackerSystem) {
		CompoundTag nbt = (CompoundTag) inbt;
		UUID trackerUuid = nbt.getUUID("Id");
		ItemTracker tracker = new ItemTracker(trackerUuid, trackerSystem);
		
		tracker.trackingPlayerId = nbt.hasUUID("Player") ? nbt.getUUID("Player") : null;
		tracker.context = nbt.getString("context");
		
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
