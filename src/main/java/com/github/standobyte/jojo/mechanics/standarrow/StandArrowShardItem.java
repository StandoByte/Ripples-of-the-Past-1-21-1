package com.github.standobyte.jojo.mechanics.standarrow;

import java.util.List;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class StandArrowShardItem extends Item {

    public StandArrowShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack shard = player.getItemInHand(usedHand);

        if (!level.isClientSide() && !StandUtil.isEntityStandUser(player)) {
        	boolean gaveStand = StandArrowItem.giveStand(level, player);
        	if (!StandArrowItem.isInvulnerable(player)) {
        		StandArrowItem.dealDamageFromArrow(player, shard, true, gaveStand);
        	}
        	if (gaveStand) {
        		if (!player.getAbilities().instabuild) {
        			shard.shrink(1);
        		}
        		return InteractionResultHolder.success(shard);
        	}
        }
        return InteractionResultHolder.fail(shard);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    	StandArrowItem.addStandNamesToTooltip(tooltipComponents, context);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
    
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTouchitemEntity(ItemEntityPickupEvent.Pre event) {
    	ItemEntity itemEntity = event.getItemEntity();
    	ItemStack item = itemEntity.getItem();
    	if (!item.isEmpty() && item.is(ModItems.STAND_ARROW_SHARD)) {
    		Player player = event.getPlayer();
    		if (!StandArrowItem.isInvulnerable(player) && !StandUtil.isEntityStandUser(player)) {
    			boolean gaveStand = StandArrowItem.giveStand(player.level(), player);
    			StandArrowItem.dealDamageFromArrow(player, item, true, gaveStand);
    			item.shrink(1);
    			event.setCanPickup(TriState.FALSE);
    		}
    	}
    }
}
