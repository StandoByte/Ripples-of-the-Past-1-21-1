package com.github.standobyte.jojo.mc.item;

import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.client.ui.DebugFunctionsScreen;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracker;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class DebugItem extends Item {
	public static UUID trackerId = UUID.fromString("1df04b26-4394-4107-aa65-1afe4dfce0cc");

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
			"track_offhand",
			"test6",
			"test7"
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
					if (trackerId != null) {
						trackingSystem.stopTracking(trackerId, level);
					}
					ItemTracker tracker = trackingSystem.startTracking(item, level);
					trackerId = tracker.trackerUuid;
					tracker.setTrackedByPlayer(player);
				}
			}
			default -> {}
		};
	}
	
}
