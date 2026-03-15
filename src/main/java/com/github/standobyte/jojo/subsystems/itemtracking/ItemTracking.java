package com.github.standobyte.jojo.subsystems.itemtracking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.ServerSavedData;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.util.functions.ItemUtil;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ItemTracking implements INBTSerializable<ListTag> {
	@Nullable protected final SavedData serverSideHolder;
	protected Map<UUID, ItemTracker> trackingMap = new HashMap<>();

	public ItemTracking(@Nullable SavedData serverSideHolder) {
		this.serverSideHolder = serverSideHolder;
	}

	public ItemTracker startTracking(ItemStack itemStack, @Nonnull ServerLevel level, UUID trackerId) {
		if (itemStack.isEmpty()) {
			throw new IllegalArgumentException("Cannot track empty items");
		}
		if (itemStack.getCount() != 1) {
			throw new IllegalArgumentException("Cannot track stacked items, only item stacks with count == 1 are supported");
		}

		ItemTracker tracker = new ItemTracker(trackerId, this);
		ItemTracker prev = trackingMap.get(trackerId);
		if (prev != null) {
			prev.setItemStack(itemStack, level);
		}
		else {
			trackingMap.put(trackerId, tracker);
		}
		setDirty();
		itemStack.set(ModItemDataComponents.TRACKER_ID, trackerId);
		return tracker;
	}

	public ItemTracker startTracking(ItemStack itemStack, @Nonnull ServerLevel level) {
		return startTracking(itemStack, level, Mth.createInsecureUUID());
	}
	
	public ItemTracker clComputeIfAbsent(UUID trackerId) {
		ItemTracker tracker = trackingMap.get(trackerId);
		if (tracker == null) {
			tracker = new ItemTracker(trackerId, this);
			trackingMap.put(trackerId, tracker);
		}
		return tracker;
	}

	public void stopTracking(UUID trackerId, @Nullable ServerLevel serverLevel) {
		ItemTracker prev = trackingMap.remove(trackerId);
		setDirty();
		if (serverLevel != null && prev != null && prev.itemStack != null) {
			prev.setDisappeared(serverLevel);
			ItemTracker.clearTrackingFromItem(prev.itemStack);
		}
	}
	
	
	@Nullable
	public static ItemTracker getItemTracker(ItemStack item, Level level) {
		UUID uuid = getTrackerId(item);
		if (uuid != null) {
			ItemTracking itemTracking = getItemTracking(level);
			ItemTracker tracker = itemTracking.trackingMap.get(uuid);
			if (tracker == null && !level.isClientSide()) {
				ItemTracker.clearTrackingFromItem(item);
			}
			return tracker;
		}
		return null;
	}
	
	@Nullable
	public static ItemTracker getItemTracker(UUID uuid, Level level) {
		ItemTracking itemTracking = getItemTracking(level);
		ItemTracker tracker = itemTracking.trackingMap.get(uuid);
		return tracker;
	}
	
	@Nullable
	public static UUID getTrackerId(ItemStack item) {
		return ItemUtil.getFromEmptyItem(item, ModItemDataComponents.TRACKER_ID.get());
	}


	public static Predicate<ItemStack> trackerIdCheck(UUID trackerId) {
		return invItem -> hasTrackerId(invItem, trackerId);
	}

	public static boolean hasTrackerId(ItemStack item, UUID trackerId) {
		return trackerId.equals(getTrackerId(item));
	}
	
	public static boolean isProbablyTracked(ItemStack item) {
		return item.has(ModItemDataComponents.TRACKER_ID);
	}

	/* when an item is being added to inventory, the original ItemStack's count is being taken from (to split the item between slots),
	 * so we have to find the new ItemStack inside the inventory first
	 */
	@Nullable
	public static ItemStack getItemWithTrackerInInventory(UUID trackerId, Stream<ItemStack> inventoryItems, Level level) {
		if (trackerId != null) {
			Optional<ItemStack> newTrackedItem = inventoryItems
					.filter(item -> {
						UUID foundTrackerId = ItemTracking.getTrackerId(item);
						return foundTrackerId != null && trackerId.equals(foundTrackerId);
					})
					.findFirst();
			if (newTrackedItem.isPresent()) return newTrackedItem.get();
		}
		return null;
	}
	
	@Nullable
	public static ItemStack getItemWithTrackerInInventory(ItemStack originalItemStack, Stream<ItemStack> inventoryItems, Level level) {
		UUID trackerId = getTrackerId(originalItemStack);
		return getItemWithTrackerInInventory(trackerId, inventoryItems, level);
	}


	public static ItemTracking getItemTracking(Level level) {
		if (!level.isClientSide()) {
			return getServerItemTracking(level.getServer());
		}
		else {
			return ClientProxy.clientTrackedItems;
		}
	}
	
	protected static ItemTracking getServerItemTracking(MinecraftServer server) {
		return ServerSavedData.get(server).itemsTracker;
	}

	public Collection<ItemTracker> values() {
		return trackingMap.values();
	}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ItemTracking itemTrackers = getServerItemTracking(server);
        if (itemTrackers != null) {
        	Iterator<ItemTracker> iter = itemTrackers.trackingMap.values().iterator();
        	while (iter.hasNext()) {
        		ItemTracker tracker = iter.next();
        		tracker.tick(server);
        	}
        }
    }

	@Override
	public ListTag serializeNBT(Provider provider) {
		ListTag nbt = new ListTag();
		for (ItemTracker tracker : trackingMap.values()) {
			nbt.add(tracker.toNBT());
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, ListTag nbt) {
		for (Tag element : nbt) {
			ItemTracker tracker = ItemTracker.fromNBT(element, this);
			if (tracker != null && tracker.trackerId != null) {
				trackingMap.put(tracker.trackerId, tracker);
			}
		}
	}
	
	
	void setDirty() {
		if (serverSideHolder != null) {
			serverSideHolder.setDirty();
		}
	}
    
}
