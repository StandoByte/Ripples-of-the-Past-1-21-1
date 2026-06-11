package com.github.standobyte.jojo.mechanics.standarrow;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
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
        		StandArrowItem.dealDamageFromArrow(player, shard, 
        				player, player, true, gaveStand);
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

    static List<Object> tlArgs = new ArrayList<>(2);
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    	StandArrowItem.addStandNamesToTooltip(tooltipComponents, context);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(CommonComponents.EMPTY);
        
    	@Nullable StandArrowShardLore lore = stack.get(ModItemDataComponents.ARROW_SHARD_LORE);
    	Component arrowItemName = lore != null ? lore.arrowItemName().orElse(null) : null;
    	Component userName = lore != null ? lore.userCharacterName().orElse(null) : null;
    	MutableComponent loreLine;
    	
    	String tlKey = "jojo_ripples.stand_shard.lore";
    	tlArgs.clear();
    	if (arrowItemName != null) {
    		if (arrowItemName.getContents() instanceof TranslatableContents arrowNameTl) {
    			String conjugation = "genitive." + arrowNameTl.key;
    			if (Language.getInstance().has(conjugation)) {
    				arrowItemName = Component.translatableWithFallback(conjugation, null, arrowNameTl.args);
    			}
    		}
    		tlKey += ".item_name";
    		tlArgs.add(arrowItemName);
    	}
    	if (userName != null) {
    		tlKey += ".user_name";
    		tlArgs.add(userName);
    	}
		loreLine = Component.translatable(tlKey, tlArgs.toArray());
    	
    	tooltipComponents.add(loreLine.withStyle(ChatFormatting.GRAY));
    }
    
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTouchItemEntity(ItemEntityPickupEvent.Pre event) {
    	ItemEntity itemEntity = event.getItemEntity();
    	ItemStack item = itemEntity.getItem();
    	if (!item.isEmpty() && item.is(ModItems.STAND_ARROW_SHARD)) {
    		Player player = event.getPlayer();
    		if (!StandArrowItem.isInvulnerable(player) && !StandUtil.isEntityStandUser(player)) {
    			boolean gaveStand = StandArrowItem.giveStand(player.level(), player);
    			StandArrowItem.dealDamageFromArrow(player, item, 
    					itemEntity, itemEntity.getOwner(), true, gaveStand);
    			item.shrink(1);
    			event.setCanPickup(TriState.FALSE);
    		}
    	}
    }
}
