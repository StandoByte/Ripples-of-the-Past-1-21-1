package com.github.standobyte.jojo.tmp.charactertest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CharacterTestItem extends Item {

	public CharacterTestItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
		// summon a new character entity
		if (player.isShiftKeyDown()) {
			if (level instanceof ServerLevel serverLevel) {
				String name = "Player" + player.getRandom().nextInt(10000);
				PowerUserMobEntity entity = new PowerUserMobEntity(serverLevel);
				entity.setCharacterName(Component.literal(name));
				entity.copyPosition(player);
				entity.setLeftHanded(level.getRandom().nextFloat() < 0.05f);
				entity.getEntityData().set(PowerUserMobEntity.DATA_PROFILE, Optional.of(new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap())));
				serverLevel.addFreshEntity(entity);
			}
			return InteractionResultHolder.consume(item);
		}
		else {
//			Entity hovered = getHovered(player);
//			if (hovered != null) {
//				if (!level.isClientSide()) {
//					// change the controlled player
//					if (hovered instanceof ServerPlayer hoveredCharacter && player instanceof ServerPlayer oldPlayer) {
//						hoveredCharacter.getInventory().add(item.copy());
//						player.setItemInHand(hand, ItemStack.EMPTY);
//						
//						CharacterControlStuff.takeControl(hoveredCharacter, oldPlayer);
//					}
//				}
//				return InteractionResultHolder.consume(item);
//			}
		}

		return InteractionResultHolder.fail(item);
	}
	

	public static List<Entity> availableCharacters(Player player) {
		return Collections.emptyList();
//		return player.level().getEntities(player, player.getBoundingBox().inflate(32), 
//				entity -> entity instanceof PlayerControl playerControl && !playerControl.jojo_ripples$getControllingPlayer().isPresent());
	}

	public static Entity getHovered(Player player) {
		return getHovered(player, availableCharacters(player));
	}

	public static Entity getHovered(Player player, List<Entity> targets) {
		Vec3 playerPos = player.getEyePosition();
		Vec3 playerLook = player.getLookAngle();

		return targets.stream()
				.filter(e -> e.getBoundingBox().getCenter().subtract(playerPos).normalize().dot(playerLook) > 0.866)
				.max(Comparator.comparingDouble(e -> e.getBoundingBox().getCenter().subtract(playerPos).normalize().dot(playerLook)))
				.orElse(null);
	}

	
	@Override
	public void appendHoverText(ItemStack item, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.literal("this is probably really broken currently").withStyle(ChatFormatting.GRAY));
	}

}
