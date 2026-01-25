package com.github.standobyte.jojo.mc.item;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.mc.entity.projectile.StandArrowEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StandArrowItem extends ArrowItem {
    // dur: 25 | 250; ench: 10 | 25
    private final int enchantability = 20;

    public StandArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack arrow = player.getItemInHand(usedHand);

        if (!level.isClientSide()) {
            player.hurt(player.damageSources().playerAttack(player), Math.min(1.0F, Math.max(player.getHealth() - 1.0F, 0)));
            arrow.hurtAndBreak(1, (ServerLevel) level, (ServerPlayer) player, item -> {});

            return InteractionResultHolder.success(arrow);
        }
        return InteractionResultHolder.fail(arrow);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new StandArrowEntity(shooter, level, ammo, weapon);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Player player = ClientUtil.getClientPlayer();
        if (player != null) {

        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return enchantability;
    }
}
