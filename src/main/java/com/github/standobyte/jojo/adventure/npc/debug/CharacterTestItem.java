package com.github.standobyte.jojo.adventure.npc.debug;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.standobyte.jojo.adventure.npc.NpcInventoryExchangeContainer;
import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;
import com.github.standobyte.jojo.subsystems.target.ActionTargetAim;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
				entity.setCustomName(Component.literal(name));
				entity.copyPosition(player);
				entity.setLeftHanded(level.getRandom().nextFloat() < 0.05f);
				entity.setSkinFromPlayerName(name);
				entity.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null);
				serverLevel.addFreshEntity(entity);
			}
			return InteractionResultHolder.consume(item);
		}
		else {
	        if (!level.isClientSide()) {
	        	PowerUserMobEntity npc = null;
	        	ActionTargetAim aim = LivingComponentAction.getAim(player);
	        	if (aim != null) {
	        		ActionTarget target = aim.getTarget();
	        		if (target != null && target.getEntity() instanceof PowerUserMobEntity crosshairNpc) {
	        			npc = crosshairNpc;
	        		}
	        	}
	        	if (npc == null) {
	        		npc = level.getEntitiesOfClass(PowerUserMobEntity.class, player.getBoundingBox().inflate(16), e -> e.isAlive())
	        				.stream()
	        				.min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
	        				.orElse(null);
	        	}
	        	if (npc != null) {
	        		player.openMenu(NpcInventoryExchangeContainer.createServerSide(npc, true));
	        	}
	        }
	        
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
}
