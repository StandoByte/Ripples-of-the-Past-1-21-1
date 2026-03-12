package com.github.standobyte.jojo.mc.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class StandArrowShardItem extends Item {

    public StandArrowShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack arrow = player.getItemInHand(usedHand);

        if (!level.isClientSide() && StandArrowItem.onPiercedByArrow(player, arrow, level, Optional.empty())) {
            player.hurt(player.damageSources().playerAttack(player), Math.min(1.0F, Math.max(player.getHealth() - 1.0F, 0)));
            arrow.shrink(1);

            return InteractionResultHolder.success(arrow);
        }
        return InteractionResultHolder.fail(arrow);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    	StandArrowItem.addStandNamesToTooltip(tooltipComponents, context);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
