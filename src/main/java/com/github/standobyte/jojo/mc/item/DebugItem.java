package com.github.standobyte.jojo.mc.item;

import java.util.List;

import com.github.standobyte.jojo.client.ui.DebugFunctionsScreen;
import com.github.standobyte.jojo.core.JojoRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

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
			"test6",
			"test7"
	};
	public static String[] getOptions() {
		return OPTIONS;
	}
	
	public static boolean onClientClick(String option) {
		return switch (option) {
			default -> {
				yield false;
			}
		};
	}
	
	public static void handleServer(String option, Player player) {
		switch (option) {
			default -> {}
		};
	}
	
}
