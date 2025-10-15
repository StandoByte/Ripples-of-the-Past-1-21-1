package com.github.standobyte.jojo.core;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.command.JojoPowerCommand;
import com.github.standobyte.jojo.core.command.PlayBgmCommand;
import com.github.standobyte.jojo.core.command.StandCommand;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.util.entitycomponent.DataEventListeners;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class EventHandler {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		CommandBuildContext context = event.getBuildContext();
		
		StandCommand.register(dispatcher, context);
		JojoPowerCommand.register(dispatcher, context);
		PlayBgmCommand.register(dispatcher, context);
	}

	public static void onEntityCreated(EntityJoinLevelEvent event) {
		/* 
		 * Attach the power data to the player.
		 * The capabilities system is now strictly per-EntityType, can't have an instanceof check anymore 
		 * for smth like a "Mobs with Stands"-type of addon, so we're not using it this time.
		 */ 
		if (event.getEntity() instanceof Player user) {
			for (PowerClass<?> powerClass : PowerClass.values()) {
				powerClass.attachPower(user);
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event) {
		DataEventListeners data = entityEventListeners(event.getEntity());
		if (data != null) {
			data.onTick();
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		DataEventListeners trackedData = entityEventListeners(event.getTarget());
		if (trackedData != null) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			trackedData.onTracking(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		Player original = event.getOriginal();
		DataEventListeners data = entityEventListeners(original);
		if (data != null) {
			Player newEntity = event.getEntity();
			boolean wasDeath = event.isWasDeath();
			data.onClone(newEntity, wasDeath);
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		syncAttachedData((ServerPlayer) event.getEntity());
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		syncAttachedData((ServerPlayer) event.getEntity());
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		syncAttachedData((ServerPlayer) event.getEntity());
	}

	public static void syncAttachedData(ServerPlayer player) {
		DataEventListeners data = entityEventListeners(player);
		if (data != null) {
			data.onSyncToPlayer(player);
		}
	}
	
	@Nullable
	private static DataEventListeners entityEventListeners(Entity entity) {
		AttachmentType<DataEventListeners> key = ModDataAttachmentTypes.DATA_EVENT_HELPER.get();
		return entity.hasData(key) ? entity.getData(key) : null;
	}

}
