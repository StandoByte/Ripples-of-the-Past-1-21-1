package com.github.standobyte.jojo.adventure;

import com.github.standobyte.jojo.JojoModLivingVariables;
import com.github.standobyte.jojo.ServerSavedData;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.ClientTickHandler;
import com.github.standobyte.jojo.client.ClientTickables;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.event.SusBlockLootEvent;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.mechanics.standarrow.StandArrowLore;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.util.OOPMoment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class SpawnArrowsInSusBlocks {
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addArrowIfNoOtherLootWasAdded(SusBlockLootEvent event) {
		if (event.replacementItem != null) {
			return;
		}
		
		Holder<Item> arrowToPut = null;
		LivingEntity player = event.player;
		JojoModLivingVariables playerVars = JojoModLivingVariables.get(player);
		ServerSavedData serverVars = ServerSavedData.get(event.level.getServer());
		RandomSource random = OOPMoment.RANDOM;

		if (!playerVars.foundAnArrow || playerVars.findMoreArrowsTimer > 0) {
			int foundFirstArrows = serverVars.getInt("foundFirstArrows", 0);
			boolean firstArrowEver = foundFirstArrows <= 0;
			if (firstArrowEver || (foundFirstArrows < 5 && playerVars.findMoreArrowsTimer > 0)) {
				if (random.nextInt(4) == 0) {
					arrowToPut = ModItems.STAND_ARROW;
					serverVars.setInt("foundFirstArrows", foundFirstArrows + 1);
					
					if (playerVars.findMoreArrowsTimer == -1) {
						playerVars.findMoreArrowsTimer = 5 * 60 * 20;
					}
				}

			}
			else {
				if (!StandUtil.isEntityStandUser(player) && random.nextInt(200) == 0) {
					boolean kingCrimsonVillain = false;
					if (!serverVars.getBoolean("foundBeetleArrow", false) && (random.nextInt(100) == 0 || kingCrimsonVillain)) {
						arrowToPut = ModItems.STAND_ARROW_BEETLE;
						serverVars.setBoolean("foundBeetleArrow", true);
					}
					else {
						arrowToPut = ModItems.STAND_ARROW;
					}
				}
			}
		}

		if (arrowToPut != null) {
			playerVars.foundAnArrow = true;
			ItemStack item = arrowToPut.value().getDefaultInstance();
			StandArrowLore.onArrowFoundInSusBlock(item, player, event.level, event.blockPos);
			event.replacementItem = item;
			event.sendMenacingParticles = true;
		}
	}



	public static void onItemSynchedToClient_addParticles(BlockEntity brushableBlockEntity, ItemStack item) {
		ClientTickables.add(() -> {
			Level level = brushableBlockEntity.getLevel();
			if (level != null && ClientProxy.getClientWorld() == level && !brushableBlockEntity.isRemoved()) {
				if (ClientTickHandler.tickCount % 3 == 0 && !ClientProxy.isClientPaused()) {
					BlockPos blockPos = brushableBlockEntity.getBlockPos();
					Vec3 particlePos = randomPointAroundBlock(blockPos, 1.25, 2);
					level.addParticle(ModParticles.MENACING.get(), particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
				}
				return true;
			}
			return false;
		});
	}
	
	public static Vec3 randomPointAroundBlock(BlockPos pos, double radiusMin, double radiusMax) {
		RandomSource random = OOPMoment.RANDOM;
		double radius = radiusMin + random.nextDouble() * (radiusMax - radiusMin);
		Vec3i normal = Direction.values()[random.nextInt(6)].getNormal();
		Vec3 vec = new Vec3(
				pos.getX() + 0.5 + (normal.getX() == 0 ? (random.nextDouble() - 0.5) * radiusMax : normal.getX() * radius * 0.5),
				pos.getY() + 0.5 + (normal.getY() == 0 ? (random.nextDouble() - 0.5) * radiusMax : normal.getY() * radius * 0.5),
				pos.getZ() + 0.5 + (normal.getZ() == 0 ? (random.nextDouble() - 0.5) * radiusMax : normal.getZ() * radius * 0.5));
		return vec;
	}
	
}
