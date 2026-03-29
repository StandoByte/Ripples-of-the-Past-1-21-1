package com.github.standobyte.jojoimpl.powers.hamon.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class HamonDataRequestsToTrain {
//	private Set<Player> newLearners = new HashSet<>();
//
//	public boolean playerWantsToLearn(Player playerEntity) {
//		return newLearners.contains(playerEntity);
//	}
//
//	public void addNewPlayerLearner(LivingEntity teacherUser, Player learnerPlayer) {
//		newLearners.add(learnerPlayer);
//		if (!teacherUser.level().isClientSide()) {
//			PacketDistributor.sendToPlayersTrackingAndSelf(
//					new TrHamonSyncPlayerLearnerPacket(teacherUser.getId(), learnerPlayer.getId(), true), teacherUser);
//		}
//	}
//
//	public void tickNewPlayerLearners(LivingEntity user) {
//		for (Iterator<Player> it = newLearners.iterator(); it.hasNext(); ) {
//			Player player = it.next();
//			if (!player.isAlive() || user.distanceToSqr(player) > 64) {
//				it.remove();
//			}
//		}
//	}
//
//	public boolean interactWithNewLearner(Player learnerPlayer) {
//		if (newLearners.contains(learnerPlayer)) {
//			if (!learnerPlayer.level().isClientSide()) {
//				HamonUtil.startLearningHamon(learnerPlayer.level(), learnerPlayer, 
//						INonStandPower.getPlayerNonStandPower(learnerPlayer), power.getUser(), this);
//				LivingEntity user = power.getUser();
//				PacketManager.sendToClientsTracking(
//						new TrHamonSyncPlayerLearnerPacket(user.getId(), learnerPlayer.getId(), false), user);
//			}
//			newLearners.remove(learnerPlayer);
//			return true;
//		}
//		return false;
//	}
//
//	public void removeNewLearner(Player player) {
//		newLearners.remove(player);
//	}
}
