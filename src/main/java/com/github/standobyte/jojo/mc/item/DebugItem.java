package com.github.standobyte.jojo.mc.item;

import java.util.List;

import com.github.standobyte.jojo.client.ui.DebugFunctionsScreen;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracker;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DebugItem extends Item {

	public DebugItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (level.isClientSide()) {
        	DebugFunctionsScreen.onDebugItemUsed();
        }
		return InteractionResultHolder.consume(item);
	}

	@Override
	public void appendHoverText(ItemStack item, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flags) {
		ctx.registries().lookup(JojoRegistries.STORY_PARTS_REG_KEY).ifPresent(registry -> {
			registry.listElements().forEach(holder -> {
				tooltip.add(holder.value().getPartName());
			});
		});
	}
	
	
	public static String[] OPTIONS = new String[] {
			"test1",
			"test2",
			"test3",
			"test4",
			"test5",
			"track_offhand",
			"drop_tracked"
	};
	public static String[] getOptions() {
		return OPTIONS;
	}
	
	/**
	 * @return true if the option should be sent to the server side for handling
	 */
	public static boolean onClientClick(String option) {
		return switch (option) {
			default -> {
				yield true;
			}
		};
	}
	
	public static void handleServer(String option, Player player) {
		switch (option) {
			case "track_offhand" -> {
				ItemStack item = player.getOffhandItem();
				if (!item.isEmpty()) {
					ServerLevel level = (ServerLevel) player.level();
					ItemTracking trackingSystem = ItemTracking.getItemTracking(level);
					ItemTracker tracker = trackingSystem.startTracking(item, level);
					tracker.context = "debug";
					tracker.setTrackedByPlayer(player);
				}
			}
			case "drop_tracked" -> {
				ServerLevel level = (ServerLevel) player.level();
				ItemTracking trackingSystem = ItemTracking.getItemTracking(level);
				for (ItemTracker tracker : trackingSystem.values()) {
					ItemStack item = tracker.getItem();
					if (item != null && !item.isEmpty()) {
						Vec3 pos = tracker.getPos(level, 1);
						if (pos != null) {
							ItemStack itemToDrop = tracker.clearAndCopyItem(level);
							ItemEntity dropItem = new ItemEntity(level, pos.x, pos.y, pos.z, itemToDrop);
							level.addFreshEntity(dropItem);
						}
					}
				}
			}
			default -> {}
		};
	}
	
}
