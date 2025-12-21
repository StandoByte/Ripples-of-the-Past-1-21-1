package com.github.standobyte.jojo.mc.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.mc.item.component.StandWrittenOnDisc;
import com.github.standobyte.jojo.mechanics.StoryPart;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class StandDiscItem extends Item {

	public StandDiscItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack item, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flags) {
		StandWrittenOnDisc discStand = item.get(ModItemDataComponents.DISC_STAND.get());
		if (discStand == null || !discStand.isValid()) return;
		
		StandInstance standInstance = discStand.getInstance();
		StandType standType = standInstance.getStandType();
		Component standName = standInstance.getStandName(true);
		if (standName != null) {
			tooltip.add(standName);
		}

		StandSkin skin = StandSkinsLoader.getInstance().getSkin(standInstance);
		if (skin != null) {
			Holder<StoryPart> storyPart = skin.getStoryPart(ctx.registries());
			if (storyPart != null) {
				tooltip.add(storyPart.value().getPartName());
			}
		}
		
		if (!standType.discExtraTooltip.isEmpty()) {
			tooltip.add(CommonComponents.EMPTY);
			tooltip.addAll(standType.discExtraTooltip);
		}
	}
	
	@Nullable
	public String getCreatorModId(ItemStack itemStack) {
		ResourceLocation id;
		StandInstance stand = getStandInstance(itemStack);
		if (stand != null) {
			id = stand.getStandId();
			if (id != null) {
				return id.getNamespace();
			}
		}
		return super.getCreatorModId(itemStack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack discItem = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			StandWrittenOnDisc discStand = discItem.get(ModItemDataComponents.DISC_STAND.get());
			if (discStand == null || !discStand.isValid()) return InteractionResultHolder.fail(discItem);
			
			PowerClass.STAND.attachPower(player);
			StandPower stand = PowerClass.STAND.get(player);
			if (stand != null) {
				stand.setStandInstance(Optional.of(discStand.copyStandInstance()));
			}
			return InteractionResultHolder.success(discItem);
		}
		return InteractionResultHolder.consume(discItem);
	}


	@Nullable
	public static StandInstance getStandInstance(ItemStack discItem) {
		StandWrittenOnDisc discStand = discItem.get(ModItemDataComponents.DISC_STAND.get());
		if (discStand == null || !discStand.isValid()) return null;

		StandInstance standInstance = discStand.getInstance();
		return standInstance;
	}

	public static ItemStack withStand(StandInstance standInstance) {
		ItemStack disc = new ItemStack(ModItems.STAND_DISC.get());
		disc.set(ModItemDataComponents.DISC_STAND.get(), new StandWrittenOnDisc(standInstance));
		return disc;
	}

}
